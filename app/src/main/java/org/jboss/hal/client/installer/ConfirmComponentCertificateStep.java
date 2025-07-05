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
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_IMPORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERT_FILE;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.marginBottomLarge;

class ConfirmComponentCertificateStep
        extends WizardStep<ImportComponentCertificateContext, ImportComponentCertificateState>
        implements AsyncStep<ImportComponentCertificateContext> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;

    public ConfirmComponentCertificateStep(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(resources.constants().confirmation());

        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        certificateForm = new ModelNodeForm.Builder<CertificateInfo>(Ids.build(Ids.UPDATE_MANAGER_LIST_UPDATES),
                Metadata.staticDescription(UpdateManagerResources.INSTANCE.componentCertificate()))
                .readOnly()
                .build();
        registerAttachable(certificateForm);

        root = div()
                .add(div().css(marginBottomLarge).innerHtml(resources.messages().importComponentCertificateConfirmation()))
                .add(certificateForm)
                .element();
    }

    private final HTMLElement root;
    private final ModelNodeForm<CertificateInfo> certificateForm;

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(final ImportComponentCertificateContext context) {
        certificateForm.view(context.getImportedCertificate());
    }

    @Override
    public void onNext(final ImportComponentCertificateContext context, final WorkflowCallback callback) {
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CERTIFICATE_IMPORT)
                .param(CERT_FILE, new ModelNode().set(0))
                .build();
        List<Task<FlowContext>> tasks = Collections.singletonList(
                flowContext -> dispatcher.upload(context.getFile(), operation).then(result -> {
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
                        wizard().showError(resources.constants().error(),
                                resources.messages().uploadError(context.getFile().name),
                                String.valueOf(failure), false);
                    }
                    return Promise.reject(failure);
                });
    }
}
