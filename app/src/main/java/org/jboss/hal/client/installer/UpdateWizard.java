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
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.installer.UpdateState.APPLY_UPDATE;
import static org.jboss.hal.client.installer.UpdateState.CHOOSE_TYPE;
import static org.jboss.hal.client.installer.UpdateState.LIST_UPDATES;
import static org.jboss.hal.client.installer.UpdateState.PREPARE_SERVER;
import static org.jboss.hal.client.installer.UpdateState.PROPERTIES;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;

class UpdateWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final UpdateManagerContext context;

    UpdateWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext,
            MetadataRegistry metadataRegistry, Resources resources, UpdateItem updateItem, List<ModelNode> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.context = updateItem == null ? new UpdateManagerContext()
                : new UpdateManagerContext(updateItem.getName(), updates);
    }

    void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, UpdateState> builder = new Wizard.Builder<>(
                resources.constants().updateExistingInstallation(), context);

        builder.stayOpenAfterFinish()
                .addStep(CHOOSE_TYPE, new ChooseTypeStep(dispatcher, statementContext, resources))
                .addStep(PROPERTIES, new PropertiesStep(dispatcher, statementContext, metadataRegistry, resources))
                .addStep(LIST_UPDATES, new ListUpdatesStep(
                        resources.constants().listUpdates(),
                        resources.messages().availableComponentsList(),
                        resources.messages().updateInstallationDescription(
                                resources.constants().listComponents(),
                                resources.constants().prepareServerCandidate(),
                                resources.constants().applyUpdates()),
                        resources.messages().noUpdatesFound()))
                .addStep(PREPARE_SERVER, new PrepareStep(
                        PREPARE_UPDATES,
                        eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_UPDATE, new ApplyStep(
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
            UpdateState previous = null;
            switch (currentState) {
                case CHOOSE_TYPE:
                    break;
                case PROPERTIES:
                    previous = CHOOSE_TYPE;
                    break;
                case LIST_UPDATES:
                    previous = ctx.updateType != UpdateType.ONLINE ? PROPERTIES : CHOOSE_TYPE;
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
            UpdateState next = null;
            switch (currentState) {
                case CHOOSE_TYPE:
                    // online requires no user input, skip properties step
                    next = ctx.updateType != UpdateType.ONLINE ? PROPERTIES : LIST_UPDATES;
                    break;
                case PROPERTIES:
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

        // revert operation triggered from column item
        if (context.isRevert()) {
            context.updateType = UpdateType.REVERT;
            builder.setInitialState(LIST_UPDATES);
        }
        builder.onFinish((wizard, ctx) -> column.refresh(CLEAR_SELECTION));

        builder.build().show();
    }
}
