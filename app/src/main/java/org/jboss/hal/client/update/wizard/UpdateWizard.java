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
package org.jboss.hal.client.update.wizard;

import java.util.List;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

public class UpdateWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateContext wizardContext;

    public UpdateWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Resources resources,
            List<String> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.wizardContext = new UpdateContext(updates);
    }

    public void show() {
        Wizard.Builder<UpdateContext, UpdateState> builder = new Wizard.Builder<>(
                resources.constants().updateServer(), wizardContext);

        builder.addStep(UpdateState.LIST_UPDATES, new ListUpdatesStep(resources))
                .addStep(UpdateState.PREPARE_SERVER, new PrepareUpdatesStep(eventBus, dispatcher, statementContext, resources))
                .addStep(UpdateState.APPLY_UPDATE, new ApplyUpdateStep(eventBus, dispatcher, statementContext, resources))
                .stayOpenAfterFinish();

        builder.onBack((ctx, currentState) -> {
            UpdateState previous = null;
            switch (currentState) {
                case LIST_UPDATES:
                    break;
                case PREPARE_SERVER:
                    previous = UpdateState.LIST_UPDATES;
                    break;
                case APPLY_UPDATE:
                    previous = ctx.prepared ? UpdateState.LIST_UPDATES : UpdateState.PREPARE_SERVER;
                    break;
            }
            return previous;
        });

        builder.onNext((ctx, currentState) -> {
            UpdateState next = null;
            switch (currentState) {
                case LIST_UPDATES:
                    next = ctx.prepared ? UpdateState.APPLY_UPDATE : UpdateState.PREPARE_SERVER;
                    break;
                case PREPARE_SERVER:
                    next = UpdateState.APPLY_UPDATE;
                    break;
                case APPLY_UPDATE:
                    break;
            }
            return next;
        });

        builder.build().show();
    }
}
