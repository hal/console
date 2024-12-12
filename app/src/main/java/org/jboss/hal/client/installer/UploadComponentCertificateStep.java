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

import java.util.Collections;
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

import elemental2.dom.File;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_PARSE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERT_FILE;
import static org.jboss.hal.flow.Flow.sequential;

class UploadComponentCertificateStep extends WizardStep<ImportComponentCertificateContext, ImportComponentCertificateState>
        implements AsyncStep<ImportComponentCertificateContext> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;
    private final UploadElement uploadElement;

    UploadComponentCertificateStep(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(resources.constants().importCertificate());
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;

        root = div()
                .add(uploadElement = new UploadElement(true, resources.messages().noContent()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(ImportComponentCertificateContext context) {
        uploadElement.reset();
    }

    @Override
    public void onNext(final ImportComponentCertificateContext context, final WorkflowCallback callback) {
        if (uploadElement.getFiles().length == 0) {
            wizard().showError(resources.constants().error(), resources.messages().noFileSelected(), false);
        }

        final File item = uploadElement.getFiles().item(0);
        wizard().showProgress(resources.constants().uploadInProgress(), resources.messages().uploadInProgress(item.name));
        context.setFile(item);

        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CERTIFICATE_PARSE)
                .param(CERT_FILE, new ModelNode().set(0))
                .build();
        List<Task<FlowContext>> tasks = Collections.singletonList(
                flowContext -> dispatcher.upload(uploadElement.getFiles().item(0), operation).then(result -> {
                    context.setImportedCertificate(new CertificateInfo(result.get()));
                    return Promise.resolve(flowContext);
                }));

        sequential(new FlowContext(Progress.NOOP), tasks)
                .timeout(Timeouts.UPLOAD * 1_000)
                .then(flowContext -> {
                    callback.proceed();
                    return Promise.resolve(context);
                })
                .catch_(failure -> {
                    if (FlowContext.timeout(failure)) {
                        wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                    } else {
                        wizard().showError(resources.constants().error(), resources.messages().uploadError(item.name),
                                String.valueOf(failure), false);
                    }
                    return Promise.reject(failure);
                });
    }
}
