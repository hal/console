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

import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.client.shared.uploadwizard.UploadElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAVEN_REPO_FILES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.flow.Flow.sequential;

class UploadAndPrepareStep<S extends Enum<S>> extends WizardStep<UpdateManagerContext, S>
        implements AsyncStep<UpdateManagerContext> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;
    private final UploadElement uploadElement;

    UploadAndPrepareStep(final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(resources.constants().prepareServerCandidate(), true);
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;

        root = div()
                .add(uploadElement = new UploadElement(false, resources.messages().noContent()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(UpdateManagerContext context) {
        uploadElement.reset();
    }

    @Override
    public void onNext(UpdateManagerContext context, WorkflowCallback callback) {
        wizard().showProgress(resources.constants().preparingServerCandidate(),
                resources.messages().prepareServerCandidatePending());

        ModelNode mavenRepoFiles = new ModelNode();
        for (int i = 0; i < uploadElement.getFiles().length; i++) {
            mavenRepoFiles.add(i);
        }
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), PREPARE_REVERT)
                .param(REVISION, context.updateItem.getName())
                .param(MAVEN_REPO_FILES, mavenRepoFiles)
                .build();
        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.upload(uploadElement.getFiles(), operation)
                        .then(result -> Promise.resolve(flowContext)));
        sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.UPLOAD * 1_000)
                .then(flowContext -> {
                    context.prepared = true;
                    wizard().showSuccess(resources.constants().prepareServerCandidateSuccess(),
                            resources.messages().prepareServerCandidateSuccessDescription(), false);
                    callback.proceed();
                    return Promise.resolve(context);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                    } else {
                        wizard().showError(resources.constants().error(), resources.messages().prepareServerCandidateError(),
                                String.valueOf(failure), false);
                    }
                    return Promise.reject(failure);
                });
    }

    @Override
    public void onCancel(final UpdateManagerContext context, final WorkflowCallback callback) {
        if (context.prepared) {
            Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CLEAN).build();
            dispatcher.execute(operation,
                    modelNode -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus, Message.success(resources.messages().serverCandidateCleanupSuccess()));
                    }, (op, error) -> {
                        callback.proceed();
                        MessageEvent.fire(eventBus, Message.error(resources.messages().serverCandidateCleanupError()));
                    });
        } else {
            callback.proceed();
        }
    }
}
