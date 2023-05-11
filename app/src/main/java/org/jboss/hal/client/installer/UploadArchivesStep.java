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
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAVEN_REPO_FILES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;

class UploadArchivesStep<S extends Enum<S>> extends WizardStep<UpdateManagerContext, S>
        implements AsyncStep<UpdateManagerContext> {

    private final SafeHtml uploadProgressMessage;
    private final SafeHtml uploadErrorMessage;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;
    private final UploadElement uploadElement;

    UploadArchivesStep(final String title,
            SafeHtml noContent,
            final SafeHtml uploadProgressMessage,
            final SafeHtml uploadErrorMessage,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(title);
        this.uploadProgressMessage = uploadProgressMessage;
        this.uploadErrorMessage = uploadErrorMessage;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;

        root = div()
                .add(uploadElement = new UploadElement(false, noContent))
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
    public void onNext(final UpdateManagerContext context, final WorkflowCallback callback) {
        wizard().showProgress(title, uploadProgressMessage);
        ModelNode mavenRepoFiles = new ModelNode();
        for (int i = 0; i < uploadElement.getFiles().length; i++) {
            mavenRepoFiles.add(i);
        }
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES)
                .param(MAVEN_REPO_FILES, mavenRepoFiles)
                .param("no-resolve-local-cache", true)
                .build();
        List<Task<FlowContext>> tasks = singletonList(
                flowContext -> dispatcher.upload(uploadElement.getFiles(), operation).then(result -> {
                    List<ModelNode> updates = result.get(UPDATES).asList();
                    flowContext.push(updates);
                    return Promise.resolve(flowContext);
                }));
        Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.UPLOAD * 1_000)
                .then(flowContext -> {
                    context.updates = flowContext.pop();
                    callback.proceed();
                    return Promise.resolve(context);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                    } else {
                        wizard().showError(resources.constants().error(), uploadErrorMessage, failure.toString(), false);
                    }
                    return Promise.reject(failure);
                });
    }
}
