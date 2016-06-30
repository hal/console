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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.IdBuilder;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.model.ResourceAddress.ROOT;

/**
 * Set of functions to read runtime data like running server of a specific server group.
 *
 * @author Harald Pehl
 */
public class TopologyFunctions {

    public static final String SERVER_GROUP = "topologyFunctions.serverGroup";
    public static final String SERVER_GROUPS = "topologyFunctions.serverGroups";
    public static final String HOST = "topologyFunctions.host";
    public static final String HOSTS = "topologyFunctions.hosts";
    public static final String SERVERS = "topologyFunctions.servers";
    public static final String RUNNING_SERVERS = "topologyFunctions.runningServers";

    private static final ResourceAddress ALL_SERVER_CONFIGS = new ResourceAddress()
            .add(ModelDescriptionConstants.HOST, "*")
            .add(SERVER_CONFIG, "*");
    private static final ResourceAddress ALL_SERVERS = new ResourceAddress()
            .add(ModelDescriptionConstants.HOST, "*")
            .add(SERVER, "*");
    private static final Operation HOSTS_OPERATION = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ROOT)
            .param(CHILD_TYPE, ModelDescriptionConstants.HOST)
            .param(INCLUDE_RUNTIME, true)
            .build();
    private static final Operation SERVER_GROUPS_OPERATION = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
            ROOT)
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
    public static class Topology implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public Topology(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                List<Host> hosts = Collections.emptyList();
                List<ServerGroup> serverGroups = Collections.emptyList();
                List<Server> servers = Collections.emptyList();
                control.getContext().set(HOSTS, hosts);
                control.getContext().set(SERVER_GROUPS, serverGroups);
                control.getContext().set(SERVERS, servers);
                control.proceed();

            } else {
                Composite composite = new Composite(HOSTS_OPERATION, SERVER_GROUPS_OPERATION,
                        serverConfigOperation(NAME, GROUP, STATUS, AUTO_START, SOCKET_BINDING_PORT_OFFSET).build());
                dispatcher.executeInFunction(control, composite,
                        (CompositeResult result) -> {

                            List<Host> hosts = orderedHostWithDomainControllerAsFirstElement(
                                    result.step(0).get(RESULT).asPropertyList());
                            control.getContext().set(HOSTS, hosts);

                            List<ServerGroup> serverGroups = result.step(1).get(RESULT).asPropertyList().stream()
                                    .map(ServerGroup::new)
                                    .sorted(comparing(ServerGroup::getName))
                                    .collect(toList());
                            control.getContext().set(SERVER_GROUPS, serverGroups);

                            Map<String, Server> serverConfigsByName = serverConfigsByName(
                                    result.step(2).get(RESULT).asList());
                            control.getContext().set(SERVERS, Lists.newArrayList(serverConfigsByName.values()));

                            addServersToHosts(hosts, serverConfigsByName.values());
                            addServersToServerGroups(serverGroups, serverConfigsByName.values());
                            control.proceed();
                        });
            }
        }
    }


    /**
     * Adds the {@code server} resource attributes for started servers. Expects a list of servers in the context as
     * provided by {@link Topology}.
     */
    public static class TopologyStartedServers implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public TopologyStartedServers(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();
            } else {
                List<Server> servers = control.getContext().get(SERVERS);
                if (servers != null) {
                    Composite composite = new Composite(servers.stream()
                            .filter(Server::isStarted)
                            .map(server -> new Operation.Builder(READ_RESOURCE_OPERATION,
                                    server.getServerAddress())
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build())
                            .collect(toList()));
                    if (!composite.isEmpty()) {
                        Map<String, Server> serverConfigsByName = servers.stream()
                                .collect(toMap(Server::getName, identity()));
                        dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {
                            addServerAttributes(serverConfigsByName, result);
                            control.proceed();
                        });
                    } else {
                        control.proceed();
                    }
                } else {
                    control.proceed();
                }
            }
        }
    }


    // ------------------------------------------------------ hosts


    /**
     * Reads the hosts as order list with the domain controller as first element. Each host contains its
     * server configs. Should be followed by {@link HostsStartedServers} to include the {@code server} resource
     * attributes for the running servers.
     * <p>
     * The list of hosts is available in the context under the key {@link #HOSTS}.
     */
    public static class HostsWithServerConfigs implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public HostsWithServerConfigs(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();
            } else {
                Composite composite = new Composite(HOSTS_OPERATION,
                        serverConfigOperation(NAME, GROUP, STATUS).build());
                dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {

                    List<Host> hosts = orderedHostWithDomainControllerAsFirstElement(
                            result.step(0).get(RESULT).asPropertyList());

                    Map<String, Server> serverConfigsByName = serverConfigsByName(result.step(1).get(RESULT).asList());
                    addServersToHosts(hosts, serverConfigsByName.values());

                    control.getContext().set(HOSTS, hosts);
                    control.proceed();
                });
            }
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers across hosts. Expects a list of hosts in the
     * context as provided by {@link HostsWithServerConfigs}.
     */
    public static class HostsStartedServers implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public HostsStartedServers(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();
            } else {
                List<Host> hosts = control.getContext().get(HOSTS);
                if (hosts != null) {
                    Composite composite = new Composite(hosts.stream()
                            .flatMap(host -> host.getServers().stream().filter(Server::isStarted))
                            .map(server -> new Operation.Builder(READ_RESOURCE_OPERATION,
                                    server.getServerAddress())
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build())
                            .collect(toList()));
                    if (!composite.isEmpty()) {
                        Map<String, Server> serverConfigsByHostAndServerName = hosts.stream()
                                .flatMap(host -> host.getServers().stream().filter(Server::isStarted))
                                .collect(toMap(server -> IdBuilder.build(server.getHost(), server.getName()),
                                        identity()));
                        //noinspection Duplicates
                        dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {
                            result.stream().forEach(step -> {
                                ModelNode payload = step.get(RESULT);
                                String hostName = payload.get(ModelDescriptionConstants.HOST).asString();
                                String serverName = payload.get(NAME).asString();
                                String id = IdBuilder.build(hostName, serverName);
                                Server server = serverConfigsByHostAndServerName.get(id);
                                if (server != null) {
                                    server.addServerAttributes(payload);
                                }
                            });
                            control.proceed();
                        });
                    } else {
                        control.proceed();
                    }
                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Reads one host and its server configs. Should be followed by {@link HostStartedServers} to include the {@code
     * server} resource attributes for the running servers.
     * <p>
     * The host is available in the context under the key {@link #HOST}.
     */
    public static class HostWithServerConfigs implements Function<FunctionContext> {

        private final String hostName;
        private final Dispatcher dispatcher;

        public HostWithServerConfigs(final String hostName, final Dispatcher dispatcher) {
            this.hostName = hostName;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress hostAddress = new ResourceAddress().add(ModelDescriptionConstants.HOST, hostName);
            Operation hostOp = new Operation.Builder(READ_RESOURCE_OPERATION, hostAddress)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverConfigsOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, hostAddress)
                    .param(CHILD_TYPE, SERVER_CONFIG)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.executeInFunction(control, new Composite(hostOp, serverConfigsOp), (CompositeResult result) -> {
                Host host = new Host(result.step(0).get(RESULT));
                result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new Server(hostName, property.getValue()))
                        .forEach(host::addServer);

                control.getContext().set(HOST, host);
                control.proceed();
            });
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers of a host. Expects the host in the context
     * as provided by {@link HostWithServerConfigs}.
     */
    public static class HostStartedServers implements Function<FunctionContext> {

        private final Dispatcher dispatcher;

        public HostStartedServers(final Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Host host = control.getContext().get(HOST);
            if (host != null) {
                readAndAddServerAttributes(dispatcher, control, host);
            } else {
                control.proceed();
            }
        }
    }


    // ------------------------------------------------------ server groups


    /**
     * Reads the server groups as order list. Each server group contains its server configs.  Should be followed by
     * {@link ServerGroupsStartedServers} to include the {@code server} resource attributes for the running servers.
     * <p>
     * The list of server groups is available in the context under the key {@link #SERVER_GROUPS}.
     */
    public static class ServerGroupsWithServerConfigs implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public ServerGroupsWithServerConfigs(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();
            } else {
                Composite composite = new Composite(SERVER_GROUPS_OPERATION,
                        serverConfigOperation(NAME, GROUP, STATUS).build());
                dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {

                    List<ServerGroup> serverGroups = result.step(0).get(RESULT).asPropertyList().stream()
                            .map(ServerGroup::new)
                            .sorted(comparing(ServerGroup::getName))
                            .collect(toList());

                    Map<String, Server> serverConfigsByName = serverConfigsByName(result.step(1).get(RESULT).asList());
                    addServersToServerGroups(serverGroups, serverConfigsByName.values());

                    control.getContext().set(SERVER_GROUPS, serverGroups);
                    control.proceed();
                });
            }
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers across server groups. Expects a list of server
     * groups in the context as provided by {@link ServerGroupsWithServerConfigs}.
     */
    public static class ServerGroupsStartedServers implements Function<FunctionContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        public ServerGroupsStartedServers(final Environment environment, final Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                control.proceed();
            } else {
                List<ServerGroup> serverGroups = control.getContext().get(SERVER_GROUPS);
                if (serverGroups != null) {
                    Composite composite = new Composite(serverGroups.stream()
                            .flatMap(serverGroup -> serverGroup.getServers().stream().filter(Server::isStarted))
                            .map(server -> new Operation.Builder(READ_RESOURCE_OPERATION,
                                    server.getServerAddress())
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build())
                            .collect(toList()));
                    if (!composite.isEmpty()) {
                        Map<String, Server> serverConfigsByServerGroupAndServerName = serverGroups.stream()
                                .flatMap(serverGroup -> serverGroup.getServers().stream().filter(Server::isStarted))
                                .collect(toMap(server -> IdBuilder.build(server.getServerGroup(), server.getName()),
                                        identity()));
                        //noinspection Duplicates
                        dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {
                            result.stream().forEach(step -> {
                                ModelNode payload = step.get(RESULT);
                                String serverGroupName = payload.get(ModelDescriptionConstants.SERVER_GROUP).asString();
                                String serverName = payload.get(NAME).asString();
                                String id = IdBuilder.build(serverGroupName, serverName);
                                Server server = serverConfigsByServerGroupAndServerName.get(id);
                                if (server != null) {
                                    server.addServerAttributes(payload);
                                }
                            });
                            control.proceed();
                        });
                    } else {
                        control.proceed();
                    }
                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Reads one server group and its server configs. Should be followed by {@link ServerGroupStartedServers} to
     * include the {@code server} resource attributes for the running servers.
     * <p>
     * The server group is available in the context under the key {@link #SERVER_GROUP}.
     */
    public static class ServerGroupWithServerConfigs implements Function<FunctionContext> {

        private final String serverGroupName;
        private final Dispatcher dispatcher;

        public ServerGroupWithServerConfigs(final String serverGroupName, final Dispatcher dispatcher) {
            this.serverGroupName = serverGroupName;
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress serverGroupAddress = new ResourceAddress()
                    .add(ModelDescriptionConstants.SERVER_GROUP, serverGroupName);
            Operation serverGroupOp = new Operation.Builder(READ_RESOURCE_OPERATION, serverGroupAddress)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            Operation serverConfigsOp = serverConfigOperation(NAME, GROUP, STATUS)
                    .param(WHERE, new ModelNode().set(GROUP, serverGroupName))
                    .build();
            dispatcher.executeInFunction(control, new Composite(serverGroupOp, serverConfigsOp),
                    (CompositeResult result) -> {
                        ServerGroup serverGroup = new ServerGroup(serverGroupName, result.step(0).get(RESULT));
                        result.step(1).get(RESULT).asList().stream()
                                .filter(modelNode -> !modelNode.isFailure())
                                .map(modelNode -> {
                                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                                    String host = address.getParent().lastValue();
                                    return new Server(host, modelNode.get(RESULT));
                                })
                                .forEach(serverGroup::addServer);

                        control.getContext().set(SERVER_GROUP, serverGroup);
                        control.proceed();
                    });
        }
    }


    /**
     * Reads the {@code server} resource attributes for started servers of a server groups. Expects the server group in
     * the context as provided by {@link ServerGroupWithServerConfigs}.
     */
    public static class ServerGroupStartedServers implements Function<FunctionContext> {

        private final Dispatcher dispatcher;

        public ServerGroupStartedServers(final Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ServerGroup serverGroup = control.getContext().get(SERVER_GROUP);
            if (serverGroup != null) {
                readAndAddServerAttributes(dispatcher, control, serverGroup);
            } else {
                control.proceed();
            }
        }
    }


    // ------------------------------------------------------ servers


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
        @SuppressWarnings("HardCodedStringLiteral")
        public void execute(final Control<FunctionContext> control) {
            if (environment.isStandalone()) {
                List<Server> servers = Collections.emptyList();
                control.getContext().set(RUNNING_SERVERS, servers);
                control.proceed();

            } else {
                ModelNode select = new ModelNode().add(ModelDescriptionConstants.HOST).add(LAUNCH_TYPE).add(NAME)
                        .add(PROFILE_NAME).add(RUNNING_MODE).add(ModelDescriptionConstants.SERVER_GROUP)
                        .add(SERVER_STATE).add(SUSPEND_STATE).add("uuid");

                Operation operation = new Operation.Builder(QUERY, ALL_SERVERS)
                        .param(SELECT, select)
                        .param(WHERE, new ModelNode().set(PROFILE_NAME, profile))
                        .build();

                dispatcher.executeInFunction(control, operation, result -> {
                    List<Server> servers = result.asList().stream()
                            .filter(modelNode -> !modelNode.isFailure())
                            .map(modelNode -> {
                                ResourceAddress adr = new ResourceAddress(modelNode.get(ADDRESS));
                                String host = adr.getParent().lastValue();
                                return new Server(host, modelNode.get(RESULT));
                            })
                            .collect(toList());
                    control.getContext().set(RUNNING_SERVERS, servers);
                    control.proceed();
                });
            }
        }
    }


    // ------------------------------------------------------ helper methods

    private static Operation.Builder serverConfigOperation(String first, String... rest) {
        ModelNode select = new ModelNode().add(first);
        if (rest != null) {
            for (String attribute : rest) {
                select.add(attribute);
            }
        }
        return new Operation.Builder(QUERY, ALL_SERVER_CONFIGS)
                .param(SELECT, select);
    }

    private static List<Host> orderedHostWithDomainControllerAsFirstElement(List<Property> properties) {
        // first collect all hosts, sort them by name and finally
        // remove the domain controller to add it as first element
        List<Host> allHosts = properties.stream()
                .map(Host::new)
                .sorted(comparing(Host::getName))
                .collect(toList());
        Host domainController = null;
        List<Host> hosts = new ArrayList<>(allHosts);
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

    private static void addServersToHosts(final List<Host> hosts, final Collection<Server> servers) {
        Map<String, List<Server>> serversByHost = servers.stream()
                .collect(groupingBy(Server::getHost));
        hosts.forEach(host -> {
            List<Server> serversOfHost = serversByHost.getOrDefault(host.getName(), emptyList());
            serversOfHost.forEach(host::addServer);
        });
    }

    private static void addServersToServerGroups(final List<ServerGroup> serverGroups, final Collection<Server> servers) {
        Map<String, List<Server>> serversByServerGroup = servers.stream()
                .collect(groupingBy(Server::getServerGroup));
        serverGroups.forEach(serverGroup -> {
            List<Server> serversOfServerGroup = serversByServerGroup
                    .getOrDefault(serverGroup.getName(), emptyList());
            serversOfServerGroup.forEach(serverGroup::addServer);
        });
    }

    private static Map<String, Server> serverConfigsByName(List<ModelNode> modelNodes) {
        return modelNodes.stream()
                .filter(modelNode -> !modelNode.isFailure())
                .map(modelNode -> {
                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                    String host = address.getParent().lastValue();
                    return new Server(host, modelNode.get(RESULT));
                })
                .collect(toMap(Server::getName, identity()));
    }

    private static void readAndAddServerAttributes(Dispatcher dispatcher, Control<FunctionContext> control,
            HasServersNode hasServers) {
        Composite composite = new Composite(hasServers.getServers().stream()
                .filter(Server::isStarted)
                .map(server -> new Operation.Builder(READ_RESOURCE_OPERATION, server.getServerAddress())
                        .param(ATTRIBUTES_ONLY, true)
                        .param(INCLUDE_RUNTIME, true)
                        .build())
                .collect(toList()));
        if (!composite.isEmpty()) {
            Map<String, Server> serverConfigsByName = hasServers.getServers().stream()
                    .collect(toMap(Server::getName, identity()));
            dispatcher.executeInFunction(control, composite, (CompositeResult result) -> {
                addServerAttributes(serverConfigsByName, result);
                control.proceed();
            });
        } else {
            control.proceed();
        }
    }

    private static void addServerAttributes(Map<String, Server> serverConfigsByName, CompositeResult result) {
        result.stream().forEach(step -> {
            ModelNode payload = step.get(RESULT);
            String serverName = payload.get(NAME).asString();
            Server server = serverConfigsByName.get(serverName);
            if (server != null) {
                server.addServerAttributes(payload);
            }
        });
    }
}