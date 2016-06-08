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
import java.util.Set;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.html.File;
import elemental.html.FileList;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Deployment related functions
 *
 * @author Harald Pehl
 */
public class DeploymentFunctions {

    public static final String UPLOAD_STATISTICS = "deploymentsFunctions.uploadStatistics";
    private static final Logger logger = LoggerFactory.getLogger(DeploymentFunctions.class);

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


    /**
     * Checks whether a deployment with the given name exists and pushes {@code 200} to the context stack if it exists,
     * {@code 404} otherwise.
     */
    public static class CheckDeployment implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String name;

        public CheckDeployment(final Dispatcher dispatcher, final String name) {
            this.dispatcher = dispatcher;
            this.name = name;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                    .param(CHILD_TYPE, DEPLOYMENT)
                    .build();
            dispatcher.executeInFunction(control, operation, result -> {
                Set<String> names = result.asList().stream().map(ModelNode::asString).collect(toSet());
                if (names.contains(name)) {
                    control.getContext().push(200);
                } else {
                    control.getContext().push(404);
                }
                control.proceed();
            });
        }
    }


    /**
     * Creates a new deployment or replaces an existing deployment. The function looks for a status code in the
     * context. If no status context or {@code 404} is found, a new deployment is created, if {@code 200} is found the
     * deployment is replaced.
     * <p>
     * The function puts an {@link UploadStatistics} under the key {@link DeploymentFunctions#UPLOAD_STATISTICS}
     * into the context.
     */
    public static class UploadOrReplace implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final File file;
        private final boolean enabled;

        public UploadOrReplace(final Dispatcher dispatcher, final File file, final boolean enabled) {
            this.dispatcher = dispatcher;
            this.file = file;
            this.enabled = enabled;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            boolean replace;
            Operation.Builder builder;

            if (control.getContext().emptyStack()) {
                replace = false;
            } else {
                Integer status = control.getContext().pop();
                replace = status == 200;
            }

            if (replace) {
                builder = new Operation.Builder(FULL_REPLACE_DEPLOYMENT, ResourceAddress.ROOT) //NON-NLS
                        .param(NAME, file.getName())
                        .param(RUNTIME_NAME, file.getName());
                // leave "enabled" as undefined to indicate that the state of the existing deployment should be retained
            } else {
                builder = new Operation.Builder(ADD, new ResourceAddress().add(DEPLOYMENT, file.getName()))
                        .param(RUNTIME_NAME, file.getName())
                        .param(ENABLED, enabled);
            }
            Operation operation = builder.build();
            operation.get("content").add().get("input-stream-index").set(0); //NON-NLS

            dispatcher.upload(file, operation,
                    result -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics();
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        if (ADD.equals(operation.getName())) {
                            statistics.recordAdded(file.getName());
                        } else {
                            statistics.recordReplaced(file.getName());
                        }
                        control.proceed();
                    },

                    (op, failure) -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics();
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        statistics.recordFailed(file.getName());
                        control.proceed();
                    },

                    (op, exception) -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics();
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        statistics.recordFailed(file.getName());
                        control.proceed();
                    });
        }
    }


    static <T> void upload(FinderColumn<T> column, final Dispatcher dispatcher, final EventBus eventBus,
            final Provider<Progress> progress, final Resources resources, final FileList files) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Function> functions = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String name = files.item(i).getName();
                builder.append(name).append(" ");
                functions.add(new CheckDeployment(dispatcher, name));
                functions.add(new UploadOrReplace(dispatcher, files.item(i), false));
            }

            logger.debug("About to upload {} file(s): {}", files.getLength(), builder.toString()); //NON-NLS
            final Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    // Should not happen since UploadOrReplace functions proceed also for errors and exceptions!
                    MessageEvent.fire(eventBus, Message.error(resources.constants().deploymentFailed()));
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
                    if (statistics != null) {
                        eventBus.fireEvent(new MessageEvent(statistics.getMessage()));
                    } else {
                        logger.error("Unable to find upload statistics in the context using key '{}'", //NON-NLS
                                UPLOAD_STATISTICS);
                    }
                    column.refresh(RESTORE_SELECTION);
                }
            };
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                    functions.toArray(new Function[functions.size()]));
        }
    }
}
