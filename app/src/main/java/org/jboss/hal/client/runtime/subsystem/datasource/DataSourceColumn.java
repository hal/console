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
package org.jboss.hal.client.runtime.subsystem.datasource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.XA_PARAM;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATA_SOURCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FLUSH_ALL_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FLUSH_GRACEFULLY_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FLUSH_IDLE_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FLUSH_INVALID_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TEST_CONNECTION_IN_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATA_SOURCE;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.fontAwesome;

// TODO Add data sources from deployments
@AsyncColumn(Ids.DATA_SOURCE_RUNTIME)
@Requires({ DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, DATA_SOURCE_DEPLOYMENT_ADDRESS,
        XA_DATA_SOURCE_DEPLOYMENT_ADDRESS })
public class DataSourceColumn extends FinderColumn<DataSource> {

    private static final String EQ_WILDCARD = "=*";

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final Finder finder;
    private Server server;

    @Inject
    public DataSourceColumn(ServerActions serverActions,
            Dispatcher dispatcher,
            EventBus eventBus,
            StatementContext statementContext,
            Environment environment,
            Resources resources,
            Finder finder,
            FinderPathFactory finderPathFactory,
            ItemActionFactory itemActionFactory,
            Places places) {

        super(new Builder<DataSource>(finder, Ids.DATA_SOURCE_RUNTIME, Names.DATASOURCE)
                .withFilter()
                .filterDescription(resources.messages().datasourceRuntimeFilterDescription())
                .useFirstActionAsBreadcrumbHandler());

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.finder = finder;

        ItemsProvider<DataSource> itemsProvider = context -> {
            List<Operation> operations = new ArrayList<>(); // 6 ops in standalone, 7 in domain

            // subsystem
            ResourceAddress dsSubsystemAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            operations.add(new Operation.Builder(dsSubsystemAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(dsSubsystemAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, XA_DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());

            // deployment
            operations.add(new Operation.Builder(DATA_SOURCE_DEPLOYMENT_TEMPLATE.resolve(statementContext),
                    READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(XA_DATA_SOURCE_DEPLOYMENT_TEMPLATE.resolve(statementContext),
                    READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());

            // subdeployment
            operations.add(new Operation.Builder(DATA_SOURCE_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext),
                    READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(XA_DATA_SOURCE_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext),
                    READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());

            if (!environment.isStandalone()) {
                ResourceAddress serverAddress = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                        .resolve(statementContext);
                operations.add(new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_RUNTIME, true)
                        .param(ATTRIBUTES_ONLY, true)
                        .build());
            }

            return dispatcher.execute(new Composite(operations)).then(result -> {
                List<DataSource> combined = new ArrayList<>();

                // 6 steps in standalone, 7 in domain
                // subsystem
                combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, false)).collect(toList()));
                combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, true)).collect(toList()));

                // deployment
                result.step(2).get(RESULT).asList().forEach(node -> {
                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                    combined.add(new DataSource(address, node.get(RESULT), false));
                });
                result.step(3).get(RESULT).asList().forEach(node -> {
                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                    combined.add(new DataSource(address, node.get(RESULT), true));
                });

                // subdeployment
                result.step(4).get(RESULT).asList().forEach(node -> {
                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                    combined.add(new DataSource(address, node.get(RESULT), false));
                });
                result.step(5).get(RESULT).asList().forEach(node -> {
                    ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                    combined.add(new DataSource(address, node.get(RESULT), true));
                });

                combined.sort(comparing(NamedNode::getName));
                server = environment.isStandalone()
                        ? Server.STANDALONE
                        : new Server(statementContext.selectedHost(), result.step(6).get(RESULT));
                return Promise.resolve(combined);
            });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider(context -> itemsProvider.items(context)
                .then(result -> Promise.resolve(result.stream()
                        .filter(ds -> !ds.fromDeployment() && ds.isStatisticsEnabled())
                        .collect(toList()))));

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return Ids.dataSourceRuntime(dataSource.getName(), dataSource.isXa());
            }

            @Override
            public HTMLElement element() {
                List<String> subtitles = new ArrayList<>();
                if (dataSource.isXa()) {
                    subtitles.add(Names.XA_DATASOURCE);
                }
                if (dataSource.fromDeployment()) {
                    subtitles.add(dataSource.getDeployment());
                }
                if (!subtitles.isEmpty()) {
                    return ItemDisplay.withSubtitle(dataSource.getName(), String.join(" / ", subtitles));
                }
                return null;
            }

            @Override
            public String getTitle() {
                return dataSource.getName();
            }

            @Override
            public HTMLElement getIcon() {
                if (dataSource.fromDeployment()) {
                    return span().css(fontAwesome("archive")).element();
                } else if (!dataSource.isEnabled()) {
                    return Icons.disabled();
                } else if (!dataSource.isStatisticsEnabled()) {
                    return Icons.unknown();
                } else {
                    return Icons.ok();
                }
            }

            @Override
            public String getTooltip() {
                if (dataSource.fromDeployment()) {
                    return resources.constants().fromDeployment();
                } else if (dataSource.isEnabled() && !dataSource.isStatisticsEnabled()) {
                    return resources.constants().statisticsDisabled();
                } else {
                    return dataSource.isEnabled() ? resources.constants().enabled() : resources.constants().disabled();
                }
            }

            @Override
            public String getFilterData() {
                // noinspection HardCodedStringLiteral
                return getTitle() + " " +
                        (dataSource.isXa() ? "xa" : "normal") + " " +
                        (dataSource.isEnabled() ? ENABLED : DISABLED) + " " +
                        (dataSource.fromDeployment() ? Names.DEPLOYMENT : "");
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public List<ItemAction<DataSource>> actions() {
                List<ItemAction<DataSource>> actions = new ArrayList<>();
                if (dataSource.isEnabled()) {
                    if (!dataSource.fromDeployment() && dataSource.isStatisticsEnabled()) {
                        PlaceRequest placeRequest = places.selectedServer(NameTokens.DATA_SOURCE_RUNTIME)
                                .with(NAME, dataSource.getName())
                                .with(XA_PARAM, String.valueOf(dataSource.isXa()))
                                .build();
                        actions.add(itemActionFactory.view(placeRequest));
                    }
                    actions.add(new ItemAction.Builder<DataSource>().title(resources.constants().test())
                            .handler(item -> testConnection(item))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    TEST_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushGracefully())
                            .handler(item -> flush(item, FLUSH_GRACEFULLY_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_GRACEFULLY_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushIdle())
                            .handler(item -> flush(item, FLUSH_IDLE_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_IDLE_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushInvalid())
                            .handler(item -> flush(item, FLUSH_INVALID_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_INVALID_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushAll())
                            .handler(item -> flush(item, FLUSH_ALL_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_ALL_CONNECTION_IN_POOL))
                            .build());
                }
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, server, item, environment, dispatcher, statementContext,
                serverActions, finderPathFactory, places, resources));
    }

    private void testConnection(DataSource dataSource) {
        Operation operation = new Operation.Builder(dataSourceAddress(dataSource), TEST_CONNECTION_IN_POOL).build();
        dispatcher.execute(operation,
                result -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().testConnectionSuccess(dataSource.getName())));
                }, (o1, failure) -> MessageEvent.fire(eventBus,
                        Message.error(resources.messages().testConnectionError(dataSource.getName()),
                                failure)));
    }

    private void flush(DataSource dataSource, String flushMode) {
        Operation operation = new Operation.Builder(dataSourceAddress(dataSource), flushMode).build();
        dispatcher.execute(operation,
                result -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus, Message.success(resources.messages().flushConnectionSuccess()));
                });
    }

    ResourceAddress dataSourceAddress(DataSource dataSource) {
        if (dataSource.fromDeployment()) {
            return dataSource.getAddress();
        } else {
            return dataSource.isXa()
                    ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                    : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
        }
    }

    AddressTemplate dataSourceConfigurationTemplate(DataSource dataSource) {
        String resourceName = dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE;
        if (environment.isStandalone()) {
            return AddressTemplate.of("/subsystem=datasources/" + resourceName + EQ_WILDCARD);
        } else {
            return AddressTemplate.of("/profile=*/subsystem=datasources/" + resourceName + EQ_WILDCARD);
        }
    }

    private ResourceAddress dataSourceConfigurationAddress(DataSource dataSource) {
        String resourceName = dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE;
        if (environment.isStandalone()) {
            return AddressTemplate.of("/subsystem=datasources/" + resourceName + EQ_WILDCARD)
                    .resolve(statementContext, dataSource.getName());
        } else {
            String profile = server.get(PROFILE_NAME).asString();
            return AddressTemplate.of("/profile=*/subsystem=datasources/" + resourceName + EQ_WILDCARD)
                    .resolve(statementContext, profile, dataSource.getName());
        }
    }

    void enableDataSource(DataSource dataSource) {
        ResourceAddress address = dataSourceConfigurationAddress(dataSource);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().dataSourceEnabled(dataSource.getName())));
            finder.refresh();
        });
    }

    void enableStatistics(DataSource dataSource) {
        ResourceAddress address = dataSourceConfigurationAddress(dataSource);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().statisticsEnabled(dataSource.getName())));
            finder.refresh();
        });
    }
}
