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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceColumn;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_PROPERTIES_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.wizard.State.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class DataSourceWizard {

    static Composite addOperation(Context context, StatementContext statementContext) {
        AddressTemplate template = context.dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE;
        ResourceAddress address = template.resolve(statementContext, context.dataSource.getName());

        ModelNode payload = context.dataSource.clone();
        payload.remove(NAME);

        List<Operation> operations = new ArrayList<>();
        if (context.isXa()) {

            // remove unsupported attributes
            payload.remove(POOL_NAME);
            payload.remove(DRIVER_CLASS);
            operations.add(new Operation.Builder(address, ADD).payload(context.dataSource).build());

            // add an operation for each property
            context.xaProperties.forEach((key, value) -> {
                ResourceAddress propertyAddress = XA_DATA_SOURCE_PROPERTIES_TEMPLATE.resolve(statementContext,
                        context.dataSource.getName(), key);
                operations.add(new Operation.Builder(propertyAddress, ADD).param(VALUE, value).build());
            });
        } else {
            operations.add(new Operation.Builder(address, ADD).payload(context.dataSource).build());
        }
        return new Composite(operations);
    }

    private final DataSourceColumn column;
    private final Resources resources;
    private final Wizard<Context, State> wizard;

    public DataSourceWizard(DataSourceColumn column,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            EventBus eventBus,
            StatementContext statementContext,
            Environment environment,
            Provider<Progress> progress,
            Resources resources,
            DataSourceTemplates templates,
            List<DataSource> dataSources,
            List<JdbcDriver> drivers,
            boolean xa) {
        this.column = column;
        this.resources = resources;

        Wizard.Builder<Context, State> builder = new Wizard.Builder<Context, State>(
                resources.messages().addResourceTitle(xa ? Names.XA_DATASOURCE : Names.DATASOURCE), new Context(xa))

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
                        ResourceAddress address = context.dataSource.isXa()
                                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, context.dataSource.getName())
                                : DATA_SOURCE_TEMPLATE.resolve(statementContext, context.dataSource.getName());
                        Operation operation = new Operation.Builder(address, REMOVE).build();
                        dispatcher.execute(operation,
                                result -> column.refresh(RESTORE_SELECTION),
                                (op, failure) -> MessageEvent.fire(eventBus, Message.error(resources.messages()
                                        .testConnectionCancelError(context.dataSource.getName()), failure)));
                    }
                })

                .onFinish((wizard, context) -> {
                    if (!context.isCreated()) {
                        dispatcher.execute(addOperation(context, statementContext),
                                (CompositeResult result) -> success(context.dataSource),
                                (op, failure) -> wizard.showError(resources.constants().operationFailed(),
                                        resources.messages().dataSourceAddError(), failure));
                    } else {
                        AddressTemplate template = context.dataSource.isXa()
                                ? XA_DATA_SOURCE_TEMPLATE
                                : DATA_SOURCE_TEMPLATE;
                        ResourceAddress address = template.resolve(statementContext, context.dataSource.getName());
                        Metadata metadata = metadataRegistry.lookup(template);
                        if (context.hasChanges()) {
                            Composite operations = new OperationFactory().fromChangeSet(address, context.changes(),
                                    metadata);
                            dispatcher.execute(operations,
                                    (CompositeResult result) -> success(context.dataSource),
                                    (op, failure) -> wizard.showError(resources.constants().operationFailed(),
                                            resources.messages().dataSourceAddError(), failure));
                        } else {
                            success(context.dataSource);
                        }
                    }
                });

        AddressTemplate dataSourceTemplate = xa ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE;
        Metadata dataSourceMetadata = metadataRegistry.lookup(dataSourceTemplate);
        Metadata xaDataSourcePropertiesMetadata = metadataRegistry.lookup(XA_DATA_SOURCE_PROPERTIES_TEMPLATE);
        Metadata driverMetadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);

        builder.addStep(CHOOSE_TEMPLATE, new ChooseTemplateStep(templates, resources, xa));
        builder.addStep(NAMES, new NamesStep(dataSources, dataSourceMetadata, resources, xa));
        builder.addStep(DRIVER, new DriverStep(drivers, driverMetadata, resources));
        if (xa) {
            builder.addStep(XA_PROPERTIES, new PropertiesStep(xaDataSourcePropertiesMetadata, resources));
        }
        builder.addStep(CONNECTION, new ConnectionStep(dataSourceMetadata, resources, xa));
        builder.addStep(TEST, new TestStep(dispatcher, statementContext, environment, progress, resources));
        builder.addStep(REVIEW, new ReviewStep(dataSourceMetadata, resources, xa));

        this.wizard = builder.build();
    }

    public void show() {
        wizard.show();
    }

    private void success(DataSource dataSource) {
        column.refresh(Ids.dataSourceConfiguration(dataSource.getName(), dataSource.isXa()));
        wizard.showSuccess(resources.constants().operationSuccessful(),
                resources.messages()
                        .addResourceSuccess(Names.DATASOURCE, dataSource.getName()),
                resources.messages().view(Names.DATASOURCE),
                cxt -> { /* nothing to do, datasource is already selected */ });
    }
}
