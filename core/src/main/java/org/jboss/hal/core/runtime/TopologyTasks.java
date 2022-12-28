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
package org.jboss.hal.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BLOCKING_TIMEOUT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ERROR_WFY_CTL_0379;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EVENTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_CONNECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAUNCH_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_BOOT_ERRORS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REGISTERED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMESTAMP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNREGISTERED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHERE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

public final class TopologyTasks {

    public static final String HOST = "topologyTasks.host"; // Host
    public static final String HOSTS = "topologyTasks.hosts"; // List<Host>
    public static final String SERVER_GROUPS = "topologyTasks.serverGroups"; // List<ServerGroup>
    public static final String SERVERS = "topologyTasks.servers"; // List<Server>
    public static final String SERVER = "server"; // Server

    private static final String HOST_NAMES = "topologyTasks.hostNames"; // List<String>
    private static final String WILDCARD = "*";
    private static final int OPERATION_TIMEOUT = 3; // seconds
    private static final Logger logger = LoggerFactory.getLogger(TopologyTasks.class);

    // ------------------------------------------------------ topology

    /** Show a blocking verification dialog and executes the specified operation. */
    public static void reloadBlocking(Dispatcher dispatcher, EventBus eventBus, Operation operation,
            String type, String name, String urlConsole, Resources resources) {
        Messages messages = resources.messages();
        String title = messages.restart(name);

        dispatcher.execute(operation,
                result -> DialogFactory.buildBlocking(title, Dialog.Size.MEDIUM,
                        messages.reloadConsoleRedirect(urlConsole))
                        .show(),
                (operation1, failure) -> MessageEvent.fire(eventBus,
                        Message.error(messages.reloadErrorCause(type, name, failure))));
    }

    /**
     * Returns a list of tasks to read the basic topology. This only includes hosts and server groups, but <em>no</em> servers.
     * Servers are read in a second step to make the whole process more resilient.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #HOSTS}: The ordered list of hosts with the domain controller as first element. Each host contains its
     * servers.</li>
     * <li>{@link #SERVER_GROUPS}: The ordered list of server groups. Each server group contains its servers.</li>
     * </ul>
     */
    public static List<Task<FlowContext>> topology(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new Hosts(environment, dispatcher));
        tasks.add(new DisconnectedHosts(environment, dispatcher));
        tasks.add(new ServerGroups(environment, dispatcher));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all hosts (connected and disconnected) and its servers.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #HOSTS}: The ordered list of hosts with the domain controller as first element. Each host contains its
     * servers.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> hosts(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new HostsAndServerConfigs(environment, dispatcher));
        tasks.add(new DisconnectedHosts(environment, dispatcher));
        tasks.add(new Topology(environment));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all server groups and its servers.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVER_GROUPS}: The ordered list of server groups. Each server group contains its servers.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> serverGroups(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new HostsAndServerConfigs(environment, dispatcher));
        tasks.add(new ServerGroups(environment, dispatcher));
        tasks.add(new Topology(environment));
        return tasks;
    }

    /**
     * Returns a list of tasks to read the server configs of one host.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of server configs of one host.</li>
     * </ul>
     */
    public static List<Task<FlowContext>> serverConfigsOfHost(Environment environment, Dispatcher dispatcher, String host) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new ServersConfigsOfHost(environment, dispatcher, host));
        return tasks;
    }

    /**
     * Returns a list of tasks to read the servers of one host.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of server configs of one host.</li>
     * </ul>
     */
    public static List<Task<FlowContext>> serversOfHost(Environment environment, Dispatcher dispatcher, String host) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new ServersConfigsOfHost(environment, dispatcher, host));
        tasks.add(new StartedServers(environment, dispatcher));
        return tasks;
    }

    /**
     * Returns a list of tasks to read the servers of one server group.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of servers of one server group.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> serversOfServerGroup(Environment environment, Dispatcher dispatcher,
            String serverGroup) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new ServerConfigsOfServerGroup(environment, dispatcher, serverGroup));
        tasks.add(new StartedServers(environment, dispatcher));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all running servers in the domain, which satisfy the specified query.
     *
     * <p>
     * The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of running servers with additional attributes and optional server boot errors for started
     * servers.</li>
     * </ul>
     */
    public static List<Task<FlowContext>> runningServers(Environment environment, Dispatcher dispatcher,
            ModelNode query) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new RunningServers(environment, dispatcher, query));
        return tasks;
    }

    /**
     * Returns a map of composite operations to read the runtime attributes of started servers.
     */
    public static Map<String, Composite> startedServerOperations(List<Server> serverConfigs) {
        return serverConfigs.stream()
                .filter(Server::isStarted)
                .collect(toMap(Server::getId, server -> {
                    List<Operation> operations = new ArrayList<>();
                    operations.add(new Operation.Builder(server.getServerAddress(), READ_RESOURCE_OPERATION)
                            .param(ATTRIBUTES_ONLY, true)
                            .param(INCLUDE_RUNTIME, true)
                            .build());
                    operations.add(new Operation.Builder(server.getServerAddress().add(CORE_SERVICE, MANAGEMENT),
                            READ_BOOT_ERRORS).build());
                    Composite composite = new Composite(operations);
                    composite.addHeader(BLOCKING_TIMEOUT, OPERATION_TIMEOUT);
                    return composite;
                }));
    }

    // ------------------------------------------------------ tasks

    private static final class Topology implements Task<FlowContext> {

        private final Environment environment;

        private Topology(Environment environment) {
            this.environment = environment;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            if (environment.isStandalone()) {
                List<Host> hosts = emptyList();
                List<ServerGroup> serverGroups = emptyList();
                List<Server> servers = emptyList();
                context.set(HOSTS, hosts);
                context.set(SERVER_GROUPS, serverGroups);
                context.set(SERVERS, servers);
                return Promise.resolve(context);

            } else {
                List<Host> hosts = Host.sort(context.get(HOSTS));
                List<Server> servers = context.get(SERVERS, emptyList());
                List<ServerGroup> serverGroups = context.get(SERVER_GROUPS, emptyList());
                ServerGroup.addServers(serverGroups, servers);
                context.set(HOSTS, hosts);
                context.set(SERVER_GROUPS, serverGroups);
                context.set(SERVERS, servers);
                return Promise.resolve(context);
            }
        }
    }

    private static final class HostsNames implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private HostsNames(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<String> hostNames = new ArrayList<>();
            context.set(HOST_NAMES, hostNames);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, ModelDescriptionConstants.HOST)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            hostNames.addAll(result.asList().stream()
                                    .map(ModelNode::asString)
                                    .collect(toList()));
                            return Promise.resolve(context);
                        })
                        .catch_(error -> {
                            logger.error("TopologyTasks.HostNames failed: {}", error);
                            return context.reject(String.valueOf(error));
                        });
            }
        }
    }

    private static final class Hosts implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private Hosts(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<Host> hosts = new ArrayList<>();
            context.set(HOSTS, hosts);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                Operation operation = new Operation.Builder(ResourceAddress.root(),
                        READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, ModelDescriptionConstants.HOST)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            hosts.addAll(result.asPropertyList().stream()
                                    .map(Host::new)
                                    .collect(toList()));
                            return Promise.resolve(context);
                        })
                        .catch_(error -> {
                            logger.error("TopologyTasks.Hosts failed: {}", error);
                            return context.reject(String.valueOf(error));
                        });
            }
        }
    }

    private static final class HostsAndServerConfigs implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private HostsAndServerConfigs(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<Host> hosts = new ArrayList<>();
            List<Server> servers = new ArrayList<>();
            context.set(HOSTS, hosts);
            context.set(SERVERS, servers);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                List<String> hostNames = context.get(HOST_NAMES, Collections.emptyList());
                List<Task<FlowContext>> tasks = hostNames.stream()
                        .map(host -> {
                            ResourceAddress hostAddress = new ResourceAddress()
                                    .add(ModelDescriptionConstants.HOST, host);
                            Operation hostOperation = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build();
                            ResourceAddress serverConfigAddress = new ResourceAddress()
                                    .add(ModelDescriptionConstants.HOST, host)
                                    .add(SERVER_CONFIG, WILDCARD);
                            Operation serverConfigOperation = new Operation.Builder(serverConfigAddress,
                                    READ_RESOURCE_OPERATION)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build();
                            Composite composite = new Composite(hostOperation, serverConfigOperation);
                            return (Task<FlowContext>) (FlowContext c) -> dispatcher.execute(composite)
                                    .then(result -> {
                                        Host h = new Host(result.step(0).get(RESULT));
                                        hosts.add(h);

                                        List<ModelNode> nodes = result.step(1).get(RESULT).asList();
                                        nodes.stream()
                                                .filter(node -> !node.isFailure())
                                                .map(node -> new Server(h.getAddressName(), node.get(RESULT)))
                                                .forEach(server -> {
                                                    h.addServer(server);
                                                    servers.add(server);
                                                });
                                        return Promise.resolve(c);
                                    })
                                    .catch_(error -> {
                                        logger.error("TopologyTasks.HostsAndServerConfigs failed: {}", error);
                                        Host h = String.valueOf(error).contains(ERROR_WFY_CTL_0379)
                                                ? Host.booting(host)
                                                : Host.failed(host);
                                        hosts.add(h);
                                        return context.reject(String.valueOf(error));
                                    });
                        })
                        .collect(toList());
                return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                        .failFast(false)
                        .promise();
            }
        }
    }

    private static final class DisconnectedHosts implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private DisconnectedHosts(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                ResourceAddress address = new ResourceAddress()
                        .add(CORE_SERVICE, MANAGEMENT)
                        .add(HOST_CONNECTION, WILDCARD);
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, new ModelNode().add(EVENTS))
                        .param(WHERE, new ModelNode().set(CONNECTED, false))
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            List<Host> disconnectedHosts = result.asList().stream()
                                    .filter(node -> !node.isFailure())
                                    .map(node -> {
                                        String name = new ResourceAddress(node.get(ADDRESS)).lastValue();
                                        long registered = 0;
                                        long unregistered = 0;
                                        for (ModelNode event : failSafeList(node, RESULT + "/" + EVENTS)) {
                                            if (event.hasDefined(TYPE) && event.hasDefined(TIMESTAMP)) {
                                                if (REGISTERED.equals(event.get(TYPE).asString())) {
                                                    registered = max(registered, event.get(TIMESTAMP).asLong());
                                                } else if (UNREGISTERED.equals(event.get(TYPE).asString())) {
                                                    unregistered = max(unregistered, event.get(TIMESTAMP).asLong());
                                                }
                                            }
                                        }
                                        Date disconnected = unregistered != 0 ? new Date(unregistered) : null;
                                        Date lastConnected = registered != 0 ? new Date(registered) : null;
                                        return Host.disconnected(name, disconnected, lastConnected);
                                    })
                                    .collect(toList());
                            List<Host> hosts = context.get(HOSTS);
                            if (hosts == null) {
                                hosts = new ArrayList<>();
                                context.set(HOSTS, hosts);
                            }
                            hosts.addAll(disconnectedHosts);
                            return Promise.resolve(context);
                        })
                        .catch_(error -> {
                            logger.error("TopologyTasks.DisconnectedHosts failed: {}", error);
                            return context.reject(String.valueOf(error));
                        });
            }
        }
    }

    private static final class ServerGroups implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private ServerGroups(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<ServerGroup> serverGroups = new ArrayList<>();
            context.set(SERVER_GROUPS, serverGroups);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                Operation operation = new Operation.Builder(ResourceAddress.root(),
                        READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, ModelDescriptionConstants.SERVER_GROUP)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            serverGroups.addAll(result.asPropertyList().stream()
                                    .map(ServerGroup::new)
                                    .sorted(comparing(ServerGroup::getName))
                                    .collect(toList()));
                            return Promise.resolve(context);
                        })
                        .catch_(error -> {
                            logger.error("TopologyTasks.ServerGroups failed: {}", error);
                            return context.reject(String.valueOf(error));
                        });
            }
        }
    }

    private static final class ServersConfigsOfHost implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String host;

        private ServersConfigsOfHost(Environment environment, Dispatcher dispatcher, String host) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.host = host;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                ResourceAddress address = new ResourceAddress().add(ModelDescriptionConstants.HOST, host);
                Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, SERVER_CONFIG)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> {
                            result.asPropertyList().stream()
                                    .map(property -> new Server(host, property))
                                    .forEach(servers::add);
                            return Promise.resolve(context);
                        })
                        .catch_(error -> {
                            logger.error("TopologyTasks.ServersOfHost failed: {}", error);
                            return context.reject(String.valueOf(error));
                        });
            }
        }
    }

    private static final class ServerConfigsOfServerGroup implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final String serverGroup;

        private ServerConfigsOfServerGroup(Environment environment, Dispatcher dispatcher, String serverGroup) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                List<String> hostNames = context.get(HOST_NAMES, Collections.emptyList());
                List<Task<FlowContext>> tasks = hostNames.stream()
                        .map(host -> {
                            ResourceAddress address = new ResourceAddress()
                                    .add(ModelDescriptionConstants.HOST, host)
                                    .add(SERVER_CONFIG, WILDCARD);
                            Operation operation = new Operation.Builder(address, QUERY)
                                    .param(WHERE, new ModelNode().set(GROUP, serverGroup))
                                    .build();
                            return (Task<FlowContext>) (FlowContext c) -> dispatcher.execute(operation)
                                    .then(result -> {
                                        result.asList().stream()
                                                .filter(modelNode -> !modelNode.isFailure())
                                                .map(modelNode -> {
                                                    ResourceAddress adr = new ResourceAddress(
                                                            modelNode.get(ADDRESS));
                                                    String h = adr.getParent().lastValue();
                                                    return new Server(h, modelNode.get(RESULT));
                                                })
                                                .forEach(servers::add);
                                        return Promise.resolve(c);
                                    })
                                    .catch_(error -> {
                                        logger.error("TopologyTasks.ServersOfServerGroup failed: {}", error);
                                        return Promise.resolve(c);
                                    });
                        })
                        .collect(toList());
                return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                        .failFast(false)
                        .promise();
            }
        }
    }

    private static final class RunningServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final ModelNode query;

        private RunningServers(Environment environment, Dispatcher dispatcher, ModelNode query) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.query = query.isDefined() ? query : new ModelNode();
            this.query.get(SERVER_STATE).set(RunningState.RUNNING.name().toLowerCase());
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                List<String> hostNames = context.get(HOST_NAMES);
                List<Task<FlowContext>> tasks = hostNames.stream()
                        .map(host -> {
                            ResourceAddress address = new ResourceAddress()
                                    .add(ModelDescriptionConstants.HOST, host)
                                    .add(SERVER, WILDCARD);
                            // Note for mixed domains with servers w/o support for SUSPEND_STATE attribute:
                            // The query operation won't fail, instead the unsupported attributes just won't be
                            // part of the response payload (kudos to the guy who implemented the query operation!)
                            Operation operation = new Operation.Builder(address, QUERY)
                                    .param(SELECT, new ModelNode()
                                            .add(ModelDescriptionConstants.HOST)
                                            .add(LAUNCH_TYPE)
                                            .add(NAME)
                                            .add(PROFILE_NAME)
                                            .add(RUNNING_MODE)
                                            .add(ModelDescriptionConstants.SERVER_GROUP)
                                            .add(SERVER_STATE)
                                            .add(SUSPEND_STATE)
                                            .add("uuid")) // NON-NLS
                                    .param(WHERE, query)
                                    .build();
                            return (Task<FlowContext>) (FlowContext c) -> dispatcher.execute(operation)
                                    .then(result -> {
                                        result.asList().stream()
                                                .filter(modelNode -> !modelNode.isFailure())
                                                .map(modelNode -> {
                                                    ResourceAddress adr = new ResourceAddress(
                                                            modelNode.get(ADDRESS));
                                                    String h = adr.getParent().lastValue();
                                                    return new Server(h, modelNode.get(RESULT));
                                                })
                                                .forEach(servers::add);
                                        return Promise.resolve(c);
                                    })
                                    .catch_(error -> {
                                        logger.error("TopologyTasks.RunningServers failed: {}", error);
                                        return Promise.resolve(c);
                                    });
                        })
                        .collect(toList());
                return Flow.sequential(new FlowContext(Progress.NOOP), tasks)
                        .failFast(false)
                        .promise();
            }
        }
    }

    private static final class StartedServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private StartedServers(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            if (environment.isStandalone()) {
                return Promise.resolve(context);
            } else {
                List<Server> servers = context.get(SERVERS, Collections.emptyList());
                List<Operation> operations = new ArrayList<>();
                for (Server server : servers) {
                    if (server.isStarted()) {
                        operations.add(new Operation.Builder(server.getServerAddress(), READ_RESOURCE_OPERATION)
                                .param(ATTRIBUTES_ONLY, true)
                                .param(INCLUDE_RUNTIME, true)
                                .build());
                        operations.add(new Operation.Builder(server.getServerAddress().add(CORE_SERVICE, MANAGEMENT),
                                READ_BOOT_ERRORS).build());
                    }
                }
                if (!operations.isEmpty()) {
                    Composite composite = new Composite(operations);
                    return dispatcher.execute(composite)
                            .then(result -> {
                                Map<String, Server> serverConfigsByName = servers.stream()
                                        .collect(toMap(Server::getId, identity()));

                                for (Iterator<ModelNode> iterator = result.iterator(); iterator.hasNext();) {
                                    ModelNode attributes = iterator.next().get(RESULT);
                                    String serverId = Ids.hostServer(
                                            attributes.get(ModelDescriptionConstants.HOST).asString(),
                                            attributes.get(NAME).asString());
                                    Server runningServer = serverConfigsByName.get(serverId);
                                    if (runningServer != null) {
                                        runningServer.addServerAttributes(attributes);
                                        if (iterator.hasNext()) {
                                            List<ModelNode> bootErrors = iterator.next().get(RESULT).asList();
                                            runningServer.setBootErrors(!bootErrors.isEmpty());
                                        } else {
                                            logger.error(
                                                    "No second step containing the boot errors for server {}",
                                                    runningServer.getName());
                                        }
                                    }
                                }
                                return Promise.resolve(context);
                            })
                            .catch_(error -> {
                                logger.error("TopologyTasks.StartedServers failed: {}", error);
                                return context.reject(String.valueOf(error));
                            });
                } else {
                    return Promise.resolve(context);
                }
            }
        }
    }

    private TopologyTasks() {
    }
}
