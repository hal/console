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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.DataSourceWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.pfIcon;

/** Column which is used for both XA and normal data sources. */
@AsyncColumn(Ids.DATA_SOURCE_CONFIGURATION)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final DataSourceTemplates templates;

    @Inject
    public DataSourceColumn(final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final CrudOperations crud,
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

        super(new Builder<DataSource>(finder, Ids.DATA_SOURCE_CONFIGURATION, Names.DATASOURCE)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.progress = progress;
        this.resources = resources;
        this.templates = templates;

        List<ColumnAction<DataSource>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<DataSource>(Ids.DATA_SOURCE_ADD)
                .title(resources.messages().addResourceTitle(Names.DATASOURCE))
                .handler(column -> prepareWizard(false))
                .constraint(Constraint.executable(DATA_SOURCE_TEMPLATE, ADD))
                .build());
        addActions.add(new ColumnAction.Builder<DataSource>(Ids.XA_DATA_SOURCE_ADD)
                .title(resources.messages().addResourceTitle(Names.XA_DATASOURCE))
                .handler(column -> prepareWizard(true))
                .constraint(Constraint.executable(XA_DATA_SOURCE_TEMPLATE, ADD))
                .build());
        addColumnActions(Ids.DATA_SOURCE_ADD_ACTIONS, pfIcon("add-circle-o"), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(Ids.DATA_SOURCE_REFRESH));

        setItemsProvider((context, callback) -> {
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation dataSourceOperation = new Operation.Builder(dataSourceAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, DATA_SOURCE).build();
            Operation xaDataSourceOperation = new Operation.Builder(dataSourceAddress, READ_CHILDREN_RESOURCES_OPERATION
            )
                    .param(CHILD_TYPE, XA_DATA_SOURCE).build();
            dispatcher.execute(new Composite(dataSourceOperation, xaDataSourceOperation), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, false)).collect(toList()));
                combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, true)).collect(toList()));
                combined.sort(comparing(NamedNode::getName));
                callback.onSuccess(combined);
            });
        });

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return Ids.dataSourceConfiguration(dataSource.getName(), dataSource.isXa());
            }

            @Override
            public HTMLElement asElement() {
                return dataSource.isXa() ? ItemDisplay.withSubtitle(dataSource.getName(), Names.XA_DATASOURCE) : null;
            }

            @Override
            public String getTitle() {
                return dataSource.getName();
            }

            @Override
            public HTMLElement getIcon() {
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
                if (dataSource.isEnabled()) {
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().disable())
                            .handler(ds -> disable(ds))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE, ADD))
                            .build());
                    // test connection w/ constraints makes only sense in standalone mode
                    if (environment.isStandalone()) {
                        actions.add(new ItemAction.Builder<DataSource>()
                                .title(resources.constants().testConnection())
                                .handler(ds -> testConnection(ds))
                                .constraint(Constraint.executable(
                                        dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                        TEST_CONNECTION_IN_POOL))
                                .build());
                    }
                } else {
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().enable())
                            .handler(ds -> enable(ds))
                            .constraint(Constraint.writable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE, ENABLED))
                            .build());
                }
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, item, resources));
    }

    private void prepareWizard(final boolean xa) {
        Task<FlowContext> readDataSources =
                (context, control) -> crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, xa ? XA_DATA_SOURCE : DATA_SOURCE,
                        children -> {
                            List<DataSource> dataSources = children.stream()
                                    .map(property -> new DataSource(property, xa)).collect(toList());
                            context.set(DATASOURCES, dataSources);
                            control.proceed();
                        });

        series(new FlowContext(progress.get()),
                readDataSources,
                new JdbcDriverTasks.ReadConfiguration(crud),
                new TopologyTasks.RunningServersQuery(environment, dispatcher, environment.isStandalone()
                        ? null
                        : new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())),
                new JdbcDriverTasks.ReadRuntime(environment, dispatcher),
                new JdbcDriverTasks.CombineDriverResults())
                .subscribe(new Outcome<FlowContext>() {
                    @Override
                    public void onError(FlowContext context, Throwable error) {
                        showWizard(Collections.emptyList(), Collections.emptyList(), xa);
                    }

                    @Override
                    public void onSuccess(FlowContext context) {
                        List<DataSource> dataSources = context.get(DATASOURCES);
                        List<JdbcDriver> drivers = context.get(JdbcDriverTasks.DRIVERS);
                        showWizard(dataSources, drivers, xa);
                    }
                });
    }

    private void showWizard(List<DataSource> dataSources, List<JdbcDriver> drivers, final boolean xa) {
        DataSourceWizard wizard = new DataSourceWizard(this, metadataRegistry, dispatcher, eventBus,
                statementContext, environment, progress, resources, templates, dataSources, drivers, xa);
        wizard.show();
    }

    private ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    void disable(final DataSource dataSource) {
        setEnabled(dataSourceAddress(dataSource), false, resources.messages().dataSourceDisabled(dataSource.getName()));
    }

    void enable(final DataSource dataSource) {
        setEnabled(dataSourceAddress(dataSource), true, resources.messages().dataSourceEnabled(dataSource.getName()));
    }

    private void setEnabled(ResourceAddress address, boolean enabled, SafeHtml message) {
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, ENABLED)
                .param(VALUE, enabled)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(message));
            refresh(RESTORE_SELECTION);
        });
    }

    private void testConnection(final DataSource dataSource) {
        if (environment.isStandalone()) {
            ResourceAddress address = new ResourceAddress()
                    .add(SUBSYSTEM, DATASOURCES)
                    .add(dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE, dataSource.getName());
            Operation operation = new Operation.Builder(address, TEST_CONNECTION_IN_POOL).build();
            dispatcher.execute(operation,
                    result -> MessageEvent.fire(eventBus,
                            Message.success(resources.messages().testConnectionSuccess(dataSource.getName()))),
                    (op, failure) -> MessageEvent.fire(eventBus,
                            Message.error(resources.messages().testConnectionError(dataSource.getName()), failure)));
        }
    }
}
