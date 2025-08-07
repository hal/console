/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.installer;

import org.jboss.elemento.InputType;
import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USE_DEFAULT_LOCAL_CACHE;

class ChooseTypeStep extends WizardStep<UpdateManagerContext, UpdateState> implements AsyncStep<UpdateManagerContext> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    ChooseTypeStep(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(resources.constants().chooseUpdateType());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        root = div()
                .add(p().textContent(resources.messages().chooseUpdateType(Names.CUSTOM))).element();

        for (UpdateType type : UpdateType.values()) {
            if (type == UpdateType.REVERT) {
                continue;
            }
            root.appendChild(div().css(CSS.radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .attr("name", "type") // NON-NLS
                                    .attr("value", type.name())
                                    .on(click, event -> {
                                        wizard().getContext().updateType = type;
                                    }))
                            .add(span().textContent(type.getTitle(resources))))
                    .element());
        }

        HTMLInputElement firstRadio = (HTMLInputElement) root.querySelector("input[type=radio]"); // NON-NLS
        firstRadio.checked = true;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void onNext(final UpdateManagerContext context, final WorkflowCallback callback) {
        context.reset();
        if (context.updateType == UpdateType.ONLINE) {
            // the next step will be skipped (no user input required) so we have to execute the operation here
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES)
                    .param(USE_DEFAULT_LOCAL_CACHE, true)
                    .build();
            dispatcher.execute(operation,
                    result -> {
                        context.updates = result.get(UPDATES).asList();
                        callback.proceed();
                    }, (op, error) -> {
                        wizard().showError(resources.constants().error(), resources.messages().lastOperationFailed(),
                                error, false);
                    });
        } else {
            callback.proceed();
        }
    }
}
