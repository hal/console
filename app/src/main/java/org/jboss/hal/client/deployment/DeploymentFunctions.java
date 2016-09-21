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
import java.util.Collections;
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
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.core.runtime.server.Server;
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
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Deployment related functions
 *
 * @author Harald Pehl
 */
class DeploymentFunctions {

    static final String SERVER_GROUP_DEPLOYMENTS = "deploymentFunctions.serverGroupDeployments";
    private static final String UPLOAD_STATISTICS = "deploymentsFunctions.uploadStatistics";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(DeploymentFunctions.class);


    /**
     * Loads the contents form the content repository and pushes a {@code List<Content>} onto the context stack.
     */
    static class LoadContent implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String serverGroup;

        LoadContent(final Dispatcher dispatcher) {
            this(dispatcher, "*");
        }

        /**
         * @param dispatcher  the dispatcher
         * @param serverGroup use "*" to find deployments on any server group
         */
        LoadContent(final Dispatcher dispatcher, final String serverGroup) {
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
            Operation deploymentsOp = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(INCLUDE_RUNTIME, true)
                    .build();

            dispatcher.executeInFunction(control, new Composite(contentOp, deploymentsOp), (CompositeResult result) -> {
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
                    ModelNode deploymentNode = node.get(RESULT);
                    ServerGroupDeployment serverGroupDeployment = new ServerGroupDeployment(groupName, deploymentNode);
                    Content content = contentByName.get(serverGroupDeployment.getName());
                    if (content != null) {
                        content.addDeployment(serverGroupDeployment);
                    }
                }
                control.getContext().push(new ArrayList<>(contentByName.values()));
                control.proceed();
            });
        }
    }


    /**
     * Reads the deployments of the specified server group. Stores the list in the context under the key {@link
     * DeploymentFunctions#SERVER_GROUP_DEPLOYMENTS}. Stores an empty list if there are no deployments or if
     * running in standalone mode.
     */
    static class ReadServerGroupDeployments implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String serverGroup;

        ReadServerGroupDeployments(final Environment environment, final Dispatcher dispatcher,
                final String serverGroup) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                List<ServerGroupDeployment> serverGroupDeployments = Collections.emptyList();
                control.getContext().set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                control.proceed();

            } else {
                ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup);
                Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                        .param(CHILD_TYPE, DEPLOYMENT)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                dispatcher.executeInFunction(control, operation, result -> {
                    List<ServerGroupDeployment> serverGroupDeployments = result.asPropertyList().stream()
                            .map(property -> new ServerGroupDeployment(serverGroup, property.getValue()))
                            .collect(toList());
                    control.getContext().set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                    control.proceed();
                });
            }
        }
    }


    /**
     * Deploys the specified content to the specified server group. The deployment is not enable on the server group.
     */
    static class AddServerGroupDeployment implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String name;
        private final String runtimeName;
        private final String serverGroup;

        AddServerGroupDeployment(final Environment environment, final Dispatcher dispatcher, final String name,
                final String runtimeName, final String serverGroup) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.name = name;
            this.runtimeName = runtimeName;
            this.serverGroup = serverGroup;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                List<ServerGroupDeployment> serverGroupDeployments = Collections.emptyList();
                control.getContext().set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                control.proceed();

            } else {
                ResourceAddress address = new ResourceAddress()
                        .add(SERVER_GROUP, serverGroup)
                        .add(DEPLOYMENT, name);
                Operation operation = new Operation.Builder(ADD, address)
                        .param(RUNTIME_NAME, runtimeName)
                        .param(ENABLED, false)
                        .build();
                dispatcher.executeInFunction(control, operation, result -> control.proceed());
            }
        }
    }


    /**
     * Loads the deployments of the first running server from the list of running servers in the context under the key
     * {@link org.jboss.hal.core.runtime.TopologyFunctions#RUNNING_SERVERS}. Expects the list of deployments under the
     * key {@link #SERVER_GROUP_DEPLOYMENTS} in the context. Updates all matching deployments with the deployments from
     * the running server.
     */
    static class LoadDeploymentsFromRunningServer implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        LoadDeploymentsFromRunningServer(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();

            } else {
                List<ServerGroupDeployment> serverGroupDeployments = control.getContext()
                        .get(DeploymentFunctions.SERVER_GROUP_DEPLOYMENTS);
                List<Server> runningServers = control.getContext().get(TopologyFunctions.RUNNING_SERVERS);
                if (serverGroupDeployments != null && runningServers != null &&
                        !serverGroupDeployments.isEmpty() && !runningServers.isEmpty()) {

                    Server referenceServer = runningServers.get(0);
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            referenceServer.getServerAddress())
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    dispatcher.executeInFunction(control, operation, result -> {

                        Map<String, Deployment> deploymentsByName = result.asPropertyList().stream()
                                .map(property -> new Deployment(referenceServer, property.getValue()))
                                .collect(toMap(Deployment::getName, identity()));
                        serverGroupDeployments.forEach(
                                sgd -> sgd.setDeployment(deploymentsByName.get(sgd.getName())));
                        control.proceed();
                    });

                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Checks whether a deployment with the given name exists and pushes {@code 200} to the context stack if it exists,
     * {@code 404} otherwise.
     */
    static class CheckDeployment implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String name;

        CheckDeployment(final Dispatcher dispatcher, final String name) {
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
    static class UploadOrReplace implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String name;
        private final String runtimeName;
        private final File file;
        private final boolean enabled;

        UploadOrReplace(final Environment environment, final Dispatcher dispatcher,
                final String name, final String runtimeName, final File file, final boolean enabled) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.name = name;
            this.runtimeName = runtimeName;
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
                        .param(NAME, name)
                        .param(RUNTIME_NAME, runtimeName);
                // leave "enabled" as undefined to indicate that the state of the existing deployment should be retained
            } else {
                builder = new Operation.Builder(ADD, new ResourceAddress().add(DEPLOYMENT, name))
                        .param(RUNTIME_NAME, runtimeName)
                        .param(ENABLED, enabled);
            }
            Operation operation = builder.build();
            operation.get(CONTENT).add().get("input-stream-index").set(0); //NON-NLS

            dispatcher.upload(file, operation,
                    result -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics(environment);
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        if (ADD.equals(operation.getName())) {
                            statistics.recordAdded(name);
                        } else {
                            statistics.recordReplaced(name);
                        }
                        control.proceed();
                    },

                    (op, failure) -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics(environment);
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        statistics.recordFailed(name);
                        control.proceed();
                    },

                    (op, exception) -> {
                        UploadStatistics statistics = control.getContext().get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics(environment);
                            control.getContext().set(UPLOAD_STATISTICS, statistics);
                        }
                        statistics.recordFailed(name);
                        control.proceed();
                    });
        }
    }


    /**
     * Adds an unmanaged deployment.
     */
    static class AddUnmanagedDeployment implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final String name;
        private final ModelNode payload;

        AddUnmanagedDeployment(final Dispatcher dispatcher, final String name, final ModelNode payload) {
            this.dispatcher = dispatcher;
            this.name = name;
            this.payload = payload;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(ADD, new ResourceAddress().add(DEPLOYMENT, name))
                    .payload(payload)
                    .build();
            dispatcher.executeInFunction(control, operation, result -> control.proceed());
        }
    }


    private static class UploadOutcome<T> implements Outcome<FunctionContext> {

        private final FinderColumn<T> column;
        private final EventBus eventBus;
        private final Resources resources;
        private final FileList files;

        private UploadOutcome(final FinderColumn<T> column, final EventBus eventBus, final FileList files,
                final Resources resources) {
            this.column = column;
            this.eventBus = eventBus;
            this.resources = resources;
            this.files = files;
        }

        @Override
        public void onFailure(final FunctionContext context) {
            MessageEvent
                    .fire(eventBus, Message.error(resources.messages().deploymentOpFailed(files.getLength())));
        }

        @Override
        public void onSuccess(final FunctionContext context) {
            UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
            if (statistics != null) {
                eventBus.fireEvent(new MessageEvent(statistics.getMessage()));
            } else {
                logger.error("Unable to find upload statistics in the context using key '{}'", UPLOAD_STATISTICS);
            }
            column.refresh(RESTORE_SELECTION);
        }
    }


    /**
     * Uploads or updates one or multiple deployment in standalone mode resp. content in domain mode.
     */
    static <T> void upload(final FinderColumn<T> column, final Environment environment, final Dispatcher dispatcher,
            final EventBus eventBus, final Provider<Progress> progress, final FileList files,
            final Resources resources) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Function> functions = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String filename = files.item(i).getName();
                builder.append(filename).append(" ");
                functions.add(new CheckDeployment(dispatcher, filename));
                functions.add(new UploadOrReplace(environment, dispatcher, filename, filename, files.item(i), false));
            }

            logger.debug("About to upload / update {} file(s): {}", files.getLength(), builder.toString());
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new UploadOutcome<>(column, eventBus, files, resources),
                    functions.toArray(new Function[functions.size()]));
        }
    }

    /**
     * Uploads a content and deploys it to a server group.
     */
    static <T> void uploadAndDeploy(final FinderColumn<T> column, final Environment environment,
            final Dispatcher dispatcher, final EventBus eventBus, final Provider<Progress> progress,
            final FileList files, final String serverGroup, final Resources resources) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Function> functions = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String filename = files.item(i).getName();
                builder.append(filename).append(" ");
                functions.add(new CheckDeployment(dispatcher, filename));
                functions.add(new UploadOrReplace(environment, dispatcher, filename, filename, files.item(i), false));
                functions.add(new AddServerGroupDeployment(environment, dispatcher, filename, filename, serverGroup));
            }

            logger.debug("About to upload and deploy {} file(s): {} to server group {}",
                    files.getLength(), builder.toString(), serverGroup);
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new UploadOutcome<>(column, eventBus, files, resources),
                    functions.toArray(new Function[functions.size()]));
        }
    }
}
