/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.FlowException;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;

/** Set of tasks to read the installed JDBC drivers. */
public class JdbcDriverTasks {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDriverTasks.class);

    private static final String CONFIGURATION_DRIVERS = "jdbcDriverFunctions.configurationDrivers";
    private static final String RUNTIME_DRIVERS = "jdbcDriverFunctions.runtimeDrivers";
    static final String DRIVERS = "jdbcDriverFunctions.drivers";


    public static List<Task<FlowContext>> jdbcDriverProperties(Environment environment, Dispatcher dispatcher,
            StatementContext statementContext, String driverName, Resources resources) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        if (!environment.isStandalone()) {
            tasks.addAll(runningServers(environment, dispatcher,
                    properties(PROFILE_NAME, statementContext.selectedProfile())));
        }
        tasks.add(new ReadJdbcDriversFromFirstServer(environment, dispatcher, statementContext, driverName, resources));
        return tasks;
    }

    /**
     * Reads the JDBC driver from the standalone server or the first running server in domain mode. Puts the list under
     * the key {@link ModelDescriptionConstants#RESULT} into the context.
     */
    private static class ReadJdbcDriversFromFirstServer implements Task<FlowContext> {

        private Environment environment;
        private final Dispatcher dispatcher;
        private StatementContext statementContext;
        private String driverName;
        private Resources resources;

        private ReadJdbcDriversFromFirstServer(Environment environment, Dispatcher dispatcher,
                StatementContext statementContext, String driverName, Resources resources) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.driverName = driverName;
            this.resources = resources;
        }

        @Override
        public Completable call(FlowContext context) {
            ResourceAddress address;
            if (environment.isStandalone()) {
                address = Server.STANDALONE.getServerAddress();
            } else {
                List<Server> servers = context.get(TopologyTasks.SERVERS);
                if (!servers.isEmpty()) {
                    Server server = servers.get(0);
                    address = server.getServerAddress();
                } else {
                    String message = resources.messages()
                            .readDatasourcePropertiesErrorDomain(statementContext.selectedProfile());
                    return Completable.error(new FlowException(message, context));
                }
            }
            address.add(SUBSYSTEM, DATASOURCES).add(JDBC_DRIVER, driverName);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> context.set(RESULT, result))
                    .toCompletable();
        }
    }


    /**
     * Reads the JDBC drivers from {@code /{selected.profile}/subsystem=datasource/jdbc-driver=*} and puts the result as
     * {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#CONFIGURATION_DRIVERS} into the context.
     */
    static class ReadConfiguration implements Task<FlowContext> {

        private final CrudOperations crud;

        ReadConfiguration(CrudOperations crud) {
            this.crud = crud;
        }

        @Override
        public Completable call(FlowContext context) {
            return crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, JDBC_DRIVER).doOnSuccess(children -> {
                List<JdbcDriver> drivers = children.stream()
                        .map(JdbcDriver::new)
                        .collect(toList());
                context.set(CONFIGURATION_DRIVERS, drivers);
            }).toCompletable();
        }
    }


    /**
     * Reads the JDBC drivers from a list of running servers which are expected in the context under the key {@link
     * org.jboss.hal.core.runtime.TopologyTasks#SERVERS}. The drivers are read using the {@code :installed-drivers-list}
     * operation. Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#RUNTIME_DRIVERS}
     * into the context.
     */
    static class ReadRuntime implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        ReadRuntime(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();

            if (environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress().add(SUBSYSTEM, DATASOURCES);
                Operation operation = new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                completable = dispatcher.execute(operation).doOnSuccess(result -> {
                    List<JdbcDriver> drivers = result.asList().stream()
                            .map(modelNode -> new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode))
                            .collect(toList());
                    context.set(RUNTIME_DRIVERS, drivers);
                }).toCompletable();

            } else {
                List<Server> servers = context.get(TopologyTasks.SERVERS);
                if (servers != null && !servers.isEmpty()) {
                    List<Operation> operations = servers.stream()
                            .map(server -> {
                                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES);
                                return new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                            })
                            .collect(toList());
                    completable = dispatcher.execute(new Composite(operations))
                            .doOnSuccess((CompositeResult result) -> {
                                List<JdbcDriver> drivers = new ArrayList<>();
                                for (ModelNode step : result) {
                                    if (!step.isFailure()) {
                                        // for each server we get the list of installed drivers
                                        for (ModelNode modelNode : step.get(RESULT).asList()) {
                                            drivers.add(
                                                    new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode));
                                        }
                                    }
                                }
                                context.set(RUNTIME_DRIVERS, drivers);
                            })
                            .toCompletable();
                }
            }
            return completable;
        }
    }


    /**
     * Combines and sorts the results form {@link ReadConfiguration} and {@link ReadRuntime} with a preference for
     * runtime drivers over configuration drivers.
     * <p>
     * Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#DRIVERS} into the context.
     */
    static class CombineDriverResults implements Task<FlowContext> {

        @Override
        public Completable call(FlowContext context) {
            Map<String, JdbcDriver> map = new HashMap<>();
            List<JdbcDriver> configDrivers = context.get(JdbcDriverTasks.CONFIGURATION_DRIVERS);
            List<JdbcDriver> runtimeDrivers = context.get(JdbcDriverTasks.RUNTIME_DRIVERS);
            if (configDrivers != null) {
                for (JdbcDriver driver : configDrivers) {
                    map.put(driver.getName(), driver);
                }
            }
            if (runtimeDrivers != null) {
                for (JdbcDriver driver : runtimeDrivers) {
                    map.put(driver.getName(), driver);
                }
            }
            List<JdbcDriver> drivers = new ArrayList<>(map.values());
            drivers.sort(Comparator.comparing(JdbcDriver::getName));
            context.set(DRIVERS, drivers);
            return Completable.complete();
        }
    }


    public static class JdbcDriverOutcome extends Outcome<FlowContext> {

        private final String dsClassname;
        private final boolean isXa;
        private final Consumer<List<String>> callback;

        public JdbcDriverOutcome(String dsClassname, boolean isXa, Consumer<List<String>> callback) {
            this.dsClassname = dsClassname;
            this.isXa = isXa;
            this.callback = callback;
        }

        @Override
        public void onError(FlowContext flowContext, Throwable error) {
            logger.warn("Failed to read jdbc-driver. Cause: {}", error.getMessage());
        }

        @Override
        public void onSuccess(FlowContext flowContext) {
            ModelNode result = flowContext.get(RESULT);
            List<String> properties = Collections.emptyList();
            String datasourceClassname;
            if (dsClassname == null) {
                String attribute = isXa ? DRIVER_XA_DATASOURCE_CLASS_NAME : DRIVER_DATASOURCE_CLASS_NAME;
                datasourceClassname = result.get(attribute).asString();
            } else {
                datasourceClassname = dsClassname;
            }
            if (result.hasDefined(DATASOURCE_CLASS_INFO)) {
                properties = result.get(DATASOURCE_CLASS_INFO).asList().stream()
                        .filter(node -> datasourceClassname.equals(node.asProperty().getName()))
                        .flatMap(node -> node.asProperty().getValue().asPropertyList().stream())
                        .map(Property::getName)
                        .collect(toList());
            }
            callback.accept(properties);
        }
    }


    private JdbcDriverTasks() {
    }
}
