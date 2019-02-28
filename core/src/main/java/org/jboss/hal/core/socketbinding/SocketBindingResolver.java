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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import rx.Completable;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.runtime.RunningState.RUNNING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class SocketBindingResolver implements ResolveSocketBindingEvent.ResolveSocketBindingHandler {

    private static final AddressTemplate SOCKET_BINDING_GROUP_TEMPLATE = AddressTemplate.of(
            "/socket-binding-group=*/socket-binding=*");

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Environment environment;
    private final Resources resources;
    private StatementContext statementContext;
    private Provider<Progress> progress;

    @Inject
    public SocketBindingResolver(final EventBus eventBus, final Environment environment, final Dispatcher dispatcher,
            final Resources resources, StatementContext statementContext, @Footer Provider<Progress> progress) {
        this.eventBus = eventBus;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.statementContext = statementContext;
        this.progress = progress;

        eventBus.addHandler(ResolveSocketBindingEvent.getType(), this);
    }

    @Override
    public void onResolveSocketBinding(ResolveSocketBindingEvent event) {
        new SocketBindingPortDialog(this, environment, resources).showAndResolve(event.getSocketBinding());
    }

    void resolve(final String socketBinding, final AsyncCallback<ModelNode> callback) {
        if (environment.isStandalone()) {
            // read the socket-binding-group name
            Operation readSbgOp = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                    .build();
            dispatcher.execute(readSbgOp, result -> {
                String sbg = result.asList().get(0).asString();
                ResourceAddress address = SOCKET_BINDING_GROUP_TEMPLATE.resolve(statementContext, sbg, socketBinding);
                Operation readSocketBindingOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_RUNTIME, true)
                        .param(RESOLVE_EXPRESSIONS, true)
                        .build();
                dispatcher.execute(readSocketBindingOp,
                        response -> {
                            ModelNode port;
                            // multicast ports are not displayed as bound-port attribute, so use the multicast-port
                            if (response.hasDefined(MULTICAST_PORT)) {
                                port = response.get(MULTICAST_PORT);
                            } else {
                                boolean bound = response.get(BOUND).asBoolean();
                                if (bound) {
                                    port = response.get(BOUND_PORT);
                                } else {
                                    port = response.get(PORT);
                                }
                            }
                            callback.onSuccess(port);
                        },
                        (op1, failure) -> callback.onFailure(new RuntimeException(failure)),
                        (op2, exception) -> callback.onFailure(exception));
            });
        } else {
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(flowContext -> {
                // /host=*/server=*:query(select=[host,name],where={suspend-state=RUNNING, profile-name=full-ha})
                ModelNode select = new ModelNode();
                select.add(HOST).add(NAME);
                ModelNode where = new ModelNode();
                where.get(SUSPEND_STATE).set(RUNNING.name());
                where.get(PROFILE_NAME).set(statementContext.selectedProfile());
                ResourceAddress address = new ResourceAddress()
                        .add(HOST, "*")
                        .add(SERVER, "*");
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, select)
                        .param(WHERE, where)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            if (result.asList().size() > 0) {
                                // aggregate as a list of host/server strings
                                List<String> hostsServers = result.asList().stream()
                                        .filter(res -> res.get(OUTCOME).asString().equals(SUCCESS)
                                                && res.get(RESULT).isDefined())
                                        .map(r -> r.get(RESULT).get(HOST).asString() + "/"
                                                + r.get(RESULT).get(NAME).asString())
                                        .collect(toList());
                                flowContext.push(hostsServers);
                            }
                        })
                        .toCompletable();
            });

            tasks.add(flowContext -> {
                if (flowContext.emptyStack()) {
                    return Completable.complete();
                } else {
                    List<String> hostsServers = flowContext.pop();
                    Composite composite = new Composite();
                    hostsServers.forEach(hostServer -> {
                        int idx = hostServer.indexOf("/");
                        String host = hostServer.substring(0, idx);
                        String server = hostServer.substring(idx + 1);
                        ResourceAddress address = new ResourceAddress()
                                .add(HOST, host)
                                .add(SERVER, server)
                                .add(SOCKET_BINDING_GROUP, "*")
                                .add(SOCKET_BINDING, socketBinding);
                        Operation operation = new Operation.Builder(address, QUERY)
                                .param(INCLUDE_RUNTIME, true)
                                .param(RESOLVE_EXPRESSIONS, true)
                                .build();
                        composite.add(operation);
                    });
                    return dispatcher.execute(composite)
                            .doOnSuccess((CompositeResult compositeResult) -> {
                                ModelNode hostServerPorts = new ModelNode();
                                for (ModelNode res: compositeResult) {
                                    if (res.hasDefined(OUTCOME) && res.get(OUTCOME).asString().equals(SUCCESS)) {
                                        // result is a list of socket-binding-group
                                        // there is only one socket-binding-group per server, it is safe to get the first one
                                        ModelNode sbRes = res.get(RESULT).asList().get(0);
                                        StringBuilder hostServer = new StringBuilder();
                                        sbRes.get(ADDRESS).asPropertyList().forEach(p -> {
                                            if (HOST.equals(p.getName())) {
                                                hostServer.append(p.getValue().asString());
                                            }
                                            if (SERVER.equals(p.getName())) {
                                                hostServer.append("/").append(p.getValue().asString());
                                            }
                                        });
                                        ModelNode sbNode = sbRes.get(RESULT);
                                        String port;
                                        // multicast ports are not displayed as bound-port attribute, so use the multicast-port
                                        if (sbNode.hasDefined(MULTICAST_PORT)) {
                                            port = sbNode.get(MULTICAST_PORT).asString();
                                        } else {
                                            boolean bound = sbNode.get(BOUND).asBoolean();
                                            if (bound) {
                                                port = sbNode.get(BOUND_PORT).asString();
                                            } else {
                                                port = sbNode.get(PORT).asString();
                                            }
                                        }
                                        hostServerPorts.get(hostServer.toString()).set(port);
                                    }
                                }
                                flowContext.push(hostServerPorts);
                            })
                            .toCompletable();
                }
            });

            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                        @Override
                        public void onSuccess(FlowContext flowContext) {
                            if (flowContext.emptyStack()) {
                                callback.onFailure(new RuntimeException(
                                        resources.messages().resolveSocketBindingNoRunningServer()));
                            } else {
                                ModelNode hostServerPorts = flowContext.pop();
                                callback.onSuccess(hostServerPorts);
                            }
                        }
                    });
        }
    }
}
