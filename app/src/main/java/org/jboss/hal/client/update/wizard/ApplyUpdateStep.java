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
package org.jboss.hal.client.update.wizard;

import java.util.List;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.update.Timeouts;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.update.AddressTemplates.ROOT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERFORM_INSTALLATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;

class ApplyUpdateStep extends WizardStep<UpdateContext, UpdateState> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    public ApplyUpdateStep(final EventBus eventBus, final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super("Apply Update", true);
        this.eventBus = eventBus;
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
        wizard().showProgress("Applying Update",
                SafeHtmlUtils
                        .fromSafeConstant("The server is being restarted to apply the update. This operation might take up to "
                                + Timeouts.APPLY_UPDATE + " seconds."));

        Operation operation = new Operation.Builder(ROOT_TEMPLATE.resolve(statementContext), SHUTDOWN)
                .param(PERFORM_INSTALLATION, true)
                .build();
        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.execute(operation).then(ignore -> Promise.resolve(flowContext)));
        return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.APPLY_UPDATE * 1_000)
                .then(ignore -> {
                    wizard().showSuccess("Update Applied",
                            SafeHtmlUtils.fromSafeConstant("The update has been successfully applied."));
                    return Promise.resolve(updateContext);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError("Timeout",
                                SafeHtmlUtils.fromSafeConstant("The operation ran into a timeout"), false);
                    } else {
                        wizard().showError("Failure",
                                SafeHtmlUtils.fromSafeConstant("Unable to apply the update."), String.valueOf(failure), false);
                    }
                    return Promise.reject(failure);
                });
    }
}
