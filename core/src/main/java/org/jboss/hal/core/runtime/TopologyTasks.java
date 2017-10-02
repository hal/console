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
package org.jboss.hal.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

/** Set of tasks to read runtime data like running server of a specific server group. */
public class TopologyTasks {

    public static final String SERVER_GROUP = "topologyFunctions.serverGroup";
    public static final String SERVER_GROUPS = "topologyFunctions.serverGroups";
    public static final String HOST = "topologyFunctions.host";
    public static final String HOSTS = "topologyFunctions.hosts";
    public static final String SERVERS = "topologyFunctions.servers";
    public static final String RUNNING_SERVERS = "topologyFunctions.runningServers";
    private static final String NO_SERVER_BOOT_ERRORS = "No second step containing the boot errors for server {}";

    @NonNls private static final Logger logger = LoggerFactory.getLogger(TopologyTasks.class);

    private static final ResourceAddress ALL_SERVER_CONFIGS = new ResourceAddress()
            .add(ModelDescriptionConstants.HOST, "*")
            .add(SERVER_CONFIG, "*");

    private static final ResourceAddress ALL_SERVERS = new ResourceAddress()
            .add(ModelDescriptionConstants.HOST, "*")
            .add(SERVER, "*");

    private static final Operation HOSTS_OPERATION = new Operation.Builder(ResourceAddress.root(),
            READ_CHILDREN_RESOURCES_OPERATION)
            .param(CHILD_TYPE, ModelDescriptionConstants.HOST)
            .param(INCLUDE_RUNTIME, true)
            .build();

    private static final Operation DISCONNECTED_HOSTS = new Operation.Builder(
            new ResourceAddress()
                    .add(CORE_SERVICE, MANAGEMENT)
                    .add(HOST_CONNECTION, "*"),
            QUERY)
            .param(SELECT, new ModelNode().add(EVENTS))
            .param(WHERE, new ModelNode().set(CONNECTED, false))
            .build();

    private static final Operation SERVER_GROUPS_OPERATION = new Operation.Builder(ResourceAddress.root(),
            READ_CHILDREN_RESOURCES_OPERATION)
            .param(CHILD_TYPE, ModelDescriptionConstants.SERVER_GROUP)
            .param(INCLUDE_RUNTIME, true)
            .build();


    // ------------------------------------------------------ topology


    /**
     * Reads the topology (hosts, server groups and servers). Should be followed by {@link TopologyStartedServers} to
     * include the {@code server} resource attributes for the running servers.
     * <p>
     * Populates the context with three collections
     * <ul>
     * <li>{@link #HOSTS}: An ordered list of hosts with the domain controller as first element. Each host contains its
     * server configs.</li>
     * <li>{@link #SERVER_GROUPS}: An ordered list of server groups. Each server group contains its server
     * configs.</li>
     * <li>{@link #SERVERS}: An unordered list of all server configs in the domain. The servers contain only selected
     * attributes from the {@code server-config} resources. Use {@link TopologyStartedServers} to add the {@code
     * server}
     * resource attributes attributes.
     * </ul>
     */
    public static class Topology implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public Topology(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                List<Host> hosts = Collections.emptyList();
                List<ServerGroup> serverGroups = Collections.emptyList();
                List<Server> servers = Collections.emptyList();
                context.set(HOSTS, hosts);
                context.set(SERVER_GROUPS, serverGroups);
                context.set(SERVERS, servers);
                return Completable.complete();

            } else {
                Composite composite = new Composite(
                        HOSTS_OPERATION,
                        DISCONNECTED_HOSTS,
                        SERVER_GROUPS_OPERATION,
                        serverConfigOperation(NAME, GROUP, STATUS, AUTO_START, SOCKET_BINDING_PORT_OFFSET).build());
                return dispatcher.execute(composite)
                        .doOnSuccess((CompositeResult result) -> {

                            List<Host> connectedHosts = result.step(0).get(RESULT).asPropertyList().stream()
                                    .map(Host::new)
                                    .collect(toList());
                            List<Host> disconnectedHosts = disconnectedHosts(result.step(1).get(RESULT));
                            List<Host> hosts = orderedHostWithDomainControllerAsFirstElement(connectedHosts,
                                    disconnectedHosts);
                            context.set(HOSTS, hosts);

                            List<ServerGroup> serverGroups = result.step(2).get(RESULT).asPropertyList().stream()
                                    .map(ServerGroup::new)
                                    .sorted(comparing(ServerGroup::getName))
                                    .collect(toList());
                            context.set(SERVER_GROUPS, serverGroups);

                            Map<String, Server> serverConfigsByName = serverConfigsById(
                                    result.step(3).get(RESULT).asList());
                            context.set(SERVERS, Lists.newArrayList(serverConfigsByName.values()));

                            addServersToHosts(hosts, serverConfigsByName.values());
                            addServersToServerGroups(serverGroups, serverConfigsByName.values());
                        }).toCompletable();
            }
        }
    }


    /**
     * Adds the {@code server} resource attributes and the server bootstrap errors for started servers. Expects a list
     * of servers in the context as provided by {@link Topology}.
     */
    public static class TopologyStartedServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public TopologyStartedServers(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                return Completable.complete();
            } else {
                List<Server> servers = context.get(SERVERS);
                return readAndAddServerRuntimeAttributes(dispatcher, servers);
            }
        }
    }


    // ------------------------------------------------------ hosts


    /**
     * Reads the hosts as order list with the domain controller as first element. Each host contains its server configs.
     * Should be followed by {@link HostsStartedServers} to include the {@code server} resource attributes for the
     * running servers.
     * <p>
     * The list of hosts is available in the context under the key {@link #HOSTS}.
     */
    public static class HostsWithServerConfigs implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public HostsWithServerConfigs(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                return Completable.complete();
            } else {
                Composite composite = new Composite(HOSTS_OPERATION, DISCONNECTED_HOSTS,
                        serverConfigOperation(NAME, GROUP, STATUS).build());
                return dispatcher.execute(composite).doOnSuccess((CompositeResult result) -> {
                    List<Host> connectedHosts = result.step(0).get(RESULT).asPropertyList().stream()
                            .map(Host::new)
                            .collect(toList());
                    List<Host> disconnectedHosts = disconnectedHosts(result.step(1).get(RESULT));
                    List<Host> hosts = orderedHostWithDomainControllerAsFirstElement(connectedHosts,
                            disconnectedHosts);

                    Map<String, Server> serverConfigsByName = serverConfigsById(result.step(2).get(RESULT).asList());
                    addServersToHosts(hosts, serverConfigsByName.values());

                    context.set(HOSTS, hosts);
                }).toCompletable();
            }
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers across connected hosts. Expects a list of hosts
     * in the context as provided by {@link HostsWithServerConfigs}.
     */
    public static class HostsStartedServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public HostsStartedServers(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                return Completable.complete();
            } else {
                List<Host> hosts = context.get(HOSTS);
                List<Host> connectedHosts = hosts.stream().filter(Host::isConnected).collect(toList());
                return processRunningServers(dispatcher, connectedHosts);
            }
        }
    }


    /**
     * Reads one host and its server configs. Should be followed by {@link HostStartedServers} to include the {@code
     * server} resource attributes for the running servers.
     * <p>
     * The host is available in the context under the key {@link #HOST}.
     */
    public static class HostWithServerConfigs implements Task<FlowContext> {

        private final String hostName;
        private final Dispatcher dispatcher;

        public HostWithServerConfigs(String hostName, Dispatcher dispatcher) {
            this.hostName = hostName;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            ResourceAddress hostAddress = new ResourceAddress().add(ModelDescriptionConstants.HOST, hostName);
            Operation hostOp = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverConfigsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, SERVER_CONFIG)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            return dispatcher.execute(new Composite(hostOp, serverConfigsOp)).doOnSuccess((CompositeResult result) -> {
                Host host = new Host(result.step(0).get(RESULT));
                result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new Server(hostName, property.getValue()))
                        .forEach(host::addServer);

                context.set(HOST, host);
            }).toCompletable();
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers of a host. Expects the host in the context
     * as provided by {@link HostWithServerConfigs}.
     */
    public static class HostStartedServers implements Task<FlowContext> {

        private final Dispatcher dispatcher;

        public HostStartedServers(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            Host host = context.get(HOST);
            return host != null
                    ? readAndAddServerRuntimeAttributes(dispatcher, host.getServers())
                    : Completable.complete();
        }
    }

    private static List<Host> orderedHostWithDomainControllerAsFirstElement(List<Host> connected,
            List<Host> disconnected) {
        List<Host> hosts = new ArrayList<>();
        hosts.addAll(connected);
        hosts.addAll(disconnected);
        hosts.sort(comparing(Host::getName));

        Host domainController = null;
        for (Iterator<Host> iterator = hosts.iterator();
                iterator.hasNext() && domainController == null; ) {
            Host host = iterator.next();
            if (host.isDomainController()) {
                domainController = host;
                iterator.remove();
            }
        }
        if (domainController != null) {
            hosts.add(0, domainController);
        }
        return hosts;
    }

    private static List<Host> disconnectedHosts(ModelNode modelNode) {
        return modelNode.asList().stream()
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
    }


    // ------------------------------------------------------ server groups


    /**
     * Reads the server groups as order list. Each server group contains its server configs.  Should be followed by
     * {@link ServerGroupsStartedServers} to include the {@code server} resource attributes for the running servers.
     * <p>
     * The list of server groups is available in the context under the key {@link #SERVER_GROUPS}.
     */
    public static class ServerGroupsWithServerConfigs implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public ServerGroupsWithServerConfigs(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                return Completable.complete();
            } else {
                Composite composite = new Composite(SERVER_GROUPS_OPERATION,
                        serverConfigOperation(NAME, GROUP, STATUS).build());
                return dispatcher.execute(composite).doOnSuccess((CompositeResult result) -> {
                    List<ServerGroup> serverGroups = result.step(0).get(RESULT).asPropertyList().stream()
                            .map(ServerGroup::new)
                            .sorted(comparing(ServerGroup::getName))
                            .collect(toList());

                    Map<String, Server> serverConfigsByName = serverConfigsById(result.step(1).get(RESULT).asList());
                    addServersToServerGroups(serverGroups, serverConfigsByName.values());

                    context.set(SERVER_GROUPS, serverGroups);
                }).toCompletable();
            }
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers across server groups. Expects a list of server
     * groups in the context as provided by {@link ServerGroupsWithServerConfigs}.
     */
    public static class ServerGroupsStartedServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public ServerGroupsStartedServers(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                return Completable.complete();
            } else {
                List<ServerGroup> serverGroups = context.get(SERVER_GROUPS);
                return processRunningServers(dispatcher, serverGroups);
            }
        }
    }


    /**
     * Reads one server group and its server configs. Should be followed by {@link ServerGroupStartedServers} to
     * include the {@code server} resource attributes for the running servers.
     * <p>
     * The server group is available in the context under the key {@link #SERVER_GROUP}.
     */
    public static class ServerGroupWithServerConfigs implements Task<FlowContext> {

        private final String serverGroupName;
        private final Dispatcher dispatcher;

        public ServerGroupWithServerConfigs(String serverGroupName, Dispatcher dispatcher) {
            this.serverGroupName = serverGroupName;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            ResourceAddress serverGroupAddress = new ResourceAddress()
                    .add(ModelDescriptionConstants.SERVER_GROUP, serverGroupName);
            Operation serverGroupOp = new Operation.Builder(serverGroupAddress, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverConfigsOp = serverConfigOperation(NAME, GROUP, STATUS)
                    .param(WHERE, new ModelNode().set(GROUP, serverGroupName))
                    .build();
            return dispatcher.execute(new Composite(serverGroupOp, serverConfigsOp))
                    .doOnSuccess((CompositeResult result) -> {
                        ServerGroup serverGroup = new ServerGroup(serverGroupName, result.step(0).get(RESULT));
                        result.step(1).get(RESULT).asList().stream()
                                .filter(modelNode -> !modelNode.isFailure())
                                .map(modelNode -> {
                                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                                    String host = address.getParent().lastValue();
                                    return new Server(host, modelNode.get(RESULT));
                                })
                                .forEach(serverGroup::addServer);

                        context.set(SERVER_GROUP, serverGroup);
                    }).toCompletable();
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers of a server groups. Expects the server group in
     * the context as provided by {@link ServerGroupWithServerConfigs}.
     */
    public static class ServerGroupStartedServers implements Task<FlowContext> {

        private final Dispatcher dispatcher;

        public ServerGroupStartedServers(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable apply(FlowContext context) {
            ServerGroup serverGroup = context.get(SERVER_GROUP);
            return serverGroup == null
                    ? Completable.complete()
                    : readAndAddServerRuntimeAttributes(dispatcher, serverGroup.getServers());
        }
    }


    // ------------------------------------------------------ servers


    /**
     * Returns a list of running servers which satisfy the specified query. Stores the list in the context under
     * the key {@link TopologyTasks#RUNNING_SERVERS}. Stores an empty list if there are no running servers or if
     * running in standalone mode.
     */
    public static class RunningServersQuery implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private final ModelNode query;

        public RunningServersQuery(Environment environment, Dispatcher dispatcher, ModelNode query) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.query = query;
        }

        @Override
        public Completable apply(FlowContext context) {
            if (environment.isStandalone()) {
                List<Server> servers = Collections.emptyList();
                context.set(RUNNING_SERVERS, servers);
                return Completable.complete();

            } else {
                // Note for mixed domains with servers w/o support for SUSPEND_STATE attribute:
                // The query operation won't fail, instead the unsupported attributes just won't be
                // part of the response payload (kudos to the guy who implemented the query operation!)
                Operation operation = new Operation.Builder(ALL_SERVERS, QUERY)
                        .param(SELECT, new ModelNode()
                                .add(ModelDescriptionConstants.HOST)
                                .add(LAUNCH_TYPE)
                                .add(NAME)
                                .add(PROFILE_NAME)
                                .add(RUNNING_MODE)
                                .add(ModelDescriptionConstants.SERVER_GROUP)
                                .add(SERVER_STATE)
                                .add(SUSPEND_STATE)
                                .add("uuid")) //NON-NLS
                        .param(WHERE, query)
                        .build();

                return dispatcher.execute(operation).doOnSuccess(result -> {
                    List<Server> servers = result.asList().stream()
                            .filter(modelNode -> !modelNode.isFailure())
                            .map(modelNode -> {
                                ResourceAddress adr = new ResourceAddress(modelNode.get(ADDRESS));
                                String host = adr.getParent().lastValue();
                                return new Server(host, modelNode.get(RESULT));
                            })
                            .collect(toList());
                    context.set(RUNNING_SERVERS, servers);
                }).toCompletable();
            }
        }
    }

    private static Operation.Builder serverConfigOperation(String first, String... rest) {
        ModelNode select = new ModelNode().add(first);
        if (rest != null) {
            for (String attribute : rest) {
                select.add(attribute);
            }
        }
        return new Operation.Builder(ALL_SERVER_CONFIGS, QUERY)
                .param(SELECT, select);
    }

    private static void addServersToHosts(List<Host> hosts, Collection<Server> servers) {
        Map<String, List<Server>> serversByHost = servers.stream()
                .collect(groupingBy(Server::getHost));
        hosts.stream()
                .filter(Host::isConnected)
                .forEach(host -> {
                    List<Server> serversOfHost = serversByHost.getOrDefault(host.getName(), emptyList());
                    serversOfHost.forEach(host::addServer);
                });
    }

    private static void addServersToServerGroups(List<ServerGroup> serverGroups, Collection<Server> servers) {
        Map<String, List<Server>> serversByServerGroup = servers.stream()
                .collect(groupingBy(Server::getServerGroup));
        serverGroups.forEach(serverGroup -> {
            List<Server> serversOfServerGroup = serversByServerGroup
                    .getOrDefault(serverGroup.getName(), emptyList());
            serversOfServerGroup.forEach(serverGroup::addServer);
        });
    }

    private static <T extends HasServersNode> Completable processRunningServers(Dispatcher dispatcher,
            List<T> hasServersNode) {
        Completable completable = Completable.complete();
        if (hasServersNode != null) {
            List<Server> servers = runningServers(hasServersNode);
            Composite composite = serverRuntimeComposite(servers);
            if (!composite.isEmpty()) {
                Map<String, Server> serverConfigsById = mapServersById(servers);
                completable = dispatcher.execute(composite).doOnSuccess((CompositeResult result) -> {
                    for (Iterator<ModelNode> iterator = result.iterator(); iterator.hasNext(); ) {
                        ModelNode payload = iterator.next().get(RESULT);
                        String hostName = payload.get(ModelDescriptionConstants.HOST).asString();
                        String serverName = payload.get(NAME).asString();
                        String id = Ids.hostServer(hostName, serverName);
                        Server server = serverConfigsById.get(id);
                        //noinspection Duplicates
                        if (server != null) {
                            server.addServerAttributes(payload);
                            if (iterator.hasNext()) {
                                List<ModelNode> bootErrors = iterator.next().get(RESULT).asList();
                                server.setBootErrors(!bootErrors.isEmpty());
                            } else {
                                logger.error(NO_SERVER_BOOT_ERRORS, server.getName());
                            }
                        }
                    }
                }).toCompletable();
            }
        }
        return completable;
    }

    private static <T extends HasServersNode> List<Server> runningServers(List<T> hasServersNode) {
        return hasServersNode.stream()
                .flatMap(hasServers -> hasServers.getServers().stream().filter(Server::isStarted))
                .collect(toList());
    }

    private static Map<String, Server> serverConfigsById(List<ModelNode> modelNodes) {
        return modelNodes.stream()
                .filter(modelNode -> !modelNode.isFailure())
                .map(modelNode -> {
                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                    String host = address.getParent().lastValue();
                    return new Server(host, modelNode.get(RESULT));
                })
                .collect(toMap(Server::getId, identity()));
    }

    private static Map<String, Server> mapServersById(List<Server> servers) {
        return servers.stream().collect(toMap(Server::getId, identity()));
    }

    private static Completable readAndAddServerRuntimeAttributes(Dispatcher dispatcher, List<Server> servers) {
        Completable completable = Completable.complete();
        if (servers != null) {
            Composite composite = serverRuntimeComposite(servers);
            if (!composite.isEmpty()) {
                completable = dispatcher.execute(composite)
                        .doOnSuccess((CompositeResult result) -> addServerRuntimeAttributes(servers, result))
                        .toCompletable();
            }
        }
        return completable;
    }

    /**
     * @return a composite operation with two operations per started server:
     * <ol>
     * <li>{@code host=h/server=s:read-resource(attributes-only,include-runtime)}</li>
     * <li>{@code host=h/server=s/core-service=management:read-boot-errors()}</li>
     * </ol>
     */
    private static Composite serverRuntimeComposite(List<Server> servers) {
        List<Operation> operations = new ArrayList<>();
        for (Server server : servers) {
            if (server.isStarted()) {
                operations.add(new Operation.Builder(server.getServerAddress(), READ_RESOURCE_OPERATION)
                        .param(ATTRIBUTES_ONLY, true)
                        .param(INCLUDE_RUNTIME, true)
                        .build());
                operations.add(new Operation.Builder(server.getServerAddress().add(CORE_SERVICE, MANAGEMENT),
                        READ_BOOT_ERRORS
                ).build());
            }
        }
        return new Composite(operations);
    }

    /**
     * Updates the server runtime attributes with information from the composite result. For each server there has to
     * be two steps in the composite result:
     * <ol>
     * <li>The server runtime attributes (including the server name)</li>
     * <li>The server boot errors</li>
     * </ol>
     */
    private static void addServerRuntimeAttributes(List<Server> servers, CompositeResult result) {
        Map<String, Server> serverConfigsByName = servers.stream().collect(toMap(Server::getId, identity()));

        for (Iterator<ModelNode> iterator = result.iterator(); iterator.hasNext(); ) {
            ModelNode attributes = iterator.next().get(RESULT);
            String serverId = Ids.hostServer(attributes.get(ModelDescriptionConstants.HOST).asString(),
                    attributes.get(NAME).asString());
            Server server = serverConfigsByName.get(serverId);
            //noinspection Duplicates
            if (server != null) {
                server.addServerAttributes(attributes);
                if (iterator.hasNext()) {
                    List<ModelNode> bootErrors = iterator.next().get(RESULT).asList();
                    server.setBootErrors(!bootErrors.isEmpty());
                } else {
                    logger.error(NO_SERVER_BOOT_ERRORS, server.getName());
                }
            }
        }
    }
}
