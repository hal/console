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

import java.util.List;

import com.google.common.collect.FluentIterable;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Set of functions to read runtime data like running server of a specific server group
 *
 * @author Harald Pehl
 */
public class TopologyFunctions {

    private static final String SERVER_GROUPS = "topologyFunctions.serverGroups";
    public static final String SERVERS = "topologyFunctions.servers";


    /**
     * Returns all server groups based on the specified profile. Stores the result as a list of strings in the context
     * under the key {@link TopologyFunctions#SERVER_GROUPS}.
     */
    public static class ServerGroupsOfProfile implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String profile;

        public ServerGroupsOfProfile(final Dispatcher dispatcher, final String profile) {
            this.dispatcher = dispatcher;
            this.profile = profile;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(QUERY, new ResourceAddress().add(SERVER_GROUP, "*"))
                    .param(SELECT, new String[]{PROFILE}) // we need to select something
                    .param(WHERE, new ModelNode().set(PROFILE, profile))
                    .build();
            dispatcher.executeInFunction(control, operation, result -> {
                //noinspection Guava
                List<String> serverGroups = FluentIterable.from(result.asList())
                        .filter(modelNode -> !modelNode.isFailure())
                        .transform(modelNode -> new ResourceAddress(modelNode.get(ADDRESS)))
                        .transform(ResourceAddress::lastValue)
                        .toList();
                control.getContext().set(SERVER_GROUPS, serverGroups);
                control.proceed();
            });
        }
    }


    /**
     * Returns a list of running servers which belong to a specific server group. Expects to find the list in the
     * context under the key {@link TopologyFunctions#SERVER_GROUPS}. Stores the list in the context under the key
     * {@link TopologyFunctions#SERVERS}.
     */
    public static class RunningServersOfGroupsInContext implements Function<FunctionContext> {

        private final Dispatcher dispatcher;

        public RunningServersOfGroupsInContext(final Dispatcher dispatcher) {this.dispatcher = dispatcher;}

        @Override
        @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
        public void execute(final Control<FunctionContext> control) {
            // select important attributes
            ModelNode select = new ModelNode().add("host").add("launch-type").add(NAME).add("profile-name")
                    .add("running-mode").add(SERVER_GROUP).add("server-state").add("suspend-state").add("uuid");
            ResourceAddress address = new ResourceAddress().add(HOST, "*").add(SERVER, "*");

            List<String> serverGroups = control.getContext().get(SERVER_GROUPS);
            if (serverGroups.size() > 1) {
                // TODO Setup and process a composite with a query operation for each host

            } else {
                Operation.Builder builder = new Operation.Builder(QUERY, address)
                        .param(SELECT, select);
                if (serverGroups.isEmpty()) {
                    builder.param(WHERE, new ModelNode().set("running-mode",
                            "NORMAL")); // pseudo query to get results even when serverGroups.isEmpty()
                } else {
                    builder.param(WHERE, new ModelNode().set(SERVER_GROUP, serverGroups.get(0)));
                }
                dispatcher.executeInFunction(control, builder.build(), result -> {
                    //noinspection Guava
                    List<Server> servers = FluentIterable.from(result.asList())
                            .filter(modelNode -> !modelNode.isFailure())
                            .transform(modelNode -> new Server(modelNode.get(RESULT)))
                            .toList();
                    control.getContext().set(SERVERS, servers);
                    control.proceed();
                });
            }
        }
    }
}
