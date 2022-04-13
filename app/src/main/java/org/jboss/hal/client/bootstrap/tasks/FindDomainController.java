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

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.bootstrap.tasks.ReadHostNames.HOST_NAMES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MASTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

public final class FindDomainController implements Task<FlowContext> {

    private static final Logger logger = LoggerFactory.getLogger(FindDomainController.class);

    private final Dispatcher dispatcher;
    private final Environment environment;

    @Inject
    public FindDomainController(Dispatcher dispatcher, Environment environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public Promise<FlowContext> apply(final FlowContext context) {
        if (!environment.isStandalone()) {
            List<String> hosts = context.get(HOST_NAMES);
            if (hosts != null) {
                List<Task<FlowContext>> hostTasks = hosts.stream()
                        .map(host -> {
                            ResourceAddress address = new ResourceAddress().add(HOST, host);
                            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build();
                            return (Task<FlowContext>) c -> dispatcher.execute(operation).then(result -> {
                                boolean master = result.get(MASTER).asBoolean();
                                if (master) {
                                    String name = result.get(NAME).asString();
                                    environment.setDomainController(name);
                                    logger.info("Found domain controller: {}", name);
                                }
                                return Promise.resolve(c);
                            });
                        })
                        .collect(toList());
                return Flow.series(context, hostTasks);
            } else {
                return Promise.resolve(context);
            }
        } else {
            return Promise.resolve(context);
        }
    }
}
