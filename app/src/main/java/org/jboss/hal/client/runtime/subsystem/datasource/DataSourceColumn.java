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
package org.jboss.hal.client.runtime.subsystem.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.XA_PARAM;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_RUNTIME)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final Finder finder;
    private Server server;

    @Inject
    public DataSourceColumn(final ServerActions serverActions,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Environment environment,
            final Resources resources,
            final Finder finder,
            final ItemActionFactory itemActionFactory) {

        super(new Builder<DataSource>(finder, Ids.DATA_SOURCE_RUNTIME, Names.DATASOURCE)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.finder = finder;

        setItemsProvider((context, callback) -> {
            List<Operation> operations = new ArrayList<>();
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            operations.add(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    dataSourceAddress)
                    .param(CHILD_TYPE, DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    dataSourceAddress)
                    .param(CHILD_TYPE, XA_DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            if (!environment.isStandalone()) {
                ResourceAddress serverAddress = AddressTemplate.of("/{selected.host}/{selected.server}")
                        .resolve(statementContext);
                operations.add(new Operation.Builder(READ_RESOURCE_OPERATION, serverAddress)
                        .param(INCLUDE_RUNTIME, true)
                        .param(ATTRIBUTES_ONLY, true)
                        .build());
            }
            dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, false)).collect(toList()));
                combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, true)).collect(toList()));
                Collections.sort(combined, (d1, d2) -> d1.getName().compareTo(d2.getName()));
                server = environment.isStandalone()
                        ? Server.STANDALONE
                        : new Server(statementContext.selectedHost(), result.step(2).get(RESULT));
                callback.onSuccess(combined);
            });
        });

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return Ids.dataSourceRuntime(dataSource.getName(), dataSource.isXa());
            }

            @Override
            public Element asElement() {
                return dataSource.isXa() ? ItemDisplay.withSubtitle(dataSource.getName(), Names.XA_DATASOURCE) : null;
            }

            @Override
            public String getTitle() {
                return dataSource.getName();
            }

            @Override
            public Element getIcon() {
                return dataSource.isEnabled() ? Icons.ok() : Icons.disabled();
            }

            @Override
            public String getTooltip() {
                return dataSource.isEnabled() ? resources.constants().enabled() : resources.constants().disabled();
            }

            @Override
            public String getFilterData() {
                //noinspection HardCodedStringLiteral
                return getTitle() + " " +
                        (dataSource.isXa() ? "xa" : "normal") + " " +
                        (dataSource.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public List<ItemAction<DataSource>> actions() {
                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(NameTokens.DATA_SOURCE_RUNTIME, NAME, dataSource.getName(), XA_PARAM,
                        String.valueOf(dataSource.isXa())));
                if (dataSource.isEnabled()) {
                    actions.add(new ItemAction<>(resources.constants().testConnection(), item -> testConnection(item)));
                    actions.add(new ItemAction<>(resources.constants().flushGracefully(),
                            item -> flush(item, "flush-gracefully-connection-in-pool")));
                    actions.add(new ItemAction<>(resources.constants().flushIdle(),
                            item -> flush(item, "flush-idle-connection-in-pool")));
                    actions.add(new ItemAction<>(resources.constants().flushInvalid(),
                            item -> flush(item, "flush-invalid-connection-in-pool")));
                    actions.add(new ItemAction<>(resources.constants().flushAll(),
                            item -> flush(item, "flush-all-connection-in-pool")));
                }
                return actions;
            }
        });

        setPreviewCallback(item ->
                new DataSourcePreview(this, server, item, environment, dispatcher, statementContext, serverActions,
                        resources));
    }

    private void testConnection(DataSource dataSource) {
        //noinspection HardCodedStringLiteral
        Operation operation = new Operation.Builder("test-connection-in-pool", dataSourceAddress(dataSource)).build();
        dispatcher.execute(operation,
                result -> MessageEvent.fire(eventBus, Message.success(resources.messages().testConnectionSuccess())),
                (o1, failure) -> MessageEvent.fire(eventBus, Message.error(resources.messages().testConnectionError(),
                        failure)),
                (o2, exception) -> MessageEvent.fire(eventBus, Message.error(resources.messages().testConnectionError(),
                        exception.getMessage())));
    }

    private void flush(DataSource dataSource, String flushMode) {
        Operation operation = new Operation.Builder(flushMode, dataSourceAddress(dataSource)).build();
        dispatcher.execute(operation,
                result -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus, Message.success(resources.messages().flushConnectionSuccess()));
                });
    }

    ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    private ResourceAddress dataSourceConfigurationAddress(DataSource dataSource) {
        String resourceName = dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE;
        if (environment.isStandalone()) {
            return AddressTemplate.of("/subsystem=datasources/" + resourceName + "=*")
                    .resolve(statementContext, dataSource.getName());
        } else {
            String profile = server.get(PROFILE_NAME).asString();
            return AddressTemplate.of("/profile=*/subsystem=datasources/" + resourceName + "=*")
                    .resolve(statementContext, profile, dataSource.getName());
        }
    }

    void enableDataSource(DataSource dataSource) {
        ResourceAddress address = dataSourceConfigurationAddress(dataSource);
        Operation operation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
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
        Operation operation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().statisticsEnabled(dataSource.getName())));
            finder.refresh();
        });
    }
}