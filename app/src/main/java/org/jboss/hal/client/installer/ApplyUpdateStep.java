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

import java.util.List;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERFORM_INSTALLATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;

class ApplyUpdateStep extends WizardStep<UpdateContext, UpdateState> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    public ApplyUpdateStep(final Dispatcher dispatcher, final StatementContext statementContext, final Resources resources) {
        super(resources.constants().applyUpdate(), true);
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected Promise<UpdateContext> onShowAndWait(final UpdateContext updateContext) {
        wizard().showProgress(resources.constants().applyUpdate(),
                resources.messages().applyUpdateDescription(Timeouts.APPLY_UPDATE));

        Operation operation = new Operation.Builder(ROOT_TEMPLATE.resolve(statementContext), SHUTDOWN)
                .param(PERFORM_INSTALLATION, true)
                .build();
        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.execute(operation).then(ignore -> Promise.resolve(flowContext)));
        return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.APPLY_UPDATE * 1_000)
                .then(ignore -> {
                    wizard().showSuccess(resources.constants().applyUpdateSuccess(),
                            resources.messages().applyUpdateSuccessDescription());
                    return Promise.resolve(updateContext);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                    } else {
                        wizard().showError(resources.constants().error(), resources.messages().applyUpdateErrorDescription(),
                                false);
                    }
                    return Promise.reject(failure);
                });
    }
}
