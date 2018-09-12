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

import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.File;
import elemental2.dom.FileList;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

/** Deployment related functions */
class DeploymentTasks {

    static final String SERVER_GROUP_DEPLOYMENTS = "deploymentFunctions.serverGroupDeployments";
    private static final String UPLOAD_STATISTICS = "deploymentsFunctions.uploadStatistics";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(DeploymentTasks.class);

    /** Uploads or updates one or multiple deployment in standalone mode resp. content in domain mode. */
    static <T> void upload(FinderColumn<T> column, Environment environment, Dispatcher dispatcher,
            EventBus eventBus, Provider<Progress> progress, FileList files,
            Resources resources) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Task<FlowContext>> tasks = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String filename = files.item(i).name;
                builder.append(filename).append(" ");
                tasks.add(new CheckDeployment(dispatcher, filename));
                tasks.add(new UploadOrReplace(environment, dispatcher, filename, filename, files.item(i), true));
            }

            logger.debug("About to upload / update {} file(s): {}", files.getLength(), builder);
            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new UploadOutcome<>(column, eventBus, files, resources));
        }
    }

    /** Uploads a content and deploys it to a server group. */
    static <T> void uploadAndDeploy(FinderColumn<T> column, Environment environment,
            Dispatcher dispatcher, EventBus eventBus, Provider<Progress> progress,
            FileList files, String serverGroup, Resources resources) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Task<FlowContext>> tasks = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String filename = files.item(i).name;
                builder.append(filename).append(" ");
                tasks.add(new CheckDeployment(dispatcher, filename));
                tasks.add(new UploadOrReplace(environment, dispatcher, filename, filename, files.item(i), false));
                tasks.add(new AddServerGroupDeployment(environment, dispatcher, filename, filename, serverGroup));
            }

            logger.debug("About to upload and deploy {} file(s): {} to server group {}",
                    files.getLength(), builder, serverGroup);
            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new UploadOutcome<>(column, eventBus, files, resources));
        }
    }

    private DeploymentTasks() {
    }


    /** Loads the contents form the content repository and pushes a {@code List<Content>} onto the context stack. */
    static class LoadContent implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final String serverGroup;

        LoadContent(Dispatcher dispatcher) {
            this(dispatcher, "*");
        }

        /**
         * @param dispatcher  the dispatcher
         * @param serverGroup use "*" to find deployments on any server group
         */
        LoadContent(Dispatcher dispatcher, String serverGroup) {
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
        }

        @Override
        public Completable call(FlowContext context) {
            Operation contentOp = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, DEPLOYMENT)
                    .build();
            ResourceAddress address = new ResourceAddress()
                    .add(SERVER_GROUP, serverGroup)
                    .add(DEPLOYMENT, "*");
            Operation deploymentsOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();

            return dispatcher.execute(new Composite(contentOp, deploymentsOp)).doOnSuccess((CompositeResult result) -> {
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
                context.push(new ArrayList<>(contentByName.values()));
            }).toCompletable();
        }
    }


    /**
     * Reads the deployments of the specified server group. Stores the list in the context under the key {@link
     * DeploymentTasks#SERVER_GROUP_DEPLOYMENTS}. Stores an empty list if there are no deployments or if
     * running in standalone mode.
     */
    static class ReadServerGroupDeployments implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String serverGroup;
        private final String deployment;

        ReadServerGroupDeployments(Environment environment, Dispatcher dispatcher, String serverGroup) {
            this(environment, dispatcher, serverGroup, null);
        }

        ReadServerGroupDeployments(Environment environment, Dispatcher dispatcher, String serverGroup,
                String deployment) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
            this.deployment = deployment;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable;

            if (environment.isStandalone()) {
                List<ServerGroupDeployment> serverGroupDeployments = Collections.emptyList();
                context.set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                completable = Completable.complete();

            } else {
                if (deployment != null) {
                    // read a single deployment
                    ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup)
                            .add(DEPLOYMENT, deployment);
                    Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    completable = dispatcher.execute(operation).doOnSuccess(result -> {
                        List<ServerGroupDeployment> serverGroupDeployments = Lists
                                .newArrayList(new ServerGroupDeployment(serverGroup, result));
                        context.set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                    }).toCompletable();

                } else {
                    // read all deployments
                    ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup);
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    completable = dispatcher.execute(operation).doOnSuccess(result -> {
                        List<ServerGroupDeployment> serverGroupDeployments = result.asPropertyList().stream()
                                .map(property -> new ServerGroupDeployment(serverGroup, property.getValue()))
                                .collect(toList());
                        context.set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                    }).toCompletable();
                }
            }
            return completable;
        }
    }


    /** Deploys the specified content to the specified server group. The deployment is not enable on the server group. */
    static class AddServerGroupDeployment implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String name;
        private final String runtimeName;
        private final String serverGroup;

        AddServerGroupDeployment(Environment environment, Dispatcher dispatcher, String name, String runtimeName,
                String serverGroup) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.name = name;
            this.runtimeName = runtimeName;
            this.serverGroup = serverGroup;
        }

        @Override
        public Completable call(FlowContext context) {
            if (environment.isStandalone()) {
                List<ServerGroupDeployment> serverGroupDeployments = Collections.emptyList();
                context.set(SERVER_GROUP_DEPLOYMENTS, serverGroupDeployments);
                return Completable.complete();

            } else {
                ResourceAddress address = new ResourceAddress()
                        .add(SERVER_GROUP, serverGroup)
                        .add(DEPLOYMENT, name);
                Operation operation = new Operation.Builder(address, ADD)
                        .param(RUNTIME_NAME, runtimeName)
                        .param(ENABLED, true)
                        .build();
                return dispatcher.execute(operation).toCompletable();
            }
        }
    }


    /**
     * Loads the deployments of the first running server from the list of running servers in the context under the key
     * {@link TopologyTasks#RUNNING_SERVERS}. Expects the list of deployments under the key {@link
     * #SERVER_GROUP_DEPLOYMENTS} in the context. Updates all matching deployments with the deployments from the running
     * server.
     */
    static class LoadDeploymentsFromRunningServer implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        LoadDeploymentsFromRunningServer(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            if (!environment.isStandalone()) {
                List<ServerGroupDeployment> serverGroupDeployments = context
                        .get(DeploymentTasks.SERVER_GROUP_DEPLOYMENTS);
                List<Server> runningServers = context.get(TopologyTasks.RUNNING_SERVERS);
                if (serverGroupDeployments != null && runningServers != null &&
                        !serverGroupDeployments.isEmpty() && !runningServers.isEmpty()) {

                    Server referenceServer = runningServers.get(0);
                    Operation operation = new Operation.Builder(referenceServer.getServerAddress(),
                            READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE_DEPTH, 1)
                            .build();
                    completable = dispatcher.execute(operation).doOnSuccess(result -> {
                        Map<String, Deployment> deploymentsByName = result.asPropertyList().stream()
                                .map(property -> new Deployment(referenceServer, property.getValue()))
                                .collect(toMap(Deployment::getName, identity()));
                        serverGroupDeployments.forEach(
                                sgd -> sgd.setDeployment(deploymentsByName.get(sgd.getName())));
                    }).toCompletable();
                }
            }
            return completable;
        }
    }


    /**
     * Checks whether a deployment with the given name exists and pushes {@code 200} to the context stack if it exists,
     * {@code 404} otherwise.
     */
    static class CheckDeployment implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final String name;

        CheckDeployment(Dispatcher dispatcher, String name) {
            this.dispatcher = dispatcher;
            this.name = name;
        }

        @Override
        public Completable call(FlowContext context) {
            Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, DEPLOYMENT)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> {
                        Set<String> names = result.asList().stream().map(ModelNode::asString).collect(toSet());
                        if (names.contains(name)) {
                            context.push(200);
                        } else {
                            context.push(404);
                        }
                    })
                    .toCompletable();
        }
    }


    /**
     * Creates a new deployment or replaces an existing deployment. The function looks for a status code in the
     * context. If no status context or {@code 404} is found, a new deployment is created, if {@code 200} is found the
     * deployment is replaced.
     * <p>
     * The function puts an {@link UploadStatistics} under the key {@link DeploymentTasks#UPLOAD_STATISTICS}
     * into the context.
     */
    static class UploadOrReplace implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String name;
        private final String runtimeName;
        private final File file;
        private final boolean enabled;

        UploadOrReplace(Environment environment, Dispatcher dispatcher, String name, String runtimeName, File file,
                boolean enabled) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.name = name;
            this.runtimeName = runtimeName;
            this.file = file;
            this.enabled = enabled;
        }

        @Override
        public Completable call(FlowContext context) {
            boolean replace;
            Operation.Builder builder;

            if (context.emptyStack()) {
                replace = false;
            } else {
                Integer status = context.pop();
                replace = status == 200;
            }

            if (replace) {
                builder = new Operation.Builder(ResourceAddress.root(), FULL_REPLACE_DEPLOYMENT) //NON-NLS
                        .param(NAME, name)
                        .param(RUNTIME_NAME, runtimeName);
                // leave "enabled" as undefined to indicate that the state of the existing deployment should be retained
            } else {
                builder = new Operation.Builder(new ResourceAddress().add(DEPLOYMENT, name), ADD)
                        .param(RUNTIME_NAME, runtimeName)
                        .param(ENABLED, enabled);

            }
            Operation operation = builder.build();
            operation.get(CONTENT).add().get(INPUT_STREAM_INDEX).set(0); //NON-NLS

            return dispatcher.upload(file, operation)
                    .doOnSuccess(result -> {
                        UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics(environment);
                            context.set(UPLOAD_STATISTICS, statistics);
                        }
                        if (ADD.equals(operation.getName())) {
                            statistics.recordAdded(name);
                        } else {
                            statistics.recordReplaced(name);
                        }
                    })
                    .doOnError(throwable -> {
                        UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
                        if (statistics == null) {
                            statistics = new UploadStatistics(environment);
                            context.set(UPLOAD_STATISTICS, statistics);
                        }
                        statistics.recordFailed(name);
                    })
                    .toCompletable();
        }
    }


    /** Adds an unmanaged deployment. */
    static class AddUnmanagedDeployment implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final String name;
        private final ModelNode payload;

        AddUnmanagedDeployment(Dispatcher dispatcher, String name, ModelNode payload) {
            this.dispatcher = dispatcher;
            this.name = name;
            this.payload = payload;
        }

        @Override
        public Completable call(FlowContext context) {
            Operation operation = new Operation.Builder(new ResourceAddress().add(DEPLOYMENT, name), ADD)
                    .payload(payload)
                    .build();
            return dispatcher.execute(operation).toCompletable();
        }
    }


    private static class UploadOutcome<T> extends Outcome<FlowContext> {

        private final FinderColumn<T> column;
        private final EventBus eventBus;
        private final Resources resources;
        private final FileList files;

        private UploadOutcome(FinderColumn<T> column, EventBus eventBus, FileList files, Resources resources) {
            this.column = column;
            this.eventBus = eventBus;
            this.resources = resources;
            this.files = files;
        }

        @Override
        public void onError(FlowContext context, Throwable throwable) {
            MessageEvent.fire(eventBus, Message.error(resources.messages().deploymentOpFailed(files.getLength())));
        }

        @Override
        public void onSuccess(FlowContext context) {
            UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
            if (statistics != null) {
                eventBus.fireEvent(new MessageEvent(statistics.getMessage()));
            } else {
                logger.error("Unable to find upload statistics in the context using key '{}'", UPLOAD_STATISTICS);
            }
            column.refresh(RESTORE_SELECTION);
        }
    }
}
