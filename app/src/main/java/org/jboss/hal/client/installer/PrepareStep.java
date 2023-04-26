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
import java.util.function.Function;

import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAN;

class PrepareStep<S extends Enum<S>> extends WizardStep<InstallerContext, S> implements AsyncStep<InstallerContext> {

    private final SafeHtml progressMessage;
    private final SafeHtml successMessage;
    private final SafeHtml errorMessage;
    private final Function<InstallerContext, Operation> operation;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;

    PrepareStep(final String title,
            final SafeHtml progressMessage,
            final SafeHtml successMessage,
            final SafeHtml errorMessage,
            final Function<InstallerContext, Operation> operation,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(title, true);
        this.progressMessage = progressMessage;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
        this.operation = operation;
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
    protected Promise<InstallerContext> onShowAndWait(final InstallerContext context) {
        wizard().showProgress(title, progressMessage);

        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.execute(operation.apply(context)).then(ignore -> Promise.resolve(flowContext)));
        return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.PREPARE * 1_000)
                .then(ignore -> {
                    context.prepared = true;
                    wizard().showSuccess(resources.constants().success(), successMessage, false);
                    return Promise.resolve(context);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                    } else {
                        wizard().showError(resources.constants().error(), errorMessage, false);
                    }
                    return Promise.reject(failure);
                });
    }

    @Override
    public void onCancel(final InstallerContext context, final WorkflowCallback callback) {
        if (context.prepared) {
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CLEAN).build();
            dispatcher.execute(operation,
                    modelNode -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus, Message.info(resources.messages().prepareCleanupSuccess()));
                    }, (op, error) -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus, Message.error(resources.messages().prepareCleanupError()));
                    });
        } else {
            callback.proceed();
        }
    }
}