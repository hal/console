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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.client.installer.RevertState.APPLY_REVERT;
import static org.jboss.hal.client.installer.RevertState.PREPARE_SERVER;
import static org.jboss.hal.client.installer.RevertState.REVIEW_REVERT;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;

class RevertWizard {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final UpdateManagerContext context;

    RevertWizard(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Resources resources,
            UpdateItem updateItem, List<ModelNode> updates) {
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.context = new UpdateManagerContext(updates, updateItem);
    }

    void show(UpdateColumn column) {
        Wizard.Builder<UpdateManagerContext, RevertState> builder = new Wizard.Builder<>(
                "Revert update to previous state", context);

        builder.stayOpenAfterFinish()
                .addStep(REVIEW_REVERT, new ListUpdatesStep<RevertState>(
                        Ids.UPDATE_MANAGER_REVERT,
                        "Review reversion",
                        new SafeHtmlBuilder().appendEscaped("The following updates will be reverted:").toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped(
                                        "<p>The wizard guides you through the steps of reverting an update.</p>" +
                                        "<h4>Review reversion</h4>" +
                                        "<p>This step lists all the components that will be reverted.</p>" +
                                        "<h4>Prepare server candidate</h4>" +
                                        "<p>This step provisions a server candidate with the reverted updates. If you want to discard this server candidate or do not want to proceed, you can cancel the reversion after this step is complete.</p>"
                                        +
                                        "<h4>Apply reversion</h4>" +
                                        "<p>This step will restart the base server and apply the reversion from the server candidate to the base server.</p>"
                                        +
                                        "<p>If a step times out it does not necessarily mean that the reversion has failed. In such cases, check the log files to see if the reversion was successful.</p>")
                                .toSafeHtml()))
                .addStep(PREPARE_SERVER, new PrepareStep<RevertState>(
                        "Prepare server candidate",
                        "Preparing server candidate",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The server candidate is being prepared with the reversion. The time taken for this operation depends on the speed of the internet connection.")
                                .toSafeHtml(),
                        "Server candidate prepared",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The server candidate with the updates has been successfully provisioned. Click next to apply the updates to the base server.")
                                .toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped("Unable to prepare the server candidate.").toSafeHtml(),
                        context -> new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), PREPARE_REVERT)
                                .param(REVISION, context.updateItem.getName())
                                .build(),
                        eventBus, dispatcher, statementContext, resources))
                .addStep(APPLY_REVERT, new ApplyStep<RevertState>(
                        "Apply reversion",
                        "Applying reversion",
                        new SafeHtmlBuilder().appendEscaped(
                                        "The reversion from the prepared candidate server is applied to the base server. To apply the reversion, the base server is restarted.")
                                .toSafeHtml(),
                        "Update reverted",
                        new SafeHtmlBuilder().appendEscaped("The update has been successfully reverted.").toSafeHtml(),
                        new SafeHtmlBuilder().appendEscaped("Unable to revert the updates.").toSafeHtml(),
                        dispatcher, statementContext, resources));

        builder.onBack((ctx, currentState) -> {
            RevertState previous = null;
            switch (currentState) {
                case REVIEW_REVERT:
                    break;
                case PREPARE_SERVER:
                    previous = REVIEW_REVERT;
                    break;
                case APPLY_REVERT:
                    previous = ctx.prepared ? REVIEW_REVERT : PREPARE_SERVER;
                    break;
            }
            return previous;
        });

        builder.onNext((ctx, currentState) -> {
            RevertState next = null;
            switch (currentState) {
                case REVIEW_REVERT:
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
