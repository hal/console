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

import java.util.List;

import com.google.common.collect.FluentIterable;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.client.runtime.domain.Server;
import org.jboss.hal.client.runtime.domain.TopologyFunctions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Set of functions to read the installed JDBC drivers.
 *
 * @author Harald Pehl
 */
public class JdbcDriverFunctions {

    public static final String DRIVERS = "jdbcDriverFunctions.drivers";


    /**
     * Reads the JDBC drivers from {@code /{selected.profile}/subsystem=datasource/jdbc-driver=*} and puts the result
     * as {@code List<JdbcDriver>} under the key {@link JdbcDriverFunctions#DRIVERS} into the context.
     */
    public static class Read implements Function<FunctionContext> {

        private final StatementContext statementContext;
        private final Dispatcher dispatcher;

        public Read(final StatementContext statementContext, final Dispatcher dispatcher) {
            this.statementContext = statementContext;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress address = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                    .param(CHILD_TYPE, JDBC_DRIVER).build();
            dispatcher.executeInFunction(control, operation, result -> {
                //noinspection Guava
                List<JdbcDriver> drivers = FluentIterable.from(result.asPropertyList())
                        .transform(JdbcDriver::new)
                        .toList();
                control.getContext().set(DRIVERS, drivers);
                control.proceed();
            });
        }
    }


    /**
     * Populates the jdbc drivers returned by {@link Read} with runtime data provided by the {@code
     * :installed-drivers-list} operation. Needs both the list of drivers and a list of running servers.
     */
    public static class AddRuntimeInfo implements Function<FunctionContext> {

        private final Dispatcher dispatcher;

        public AddRuntimeInfo(final Dispatcher dispatcher) {this.dispatcher = dispatcher;}

        @Override
        public void execute(final Control<FunctionContext> control) {
            List<JdbcDriver> drivers = control.getContext().get(DRIVERS);
            List<Server> servers = control.getContext().get(TopologyFunctions.SERVERS);
            if (servers != null && !servers.isEmpty()) {
                //noinspection Guava
                List<Operation> operations = FluentIterable.from(servers)
                        .transform(server -> {
                            ResourceAddress address = new ResourceAddress().add(HOST, server.getHost())
                                    .add(SERVER, server.getName())
                                    .add(SUBSYSTEM, DATASOURCES);
                            return new Operation.Builder("installed-drivers-list", address).build(); //NON-NLS
                        })
                        .toList();
                dispatcher.executeInFunction(control, new Composite(operations), (CompositeResult result) -> {
                    for (ModelNode step : result) {
                        if (!step.isFailure()) {
                            // for each server we get the List of installed drivers
                            for (ModelNode modelNode : step.get(RESULT).asList()) {
                                String driverName = modelNode.get(DRIVER_NAME).asString();
                                JdbcDriver existingDriver = findDriver(drivers, driverName);
                                if (existingDriver != null) {
                                    existingDriver.reset(modelNode);
                                }
                            }
                        }
                    }
                    control.proceed();
                });

            } else {
                control.proceed();
            }
        }

        private JdbcDriver findDriver(List<JdbcDriver> drivers, String name) {
            for (JdbcDriver driver : drivers) {
                if (driver.getName().equals(name)) {
                    return driver;
                }
            }
            return null;
        }
    }
}
