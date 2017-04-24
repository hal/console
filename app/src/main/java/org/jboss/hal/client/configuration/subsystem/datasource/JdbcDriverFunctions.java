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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Set of functions to read the installed JDBC drivers.
 *
 * @author Harald Pehl
 */
class JdbcDriverFunctions {

    private static final String CONFIGURATION_DRIVERS = "jdbcDriverFunctions.configurationDrivers";
    private static final String RUNTIME_DRIVERS = "jdbcDriverFunctions.runtimeDrivers";
    static final String DRIVERS = "jdbcDriverFunctions.drivers";


    /**
     * Reads the JDBC drivers from {@code /{selected.profile}/subsystem=datasource/jdbc-driver=*} and puts the result
     * as {@code List<JdbcDriver>} under the key {@link JdbcDriverFunctions#CONFIGURATION_DRIVERS} into the context.
     */
    static class ReadConfiguration implements Function<FunctionContext> {

        private final CrudOperations crud;

        ReadConfiguration(final CrudOperations crud) {
            this.crud = crud;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, JDBC_DRIVER, children -> {
                List<JdbcDriver> drivers = children.stream()
                        .map(JdbcDriver::new)
                        .collect(toList());
                control.getContext().set(CONFIGURATION_DRIVERS, drivers);
                control.proceed();
            });
        }
    }


    /**
     * Reads the JDBC drivers from a list of running servers which are expected in the context under the key
     * {@link TopologyFunctions#RUNNING_SERVERS}. The drivers are read using the {@code :installed-drivers-list}
     * operation. Stores the result as {@code List<JdbcDriver>} under the key {@link
     * JdbcDriverFunctions#RUNTIME_DRIVERS} into the context.
     */
    static class ReadRuntime implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        ReadRuntime(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress().add(SUBSYSTEM, DATASOURCES);
                Operation operation = new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                dispatcher.executeInFunction(control, operation, result -> {
                    List<JdbcDriver> drivers = result.asList().stream()
                            .map(modelNode -> new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode))
                            .collect(toList());
                    control.getContext().set(RUNTIME_DRIVERS, drivers);
                    control.proceed();
                });

            } else {
                List<Server> servers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
                if (servers != null && !servers.isEmpty()) {
                    List<Operation> operations = servers.stream()
                            .map(server -> {
                                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES);
                                return new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                            })
                            .collect(toList());
                    dispatcher.executeInFunction(control, new Composite(operations), (CompositeResult result) -> {
                        List<JdbcDriver> drivers = new ArrayList<>();
                        for (ModelNode step : result) {
                            if (!step.isFailure()) {
                                // for each server we get the list of installed drivers
                                for (ModelNode modelNode : step.get(RESULT).asList()) {
                                    drivers.add(new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode));
                                }
                            }
                        }
                        control.getContext().set(RUNTIME_DRIVERS, drivers);
                        control.proceed();
                    });

                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Combines and sorts the results form {@link ReadConfiguration} and {@link
     * ReadRuntime} with a preference for runtime drivers over configuration drivers.
     * <p>
     * Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverFunctions#DRIVERS} into the
     * context.
     */
    static class CombineDriverResults implements Function<FunctionContext> {

        @Override
        public void execute(final Control<FunctionContext> control) {
            Map<String, JdbcDriver> map = new HashMap<>();
            List<JdbcDriver> configDrivers = control.getContext().get(JdbcDriverFunctions.CONFIGURATION_DRIVERS);
            List<JdbcDriver> runtimeDrivers = control.getContext().get(JdbcDriverFunctions.RUNTIME_DRIVERS);
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
            Collections.sort(drivers, (driver1, driver2) -> driver1.getName().compareTo(driver2.getName()));
            control.getContext().set(DRIVERS, drivers);
            control.proceed();
        }
    }
}
