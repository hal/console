/*
 *  Copyright 2024 Red Hat
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.Composite;
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
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_IMPORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FINGERPRINT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_ID;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.marginBottomLarge;

public class ImportMissingComponentCertificateStep<S extends Enum<S>>
        extends org.jboss.hal.ballroom.wizard.WizardStep<UpdateManagerContext, S>
        implements AsyncStep<UpdateManagerContext> {
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final ModelNodeTable<CertificateInfo> table;
    private final HTMLElement root;
    private final EventBus eventBus;

    public ImportMissingComponentCertificateStep(final Dispatcher dispatcher,
                                                 final StatementContext statementContext,
                                                 final Resources resources,
                                                 EventBus eventBus) {
        super(resources.constants().missingComponentCertificates());

        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.eventBus = eventBus;

        this.table = new ModelNodeTable.Builder<CertificateInfo>(Ids.build(Ids.UPDATE_MANAGER_CERTIFICATE),
                Metadata.staticDescription(UpdateManagerResources.INSTANCE.componentCertificate()))
                .columns(KEY_ID, DESCRIPTION, FINGERPRINT)
                .build();
        registerAttachable(table);

        this.root = div()
                .add(div().css(marginBottomLarge).innerHtml(resources.messages().importComponentCertificateConfirmation()))
                .add(table)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(UpdateManagerContext context) {
        table.update(context.missingCertInfos);
    }

    @Override
    public void onNext(final UpdateManagerContext context, final WorkflowCallback callback) {
        // import the certificates
        Composite importCerts = new Composite();

        for (String missingCert : context.missingCerts) {
            importCerts.add(new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext),
                    CERTIFICATE_IMPORT)
                    .param(CERTIFICATE_CONTENT, missingCert)
                    .build());
        }

        List<Task<FlowContext>> tasks = List.of(
                (flowContext) -> dispatcher.execute(importCerts).then(result -> Promise.resolve(flowContext)),
                (flowContext) -> {
                    return dispatcher.execute(context.listOperation).then(
                            result -> {
                                List<ModelNode> updates = new ArrayList<>();
                                updates.addAll(result.get(context.nodeType).asList());
                                context.updates.addAll(updates);
                                MessageEvent.fire(eventBus,
                                        Message.success(resources.messages().componentCertificateImportedDescription()));
                                return Promise.resolve(flowContext);
                            }).catch_((error) -> {
                                MessageEvent.fire(eventBus,
                                        Message.error(resources.messages().lastOperationFailed(), error.toString()));
                                return Promise.resolve(flowContext);
                            });
                });

        sequential(new FlowContext(Progress.NOOP), tasks)
                .then(flowContext -> {
                    callback.proceed();
                    return Promise.resolve(context);
                })
                .catch_(Promise::reject);
    }
}
