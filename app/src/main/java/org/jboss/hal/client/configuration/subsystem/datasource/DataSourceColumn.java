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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.ChooseTemplateStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.ConnectionStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.Context;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.DriverStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.NamesStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.PropertiesStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.ReviewStep;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.State;
import org.jboss.hal.client.configuration.subsystem.datasource.wizard.TestStep;
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
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
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
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.State.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Column which is used for both XA and normal data sources.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_CONFIGURATION)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS, JDBC_DRIVER_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private static final String WIZARD_TITLE = "wizard-title";
    private static final String WIZARD_TEXT = "wizard-text";
    private static final String WIZARD_ERROR = "wizard-error";

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
        addActions.add(new ColumnAction<>(Ids.DATA_SOURCE_ADD,
                resources.messages().addResourceTitle(Names.DATASOURCE),
                column -> prepareWizard(false)));
        addActions.add(new ColumnAction<>(Ids.XA_DATA_SOURCE_ADD,
                resources.messages().addResourceTitle(Names.XA_DATASOURCE),
                column -> prepareWizard(true)));
        addColumnActions(Ids.DATA_SOURCE_ADD_ACTIONS, pfIcon("add-circle-o"), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(Ids.DATA_SOURCE_REFRESH));

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
            public List<ItemAction<DataSource>> actions() {
                PlaceRequest.Builder builder = places.selectedProfile(NameTokens.DATA_SOURCE_CONFIGURATION)
                        .with(NAME, dataSource.getName());
                if (dataSource.isXa()) {
                    builder.with(DataSourcePresenter.XA_PARAM, String.valueOf(true));
                }

                List<ItemAction<DataSource>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(builder.build()));
                if (dataSource.isEnabled()) {
                    actions.add(new ItemAction<>(resources.constants().disable(), ds -> disable(ds)));
                    actions.add(new ItemAction<>(resources.constants().testConnection(), ds -> testConnection(ds)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().enable(), ds -> enable(ds)));
                }
                actions.add(itemActionFactory.remove(Names.DATASOURCE, dataSource.getName(),
                        dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE, DataSourceColumn.this));
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, item, resources));
    }

    private void prepareWizard(final boolean xa) {
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                showWizard(Collections.emptyList(), Collections.emptyList(), xa);
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                List<DataSource> dataSources = context.get(DATASOURCES);
                List<JdbcDriver> drivers = context.get(JdbcDriverFunctions.DRIVERS);
                showWizard(dataSources, drivers, xa);
            }
        };

        Function<FunctionContext> readDataSources =
                control -> crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, xa ? XA_DATA_SOURCE : DATA_SOURCE,
                        children -> {
                            List<DataSource> dataSources = children.stream()
                                    .map(property -> new DataSource(property, xa)).collect(toList());
                            control.getContext().set(DATASOURCES, dataSources);
                            control.proceed();
                        });

        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                readDataSources,
                new JdbcDriverFunctions.ReadConfiguration(crud),
                new TopologyFunctions.RunningServersQuery(environment, dispatcher,
                        new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())),
                new JdbcDriverFunctions.ReadRuntime(environment, dispatcher),
                new JdbcDriverFunctions.CombineDriverResults());
    }

    private void showWizard(List<DataSource> dataSources, List<JdbcDriver> drivers, final boolean xa) {

        Wizard.Builder<Context, State> builder = new Wizard.Builder<Context, State>(
                resources.messages().addResourceTitle(xa ? Names.XA_DATASOURCE : Names.DATASOURCE),
                new Context(xa))

                .onBack((context, currentState) -> {
                    State previous = null;
                    switch (currentState) {
                        case CHOOSE_TEMPLATE:
                            break;
                        case NAMES:
                            previous = CHOOSE_TEMPLATE;
                            break;
                        case DRIVER:
                            previous = NAMES;
                            break;
                        case XA_PROPERTIES:
                            previous = DRIVER;
                            break;
                        case CONNECTION:
                            previous = context.isXa() ? XA_PROPERTIES : DRIVER;
                            break;
                        case TEST:
                            previous = CONNECTION;
                            break;
                        case REVIEW:
                            previous = TEST;
                            break;
                    }
                    return previous;
                })

                .onNext((context, currentState) -> {
                    State next = null;
                    switch (currentState) {
                        case CHOOSE_TEMPLATE:
                            next = NAMES;
                            break;
                        case NAMES:
                            next = DRIVER;
                            break;
                        case DRIVER:
                            next = context.isXa() ? XA_PROPERTIES : CONNECTION;
                            break;
                        case XA_PROPERTIES:
                            next = CONNECTION;
                            break;
                        case CONNECTION:
                            next = TEST;
                            break;
                        case TEST:
                            next = REVIEW;
                            break;
                        case REVIEW:
                            break;
                    }
                    return next;
                })

                .stayOpenAfterFinish()
                .onCancel(context -> {
                    if (context.isCreated()) {
                        // cleanup
                        DataSource dataSource = context.getDataSource();
                        ResourceAddress address = dataSource.isXa()
                                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
                        Operation operation = new Operation.Builder(REMOVE, address).build();
                        dispatcher.execute(operation,
                                result -> refresh(RESTORE_SELECTION),
                                (op, failure) -> MessageEvent.fire(eventBus, Message.error(resources.messages()
                                        .testConnectionCancelError(dataSource.getName()), failure)));
                    }
                })
                .onFinish((wizard, context) -> {
                    DataSource dataSource = context.getDataSource();
                    if (!context.isCreated()) {
                        ResourceAddress address = dataSource.isXa()
                                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
                        Operation operation = new Operation.Builder(ADD, address).payload(dataSource).build();
                        dispatcher.execute(operation,
                                result -> {
                                    refresh(Ids.dataSourceConfiguration(dataSource.getName(), dataSource.isXa()));
                                    wizard.showSuccess(resources.constants().operationSuccessful(),
                                            resources.messages()
                                                    .addResourceSuccess(Names.DATASOURCE, dataSource.getName()),
                                            resources.messages().view(Names.DATASOURCE),
                                            cxt -> { /* nothing to do, datasource is already selected */ });
                                },
                                (op, failure) -> wizard.showError(resources.constants().operationFailed(),
                                        resources.messages().dataSourceAddError(), failure));
                    } else {
                        refresh(Ids.dataSourceConfiguration(dataSource.getName(), dataSource.isXa()));
                        wizard.showSuccess(resources.constants().operationSuccessful(),
                                resources.messages()
                                        .addResourceSuccess(Names.DATASOURCE, dataSource.getName()),
                                resources.messages().view(Names.DATASOURCE),
                                cxt -> { /* nothing to do, datasource is already selected */ });
                    }
                });

        AddressTemplate dataSourceTemplate = xa ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE;
        Metadata dataSourceMetadata = metadataRegistry.lookup(dataSourceTemplate);
        Metadata driverMetadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);

        builder.addStep(CHOOSE_TEMPLATE, new ChooseTemplateStep(templates, resources, xa));
        builder.addStep(NAMES, new NamesStep(dataSources, dataSourceMetadata, resources, xa));
        builder.addStep(DRIVER, new DriverStep(drivers, driverMetadata, resources));
        if (xa) {
            builder.addStep(XA_PROPERTIES, new PropertiesStep(resources));
        }
        builder.addStep(CONNECTION, new ConnectionStep(dataSourceMetadata, resources, xa));
        builder.addStep(TEST, new TestStep(this, resources, xa, environment.isStandalone()));
        builder.addStep(REVIEW, new ReviewStep(dataSourceMetadata, resources, xa));

        builder.build().show();
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
        Operation operation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, ENABLED)
                .param(VALUE, enabled)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(message));
            refresh(RESTORE_SELECTION);
        });
    }

    public void testConnectionFromWizard(Wizard<Context, State> wizard) {
        Context context = wizard.getContext();
        DataSource dataSource = context.getDataSource();

        List<Function<FunctionContext>> functions = new ArrayList<>();
        if (!context.isCreated()) {
            // add data source
            functions.add(control -> {
                ResourceAddress address = dataSource.isXa()
                        ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                        : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
                Operation operation = new Operation.Builder(ADD, address).payload(dataSource).build();
                dispatcher.executeInFunction(control, operation,
                        result -> {
                            context.setCreated(true);
                            control.proceed();
                        },
                        (op, failure) -> {
                            control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                            control.getContext().set(WIZARD_TEXT, resources.messages().dataSourceAddError());
                            control.getContext().set(WIZARD_ERROR, failure);
                            control.abort();
                        });
            });
        }

        // check running server(s)
        functions.add(new TopologyFunctions.RunningServersQuery(
                environment, dispatcher, new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())));

        // test connection
        functions.add(control -> {
            List<Server> servers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES)
                        .add(DATA_SOURCE, dataSource.getName());
                Operation operation = new Operation.Builder(TEST_CONNECTION_IN_POOL, address).build();
                dispatcher.executeInFunction(control, operation,
                        result -> control.proceed(),
                        (op, failure) -> {
                            control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                            control.getContext()
                                    .set(WIZARD_TEXT, resources.messages().testConnectionError(dataSource.getName()));
                            control.getContext().set(WIZARD_ERROR, failure);
                            control.abort();
                        });

            } else {
                control.getContext().set(WIZARD_TITLE, resources.constants().testConnectionError());
                control.getContext()
                        .set(WIZARD_TEXT, SafeHtmlUtils.fromString(resources.constants().noRunningServers()));
                control.abort();
            }
        });

        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                String title = context.get(WIZARD_TITLE);
                SafeHtml text = context.get(WIZARD_TEXT);
                String error = context.get(WIZARD_ERROR);
                wizard.showError(title, text, error, false);
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                wizard.showSuccess(resources.constants().testConnectionSuccess(),
                        resources.messages().testConnectionSuccess(dataSource.getName()), false);
            }
        };
        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                functions.toArray(new Function[functions.size()]));
    }

    private void testConnection(final DataSource dataSource) {
        TopologyFunctions.RunningServersQuery runningServers = new TopologyFunctions.RunningServersQuery(
                environment, dispatcher, new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile()));
        Function<FunctionContext> testConnection = control -> {
            List<Server> servers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
            if (!servers.isEmpty()) {
                Server server = servers.get(0);
                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES)
                        .add(DATA_SOURCE, dataSource.getName());
                Operation operation = new Operation.Builder(TEST_CONNECTION_IN_POOL, address).build();
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
                        Message.error(resources.messages().testConnectionError(dataSource.getName()),
                                context.getErrorMessage()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().testConnectionSuccess(dataSource.getName())));
            }
        };

        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), outcome, runningServers, testConnection);
    }
}
