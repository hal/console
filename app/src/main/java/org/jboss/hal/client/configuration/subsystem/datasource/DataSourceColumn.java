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
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.NewDataSourceWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * Column which is used for both XA and normal data sources.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_COLUMN)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final DataSourceTemplates templates;

    @Inject
    public DataSourceColumn(final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Environment environment,
            final @Footer Provider<Progress> progress,
            final Resources resources,
            final Places places,
            final DataSourceTemplates templates,
            final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory) {

        super(new Builder<DataSource>(finder, Ids.DATA_SOURCE_COLUMN, Names.DATASOURCE)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.progress = progress;
        this.resources = resources;
        this.templates = templates;

        addColumnAction(columnActionFactory.add(IdBuilder.build(DATA_SOURCE, "add"), Names.DATASOURCE,
                column -> launchNewDataSourceWizard(false)));
        addColumnAction(columnActionFactory.add(IdBuilder.build(XA_DATA_SOURCE, "add"), Names.XA_DATASOURCE,
                fontAwesome("credit-card"), column -> launchNewDataSourceWizard(true)));
        addColumnAction(columnActionFactory.refresh(IdBuilder.build(DATA_SOURCE, "refresh")));

        setItemsProvider((context, callback) -> {
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation dataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, dataSourceAddress)
                    .param(CHILD_TYPE, DATA_SOURCE).build();
            Operation xaDataSourceOperation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                    dataSourceAddress)
                    .param(CHILD_TYPE, XA_DATA_SOURCE).build();
            dispatcher.execute(new Composite(dataSourceOperation, xaDataSourceOperation), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, false)).collect(toList()));
                combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, true)).collect(toList()));
                callback.onSuccess(combined);
            });
        });

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return DataSource.id(dataSource.getName(), dataSource.isXa());
            }

            @Override
            public Element asElement() {
                //noinspection HardCodedStringLiteral
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
            public List<ItemAction<DataSource>> actions() {
                PlaceRequest.Builder builder = places.selectedProfile(NameTokens.DATA_SOURCE_CONFIGURATION)
                        .with(NAME, dataSource.getName());
                if (dataSource.isXa()) {
                    builder.with(DataSourcePresenter.XA_PARAM, String.valueOf(true));
                }

                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(builder.build()));
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
                if (dataSource.isEnabled()) {
                    actions.add(new ItemAction<>(resources.constants().disable(), ds -> disable(ds)));
                    actions.add(new ItemAction<>(resources.constants().testConnection(), ds -> testConnection(ds)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().enable(), ds -> enable(ds)));
                }
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, item, resources));
    }

    private void launchNewDataSourceWizard(final boolean xa) {
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                launchWizard(Collections.emptyList(), Collections.emptyList());
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                List<DataSource> dataSources = context.get(DATASOURCES);
                List<JdbcDriver> drivers = context.get(JdbcDriverFunctions.DRIVERS);
                launchWizard(dataSources, drivers);
            }

            private void launchWizard(List<DataSource> dataSources, List<JdbcDriver> drivers) {
                NewDataSourceWizard wizard = new NewDataSourceWizard(metadataRegistry, environment, resources,
                        templates, dataSources, drivers, xa, context -> {
                    DataSource dataSource = context.getDataSource();
                    Operation operation = new Operation.Builder(ADD, dataSourceAddress(dataSource))
                            .payload(dataSource)
                            .build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().addResourceSuccess(Names.DATASOURCE, dataSource.getName())));
                        refresh(DataSource.id(dataSource.getName(), dataSource.isXa()));
                    });
                });
                wizard.show();
            }
        };

        Function<FunctionContext> readDataSources = control -> {
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, dataSourceAddress)
                    .param(CHILD_TYPE, xa ? XA_DATA_SOURCE : DATA_SOURCE).build();
            dispatcher.executeInFunction(control, operation, result -> {
                List<DataSource> dataSources = result.asPropertyList().stream()
                        .map(property -> new DataSource(property, xa)).collect(toList());
                control.getContext().set(DATASOURCES, dataSources);
                control.proceed();
            });
        };

        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                readDataSources,
                new JdbcDriverFunctions.ReadConfiguration(statementContext, dispatcher),
                new TopologyFunctions.RunningServersOfProfile(environment, dispatcher,
                        statementContext.selectedProfile()),
                new JdbcDriverFunctions.ReadRuntime(environment, dispatcher),
                new JdbcDriverFunctions.CombineDriverResults());
    }

    private ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    void disable(final DataSource dataSource) {
        ResourceAddress address = dataSourceAddress(dataSource);
        Operation operation = new Operation.Builder("disable", address).build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().databaseDisabled(dataSource.getName())));
            refresh(RefreshMode.RESTORE_SELECTION);
        });
    }

    void enable(final DataSource dataSource) {
        ResourceAddress address = dataSourceAddress(dataSource);
        Operation operation = new Operation.Builder("enable", address).build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus,
                    Message.success(resources.messages().databaseEnabled(dataSource.getName())));
            refresh(RefreshMode.RESTORE_SELECTION);
        });
    }

    private void testConnection(final DataSource dataSource) {
        TopologyFunctions.RunningServersOfProfile runningServers = new TopologyFunctions.RunningServersOfProfile(
                environment, dispatcher, statementContext.selectedProfile());
        Function<FunctionContext> testConnection = control -> {
            List<Server> servers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES)
                        .add(DATA_SOURCE, dataSource.getName());
                Operation operation = new Operation.Builder("test-connection-in-pool", address).build(); //NON-NLS
                dispatcher.executeInFunction(control, operation, result -> control.proceed());

            } else {
                control.getContext().setErrorMessage(resources.constants().noRunningServers());
                control.abort();
            }
        };

        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                MessageEvent.fire(eventBus,
                        Message.error(resources.messages().testConnectionError(), context.getErrorMessage()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                MessageEvent.fire(eventBus, Message.success(resources.messages().testConnectionSuccess()));
            }
        };

        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), outcome, runningServers, testConnection);
    }
}
