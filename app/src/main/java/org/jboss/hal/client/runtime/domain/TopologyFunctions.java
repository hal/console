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
package org.jboss.hal.client.runtime.domain;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.FluentIterable;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.client.runtime.Server;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Set of functions to read runtime data like running server of a specific server group.
 *
 * @author Harald Pehl
 */
public class TopologyFunctions {

    /**
     * The list of running servers as {@code List&lt;Server&gt;} provided by the {@link RunningServersOfProfile}
     * function
     */
    public static final String RUNNING_SERVERS = "topologyFunctions.runningServers";


    /**
     * Returns a list of running servers which belong to the specified profile. Stores the list in the context under
     * the key {@link TopologyFunctions#RUNNING_SERVERS}. Stores an empty list when running in standalone mode.
     */
    public static class RunningServersOfProfile implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String profile;

        public RunningServersOfProfile(final Environment environment, final Dispatcher dispatcher,
                final String profile) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.profile = profile;
        }

        @Override
        @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                List<Server> servers = Collections.emptyList();
                control.getContext().set(RUNNING_SERVERS, servers);
                control.proceed();

            } else {
                ResourceAddress address = new ResourceAddress().add(HOST, "*").add(SERVER, "*");

                ModelNode select = new ModelNode().add(HOST).add("launch-type").add(NAME).add("profile-name")
                        .add("running-mode").add(SERVER_GROUP).add("server-state").add("suspend-state").add("uuid");

                Operation operation = new Operation.Builder(QUERY, address)
                        .param(SELECT, select)
                        .param(WHERE, new ModelNode().set("profile-name", profile))
                        .build();

                dispatcher.executeInFunction(control, operation, result -> {
                    //noinspection Guava
                    List<Server> servers = FluentIterable.from(result.asList())
                            .filter(modelNode -> !modelNode.isFailure())
                            .transform(modelNode -> new Server(modelNode.get(RESULT)))
                            .toList();
                    control.getContext().set(RUNNING_SERVERS, servers);
                    control.proceed();
                });
            }
        }
    }
}