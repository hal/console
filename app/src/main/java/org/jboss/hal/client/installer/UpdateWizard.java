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
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.client.installer.UpdateState.APPLY_UPDATE;
import static org.jboss.hal.client.installer.UpdateState.LIST_UPDATES;
import static org.jboss.hal.client.installer.UpdateState.PREPARE_SERVER;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;

public class UpdateWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateManagerContext context;

    public UpdateWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Resources resources,
            List<ModelNode> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.context = new UpdateManagerContext(updates);
    }

    public void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, UpdateState> builder = new Wizard.Builder<>(
                resources.constants().updateServer(), context);

        builder.stayOpenAfterFinish()
                .addStep(LIST_UPDATES, new InitStep<UpdateState>(
                        Ids.UPDATE_MANAGER_UPDATE,
                        resources.constants().listUpdates(),
                        resources.messages().listUpdatesTable(),
                        resources.messages().listUpdatesDescription()))
                .addStep(PREPARE_SERVER, new PrepareStep<UpdateState>(
                        resources.constants().prepareUpdate(),
                        resources.messages().prepareUpdatePending(),
                        resources.messages().prepareUpdateSuccess(),
                        resources.messages().prepareUpdateError(),
                        (__) -> new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), PREPARE_UPDATES).build(),
                        eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_UPDATE, new ApplyStep<UpdateState>(
                        resources.constants().applyUpdate(),
                        resources.messages().applyUpdatePending(),
                        resources.messages().applyUpdateSuccess(),
                        resources.messages().applyUpdateError(),
                        dispatcher, statementContext, resources));

        builder.onBack((ctx, currentState) -> {
            UpdateState previous = null;
            switch (currentState) {
                case LIST_UPDATES:
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

        builder.onFinish((wizard, ctx) -> column.refresh(RESTORE_SELECTION));

        builder.build().show();
    }
}
