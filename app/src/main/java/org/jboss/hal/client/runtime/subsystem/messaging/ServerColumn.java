/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@AsyncColumn(Ids.MESSAGING_SERVER_RUNTIME)
@Requires(value = MESSAGING_SERVER_ADDRESS, recursive = false)
public class ServerColumn extends FinderColumn<NamedNode> {

    private final MetadataRegistry metadataRegistry;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public ServerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            MetadataRegistry metadataRegistry,
            PlaceManager placeManager,
            Places places,
            EventBus eventBus,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.MESSAGING_SERVER_RUNTIME, Names.SERVER)
                .columnAction(columnActionFactory.refresh(Ids.MESSAGING_SERVER_RUNTIME_REFRESH))
                .onBreadcrumbItem((item, context) -> {
                    // try to replace Ids.MESSAGING_SERVER request parameter
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest place = places.replaceParameter(current, Ids.MESSAGING_SERVER, item.getName()).build();
                    placeManager.revealPlace(place);
                })
                .onPreview(server -> new ServerPreview(server, statementContext, resources)));
        this.metadataRegistry = metadataRegistry;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        ItemsProvider<NamedNode> itemsProvider = (context, callback) -> {
            ResourceAddress address = MESSAGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, SERVER)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(operation, result -> callback.onSuccess(asNamedNodes(result.asPropertyList())));
        };
        setItemsProvider(itemsProvider);

        setBreadcrumbItemsProvider(
                (context, callback) -> itemsProvider.get(context, new AsyncCallback<List<NamedNode>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<NamedNode> result) {
                        callback.onSuccess(result.stream()
                                .filter(server -> server.get(STARTED).asBoolean(false))
                                .collect(toList()));
                    }
                }));

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.messagingServer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement getIcon() {
                boolean started = item.get(STARTED).asBoolean(false);
                return started ? Icons.ok() : Icons.stopped();
            }

            @Override
            public String nextColumn() {
                return Ids.MESSAGING_SERVER_DESTINATION_RUNTIME;
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                if (item.get(STARTED).asBoolean(false)) {
                    actions.add(itemActionFactory.view(places.selectedServer(NameTokens.MESSAGING_SERVER_RUNTIME)
                            .with(Ids.MESSAGING_SERVER, getTitle()).build()));
                    actions.add(new ItemAction.Builder<NamedNode>()
                            .title(resources.constants().forceFailover())
                            .constraint(Constraint.executable(MESSAGING_SERVER_TEMPLATE, FORCE_FAILOVER))
                            .handler(itm -> forceFailover(itm))
                            .build());
                    Constraints constraints = Constraints.and(
                            Constraint.executable(MESSAGING_SERVER_TEMPLATE, RESET_ALL_MESSAGE_COUNTER_HISTORIES),
                            Constraint.executable(MESSAGING_SERVER_TEMPLATE, RESET_ALL_MESSAGE_COUNTERS));
                    actions.add(new ItemAction.Builder<NamedNode>()
                            .title(resources.constants().reset())
                            .constraints(constraints)
                            .handler(itm -> openResetServerDialog(itm.getName()))
                            .build());
                }
                return actions;
            }
        });
    }

    private void openResetServerDialog(String messagingServer) {
        Metadata metadata = metadataRegistry.lookup(MESSAGING_SERVER_TEMPLATE);
        new ResetServerDialog(this, metadata, resources).reset(messagingServer);
    }

    void resetServer(String messagingServer, boolean messageCounters, boolean messageCounterHistories) {
        ResourceAddress address = MESSAGING_SERVER_TEMPLATE.resolve(statementContext, messagingServer);
        Composite composite = new Composite();
        if (messageCounters) {
            composite.add(new Operation.Builder(address, RESET_ALL_MESSAGE_COUNTERS).build());
        }
        if (messageCounterHistories) {
            composite.add(new Operation.Builder(address, RESET_ALL_MESSAGE_COUNTER_HISTORIES).build());
        }
        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().resetMessageCounterSuccess(messagingServer)));
            refresh(RefreshMode.RESTORE_SELECTION);
        });
    }

    private void forceFailover(NamedNode server) {
        DialogFactory.showConfirmation(resources.constants().forceFailover(),
                resources.messages().forceFailoverQuestion(server.getName()), () -> {
                    ResourceAddress address = MESSAGING_SERVER_TEMPLATE.resolve(statementContext, server.getName());
                    Operation operation = new Operation.Builder(address, FORCE_FAILOVER).build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().forceFailoverSuccess(server.getName())));
                        refresh(RefreshMode.RESTORE_SELECTION);
                    });
                });
    }
}
