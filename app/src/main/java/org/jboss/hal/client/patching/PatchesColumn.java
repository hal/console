/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.patching;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.client.patching.wizard.ApplyPatchWizard;
import org.jboss.hal.client.patching.wizard.RollbackWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.resources.Ids.ADD_SUFFIX;
import static org.jboss.hal.resources.Ids.PATCHES_AGEOUT;

@Column(Ids.PATCHING)
public class PatchesColumn extends FinderColumn<ModelNode> {

    public static final AddressTemplate PATCHING_TEMPLATE = AddressTemplate.of(SELECTED_HOST, "core-service=patching");

    private EventBus eventBus;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private Environment environment;
    private HostActions hostActions;
    private MetadataProcessor metadataProcessor;
    private ServerActions serverActions;
    private Provider<Progress> progress;
    private Resources resources;

    @Inject
    public PatchesColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Environment environment,
            final HostActions hostActions,
            final MetadataProcessor metadataProcessor,
            final ServerActions serverActions,
            @Footer final Provider<Progress> progress,
            final ColumnActionFactory columnActionFactory,
            final Resources resources) {

        super(new Builder<ModelNode>(finder, Ids.PATCHING, Names.PATCHES)

                .columnAction(columnActionFactory.refresh(Ids.PATCHES_REFRESH))

                .itemsProvider((context, callback) -> {
                    ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, SHOW_HISTORY_OPERATION).build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList()));
                })
                .onPreview(PatchesPreview::new)
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.environment = environment;
        this.hostActions = hostActions;
        this.metadataProcessor = metadataProcessor;
        this.serverActions = serverActions;
        this.progress = progress;
        this.resources = resources;

        setItemRenderer(item -> new ItemDisplay<ModelNode>() {
            @Override
            public String getId() {
                return Ids.build(HOST, PATCHING, item.get(PATCH_ID).asString());
            }

            @Override
            public String getTitle() {
                return item.get(PATCH_ID).asString();
            }

            @Override
            public List<ItemAction<ModelNode>> actions() {

                List<ItemAction<ModelNode>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<ModelNode>()
                        .title(resources.constants().rollback())
                        .handler(item1 -> rollback(item1.get(PATCH_ID).asString()))
                        .constraint(Constraint.executable(PATCHING_TEMPLATE, ROLLBACK_OPERATION))
                        .build());

                return actions;
            }
        });

        ColumnAction<ModelNode> applyPatchAction = new ColumnAction.Builder<ModelNode>(Ids.PATCH_ADD)
                .element(columnActionFactory.addButton(Names.PATCH))
                .handler(column -> applyPatch())
                .build();
        ColumnAction<ModelNode> ageoutAction = new ColumnAction.Builder<ModelNode>(
                Ids.build(HOSTS, PATCHES_AGEOUT, ADD_SUFFIX))
                .element(columnActionFactory.addButton(resources.messages().cleanPatchHistory(), "fa fa-eraser"))
                .handler(column -> ageoutHistory())
                .build();
        addColumnAction(applyPatchAction);
        addColumnAction(ageoutAction);

    }

    private void rollback(final String patchId) {

        // check the host controller (or standalone server) for restart-required
        checkHostState(() ->
                metadataProcessor.lookup(PATCHING_TEMPLATE, progress.get(),
                        new SuccessfulMetadataCallback(eventBus, resources) {

                            @Override
                            public void onMetadata(final Metadata metadata) {
                                Metadata metadataRollback = metadata.forOperation(ROLLBACK_OPERATION);
                                new RollbackWizard(resources, environment, patchId, metadataRollback, statementContext,
                                        dispatcher, progress, serverActions, () -> refresh(RESTORE_SELECTION))
                                        .show();
                            }
                        }));
    }

    private void ageoutHistory() {
        metadataProcessor
                .lookup(PATCHING_TEMPLATE, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
                    @Override
                    public void onMetadata(final Metadata metadata) {
                        ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
                        Metadata operationMetadata = metadata.forOperation(AGEOUT_HISTORY_OPERATION);
                        Messages messages = resources.messages();
                        // prepend the ageout-history description to let user know the intended action.
                        SafeHtml message = messages
                                .cleanPatchHistoryQuestion(operationMetadata.getDescription().getDescription());

                        DialogFactory.showConfirmation(messages.cleanPatchHistory(), message, () -> {
                            Operation operation = new Operation.Builder(address, AGEOUT_HISTORY_OPERATION).build();
                            dispatcher.execute(operation, result -> {
                                MessageEvent.fire(eventBus, Message.success(messages.cleanPatchHistorySuccess()));
                                refresh(RESTORE_SELECTION);
                            }, (operation1, failure) -> {
                                MessageEvent.fire(eventBus, Message.error(messages.cleanPatchHistoryFailure(failure)));
                                refresh(RESTORE_SELECTION);
                            });
                        });
                    }
                });
    }

    private void applyPatch() {
        // check the host controller for restart-required
        checkHostState(() ->
                metadataProcessor.lookup(PATCHING_TEMPLATE, progress.get(),
                        new SuccessfulMetadataCallback(eventBus, resources) {
                            @Override
                            public void onMetadata(final Metadata metadata) {
                                Metadata metadataOp = metadata.forOperation(PATCH);
                                new ApplyPatchWizard(resources, environment, metadataOp, statementContext,
                                        dispatcher, progress, serverActions, () -> refresh(RESTORE_SELECTION))
                                        .show();
                            }
                        })

        );
    }


    /**
     * Checks if the host or standalone server is in restart mode, if yes then asks user to restart host/server, as it
     * must be restarted before a patch can be installed or to call a rollback on an installed patch.
     */
    private void checkHostState(Callback callback) {

        Messages messages = resources.messages();
        if (environment.isStandalone()) {
            Operation operation = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();

            dispatcher.execute(operation, result -> {
                Server.STANDALONE.addServerAttributes(result);
                if (Server.STANDALONE.needsRestart()) {
                    serverActions.restartStandalone(Server.STANDALONE, messages.patchRestartStandaloneQuestion());
                } else {
                    callback.execute();
                }
            });
        } else {
            ResourceAddress address = new ResourceAddress().add(HOST, statementContext.selectedHost());
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();

            dispatcher.execute(operation, result -> {

                Property prop = new Property(statementContext.selectedHost(), result);
                Host host = new Host(prop);
                if (host.needsRestart()) {
                    SafeHtml question = host.isDomainController()
                            ? messages.patchRestartDomainControllerQuestion(host.getName())
                            : messages.patchRestartHostControllerQuestion(host.getName());

                    hostActions.restart(host, question);
                } else {
                    callback.execute();
                }
            });
        }

    }
}
