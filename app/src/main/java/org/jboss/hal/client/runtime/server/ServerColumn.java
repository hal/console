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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Iterables;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.runtime.BrowseByColumn;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.core.runtime.server.ServerSelectionEvent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static elemental2.dom.DomGlobal.alert;
import static elemental2.dom.DomGlobal.document;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;

/**
 * @author Harald Pehl
 */
@Column(Ids.SERVER)
@Requires(value = {"/host=*/server-config=*", "/host=*/server=*"}, recursive = false)
public class ServerColumn extends FinderColumn<Server> implements ServerActionHandler, ServerResultHandler {

    static AddressTemplate serverConfigTemplate(Server server) {
        return serverConfigTemplate(server.getHost());
    }

    static AddressTemplate serverConfigTemplate(String host) {
        return AddressTemplate.of("/host=" + host + "/server-config=*");
    }

    private final Finder finder;
    private final Environment environment;
    private final SecurityContextRegistry securityContextRegistry;
    private FinderPath refreshPath;

    @Inject
    public ServerColumn(final Finder finder,
            final Dispatcher dispatcher,
            final Environment environment,
            final EventBus eventBus,
            final @Footer Provider<Progress> progress,
            final SecurityContextRegistry securityContextRegistry,
            final StatementContext statementContext,
            final PlaceManager placeManager,
            final Places places,
            final FinderPathFactory finderPathFactory,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final ServerActions serverActions,
            final Resources resources) {

        super(new Builder<Server>(finder, Ids.SERVER, Names.SERVER)

                .onItemSelect(server -> {
                    if (BrowseByColumn.browseByServerGroups(finder.getContext())) {
                        // if we browse by server groups we still need to have a valid {selected.host}
                        eventBus.fireEvent(new HostSelectionEvent(server.getHost()));
                    } else if (BrowseByColumn.browseByHosts(finder.getContext())) {
                        // if we browse by hosts we still need to have a valid {selected.group}
                        eventBus.fireEvent(new ServerGroupSelectionEvent(server.getServerGroup()));
                    }
                    eventBus.fireEvent(new ServerSelectionEvent(server.getName()));
                })

                .onBreadcrumbItem((item, context) -> {
                    PlaceRequest.Builder builder;
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();

                    if (NameTokens.GENERIC_SUBSYSTEM.equals(current.getNameToken())) {
                        // switch server in address parameter of generic presenter
                        builder = new PlaceRequest.Builder().nameToken(current.getNameToken());
                        String addressParam = current.getParameter(Places.ADDRESS_PARAM, null);
                        if (addressParam != null) {
                            ResourceAddress currentAddress = AddressTemplate.of(addressParam).resolve(statementContext);
                            ResourceAddress newAddress = currentAddress
                                    .replaceValue(HOST, item.getHost())
                                    .replaceValue(SERVER, item.getName())
                                    .replaceValue(SERVER_CONFIG, item.getName());
                            builder.with(Places.ADDRESS_PARAM, newAddress.toString());
                        }

                    } else {
                        // try to replace 'host', 'server' and 'server-config' request parameter
                        PlaceRequest place = places.replaceParameter(current, HOST, item.getHost()).build();
                        place = places.replaceParameter(place, SERVER, item.getName()).build();
                        builder = places.replaceParameter(place, SERVER_CONFIG, item.getName());
                    }
                    placeManager.revealPlace(builder.build());
                })

                .pinnable()
                .showCount()
                .withFilter()
                .onPreview(item -> new ServerPreview(serverActions, item, placeManager, places, finderPathFactory,
                        resources))
        );
        this.finder = finder;
        this.environment = environment;
        this.securityContextRegistry = securityContextRegistry;

        ItemsProvider<Server> itemsProvider = (context, callback) -> {
            Function<FunctionContext> serverConfigsFn;
            boolean browseByHosts = BrowseByColumn.browseByHosts(context);

            if (browseByHosts) {
                processAddColumnAction(statementContext.selectedHost());
                serverConfigsFn = control -> {
                    ResourceAddress address = AddressTemplate.of(SELECTED_HOST).resolve(statementContext);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, SERVER_CONFIG)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.executeInFunction(control, operation, result -> {
                        List<Server> servers = result.asPropertyList().stream()
                                .map(property -> new Server(statementContext.selectedHost(), property))
                                .collect(toList());
                        control.getContext().set(TopologyFunctions.SERVERS, servers);
                        control.proceed();
                    });
                };

            } else {
                serverConfigsFn = control -> {
                    ResourceAddress serverConfigAddress = AddressTemplate.of("/host=*/server-config=*")
                            .resolve(statementContext);
                    Operation operation = new Operation.Builder(serverConfigAddress, QUERY)
                            .param(WHERE, new ModelNode().set(GROUP, statementContext.selectedServerGroup()))
                            .build();
                    dispatcher.executeInFunction(control, operation, result -> {
                        List<Server> servers = result.asList().stream()
                                .filter(modelNode -> !modelNode.isFailure())
                                .map(modelNode -> {
                                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                                    String host = address.getParent().lastValue();
                                    return new Server(host, modelNode.get(RESULT));
                                })
                                .collect(toList());
                        control.getContext().set(TopologyFunctions.SERVERS, servers);
                        control.proceed();
                    });
                };
            }

            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getException());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Server> servers = context.get(TopologyFunctions.SERVERS);
                            if (servers == null) {
                                servers = Collections.emptyList();
                            }
                            callback.onSuccess(servers.stream().sorted(comparing(Server::getName))
                                    .collect(toList()));

                            // Restore pending servers visualization
                            servers.stream()
                                    .filter(serverActions::isPending)
                                    .forEach(server -> ItemMonitor.startProgress(server.getId()));
                        }
                    },
                    serverConfigsFn, new TopologyFunctions.TopologyStartedServers(environment, dispatcher));
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<Server>>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        callback.onFailure(throwable);
                    }

                    @Override
                    public void onSuccess(final List<Server> servers) {
                        if (!serverIsLastSegment()) {
                            // When the server is not the last segment in the finder path, we assume that
                            // the current path is related to something which requires a running server.
                            // In that case return only started servers.
                            callback.onSuccess(servers.stream().filter(Server::isStarted).collect(toList()));
                        } else {
                            callback.onSuccess(servers);
                        }
                    }
                }));

        setItemRenderer(item -> new ItemDisplay<Server>() {
            @Override
            public String getId() {
                return item.getId();
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                return ItemDisplay.withSubtitle(item.getName(),
                        BrowseByColumn.browseByHosts(finder.getContext())
                                ? item.getServerGroup()
                                : item.getHost());
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(item.getName());
                data.add(item.getHost());
                data.add(item.getServerGroup());
                data.add(ModelNodeHelper.asAttributeValue(item.getServerConfigStatus()));
                if (item.isStarted()) {
                    data.add(ModelNodeHelper.asAttributeValue(item.getServerState()));
                    data.add(ModelNodeHelper.asAttributeValue(item.getSuspendState()));
                } else {
                    data.add("stopped");
                }
                return String.join(" ", data);
            }

            @Override
            public String getTooltip() {
                return new ServerTooltip(serverActions, resources).apply(item);
            }

            @Override
            public HTMLElement getIcon() {
                return new ServerIcon(serverActions).apply(item);
            }

            @Override
            public List<ItemAction<Server>> actions() {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_CONFIGURATION)
                        .with(HOST, item.getHost())
                        .with(SERVER_CONFIG, item.getName())
                        .build();
                List<ItemAction<Server>> actions = new ArrayList<>();

                actions.add(itemActionFactory.viewAndMonitor(item.getId(), placeRequest));
                if (item.hasBootErrors()) {
                    PlaceRequest bootErrorsRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_BOOT_ERRORS)
                            .with(HOST, item.getHost())
                            .with(SERVER, item.getName())
                            .build();
                    actions.add(itemActionFactory.placeRequest(Names.BOOT_ERRORS, bootErrorsRequest));
                }
                if (!serverActions.isPending(item)) {
                    if (!item.isStarted()) {
                        actions.add(new ItemAction.Builder<Server>()
                                .title(resources.constants().start())
                                .handler(serverActions::start)
                                .constraint(Constraint.executable(serverConfigTemplate(item), START))
                                .build());
                        AddressTemplate template = AddressTemplate
                                .of("/host=" + item.getHost() + "/server-config=" + item.getName());
                        actions.add(itemActionFactory.remove(Names.SERVER, item.getName(),
                                template, serverConfigTemplate(item), ServerColumn.this));
                    }
                    actions.add(new ItemAction.Builder<Server>()
                            .title(resources.constants().copy())
                            .handler(itm -> copyServer(itm, BrowseByColumn.browseByHosts(finder.getContext())))
                            .constraint(Constraint.executable(serverConfigTemplate(item), ADD))
                            .build());
                    if (item.isStarted()) {
                        // Order is: reload, restart, (resume | suspend), stop
                        actions.add(new ItemAction.Builder<Server>()
                                .title(resources.constants().reload())
                                .handler(serverActions::reload)
                                .constraint(Constraint.executable(serverConfigTemplate(item), RELOAD))
                                .build());
                        actions.add(new ItemAction.Builder<Server>()
                                .title(resources.constants().restart())
                                .handler(serverActions::restart)
                                .constraint(Constraint.executable(serverConfigTemplate(item), RESTART))
                                .build());
                        if (ManagementModel.supportsSuspend(item.getManagementVersion())) {
                            if (item.isSuspended()) {
                                actions.add(new ItemAction.Builder<Server>()
                                        .title(resources.constants().resume())
                                        .handler(serverActions::resume)
                                        .constraint(Constraint.executable(serverConfigTemplate(item), RESUME))
                                        .build());
                            } else {
                                actions.add(new ItemAction.Builder<Server>()
                                        .title(resources.constants().suspend())
                                        .handler(serverActions::suspend)
                                        .constraint(Constraint.executable(serverConfigTemplate(item), SUSPEND))
                                        .build());
                            }
                        }
                        actions.add(new ItemAction.Builder<Server>()
                                .title(resources.constants().stop())
                                .handler(serverActions::stop)
                                .constraint(Constraint.executable(serverConfigTemplate(item), STOP))
                                .build());
                    }
                }
                // add kill action regardless of server state to kill servers which might show a wrong state
                actions.add(new ItemAction.Builder<Server>()
                        .title(resources.constants().kill())
                        .handler(serverActions::kill)
                        .constraint(Constraint.executable(serverConfigTemplate(item), KILL))
                        .build());
                return actions;
            }

            @Override
            public String nextColumn() {
                return item.isStarted() ? Ids.SERVER_MONITOR : null;
            }
        });

        // Don't use columnActionFactory.add() here. This would add a default constraint,
        // but we want to manage the visibility by ourselves.
        ColumnAction<Server> addAction = new ColumnAction.Builder<Server>(Ids.SERVER_ADD)
                .element(columnActionFactory.addButton(Names.SERVER))
                .handler(column -> addServer(BrowseByColumn.browseByHosts(finder.getContext())))
                .build();
        addColumnAction(addAction);
        addColumnAction(columnActionFactory.refresh(Ids.SERVER_REFRESH));

        eventBus.addHandler(ServerActionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);
    }

    private void addServer(boolean browseByHost) {
        alert(Names.NYI);
    }

    private void copyServer(Server server, boolean browseByHost) {
        alert(Names.NYI);
    }

    private boolean serverIsLastSegment() {
        FinderSegment segment = Iterables.getLast(finder.getContext().getPath(), null);
        return segment != null && Ids.SERVER.equals(segment.getColumnId());
    }

    private void processAddColumnAction(String host) {
        AuthorisationDecision ad = AuthorisationDecision.from(environment, c -> {
            if (securityContextRegistry.contains(c.getTemplate())) {
                return Optional.of(securityContextRegistry.lookup(c.getTemplate()));
            }
            return Optional.empty();
        });
        HTMLElement addButton = (HTMLElement) document.getElementById(Ids.SERVER_ADD);
        ElementGuard.toggle(addButton, !ad.isAllowed(Constraint.executable(serverConfigTemplate(host), ADD)));
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        if (isVisible()) {
            // remember current selection for onServerResult()
            refreshPath = finder.getContext().getPath().copy();
            ItemMonitor.startProgress(event.getServer().getId());
            refresh(RESTORE_SELECTION);
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        //noinspection Duplicates
        if (isVisible()) {
            ItemMonitor.stopProgress(event.getServer().getId());

            FinderPath path = refreshPath != null ? refreshPath : finder.getContext().getPath();
            refreshPath = null;
            finder.refresh(path);
        }
    }
}
