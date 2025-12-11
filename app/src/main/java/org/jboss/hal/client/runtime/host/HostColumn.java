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
package org.jboss.hal.client.runtime.host;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActionEvent;
import org.jboss.hal.core.runtime.host.HostActionEvent.HostActionHandler;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.runtime.configurationchanges.ConfigurationChangesPresenter.CONFIGURATION_CHANGES_ADDRESS;
import static org.jboss.hal.client.runtime.host.AddressTemplates.HOST_CONNECTION_TEMPLATE;
import static org.jboss.hal.client.runtime.managementoperations.ManagementOperationsPresenter.MANAGEMENT_OPERATIONS_ADDRESS;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.core.runtime.TopologyTasks.hosts;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_CONNECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRUNE_DISCONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRUNE_EXPIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.meta.AddressTemplate.OPTIONAL;
import static org.jboss.hal.resources.CSS.pfIcon;

@Column(Ids.HOST)
@Requires(value = {
        "/core-service=management/host-connection=*",
        OPTIONAL + "host=*" + CONFIGURATION_CHANGES_ADDRESS,
        "host=*" + MANAGEMENT_OPERATIONS_ADDRESS }, recursive = false)
public class HostColumn extends FinderColumn<Host> implements HostActionHandler, HostResultHandler {

    static AddressTemplate hostTemplate(Host host) {
        return AddressTemplate.of("/host=" + host.getAddressName());
    }

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public HostColumn(Finder finder,
            Environment environment,
            Dispatcher dispatcher,
            CrudOperations crud,
            EventBus eventBus,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            HostActions hostActions,
            Resources resources,
            MetadataRegistry metadataRegistry) {

        super(new Builder<Host>(finder, Ids.HOST, Names.HOST)
                .onItemSelect(host -> eventBus.fireEvent(new HostSelectionEvent(host.getAddressName())))
                .onPreview(item -> new HostPreview(dispatcher, hostActions, item, resources))
                .pinnable()
                .showCount()
                // Unlike other columns the host column does not have a custom breadcrumb item handler.
                // It makes no sense to replace the host in a finder path like
                // "host => primary / server => server-one / subsystem => logging / log-file => server.log"
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .filterDescription(resources.messages().hostColumnFilterDescription()));
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.resources = resources;

        addColumnAction(columnActionFactory.refresh(Ids.HOST_REFRESH));
        List<ColumnAction<Host>> pruneActions = new ArrayList<>();
        pruneActions.add(new ColumnAction.Builder<Host>(Ids.HOST_PRUNE_EXPIRED)
                .title(resources.constants().pruneExpired())
                .handler(column -> pruneExpired())
                .constraint(Constraint.executable(HOST_CONNECTION_TEMPLATE, PRUNE_EXPIRED))
                .build());
        pruneActions.add(new ColumnAction.Builder<Host>(Ids.HOST_PRUNE_DISCONNECTED)
                .title(resources.constants().pruneDisconnected())
                .handler(column -> pruneDisconnected())
                .constraint(Constraint.executable(HOST_CONNECTION_TEMPLATE, PRUNE_DISCONNECTED))
                .build());
        addColumnActions(Ids.HOST_PRUNE_ACTIONS, pfIcon("remove"), resources.constants().prune(), pruneActions);

        ItemsProvider<Host> itemsProvider = finderContext -> sequential(new FlowContext(progress.get()),
                hosts(environment, dispatcher)).then(flowContext -> {
                    List<Host> hosts = flowContext.get(TopologyTasks.HOSTS);
                    // Restore pending visualization
                    hosts.stream()
                            .filter(hostActions::isPending)
                            .forEach(host -> ItemMonitor.startProgress(Ids.host(host.getAddressName())));
                    return Promise.resolve(hosts);
                });
        setItemsProvider(itemsProvider);

        setBreadcrumbItemsProvider(context -> itemsProvider.items(context)
                .then(result -> Promise.resolve(result.stream()
                        .filter(Host::isAlive)
                        .collect(toList()))));

        setItemRenderer(item -> new HostDisplay(item, hostActions, resources) {
            @Override
            public String nextColumn() {
                return item.isAlive() ? SERVER : null;
            }

            @Override
            public List<ItemAction<Host>> actions() {
                if (item.isAlive()) {
                    PlaceRequest placeRequest = new PlaceRequest.Builder()
                            .nameToken(NameTokens.HOST_CONFIGURATION)
                            .with(HOST, item.getAddressName()).build();
                    List<ItemAction<Host>> actions = new ArrayList<>();
                    actions.add(itemActionFactory.viewAndMonitor(Ids.host(item.getAddressName()), placeRequest));
                    if (!hostActions.isPending(item)) {
                        if (ManagementModel.supportsConfigurationChanges(item.getManagementVersion())
                                && metadataRegistry.contains(hostTemplate(item).append(CONFIGURATION_CHANGES_ADDRESS))) {
                            PlaceRequest ccPlaceRequest = new PlaceRequest.Builder()
                                    .nameToken(NameTokens.CONFIGURATION_CHANGES)
                                    .with(HOST, item.getAddressName())
                                    .build();
                            actions.add(itemActionFactory.placeRequest(resources.constants().configurationChanges(),
                                    ccPlaceRequest,
                                    Constraint.executable(hostTemplate(item).append(CONFIGURATION_CHANGES_ADDRESS),
                                            ADD)));
                            actions.add(ItemAction.separator());
                        }
                        // TODO Add additional operations like :reload(admin-mode=true), :clean-obsolete-content or
                        // :take-snapshot
                        actions.add(new ItemAction.Builder<Host>()
                                .title(resources.constants().reload())
                                .handler(hostActions::reload)
                                .constraint(Constraint.executable(hostTemplate(item), RELOAD))
                                .build());
                        actions.add(new ItemAction.Builder<Host>()
                                .title(resources.constants().restart())
                                .handler(hostActions::restart)
                                .constraint(Constraint.executable(hostTemplate(item), SHUTDOWN))
                                .build());
                    }
                    return actions;
                } else {
                    return emptyList();
                }
            }
        });

        eventBus.addHandler(HostActionEvent.getType(), this);
        eventBus.addHandler(HostResultEvent.getType(), this);
    }

    @Override
    public void onHostAction(HostActionEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.startProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.startProgress(server.getId()));
        }
    }

    @Override
    public void onHostResult(HostResultEvent event) {
        if (isVisible()) {
            Host host = event.getHost();
            ItemMonitor.stopProgress(Ids.host(host.getAddressName()));
            event.getServers().forEach(server -> ItemMonitor.stopProgress(server.getId()));
            refresh(RESTORE_SELECTION);
        }
    }

    private void pruneExpired() {
        DialogFactory.showConfirmation(resources.constants().pruneExpired(),
                resources.messages().pruneExpiredQuestion(),
                () -> prune(PRUNE_EXPIRED));
    }

    private void pruneDisconnected() {
        DialogFactory.showConfirmation(resources.constants().pruneDisconnected(),
                resources.messages().pruneDisconnectedQuestion(),
                () -> prune(PRUNE_DISCONNECTED));
    }

    private void prune(String operation) {
        ResourceAddress address = new ResourceAddress().add(CORE_SERVICE, MANAGEMENT);
        crud.readChildren(address, HOST_CONNECTION, children -> {
            List<Operation> operations = children.stream()
                    .map(property -> {
                        ResourceAddress hcAddress = HOST_CONNECTION_TEMPLATE.resolve(statementContext,
                                property.getName());
                        return new Operation.Builder(hcAddress, operation).build();
                    })
                    .collect(toList());
            dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                MessageEvent.fire(eventBus, Message.success(resources.messages().pruneSuccessful()));
                refresh(RESTORE_SELECTION);
            });
        });
    }
}
