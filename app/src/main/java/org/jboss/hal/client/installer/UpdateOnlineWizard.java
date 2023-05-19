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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import java.util.List;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.client.installer.UpdateOnlineState.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;

class UpdateOnlineWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateManagerContext context;

    UpdateOnlineWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Resources resources,
            List<ModelNode> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.context = new UpdateManagerContext(updates);
    }

    void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, UpdateOnlineState> builder = new Wizard.Builder<>(
                "Update existing installation", context);

        builder.stayOpenAfterFinish()
                .addStep(LIST_UPDATES, new ListUpdatesStep<UpdateOnlineState>(
                        "List components",
                        new SafeHtmlBuilder()
                                .appendEscaped(
                                        "The following components are available for the existing JBoss EAP installation:")
                                .toSafeHtml(),
                        new SafeHtmlBuilder().appendHtmlConstant(
                                        "<p>The wizard guides you through the process of updating your existing installation.</p>" +
                                                "<h4>List components</h4>" +
                                        "<p>This step lists all the components that will be updated.</p>" +
                                        "<h4>Prepare server candidate</h4>" +
                                        "<p>This step provisions a server candidate with the latest available patches. If you want to discard this server candidate or do not want to proceed, you can cancel the update after this step is complete.</p>"
                                        +
                                                "<h4>Apply updates</h4>" +
                                        "<p>This step will restart the base server and apply the updates from the server candidate to the base server.</p>"
                                        +
                                        "<p>If a step times out it does not necessarily mean that the update has failed. In such cases, check the log files to see if the update was successful.</p>")
                                .toSafeHtml()))
                .addStep(PREPARE_SERVER, new PrepareStep<UpdateOnlineState>(
                        "Prepare server candidate",
                        "Preparing server candidate",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The server candidate is being prepared with the updates. The time taken for this operation depends on the speed of the internet connection.")
                                .toSafeHtml(),
                        "Server candidate prepared",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The server candidate with the updates has been successfully provisioned. Click next to apply the updates to the base server.")
                                .toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped("Unable to prepare the server candidate.").toSafeHtml(),
                        (__) -> new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), PREPARE_UPDATES).build(),
                        eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_UPDATE, new ApplyStep<UpdateOnlineState>(
                        "Apply updates",
                        "Applying updates",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The updates from the prepared candidate server are applied to the base server. To apply the updates, the base server is restarted.")
                                .toSafeHtml(),
                        "Updates applied",
                        new SafeHtmlBuilder().appendEscaped("The updates have been successfully applied.").toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped("Unable to apply the updates.").toSafeHtml(),
                        dispatcher,
                        statementContext,
                        resources));

        builder.onBack((ctx, currentState) -> {
            UpdateOnlineState previous = null;
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
            UpdateOnlineState next = null;
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

        builder.onFinish((wizard, ctx) -> column.refresh(CLEAR_SELECTION));

        builder.build().show();
    }
}
