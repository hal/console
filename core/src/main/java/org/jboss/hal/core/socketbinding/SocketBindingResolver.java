/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.core.socketbinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class SocketBindingResolver implements ResolveSocketBindingEvent.ResolveSocketBindingHandler {

    private static final AddressTemplate SOCKET_BINDING_GROUP_TEMPLATE = AddressTemplate.of(
            "/socket-binding-group=*/socket-binding=*");
    @NonNls private static final Logger logger = LoggerFactory.getLogger(SocketBindingResolver.class);

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Environment environment;
    private final Resources resources;
    private StatementContext statementContext;

    @Inject
    public SocketBindingResolver(final EventBus eventBus, final Environment environment, final Dispatcher dispatcher,
            final Resources resources, StatementContext statementContext) {
        this.eventBus = eventBus;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.statementContext = statementContext;

        eventBus.addHandler(ResolveSocketBindingEvent.getType(), this);
    }

    @Override
    public void onResolveSocketBinding(ResolveSocketBindingEvent event) {
        // logger.info("  SocketBindingResolver.onResolveSocketBinding");
        new SocketBindingPortDialog(this, environment, resources).showAndResolve(event.getSocketBinding());
    }

    void resolve(final String socketBinding, final AsyncCallback<String> callback) {
        // logger.debug("Resolving socket-binding {}", socketBinding);
        if (environment.isStandalone()) {

            // read the socket-binding-group name
            Operation readSbgOp = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                    .build();
            dispatcher.execute(readSbgOp, result -> {
                // logger.info(" SocketBindingResolver sbg: {}", result);
                String sbg = result.asList().get(0).asString();
                // String httpsBinding = context.model.get(SECURE_SOCKET_BINDING).asString();
                ResourceAddress address = SOCKET_BINDING_GROUP_TEMPLATE.resolve(statementContext, sbg, socketBinding);
                Operation readSocketBindingOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_RUNTIME, true)
                        .param(RESOLVE_EXPRESSIONS, true)
                        .build();
                // logger.info(" SocketBindingResolver read socket binding op: {}", readSocketBindingOp);
                dispatcher.execute(readSocketBindingOp,
                        response -> {
                            // logger.info("  response: {}", response);
                            String port;
                            // multicast ports are not displayed as bound-port attribute, so use the multicast-port
                            if (response.hasDefined(MULTICAST_PORT)) {
                                port = response.get(MULTICAST_PORT).asString();
                            } else {
                                boolean bound = response.get(BOUND).asBoolean();
                                if (bound) {
                                    port = response.get(BOUND_PORT).asString();
                                } else {
                                    port = response.get(PORT).asString();
                                }
                            }
                            callback.onSuccess(port);
                        },
                        (op1, failure) -> callback.onFailure(new RuntimeException(failure)),
                        (op2, exception) -> callback.onFailure(exception));
            });
        } else {
            // Operation operation = new Operation.Builder(ResourceAddress.root(), RESOLVE_EXPRESSION_ON_DOMAIN)
            //         .param(EXPRESSION, expression.toString())
            //         .build();
            // dispatcher.executeDMR(operation,
            //         (res) -> callback.onSuccess(parseServerGroups(res.get(SERVER_GROUPS))),
            //         (op1, failure) -> callback.onFailure(new RuntimeException(failure)),
            //         (op2, exception) -> callback.onFailure(exception));
        }
    }

    private Map<String, String> parseServerGroups(ModelNode serverGroups) {
        Map<String, String> values = new HashMap<>();
        if (serverGroups.isDefined()) {
            List<Property> groups = serverGroups.asPropertyList();
            for (Property serverGroup : groups) {
                List<Property> hosts = serverGroup.getValue().get(HOST).asPropertyList();
                for (Property host : hosts) {
                    List<Property> servers = host.getValue().asPropertyList();
                    for (Property server : servers) {
                        values.put(server.getName(),
                                server.getValue().get(RESPONSE).get(RESULT).asString()
                        );
                    }
                }
            }
        }
        return values;
    }
}
