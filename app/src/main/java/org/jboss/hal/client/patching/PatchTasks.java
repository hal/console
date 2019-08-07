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
package org.jboss.hal.client.patching;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import rx.Completable;
import rx.Single;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

final class PatchTasks {

    /**
     * Reads the hosts and its patches.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link TopologyTasks#HOSTS}: The ordered list of hosts with the domain controller as first element.
     * Each host contains its patches.</li>
     * </ul>
     */
    static List<Task<FlowContext>> patches(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>(TopologyTasks.hosts(environment, dispatcher));
        tasks.add(new Patches(dispatcher));
        return tasks;
    }


    private static class Patches implements Task<FlowContext> {

        private final Dispatcher dispatcher;

        Patches(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            List<Host> hosts = context.get(TopologyTasks.HOSTS);

            if (hosts != null && !hosts.isEmpty()) {
                List<Completable> completables = hosts.stream()
                        .filter(host -> host.isAlive() && !host.isStarting() && host.isRunning()) // alive is not enough here!
                        .map(host -> {
                            ResourceAddress address = host.getAddress().add(CORE_SERVICE, PATCHING);
                            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                    .param(INCLUDE_RUNTIME, true)
                                    .param(RECURSIVE, true)
                                    .build();
                            return dispatcher.execute(operation)
                                    .doOnSuccess(result -> host.get(CORE_SERVICE_PATCHING).set(result))
                                    .onErrorResumeNext(error -> Single.just(new ModelNode()))
                                    .toCompletable();
                        })
                        .collect(toList());
                completable = Completable.concat(completables);
            }
            return completable;
        }
    }


    private PatchTasks() {
    }
}
