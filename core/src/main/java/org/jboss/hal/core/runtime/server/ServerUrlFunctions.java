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
package org.jboss.hal.core.runtime.server;

import java.util.Optional;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import static java.util.Comparator.comparing;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Umbrella class for functions to read the server URL */
class ServerUrlFunctions {

    private static final String SOCKET_BINDING_GROUP_KEY = "socket-binding-group";
    static final String URL_KEY = "url";


    /**
     * Reads the socket binding group which is used to look for a {@code http} or {@code https} socket binding
     * resource.
     *
     * <p>In standalone mode it uses the first {@code socket-binding-group} child resource of the root resource. In
     * domain mode it reads the {@code socket-binding-group} attribute of the selected group in the statement
     * context.</p>
     */
    static class ReadSocketBindingGroup implements Function<FunctionContext> {

        private final boolean standalone;
        private final String serverGroup;
        private final Dispatcher dispatcher;

        ReadSocketBindingGroup(boolean standalone, String serverGroup, Dispatcher dispatcher) {
            this.standalone = standalone;
            this.serverGroup = serverGroup;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(Control<FunctionContext> control) {
            if (standalone) {
                Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                        .build();
                dispatcher.executeInFunction(control, operation, result -> {
                    if (result.asList().isEmpty()) {
                        control.getContext().failed("ReadSocketBindingGroup: No socket binding groups defined");
                        control.abort();
                    } else {
                        String sbg = result.asList().get(0).asString();
                        control.getContext().set(SOCKET_BINDING_GROUP_KEY, sbg);
                        control.proceed();
                    }
                });

            } else {
                ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup);
                Operation operation = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                        .param(NAME, SOCKET_BINDING_GROUP)
                        .build();
                dispatcher.executeInFunction(control, operation, result -> {
                    control.getContext().set(SOCKET_BINDING_GROUP_KEY, result.asString());
                    control.proceed();
                });
            }
        }
    }


    /**
     * Checks whether there's a {@code http} or {@code https} socket binding and puts the name of that socket binding
     * into the context. Aborts otherwise. Expects the name of the socket binding group in the context.
     */
    static class ReadSocketBinding implements Function<FunctionContext> {

        private final boolean standalone;
        private final String host;
        private final String server;
        private final Dispatcher dispatcher;

        ReadSocketBinding(boolean standalone, String host, String server, Dispatcher dispatcher) {
            this.standalone = standalone;
            this.host = host;
            this.server = server;
            this.dispatcher = dispatcher;
        }

        @Override
        @SuppressWarnings("HardCodedStringLiteral")
        public void execute(Control<FunctionContext> control) {
            String sbg = control.getContext().get(SOCKET_BINDING_GROUP_KEY);
            if (sbg != null) {
                ResourceAddress address = new ResourceAddress();
                if (!standalone) {
                    address.add(HOST, host).add(SERVER, server);
                }
                address.add(SOCKET_BINDING_GROUP, sbg);
                Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                dispatcher.executeInFunction(control, operation, result -> {
                    Optional<Property> optional = result.asPropertyList().stream()
                            .filter(p -> p.getName().startsWith("http"))
                            .sorted(comparing(Property::getName))
                            .findFirst();
                    if (optional.isPresent()) {
                        Property property = optional.get();
                        if (property.getValue().hasDefined(BOUND_ADDRESS)) {
                            StringBuilder url = new StringBuilder();
                            url.append(optional.get().getName())
                                    .append("://")
                                    .append(optional.get().getValue().get(BOUND_ADDRESS).asString());
                            if (property.getValue().hasDefined(BOUND_PORT)) {
                                url.append(":").append(property.getValue().get(BOUND_PORT).asInt());
                            }
                            control.getContext().set(URL_KEY, new ServerUrl(url.toString(), false));
                            control.proceed();
                        } else {
                            control.getContext()
                                    .failed("ReadSocketBinding: No address defined for " + sbg + " / " + property.getName());
                            control.abort();
                        }
                    } else {
                        control.getContext().failed("ReadSocketBinding: No http(s) socket binding defined for " + sbg);
                        control.abort();
                    }
                });
            } else {
                control.getContext().failed("ReadSocketBinding: No socket binding group in context");
                control.abort();
            }
        }
    }
}
