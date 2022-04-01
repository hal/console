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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.NonProgressingOperationEvent;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FIND_NON_PROGRESSING_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOSTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHERE;
import static org.jboss.hal.flow.Flow.series;

public final class FindNonProgressingTask implements Task<FlowContext> {

    private static final String EQ = "=";
    private static final String WILDCARD = "*";
    private static final AddressTemplate MGMT_OPERATIONS_TEMPLATE = AddressTemplate
            .of("/core-service=management/service=management-operations");

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final StatementContext statementContext;
    private final Provider<Progress> progress;

    public FindNonProgressingTask(EventBus eventBus, Dispatcher dispatcher, Environment environment,
            StatementContext statementContext, Provider<Progress> progress) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.statementContext = statementContext;
        this.progress = progress;
    }

    @Override
    public Promise<FlowContext> apply(final FlowContext context) {
        if (environment.isStandalone()) {
            ResourceAddress address = MGMT_OPERATIONS_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, FIND_NON_PROGRESSING_OPERATION).build();

            return dispatcher.execute(operation)
                    .then(result -> {
                        boolean hasNonProgressingOp = result != null && result.isDefined();
                        eventBus.fireEvent(new NonProgressingOperationEvent(hasNonProgressingOp));
                        return Promise.resolve(context);
                    });
        } else {

            // return running hosts, to later call a find-non-progressing-operation on each host
            Task<FlowContext> hostsTask = c -> {
                ResourceAddress address = new ResourceAddress();
                Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, HOST)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            List<String> hosts = result.asList().stream()
                                    .map(ModelNode::asString)
                                    .collect(toList());
                            return Promise.resolve(c.set(HOSTS, hosts));
                        });
            };

            // return running servers, to later call a find-non-progressing-operation on each runtime server
            Task<FlowContext> serversTask = c -> {
                // /host=*/server=*:query(select=[host,name],where={server-state=running})
                ResourceAddress address = new ResourceAddress()
                        .add(HOST, WILDCARD)
                        .add(SERVER, WILDCARD);
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, new ModelNode().add(HOST).add(NAME))
                        .param(WHERE, new ModelNode().set(SERVER_STATE, "running"))
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            List<String> servers = Collections.emptyList();
                            if (result != null && result.isDefined()) {
                                servers = result.asList().stream()
                                        .map(r -> hostServerAddress(r.get(RESULT)))
                                        .collect(Collectors.toList());
                            }
                            return Promise.resolve(c.set("servers", servers));
                        });
            };

            // call find-non-progressing-operation on each host and server
            Task<FlowContext> findNonProgressingTask = c -> {

                List<String> hosts = c.get(HOSTS);
                List<String> servers = c.get("servers");

                Composite composite = new Composite();
                for (String host : hosts) {
                    ResourceAddress address = new ResourceAddress().add(HOST, host)
                            .add(CORE_SERVICE, MANAGEMENT)
                            .add(SERVICE, MANAGEMENT_OPERATIONS);
                    Operation operation = new Operation.Builder(address, FIND_NON_PROGRESSING_OPERATION).build();
                    composite.add(operation);
                }
                if (!servers.isEmpty()) {
                    for (String server : servers) {
                        ResourceAddress address = AddressTemplate.of(server)
                                .append(MGMT_OPERATIONS_TEMPLATE)
                                .resolve(statementContext);
                        Operation operation = new Operation.Builder(address, FIND_NON_PROGRESSING_OPERATION).build();
                        composite.add(operation);
                    }
                }
                return dispatcher.execute(composite)
                        .then(result -> {
                            boolean nonProgressingOp = false;
                            for (ModelNode r : result) {
                                ModelNode findResult = r.get(RESULT);
                                if (findResult != null && findResult.isDefined()) {
                                    nonProgressingOp = true;
                                    break;
                                }
                            }
                            return Promise.resolve(c.set("nonProgressingOp", nonProgressingOp));
                        });
            };

            return series(new FlowContext(progress.get()), asList(hostsTask, serversTask, findNonProgressingTask))
                    .then(c -> {
                        boolean nonProgressingOp = c.get("nonProgressingOp");
                        eventBus.fireEvent(new NonProgressingOperationEvent(nonProgressingOp));
                        return Promise.resolve(context);
                    });
        }
    }

    private String hostServerAddress(ModelNode model) {
        return HOST + EQ + model.get(HOST).asString() + "/" + SERVER + EQ + model.get(NAME).asString();
    }
}
