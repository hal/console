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

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.installer.RevertState.APPLY_REVERT;
import static org.jboss.hal.client.installer.RevertState.LIST_UPDATES;
import static org.jboss.hal.client.installer.RevertState.PREPARE_SERVER;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;

class RevertUsingArchivesWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateManagerContext context;

    RevertUsingArchivesWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Resources resources,
            UpdateItem updateItem, List<ModelNode> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.context = new UpdateManagerContext(updates, updateItem);
    }

    void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, RevertState> builder = new Wizard.Builder<>(
                resources.constants().revertUpdatePreviousState(), context);

        builder.stayOpenAfterFinish()
                .addStep(LIST_UPDATES, new ListUpdatesStep<RevertState>(
                        resources.constants().listComponents(),
                        resources.messages().revertComponentsList(),
                        resources.messages().revertComponentsDescription(
                                resources.constants().listComponents(),
                                resources.constants().prepareServerCandidate(),
                                resources.constants().applyUpdates())))
                .addStep(PREPARE_SERVER,
                        new UploadAndPrepareStep<RevertState>(eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_REVERT, new ApplyStep<RevertState>(
                        resources.constants().applyUpdates(),
                        resources.constants().applyingUpdates(),
                        resources.messages().applyUpdatesPending(),
                        resources.constants().applyUpdatesSuccess(),
                        resources.messages().revertUpdatesSuccess(),
                        resources.messages().revertUpdatesError(),
                        dispatcher, statementContext, resources));

        builder.onBack((ctx, currentState) -> {
            RevertState previous = null;
            switch (currentState) {
                case LIST_UPDATES:
                    break;
                case PREPARE_SERVER:
                    previous = LIST_UPDATES;
                    break;
                case APPLY_REVERT:
                    previous = ctx.prepared ? LIST_UPDATES : PREPARE_SERVER;
                    break;
            }
            return previous;
        });

        builder.onNext((ctx, currentState) -> {
            RevertState next = null;
            switch (currentState) {
                case LIST_UPDATES:
                    next = ctx.prepared ? APPLY_REVERT : PREPARE_SERVER;
                    break;
                case PREPARE_SERVER:
                    next = APPLY_REVERT;
                    break;
                case APPLY_REVERT:
                    break;
            }
            return next;
        });

        builder.onFinish((wizard, ctx) -> column.refresh(RESTORE_SELECTION));

        builder.build().show();
    }
}
