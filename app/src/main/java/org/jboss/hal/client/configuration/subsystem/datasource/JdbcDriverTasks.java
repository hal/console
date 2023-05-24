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
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import elemental2.promise.IThenable;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATASOURCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATASOURCE_CLASS_INFO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JDBC_DRIVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;

/** Set of tasks to read the installed JDBC drivers. */
public class JdbcDriverTasks {

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
     * Reads the JDBC driver from the standalone server or the first running server in domain mode. Puts the list under the key
     * {@link ModelDescriptionConstants#RESULT} into the context.
     */
    private static final class ReadJdbcDriversFromFirstServer implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final StatementContext statementContext;
        private final String driverName;
        private final Resources resources;

        private ReadJdbcDriversFromFirstServer(Environment environment, Dispatcher dispatcher,
                StatementContext statementContext, String driverName, Resources resources) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.statementContext = statementContext;
            this.driverName = driverName;
            this.resources = resources;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
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
                    return context.reject(message);
                }
            }
            address.add(SUBSYSTEM, DATASOURCES).add(JDBC_DRIVER, driverName);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(operation)
                    .then(result -> context.resolve(RESULT, result));
        }
    }

    /**
     * Reads the JDBC drivers from {@code /{selected.profile}/subsystem=datasource/jdbc-driver=*} and puts the result as
     * {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#CONFIGURATION_DRIVERS} into the context.
     */
    static final class ReadConfiguration implements Task<FlowContext> {

        private final CrudOperations crud;

        ReadConfiguration(CrudOperations crud) {
            this.crud = crud;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            return crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, JDBC_DRIVER)
                    .then(children -> {
                        List<JdbcDriver> drivers = children.stream()
                                .map(JdbcDriver::new)
                                .collect(toList());
                        return context.resolve(CONFIGURATION_DRIVERS, drivers);
                    });
        }
    }

    /**
     * Reads the JDBC drivers from a list of running servers which are expected in the context under the key
     * {@link org.jboss.hal.core.runtime.TopologyTasks#SERVERS}. The drivers are read using the {@code :installed-drivers-list}
     * operation. Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#RUNTIME_DRIVERS} into the
     * context.
     */
    static final class ReadRuntime implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        ReadRuntime(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            if (environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress().add(SUBSYSTEM, DATASOURCES);
                Operation operation = new Operation.Builder(address, "installed-drivers-list").build(); // NON-NLS
                return dispatcher.execute(operation)
                        .then(result -> {
                            List<JdbcDriver> drivers = result.asList().stream()
                                    .map(modelNode -> new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode))
                                    .collect(toList());
                            return context.resolve(RUNTIME_DRIVERS, drivers);
                        });

            } else {
                List<Server> servers = context.get(TopologyTasks.SERVERS);
                if (servers != null && !servers.isEmpty()) {
                    List<Operation> operations = servers.stream()
                            .map(server -> {
                                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES);
                                return new Operation.Builder(address, "installed-drivers-list").build(); // NON-NLS
                            })
                            .collect(toList());
                    return dispatcher.execute(new Composite(operations))
                            .then(result -> {
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
                                return context.resolve(RUNTIME_DRIVERS, drivers);
                            });
                } else {
                    return Promise.resolve(context);
                }
            }
        }
    }

    /**
     * Combines and sorts the results form {@link ReadConfiguration} and {@link ReadRuntime} with a preference for runtime
     * drivers over configuration drivers.
     * <p>
     * Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverTasks#DRIVERS} into the context.
     */
    static final class CombineDriverResults implements Task<FlowContext> {

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
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
            return Promise.resolve(context);
        }
    }

    public static final class JdbcDriverOutcome implements ThenOnFulfilledCallbackFn<FlowContext, Object> {

        private final String dsClassname;
        private final boolean isXa;
        private final Consumer<List<String>> callback;

        public JdbcDriverOutcome(String dsClassname, boolean isXa, Consumer<List<String>> callback) {
            this.dsClassname = dsClassname;
            this.isXa = isXa;
            this.callback = callback;
        }

        @Override
        public IThenable<Object> onInvoke(final FlowContext context) {
            ModelNode result = context.get(RESULT);
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
            return null;
        }
    }

    private JdbcDriverTasks() {
    }
}
