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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Deployment related functions
 *
 * @author Harald Pehl
 */
public class DeploymentFunctions {

    /**
     * Loads the contents form the content repository and pushes a {@code List&lt;Content&gt;} onto the context stack.
     */
    public static class LoadContentAssignments implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String serverGroup;

        public LoadContentAssignments(final Dispatcher dispatcher) {
            this(dispatcher, "*");
        }

        /**
         * @param dispatcher  the dispatcher
         * @param serverGroup use "*" to find assignments on any server group
         */
        public LoadContentAssignments(final Dispatcher dispatcher, final String serverGroup) {
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation contentOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                    .param(CHILD_TYPE, DEPLOYMENT)
                    .build();
            ResourceAddress address = new ResourceAddress()
                    .add(SERVER_GROUP, serverGroup)
                    .add(DEPLOYMENT, "*");
            Operation assignmentsOp = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();

            dispatcher.executeInFunction(control, new Composite(contentOp, assignmentsOp), (CompositeResult result) -> {
                Map<String, Content> contentByName = new HashMap<>();
                List<Property> properties = result.step(0).get(RESULT).asPropertyList();
                for (Property property : properties) {
                    Content content = new Content(property.getValue());
                    contentByName.put(content.getName(), content);
                }

                List<ModelNode> nodes = result.step(1).get(RESULT).asList();
                for (ModelNode node : nodes) {
                    ModelNode addressNode = node.get(ADDRESS);
                    String groupName = addressNode.asList().get(0).get(SERVER_GROUP).asString();
                    ModelNode assignmentNode = node.get(RESULT);
                    Assignment assignment = new Assignment(groupName, assignmentNode);
                    Content content = contentByName.get(assignment.getName());
                    if (content != null) {
                        content.addAssignment(assignment);
                    }
                }
                control.getContext().push(new ArrayList<>(contentByName.values()));
                control.proceed();
            });
        }
    }
}
