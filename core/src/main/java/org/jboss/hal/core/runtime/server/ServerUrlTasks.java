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
package org.jboss.hal.core.runtime.server;

import java.util.Optional;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;

import elemental2.promise.Promise;

import static java.util.Comparator.comparing;

import static org.jboss.hal.dmr.ModelDescriptionConstants.BOUND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BOUND_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;

/** Umbrella class for functions to read the server URL */
class ServerUrlTasks {

    private static final String SOCKET_BINDING_GROUP_KEY = "socket-binding-group";
    static final String SERVER_URL_KEY = "serverUrl";

    /**
     * Reads the socket binding group which is used to look for a {@code http} or {@code https} socket binding resource.
     *
     * <p>
     * In standalone mode it uses the first {@code socket-binding-group} child resource of the root resource. In domain mode it
     * reads the {@code socket-binding-group} attribute of the selected group in the statement context.
     * </p>
     */
    static final class ReadSocketBindingGroup implements Task<FlowContext> {

        private final boolean standalone;
        private final String serverGroup;
        private final Dispatcher dispatcher;

        ReadSocketBindingGroup(boolean standalone, String serverGroup, Dispatcher dispatcher) {
            this.standalone = standalone;
            this.serverGroup = serverGroup;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            if (standalone) {
                Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            if (result.asList().isEmpty()) {
                                return context.reject("ReadSocketBindingGroup: No socket binding groups defined");
                            } else {
                                String sbg = result.asList().get(0).asString();
                                return context.resolve(SOCKET_BINDING_GROUP_KEY, sbg);
                            }
                        });
            } else {
                ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup);
                Operation operation = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                        .param(NAME, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> context.resolve(SOCKET_BINDING_GROUP_KEY, result.asString()));
            }
        }
    }

    /**
     * Checks whether there's a {@code http} or {@code https} socket binding and puts an instance of {@link ServerUrl} into the
     * context. Aborts otherwise. Expects the name of the socket binding group in the context.
     */
    static final class ReadSocketBinding implements Task<FlowContext> {

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
        public Promise<FlowContext> apply(final FlowContext context) {
            String sbg = context.get(SOCKET_BINDING_GROUP_KEY);
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
                return dispatcher.execute(operation).then(result -> {
                    Optional<Property> optional = result.asPropertyList().stream()
                            .filter(p -> p.getName().startsWith("http"))
                            .filter(p -> p.getValue().hasDefined(BOUND))
                            .filter(p -> p.getValue().get(BOUND).asBoolean()).min(comparing(Property::getName));
                    if (optional.isPresent()) {
                        Property property = optional.get();
                        if (property.getValue().hasDefined(BOUND_ADDRESS)) {
                            return context.resolve(SERVER_URL_KEY, ServerUrl.fromManagementModel(property));
                        } else {
                            return context
                                    .reject("ReadSocketBinding: No address defined for " + sbg + " / " + property.getName());
                        }
                    } else {
                        return context.reject("ReadSocketBinding: No http(s) socket binding defined for " + sbg);
                    }
                });
            } else {
                return context.reject("ReadSocketBinding: No socket binding group in context");
            }
        }
    }

    private ServerUrlTasks() {
    }
}
