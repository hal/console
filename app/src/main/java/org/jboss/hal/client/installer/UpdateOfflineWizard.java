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

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.client.installer.UpdateOfflineState.APPLY_UPDATE;
import static org.jboss.hal.client.installer.UpdateOfflineState.LIST_UPDATES;
import static org.jboss.hal.client.installer.UpdateOfflineState.PREPARE_SERVER;
import static org.jboss.hal.client.installer.UpdateOfflineState.UPLOAD_ARCHIVES;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USE_DEFAULT_LOCAL_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WORK_DIR;

class UpdateOfflineWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateManagerContext context;

    UpdateOfflineWizard(final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.context = new UpdateManagerContext();
    }

    void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, UpdateOfflineState> builder = new Wizard.Builder<>(
                resources.constants().updateExistingInstallation(), context);

        builder.stayOpenAfterFinish()
                .addStep(UPLOAD_ARCHIVES, new UploadArchivesStep<UpdateOfflineState>(
                        dispatcher,
                        statementContext,
                        resources))
                .addStep(LIST_UPDATES, new ListUpdatesStep<UpdateOfflineState>(
                        resources.constants().listComponents(),
                        resources.messages().availableComponentsList(),
                        resources.messages().updateInstallationDescription(
                                resources.constants().listComponents(),
                                resources.constants().prepareServerCandidate(),
                                resources.constants().applyUpdates()),
                        resources.constants().noUpdates(),
                        resources.messages().noUpdatesFound()))
                .addStep(PREPARE_SERVER, new PrepareStep<UpdateOfflineState>(
                        updateManagerContext -> new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext),
                                PREPARE_UPDATES)
                                .param(WORK_DIR, updateManagerContext.workDir)
                                .param(USE_DEFAULT_LOCAL_CACHE, false)
                                .build(),
                        eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_UPDATE, new ApplyStep<UpdateOfflineState>(
                        resources.constants().applyUpdates(),
                        resources.constants().applyingUpdates(),
                        resources.messages().applyUpdatesPending(),
                        resources.constants().applyUpdatesSuccess(),
                        resources.messages().applyUpdatesSuccess(),
                        resources.messages().applyUpdatesError(),
                        dispatcher,
                        statementContext,
                        resources));

        builder.onBack((ctx, currentState) -> {
            UpdateOfflineState previous = null;
            switch (currentState) {
                case UPLOAD_ARCHIVES:
                    break;
                case LIST_UPDATES:
                    previous = UPLOAD_ARCHIVES;
                    break;
                case PREPARE_SERVER:
                    previous = LIST_UPDATES;
                    break;
                case APPLY_UPDATE:
                    previous = ctx.prepared ? LIST_UPDATES : PREPARE_SERVER;
                    break;
            }
            return previous;
        });

        builder.onNext((ctx, currentState) -> {
            UpdateOfflineState next = null;
            switch (currentState) {
                case UPLOAD_ARCHIVES:
                    next = LIST_UPDATES;
                    break;
                case LIST_UPDATES:
                    next = ctx.prepared ? APPLY_UPDATE : PREPARE_SERVER;
                    break;
                case PREPARE_SERVER:
                    next = APPLY_UPDATE;
                    break;
                case APPLY_UPDATE:
                    break;
            }
            return next;
        });

        builder.onFinish((wizard, ctx) -> column.refresh(CLEAR_SELECTION));

        builder.build().show();
    }
}
