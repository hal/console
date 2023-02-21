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

import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.client.update.Timeouts;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.update.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;

class PrepareUpdatesStep extends WizardStep<UpdateContext, UpdateState> implements AsyncStep<UpdateContext> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    PrepareUpdatesStep(final EventBus eventBus, final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super("Prepare Server", true);
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
        wizard().showProgress("Preparing Server",
                SafeHtmlUtils.fromSafeConstant(
                        "The server is being prepared with the updates. This operation might take up to "
                                + Timeouts.PREPARE_UPDATES + " seconds."));

        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), PREPARE_UPDATES).build();
        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.execute(operation).then(ignore -> Promise.resolve(flowContext)));
        return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.PREPARE_UPDATES * 1_000)
                .then(ignore -> {
                    updateContext.prepared = true;
                    wizard().showSuccess("Server Prepared",
                            SafeHtmlUtils.fromSafeConstant("The server has been successfully prepared."), false);
                    return Promise.resolve(updateContext);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError("Timeout",
                                SafeHtmlUtils.fromSafeConstant("The operation ran into a timeout"), false);
                    } else {
                        wizard().showError("Failure",
                                SafeHtmlUtils.fromSafeConstant("Unable to prepare server."), String.valueOf(failure), false);
                    }
                    return Promise.reject(failure);
                });
    }

    @Override
    public void onCancel(final UpdateContext context, final WorkflowCallback callback) {
        if (context.prepared) {
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CLEAN).build();
            dispatcher.execute(operation,
                    modelNode -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus,
                                Message.info(SafeHtmlUtils.fromSafeConstant("Server preparation cleaned up.")));
                    }, (op, error) -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus,
                                Message.error(SafeHtmlUtils.fromSafeConstant("Unable to clean up server preparation.")));
                    });
        } else {
            callback.proceed();
        }
    }
}
