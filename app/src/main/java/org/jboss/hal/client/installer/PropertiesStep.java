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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FileItem;
import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.NotMoreThanOneAlternativeValidation;
import org.jboss.hal.core.mbui.form.RequiredByValidation;
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

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ARTIFACT_CHANGES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM_PATCH_FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY_FROM_REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCAL_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANIFEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAVEN_REPO_FILES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OFFLINE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPOSITORIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPLOAD_CUSTOM_PATCH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USE_DEFAULT_LOCAL_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WORK_DIR;
import static org.jboss.hal.flow.Flow.sequential;

class PropertiesStep extends WizardStep<UpdateManagerContext, UpdateState>
        implements AsyncStep<UpdateManagerContext> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final HTMLElement root;
    private ModelNodeForm<ModelNode> form;
    private FileItem mavenRepos;
    private FileItem customPatch;

    private static final String[] UPDATE_PROPERTIES = { LOCAL_CACHE, OFFLINE, USE_DEFAULT_LOCAL_CACHE, REPOSITORIES };

    PropertiesStep(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(resources.constants().properties());
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.resources = resources;

        // form inputs depend on UpdateManagerContext.updateType, we can't build it now
        root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(UpdateManagerContext context) {
        if (context.isPropertiesFormBuilt) {
            // do not rebuild form unless user starts a new update from scratch
            return;
        }
        Metadata metadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forOperation(LIST_UPDATES);
        Metadata customPatchMetadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forOperation(UPLOAD_CUSTOM_PATCH);
        Metadata revertMetadata = metadataRegistry.lookup(INSTALLER_TEMPLATE).forOperation(HISTORY_FROM_REVISION);
        revertMetadata.copyAttribute(REVISION, metadata);
        customPatchMetadata.copyAttribute(MANIFEST, metadata);

        LabelBuilder lb = new LabelBuilder();
        mavenRepos = new FileItem(MAVEN_REPO_FILES, lb.label(MAVEN_REPO_FILES), true);
        customPatch = new FileItem(CUSTOM_PATCH_FILE, lb.label(CUSTOM_PATCH_FILE));

        ModelNodeForm.Builder<ModelNode> formBuilder = new ModelNodeForm.Builder<>(
                Ids.build(Ids.UPDATE_MANAGER, PROPERTIES, Ids.FORM), metadata)
                .unsorted()
                .addOnly()
                .dontVerifyExcludes();

        switch (context.updateType) {
            case OFFLINE_WITH_REPO:
                formBuilder.exclude(UPDATE_PROPERTIES);
                formBuilder.exclude(MANIFEST, REVISION);
                formBuilder.unboundFormItem(mavenRepos);
                break;
            case CUSTOM_PATCH:
                formBuilder.exclude(UPDATE_PROPERTIES);
                formBuilder.exclude(REVISION);
                formBuilder.unboundFormItem(customPatch);
                break;
            case CUSTOM:
                formBuilder.unboundFormItem(customPatch);
                formBuilder.unboundFormItem(mavenRepos);
                break;
        }

        form = formBuilder.build();
        form.attach();
        root.replaceChildren(form.element());

        switch (context.updateType) {
            case OFFLINE_WITH_REPO:
                form.getFormItem(MAVEN_REPO_FILES).setRequired(true);
                break;
            case CUSTOM_PATCH:
                form.getFormItem(CUSTOM_PATCH_FILE).setRequired(true);
                break;
            case CUSTOM:
                // for normal update required attributes need to be unmarked
                form.getFormItem(MANIFEST).setRequired(false);
                form.getFormItem(REVISION).setRequired(false);
                // for upload-custom-patch both manifest and custom-patch need to be set
                form.getFormItem(MANIFEST).addValidationHandler(new RequiredByValidation<>(form.getFormItem(MANIFEST),
                        List.of(CUSTOM_PATCH_FILE), form, resources.constants(), resources.messages()));
                form.getFormItem(CUSTOM_PATCH_FILE)
                        .addValidationHandler(new RequiredByValidation<>(form.getFormItem(CUSTOM_PATCH_FILE),
                                List.of(MANIFEST), form, resources.constants(), resources.messages()));
                // revert and upload-custom-patch are mutually exclusive
                form.addFormValidation(new NotMoreThanOneAlternativeValidation<>(asList(MANIFEST, REVISION), form,
                        resources.constants(), resources.messages()));
                break;
        }

        context.isPropertiesFormBuilt = true;
        form.edit(new ModelNode());
    }

    @Override
    public void onNext(final UpdateManagerContext context, final WorkflowCallback callback) {
        boolean valid = form.save();
        ModelNode payload = form.getModel();
        if (valid) {
            context.properties = payload.clone();
            if (payload.hasDefined(REVISION)) {
                context.revision = payload.get(REVISION).asString();
            }

            Operation.Builder operationBuilder = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext),
                    !context.isRevert() ? LIST_UPDATES : HISTORY_FROM_REVISION)
                    .payload(payload);
            if (context.isRevert()) {
                context.mavenReposForRevert = mavenRepos.getFiles();
                operationBuilder.param(REVISION, context.revision);
            } else if (!mavenRepos.isEmpty()) {
                ModelNode mavenRepoFiles = new ModelNode();
                for (int i = 0; i < mavenRepos.getFiles().length; i++) {
                    mavenRepoFiles.add(i);
                }
                operationBuilder.param(MAVEN_REPO_FILES, mavenRepoFiles);
            }

            Operation operation = operationBuilder.build();
            Task<FlowContext> listUpdatesTask = flowContext -> {
                Promise<ModelNode> promise;
                if (!(context.isRevert() || mavenRepos.isEmpty())) {
                    wizard().showProgress(resources.constants().uploadingArchives(),
                            resources.messages().uploadArchivesPending());
                    promise = dispatcher.upload(mavenRepos.getFiles(), operation);
                } else {
                    promise = dispatcher.execute(operation);
                }

                return promise.then(result -> {
                    context.updates = result.get(!context.isRevert() ? UPDATES : ARTIFACT_CHANGES).asList();
                    if (result.hasDefined(WORK_DIR)) {
                        context.workDir = result.get(WORK_DIR).asString();
                    }
                    return flowContext.resolve();
                });
            };

            List<Task<FlowContext>> tasks;
            if (!customPatch.isEmpty()) {
                wizard().showProgress(resources.constants().uploadingCustomPatches(),
                        resources.messages().uploadCustomPatchesPending());
                ModelNode patchPayload = new ModelNode();
                patchPayload.get(MANIFEST).set(payload.get(MANIFEST));
                patchPayload.get(CUSTOM_PATCH_FILE).set(0);

                Operation uploadPatchOperation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext),
                        UPLOAD_CUSTOM_PATCH)
                        .payload(patchPayload)
                        .build();
                Task<FlowContext> uploadPatchTask = fc -> dispatcher.upload(customPatch.getFiles(), uploadPatchOperation)
                        .then(result -> fc.resolve());

                tasks = asList(uploadPatchTask, listUpdatesTask);
            } else {
                tasks = singletonList(listUpdatesTask);
            }

            sequential(new FlowContext(Progress.NOOP), tasks)
                    .timeout(Timeouts.UPLOAD * 1_000)
                    .then(flowContext -> {
                        callback.proceed();
                        return Promise.resolve(context);
                    })
                    .catch_(failure -> {
                        if (FlowContext.timeout(failure)) {
                            wizard().showError(resources.constants().timeout(), resources.messages().operationTimeout(),
                                    false);
                        } else {
                            wizard().showError(resources.constants().error(), resources.messages().uploadArchivesError(),
                                    String.valueOf(failure), false);
                        }
                        return Promise.reject(failure);
                    });
        }
    }
}
