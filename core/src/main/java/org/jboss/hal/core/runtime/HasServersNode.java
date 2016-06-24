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
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Harald Pehl
 */
public abstract class HasServersNode extends NamedNode {

    private final List<Server> servers;
    private final Multimap<ServerConfigStatus, Server> serversByServerConfigStatus;
    private final Multimap<RunningMode, Server> serversByRunningMode;
    private final Multimap<RunningState, Server> serversByServerState;
    private final Multimap<SuspendState, Server> serversBySuspendState;

    public HasServersNode(final ModelNode node) {
        super(node);
        this.servers = new ArrayList<>();
        this.serversByServerConfigStatus = ArrayListMultimap.create();
        this.serversByRunningMode = ArrayListMultimap.create();
        this.serversByServerState = ArrayListMultimap.create();
        this.serversBySuspendState = ArrayListMultimap.create();
    }

    public HasServersNode(final String name, final ModelNode node) {
        super(name, node);
        this.servers = new ArrayList<>();
        this.serversByServerConfigStatus = ArrayListMultimap.create();
        this.serversByRunningMode = ArrayListMultimap.create();
        this.serversByServerState = ArrayListMultimap.create();
        this.serversBySuspendState = ArrayListMultimap.create();
    }

    public HasServersNode(final Property property) {
        super(property);
        this.servers = new ArrayList<>();
        this.serversByServerConfigStatus = ArrayListMultimap.create();
        this.serversByRunningMode = ArrayListMultimap.create();
        this.serversByServerState = ArrayListMultimap.create();
        this.serversBySuspendState = ArrayListMultimap.create();
    }

    public boolean hasServers() {
        return !servers.isEmpty();
    }

    public boolean hasServers(ServerConfigStatus first, ServerConfigStatus... rest) {
        if (serversByServerConfigStatus.containsKey(first)) {
            return true;
        } else if (rest != null) {
            for (ServerConfigStatus status : rest) {
                if (serversByServerConfigStatus.containsKey(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasServers(RunningMode runningMode) {
        return serversByRunningMode.containsKey(runningMode);
    }

    public boolean hasServers(RunningState serverState) {
        return serversByServerState.containsKey(serverState);
    }

    public boolean hasServers(SuspendState suspendState) {
        return serversBySuspendState.containsKey(suspendState);
    }

    public void addServer(Server server) {
        servers.add(server);
        serversByServerConfigStatus.put(server.getServerConfigStatus(), server);
        serversByRunningMode.put(server.getRunningMode(), server);
        serversByServerState.put(server.getServerState(), server);
        serversBySuspendState.put(server.getSuspendState(), server);
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Server> getServers(ServerConfigStatus first, ServerConfigStatus... rest) {
        List<Server> servers = new ArrayList<>();
        servers.addAll(serversByServerConfigStatus.get(first));
        if (rest != null) {
            for (ServerConfigStatus status : rest) {
                servers.addAll(serversByServerConfigStatus.get(status));
            }
        }
        return servers;
    }

    public List<Server> getServers(RunningMode runningMode) {
        return Lists.newArrayList(serversByRunningMode.get(runningMode));
    }

    public List<Server> getServers(RunningState serverState) {
        return Lists.newArrayList(serversByServerState.get(serverState));
    }

    public List<Server> getServers(SuspendState suspendState) {
        return Lists.newArrayList(serversBySuspendState.get(suspendState));
    }
}
