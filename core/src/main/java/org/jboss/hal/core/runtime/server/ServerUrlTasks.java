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
package org.jboss.hal.core.runtime.server;

import java.util.Optional;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import rx.Completable;

import static java.util.Comparator.comparing;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Umbrella class for functions to read the server URL */
class ServerUrlTasks {

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
    static class ReadSocketBindingGroup implements Task<FlowContext> {

        private final boolean standalone;
        private final String serverGroup;
        private final Dispatcher dispatcher;

        ReadSocketBindingGroup(boolean standalone, String serverGroup, Dispatcher dispatcher) {
            this.standalone = standalone;
            this.serverGroup = serverGroup;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            if (standalone) {
                Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            if (result.asList().isEmpty()) {
                                throw new RuntimeException(
                                        "ReadSocketBindingGroup: No socket binding groups defined"); //NON-NLS
                            } else {
                                String sbg = result.asList().get(0).asString();
                                context.set(SOCKET_BINDING_GROUP_KEY, sbg);
                            }
                        })
                        .toCompletable();
            } else {
                ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup);
                Operation operation = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                        .param(NAME, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> context.set(SOCKET_BINDING_GROUP_KEY, result.asString()))
                        .toCompletable();
            }
        }
    }


    /**
     * Checks whether there's a {@code http} or {@code https} socket binding and puts the name of that socket binding
     * into the context. Aborts otherwise. Expects the name of the socket binding group in the context.
     */
    static class ReadSocketBinding implements Task<FlowContext> {

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
        public Completable call(FlowContext context) {
            Completable completable;

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
                completable = dispatcher.execute(operation).doOnSuccess(result -> {
                    Optional<Property> optional = result.asPropertyList().stream()
                            .filter(p -> p.getName().startsWith("http"))
                            .filter(p -> p.getValue().hasDefined(BOUND))
                            .filter(p -> p.getValue().get(BOUND).asBoolean())
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
                            context.set(URL_KEY, new ServerUrl(url.toString(), false));
                        } else {
                            throw new RuntimeException(
                                    "ReadSocketBinding: No address defined for " + sbg + " / " + property.getName());
                        }
                    } else {
                        throw new RuntimeException("ReadSocketBinding: No http(s) socket binding defined for " + sbg);
                    }
                }).toCompletable();
            } else {
                completable = Completable.error(
                        new RuntimeException("ReadSocketBinding: No socket binding group in context"));
            }
            return completable;
        }
    }

    private ServerUrlTasks() {
    }
}
