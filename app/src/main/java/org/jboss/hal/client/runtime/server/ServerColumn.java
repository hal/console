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
package org.jboss.hal.client.runtime.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.core.runtime.server.ServerSelectionEvent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@Column(SERVER)
@Requires(value = {"/host=*/server-config=*", "/host=*/server=*"}, recursive = false)
public class ServerColumn extends FinderColumn<Server> implements ServerActionHandler, ServerResultHandler {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public ServerColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final @Footer Provider<Progress> progress,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final ServerActions serverActions,
            final Resources resources) {
        super(new Builder<Server>(finder, SERVER, Names.SERVER)
                .onItemSelect(server -> eventBus.fireEvent(new ServerSelectionEvent(server.getName())))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .onPreview(item -> new ServerPreview(serverActions, item, resources))
        );
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.resources = resources;

        addColumnAction(columnActionFactory.add(IdBuilder.build(SERVER, "add"), Names.SERVER,
                column -> addServer(browseByHosts(finder.getContext()))));
        addColumnAction(columnActionFactory.refresh(IdBuilder.build(SERVER, "refresh")));

        // TODO Use functions instead of composites to prevent errors when reading server resources for stopped servers
        setItemsProvider((context, callback) -> {
            Operation serverOp;
            Operation serverConfigOp;
            boolean browseByHosts = browseByHosts(context);

            if (browseByHosts) {
                ResourceAddress address = AddressTemplate.of("/{selected.host}").resolve(statementContext);
                serverConfigOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                        .param(CHILD_TYPE, SERVER_CONFIG)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                serverOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                        .param(CHILD_TYPE, SERVER)
                        .param(INCLUDE_RUNTIME, true)
                        .build();

            } else {
                ResourceAddress serverConfigAddress = AddressTemplate.of("/host=*/server-config=*")
                        .resolve(statementContext);
                serverConfigOp = new Operation.Builder(QUERY, serverConfigAddress)
                        .param(WHERE, new ModelNode().set(GROUP, statementContext.selectedServerGroup()))
                        .build();
                ResourceAddress serverAddress = AddressTemplate.of("/host=*/server=*")
                        .resolve(statementContext);
                serverOp = new Operation.Builder(QUERY, serverAddress)
                        .param(WHERE, new ModelNode().set(SERVER_GROUP, statementContext.selectedServerGroup()))
                        .build();
            }

            dispatcher.execute(new Composite(serverConfigOp, serverOp), (CompositeResult result) -> {
                Map<String, Server> serverConfigsByName;
                if (browseByHosts) {
                    serverConfigsByName = result.step(0).get(RESULT).asPropertyList().stream()
                            .map(property -> new Server(statementContext.selectedHost(), property))
                            .collect(toMap(Server::getName, identity()));
                    // add server attributes
                    for (Property property : result.step(1).get(RESULT).asPropertyList()) {
                        Server serverConfig = serverConfigsByName.get(property.getName());
                        if (serverConfig != null) {
                            serverConfig.get(HOST).set(statementContext.selectedHost());
                            serverConfig.addServerAttributes(property.getValue());
                        }
                    }

                } else {
                    serverConfigsByName = result.step(0).get(RESULT).asList().stream()
                            .filter(modelNode -> !modelNode.isFailure())
                            .map(modelNode -> {
                                ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                                String host = address.getParent().lastValue();
                                return new Server(host, modelNode.get(RESULT));
                            })
                            .collect(toMap(Server::getName, identity()));
                    // add server attributes
                    for (ModelNode modelNode : result.step(1).get(RESULT).asList()) {
                        if (!modelNode.isFailure()) {
                            ModelNode serverNode = modelNode.get(RESULT);
                            String name = serverNode.get(NAME).asString();
                            Server serverConfig = serverConfigsByName.get(name);
                            if (serverConfig != null) {
                                serverConfig.addServerAttributes(serverNode);
                            }
                        }
                    }
                }
                // return the server instances as ordered list
                callback.onSuccess(serverConfigsByName.values().stream().sorted(comparing(Server::getName))
                        .collect(toList()));
            });
        });

        setItemRenderer(item -> new ItemDisplay<Server>() {
            @Override
            public String getId() {
                return Server.id(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(item.getName());
                data.add(ModelNodeHelper.asAttributeValue(item.getServerConfigStatus()));
                if (item.isStarted()) {
                    data.add(ModelNodeHelper.asAttributeValue(item.getServerState()));
                    data.add(ModelNodeHelper.asAttributeValue(item.getSuspendState()));
                } else {
                    data.add("stopped");
                }
                return Joiner.on(' ').join(data);
            }

            @Override
            public String getTooltip() {
                if (item.isAdminMode()) {
                    return resources.constants().adminOnly();
                } else if (item.isStarting()) {
                    return resources.constants().starting();
                } else if (item.isSuspending()) {
                    return resources.constants().suspending();
                } else if (item.needsReload()) {
                    return resources.constants().needsReload();
                } else if (item.needsRestart()) {
                    return resources.constants().needsRestart();
                } else if (item.isRunning()) {
                    return resources.constants().running();
                } else if (item.isFailed()) {
                    return resources.constants().failed();
                } else if (item.isStopped()) {
                    return resources.constants().stopped();
                } else {
                    return resources.constants().unknownState();
                }
            }

            @Override
            public Element getIcon() {
                if (item.isAdminMode() || item.isStarting()) {
                    return Icons.disabled();
                } else if (item.isSuspending() || item.needsReload() || item.needsRestart()) {
                    return Icons.warning();
                } else if (item.isRunning()) {
                    return Icons.ok();
                } else if (item.isFailed()) {
                    return Icons.error();
                } else if (item.isStopped()) {
                    return Icons.stopped();
                } else {
                    return Icons.error();
                }
            }

            @Override
            public List<ItemAction<Server>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_CONFIGURATION)
                        .with(HOST, item.getHost())
                        .with(SERVER_CONFIG, item.getName())
                        .build();
                List<ItemAction<Server>> actions = new ArrayList<>();
                actions.add(itemActionFactory.viewAndMonitor(Server.id(item.getName()), placeRequest));
                if (!item.isStarted()) {
                    AddressTemplate template = AddressTemplate
                            .of("/host=" + item.getHost() + "/server-config=" + item.getName());
                    actions.add(itemActionFactory.remove(Names.SERVER, item.getName(), template, ServerColumn.this));
                }
                actions.add(new ItemAction<>(resources.constants().copy(),
                        itm -> copyServer(itm, browseByHosts(finder.getContext()))));
                if (!item.isStarted()) {
                    actions.add(new ItemAction<>(resources.constants().start(), serverActions::start));
                } else {
                    // Order is: reload, restart, (resume | suspend), stop
                    actions.add(new ItemAction<>(resources.constants().reload(), serverActions::reload));
                    actions.add(new ItemAction<>(resources.constants().restart(), serverActions::restart));
                    if (item.isSuspending()) {
                        actions.add(new ItemAction<>(resources.constants().resume(), serverActions::resume));
                    } else {
                        actions.add(new ItemAction<>(resources.constants().suspend(), serverActions::suspend));
                    }
                    actions.add(new ItemAction<>(resources.constants().stop(), serverActions::stop));
                }
                return actions;
            }

            @Override
            public String nextColumn() {
                return item.isStarted() ? Ids.SERVER_MONITOR_COLUMN : null;
            }
        });

        eventBus.addHandler(ServerActionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);
    }

    static boolean browseByHosts(FinderContext context) {
        FinderSegment firstSegment = context.getPath().iterator().next();
        return firstSegment.getKey().equals(HOST) || firstSegment.getValue().equals(IdBuilder.asId(Names.HOSTS));
    }

    private void addServer(boolean browseByHost) {
        Browser.getWindow().alert(Names.NYI);
    }

    private void copyServer(Server server, boolean browseByHost) {
        Browser.getWindow().alert(Names.NYI);
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        ItemMonitor.startProgress(Server.id(event.getServer().getName()));
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        Server server = event.getServer();
        String itemId = Server.id(server.getName());
        ItemMonitor.stopProgress(itemId);
        refresh(RESTORE_SELECTION);
    }
}
