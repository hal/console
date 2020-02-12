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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
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
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Single;
import rx.functions.Func1;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

public final class TopologyTasks {

    public static final String HOST = "topologyFunctions.host";                      // Host
    public static final String HOSTS = "topologyFunctions.hosts";                    // List<Host>
    public static final String SERVER_GROUPS = "topologyFunctions.serverGroups";     // List<ServerGroup>
    public static final String SERVERS = "topologyFunctions.servers";                // List<Server>

    private static final String HOST_NAMES = "topologyFunctions.hostNames";           // List<String>
    private static final String WILDCARD = "*";
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
                        Message.error(messages.reloadErrorCause(type, name, failure))),
                (operation1, exception) -> MessageEvent.fire(eventBus,
                        Message.error(messages.reloadErrorCause(type, name, exception.getMessage()))));
    }

    /**
     * Returns a list of tasks to read the topology.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #HOSTS}: The ordered list of hosts with the domain controller as first element. Each host contains
     * its servers.</li>
     * <li>{@link #SERVER_GROUPS}: The ordered list of server groups. Each server group contains its servers.</li>
     * <li>{@link #SERVERS}: The list of all servers in the domain.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> topology(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new Hosts(environment, dispatcher));
        tasks.add(new DisconnectedHosts(environment, dispatcher));
        tasks.add(new ServerGroups(environment, dispatcher));
        tasks.add(new StartedServers(environment, dispatcher));
        tasks.add(new Topology(environment));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all hosts (connected and disconnected) and its servers.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #HOSTS}: The ordered list of hosts with the domain controller as first element. Each host contains
     * its servers.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> hosts(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new Hosts(environment, dispatcher));
        tasks.add(new DisconnectedHosts(environment, dispatcher));
        tasks.add(new StartedServers(environment, dispatcher));
        tasks.add(new Topology(environment));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all server groups and its servers.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVER_GROUPS}: The ordered list of server groups. Each server group contains its servers.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    @SuppressWarnings("Duplicates")
    public static List<Task<FlowContext>> serverGroups(Environment environment, Dispatcher dispatcher) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new Hosts(environment, dispatcher));
        tasks.add(new ServerGroups(environment, dispatcher));
        tasks.add(new StartedServers(environment, dispatcher));
        tasks.add(new Topology(environment));
        return tasks;
    }

    /**
     * Returns a list of tasks to read the servers of one host.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of servers of one host.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> serversOfHost(Environment environment, Dispatcher dispatcher, String host) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new ServersOfHost(environment, dispatcher, host));
        tasks.add(new StartedServers(environment, dispatcher));
        return tasks;
    }

    /**
     * Returns a list of tasks to read the servers of one server group.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of servers of one server group.</li>
     * </ul>
     * Started servers contain additional attributes and optional server boot errors.
     */
    public static List<Task<FlowContext>> serversOfServerGroup(Environment environment, Dispatcher dispatcher,
            String serverGroup) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new ServersOfServerGroup(environment, dispatcher, serverGroup));
        tasks.add(new StartedServers(environment, dispatcher));
        return tasks;
    }

    /**
     * Returns a list of tasks to read all running servers in the domain, which satisfy the specified query.
     *
     * <p>The context is populated with the following keys:
     * <ul>
     * <li>{@link #SERVERS}: The list of running servers with additional attributes and optional server boot errors for
     * started servers.</li>
     * </ul>
     */
    public static List<Task<FlowContext>> runningServers(Environment environment, Dispatcher dispatcher,
            ModelNode query) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        tasks.add(new HostsNames(environment, dispatcher));
        tasks.add(new RunningServers(environment, dispatcher, query));
        return tasks;
    }


    // ------------------------------------------------------ public callbacks


    /**
     * Function, which is used for {@link Single#onErrorResumeNext(rx.functions.Func1)} in case of an error in tasks,
     * which read the hosts. The erroneous host is added to the list of hosts as {@link Host#booting(String)} if the
     * error contains {@link ModelDescriptionConstants#ERROR_WFY_CTL_0379} or as {@link Host#failed(String)} otherwise.
     */
    public static class HostError<T> implements Func1<Throwable, Single<T>> {

        private String hostName;
        private final List<Host> hosts;
        private Function<Throwable, T> resume;

        public HostError(String hostName, List<Host> hosts, Function<Throwable, T> resume) {
            this.hostName = hostName;
            this.hosts = hosts;
            this.resume = resume;
        }

        @Override
        public Single<T> call(Throwable throwable) {
            Host h;
            if (throwable.getMessage() != null &&
                    throwable.getMessage().contains(ERROR_WFY_CTL_0379)) {
                h = Host.booting(hostName);
            } else {
                h = Host.failed(hostName);
            }
            hosts.add(h);
            logger.warn("Unable to read host {}: {}", hostName, throwable.getMessage());
            T resumeWith = resume.apply(throwable);
            return Single.just(resumeWith);
        }
    }


    // ------------------------------------------------------ tasks


    private static class Topology implements Task<FlowContext> {

        private final Environment environment;

        private Topology(Environment environment) {
            this.environment = environment;
        }

        @Override
        public Completable call(FlowContext context) {
            if (environment.isStandalone()) {
                List<Host> hosts = emptyList();
                List<ServerGroup> serverGroups = emptyList();
                List<Server> servers = emptyList();
                context.set(HOSTS, hosts);
                context.set(SERVER_GROUPS, serverGroups);
                context.set(SERVERS, servers);
                return Completable.complete();

            } else {
                List<Host> hosts = context.get(HOSTS);
                List<Host> sortedHosts;
                if (hosts != null) {
                    sortedHosts = new ArrayList<>(hosts);
                    sortedHosts.sort(comparing(Host::getName));
                } else {
                    sortedHosts = new ArrayList<>();
                }
                Host domainController = null;
                for (Iterator<Host> iterator = sortedHosts.iterator();
                        iterator.hasNext() && domainController == null; ) {
                    Host host = iterator.next();
                    if (host.isDomainController()) {
                        domainController = host;
                        iterator.remove();
                    }
                }
                if (domainController != null) {
                    sortedHosts.add(0, domainController);
                }
                hosts = sortedHosts;

                List<Server> servers = context.get(SERVERS);
                List<ServerGroup> serverGroups = context.get(SERVER_GROUPS);
                if (serverGroups != null && servers != null) {
                    Map<String, List<Server>> serversByServerGroup = servers.stream()
                            .collect(groupingBy(Server::getServerGroup));
                    for (ServerGroup serverGroup : serverGroups) {
                        List<Server> serversOfServerGroup = serversByServerGroup
                                .getOrDefault(serverGroup.getName(), emptyList());
                        serversOfServerGroup.forEach(serverGroup::addServer);
                    }
                } else {
                    serverGroups = emptyList();
                    servers = emptyList();
                }
                context.set(HOSTS, hosts);
                context.set(SERVER_GROUPS, serverGroups);
                context.set(SERVERS, servers);
                return Completable.complete();
            }
        }
    }


    private static class HostsNames implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private HostsNames(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            if (!environment.isStandalone()) {
                Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, ModelDescriptionConstants.HOST)
                        .build();
                completable = dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            List<String> hostNames = result.asList().stream()
                                    .map(ModelNode::asString)
                                    .collect(toList());
                            context.set(HOST_NAMES, hostNames);
                        })
                        .doOnError(throwable -> logger.error("TopologyTasks.HostNames failed: {}",
                                throwable.getMessage()))
                        .toCompletable();
            }
            return completable;
        }
    }


    private static class Hosts implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private Hosts(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            List<Host> hosts = new ArrayList<>();
            List<Server> servers = new ArrayList<>();
            Completable completable = Completable.complete();
            context.set(HOSTS, hosts);
            context.set(SERVERS, servers);

            if (!environment.isStandalone()) {
                List<String> hostNames = context.get(HOST_NAMES);
                if (hostNames != null && !hostNames.isEmpty()) {
                    List<Completable> completables = hostNames.stream()
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
                                return dispatcher.execute(composite)
                                        .doOnSuccess((CompositeResult result) -> {
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
                                        })
                                        .doOnError(throwable -> logger.error("TopologyTasks.Hosts failed: {}",
                                                throwable.getMessage()))
                                        .onErrorResumeNext(new HostError<>(host, hosts,
                                                error -> new CompositeResult(new ModelNode())))
                                        .toCompletable();
                            })
                            .collect(toList());
                    completable = Completable.concat(completables);
                }
            }
            return completable;
        }
    }


    private static class DisconnectedHosts implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private DisconnectedHosts(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            if (!environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress()
                        .add(CORE_SERVICE, MANAGEMENT)
                        .add(HOST_CONNECTION, WILDCARD);
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, new ModelNode().add(EVENTS))
                        .param(WHERE, new ModelNode().set(CONNECTED, false))
                        .build();
                completable = dispatcher.execute(operation)
                        .doOnSuccess(result -> {
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
                        })
                        .doOnError(throwable -> logger.error("TopologyTasks.DisconnectedHosts failed: {}",
                                throwable.getMessage()))
                        .toCompletable();
            }
            return completable;
        }
    }


    private static class ServerGroups implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private ServerGroups(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            if (!environment.isStandalone()) {
                Operation operation = new Operation.Builder(ResourceAddress.root(),
                        READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, ModelDescriptionConstants.SERVER_GROUP)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                completable = dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            List<ServerGroup> serverGroups = result.asPropertyList().stream()
                                    .map(ServerGroup::new)
                                    .sorted(comparing(ServerGroup::getName))
                                    .collect(toList());
                            context.set(SERVER_GROUPS, serverGroups);
                        })
                        .doOnError(throwable -> logger.error("TopologyTasks.ServerGroups failed: {}",
                                throwable.getMessage()))
                        .toCompletable();
            }
            return completable;
        }
    }


    private static class ServersOfHost implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private String host;

        private ServersOfHost(Environment environment, Dispatcher dispatcher, String host) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.host = host;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (!environment.isStandalone()) {
                ResourceAddress address = new ResourceAddress().add(ModelDescriptionConstants.HOST, host);
                Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                        .param(CHILD_TYPE, SERVER_CONFIG)
                        .param(INCLUDE_RUNTIME, true)
                        .build();
                completable = dispatcher.execute(operation)
                        .doOnSuccess(result -> result.asPropertyList().stream()
                                .map(property -> new Server(host, property))
                                .forEach(servers::add))
                        .doOnError(throwable -> logger.error("TopologyTasks.ServersOfHost failed: {}",
                                throwable.getMessage()))
                        .toCompletable();
            }
            return completable;
        }
    }


    private static class ServersOfServerGroup implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private String serverGroup;

        private ServersOfServerGroup(Environment environment, Dispatcher dispatcher, String serverGroup) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.serverGroup = serverGroup;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (!environment.isStandalone()) {
                List<String> hostNames = context.get(HOST_NAMES);
                if (hostNames != null && !hostNames.isEmpty()) {
                    List<Completable> completables = hostNames.stream()
                            .map(host -> {
                                ResourceAddress address = new ResourceAddress()
                                        .add(ModelDescriptionConstants.HOST, host)
                                        .add(SERVER_CONFIG, WILDCARD);
                                Operation operation = new Operation.Builder(address, QUERY)
                                        .param(WHERE, new ModelNode().set(GROUP, serverGroup))
                                        .build();
                                //noinspection Duplicates
                                return dispatcher.execute(operation)
                                        .doOnSuccess(result -> result.asList().stream()
                                                .filter(modelNode -> !modelNode.isFailure())
                                                .map(modelNode -> {
                                                    ResourceAddress adr = new ResourceAddress(
                                                            modelNode.get(ADDRESS));
                                                    String h = adr.getParent().lastValue();
                                                    return new Server(h, modelNode.get(RESULT));
                                                })
                                                .forEach(servers::add))
                                        .doOnError(throwable -> logger.error(
                                                "TopologyTasks.ServersOfServerGroup failed: {}",
                                                throwable.getMessage()))
                                        .onErrorResumeNext(error -> {
                                            logger.warn("Unable to read servers of host {}: {}", host,
                                                    error.getMessage());
                                            return Single.just(new ModelNode());
                                        })
                                        .toCompletable();
                            })
                            .collect(toList());
                    completable = Completable.concat(completables);
                }
            }
            return completable;
        }
    }


    private static class RunningServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;
        private ModelNode query;

        private RunningServers(Environment environment, Dispatcher dispatcher, ModelNode query) {
            this.environment = environment;
            this.dispatcher = dispatcher;
            this.query = query.isDefined() ? query : new ModelNode();
            this.query.get(SERVER_STATE).set(RunningState.RUNNING.name().toLowerCase());
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            List<Server> servers = new ArrayList<>();
            context.set(SERVERS, servers);

            if (!environment.isStandalone()) {
                List<String> hostNames = context.get(HOST_NAMES);
                if (hostNames != null && !hostNames.isEmpty()) {
                    List<Completable> completables = hostNames.stream()
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
                                                .add("uuid")) //NON-NLS
                                        .param(WHERE, query)
                                        .build();
                                //noinspection Duplicates
                                return dispatcher.execute(operation)
                                        .doOnSuccess(result -> result.asList().stream()
                                                .filter(modelNode -> !modelNode.isFailure())
                                                .map(modelNode -> {
                                                    ResourceAddress adr = new ResourceAddress(
                                                            modelNode.get(ADDRESS));
                                                    String h = adr.getParent().lastValue();
                                                    return new Server(h, modelNode.get(RESULT));
                                                })
                                                .forEach(servers::add))
                                        .doOnError(throwable -> logger.error("TopologyTasks.RunningServers failed: {}",
                                                throwable.getMessage()))
                                        .onErrorResumeNext(error -> {
                                            logger.warn("Unable to read servers of host {}: {}", host,
                                                    error.getMessage());
                                            return Single.just(new ModelNode());
                                        })
                                        .toCompletable();
                            })
                            .collect(toList());
                    completable = Completable.concat(completables);
                }
            }
            return completable;
        }
    }


    private static class StartedServers implements Task<FlowContext> {

        private final Environment environment;
        private final Dispatcher dispatcher;

        private StartedServers(Environment environment, Dispatcher dispatcher) {
            this.environment = environment;
            this.dispatcher = dispatcher;
        }

        @Override
        public Completable call(FlowContext context) {
            Completable completable = Completable.complete();
            if (!environment.isStandalone()) {
                List<Server> servers = context.get(SERVERS);
                if (servers != null) {
                    List<Operation> operations = new ArrayList<>();
                    for (Server server : servers) {
                        if (server.isStarted()) {
                            operations.add(new Operation.Builder(server.getServerAddress(), READ_RESOURCE_OPERATION)
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build());
                            operations.add(
                                    new Operation.Builder(server.getServerAddress().add(CORE_SERVICE, MANAGEMENT),
                                            READ_BOOT_ERRORS
                                    ).build());
                        }
                        if (!operations.isEmpty()) {
                            Composite composite = new Composite(operations);
                            completable = dispatcher.execute(composite)
                                    .doOnSuccess((CompositeResult result) -> {
                                        Map<String, Server> serverConfigsByName = servers.stream()
                                                .collect(toMap(Server::getId, identity()));

                                        for (Iterator<ModelNode> iterator = result.iterator(); iterator.hasNext(); ) {
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
                                    })
                                    .doOnError(throwable -> logger.error("TopologyTasks.StartedServers failed: {}",
                                            throwable.getMessage()))
                                    .toCompletable();
                        }
                    }
                }
            }
            return completable;
        }
    }


    private TopologyTasks() {
    }
}
