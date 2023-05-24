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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FileItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
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
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.File;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM_PATCH_FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPLOAD_CUSTOM_PATCH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WORK_DIR;
import static org.jboss.hal.flow.Flow.sequential;

class UploadPatchesStep<S extends Enum<S>> extends WizardStep<UpdateManagerContext, S>
        implements AsyncStep<UpdateManagerContext> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement root;
    private final Form<ModelNode> form;

    UploadPatchesStep(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(resources.constants().uploadCustomPatches());
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;

        Metadata metadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forOperation(UPLOAD_CUSTOM_PATCH);
        form = new ModelNodeForm.Builder<>(Ids.build(Ids.UPDATE_MANAGER_UPDATE_PATCH, Ids.FORM), metadata)
                .addOnly()
                // unbound to skip model node mapping
                .unboundFormItem(new FileItem(CUSTOM_PATCH_FILE, new LabelBuilder().label(CUSTOM_PATCH_FILE)))
                .build();
        form.getFormItem(CUSTOM_PATCH_FILE).setRequired(true);
        registerAttachable(form);
        root = div()
                .add(form)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(UpdateManagerContext context) {
        form.edit(new ModelNode());
    }

    @Override
    public void onNext(final UpdateManagerContext context, final WorkflowCallback callback) {
        boolean valid = form.save();
        if (valid) {
            FormItem<File> files = form.getFormItem(CUSTOM_PATCH_FILE);
            ModelNode payload = form.getModel();
            payload.get(CUSTOM_PATCH_FILE).set(0);

            Operation uploadOperation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), UPLOAD_CUSTOM_PATCH)
                    .payload(payload)
                    .build();
            Task<FlowContext> uploadTask = fc -> dispatcher.upload(files.getValue(), uploadOperation)
                    .then(result -> fc.resolve());

            Operation listUpdatesOperation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES)
                    .build();
            Task<FlowContext> listUpdatesTask = fc -> dispatcher.execute(listUpdatesOperation).then(result -> {
                context.updates = result.get(UPDATES).asList();
                if (result.hasDefined(WORK_DIR)) {
                    context.workDir = result.get(WORK_DIR).asString();
                }
                return fc.resolve();
            });

            wizard().showProgress(resources.constants().uploadingCustomPatches(),
                    resources.messages().uploadCustomPatchesPending());
            sequential(new FlowContext(Progress.NOOP), asList(uploadTask, listUpdatesTask))
                    .timeout(Timeouts.UPLOAD * 1_000)
                    .then(flowContext -> {
                        callback.proceed();
                        return Promise.resolve(context);
                    })
                    .catch_(failure -> {
                        if (FlowContext.timeout(failure)) {
                            wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(), false);
                        } else {
                            wizard().showError(resources.constants().error(), resources.messages().uploadCustomPatchesError(),
                                    String.valueOf(failure), false);
                        }
                        return Promise.reject(failure);
                    });
        }
    }
}
