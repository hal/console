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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.runtime.TopologySteps;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Step;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Set of steps to read the installed JDBC drivers. */
class JdbcDriverSteps {

    private static final String CONFIGURATION_DRIVERS = "jdbcDriverFunctions.configurationDrivers";
    private static final String RUNTIME_DRIVERS = "jdbcDriverFunctions.runtimeDrivers";
    static final String DRIVERS = "jdbcDriverFunctions.drivers";


    /**
     * Reads the JDBC drivers from {@code /{selected.profile}/subsystem=datasource/jdbc-driver=*} and puts the result
     * as {@code List<JdbcDriver>} under the key {@link JdbcDriverSteps#CONFIGURATION_DRIVERS} into the context.
     */
    static class ReadConfiguration implements Step<FlowContext> {

        private final CrudOperations crud;

        ReadConfiguration(CrudOperations crud) {
            this.crud = crud;
        }

        @Override
        public void execute(FlowContext context, Control control) {
            crud.readChildren(DATA_SOURCE_SUBSYSTEM_TEMPLATE, JDBC_DRIVER, children -> {
                List<JdbcDriver> drivers = children.stream()
                        .map(JdbcDriver::new)
                        .collect(toList());
                context.set(CONFIGURATION_DRIVERS, drivers);
                control.proceed();
            });
        }
    }


    /**
     * Reads the JDBC drivers from a list of running servers which are expected in the context under the key
     * {@link TopologySteps#RUNNING_SERVERS}. The drivers are read using the {@code :installed-drivers-list}
     * operation. Stores the result as {@code List<JdbcDriver>} under the key {@link
     * JdbcDriverSteps#RUNTIME_DRIVERS} into the context.
     */
    static class ReadRuntime implements Step<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        ReadRuntime(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(FlowContext context, Control control) {
            if (environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress().add(SUBSYSTEM, DATASOURCES);
                Operation operation = new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                dispatcher.executeInFlow(control, operation, result -> {
                    List<JdbcDriver> drivers = result.asList().stream()
                            .map(modelNode -> new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode))
                            .collect(toList());
                    context.set(RUNTIME_DRIVERS, drivers);
                    control.proceed();
                });

            } else {
                List<Server> servers = context.get(TopologySteps.RUNNING_SERVERS);
                if (servers != null && !servers.isEmpty()) {
                    List<Operation> operations = servers.stream()
                            .map(server -> {
                                ResourceAddress address = server.getServerAddress().add(SUBSYSTEM, DATASOURCES);
                                return new Operation.Builder(address, "installed-drivers-list").build(); //NON-NLS
                            })
                            .collect(toList());
                    dispatcher.executeInFlow(control, new Composite(operations), (CompositeResult result) -> {
                        List<JdbcDriver> drivers = new ArrayList<>();
                        for (ModelNode step : result) {
                            if (!step.isFailure()) {
                                // for each server we get the list of installed drivers
                                for (ModelNode modelNode : step.get(RESULT).asList()) {
                                    drivers.add(new JdbcDriver(modelNode.get(DRIVER_NAME).asString(), modelNode));
                                }
                            }
                        }
                        context.set(RUNTIME_DRIVERS, drivers);
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
     * Stores the result as {@code List<JdbcDriver>} under the key {@link JdbcDriverSteps#DRIVERS} into the
     * context.
     */
    static class CombineDriverResults implements Step<FlowContext> {

        @Override
        public void execute(FlowContext context, Control control) {
            Map<String, JdbcDriver> map = new HashMap<>();
            List<JdbcDriver> configDrivers = context.get(JdbcDriverSteps.CONFIGURATION_DRIVERS);
            List<JdbcDriver> runtimeDrivers = context.get(JdbcDriverSteps.RUNTIME_DRIVERS);
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
            control.proceed();
        }
    }
}
