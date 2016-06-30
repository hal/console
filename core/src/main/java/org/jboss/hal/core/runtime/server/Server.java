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
package org.jboss.hal.core.runtime.server;

import org.jboss.hal.core.runtime.RunningMode;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.IdBuilder;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Combination of the two resources {@code server-config} and {@code server}. Make sure to check {@link #isStarted()}
 * before reading server related attributes.
 *
 * @author Harald Pehl
 */
public class Server extends NamedNode {

    private static final String STANDALONE_SERVER = "standalone.server";
    private static final String STANDALONE_HOST = "standalone.host";

    public static final Server STANDALONE = new Server(STANDALONE_HOST, STANDALONE_SERVER,
            ServerConfigStatus.STARTED, RunningState.RUNNING);

    public static String id(final String name) {
        return IdBuilder.build(SERVER, name);
    }

    private Server(String host, String server, ServerConfigStatus serverConfigStatus,
            RunningState serverState) {
        super(server, new ModelNode());
        get(HOST).set(host);
        get(STATUS).set(serverConfigStatus.name().toLowerCase());
        get(SERVER_STATE).set(serverState.name());
    }

    public Server(final String host, final ModelNode node) {
        super(node.get(NAME).asString(), node);
        get(HOST).set(host);
    }

    public Server(final String host, final Property property) {
        super(property);
        get(HOST).set(host);
    }

    public boolean isStandalone() {
        return STANDALONE_SERVER.equals(getName()) && STANDALONE_HOST.equals(getHost());
    }

    public String getServerGroup() {
        if (hasDefined(GROUP)) { // first try to read from server-config
            return get(GROUP).asString();
        } else if (hasDefined(SERVER_GROUP)) { // then from server
            return get(SERVER_GROUP).asString();
        }
        return null;
    }

    public String getHost() {
        return hasDefined(HOST) ? get(HOST).asString() : null;
    }

    /**
     * @return the status as defined by {@code server-config.status}
     */
    public ServerConfigStatus getServerConfigStatus() {
        return asEnumValue(this, STATUS, ServerConfigStatus::valueOf, ServerConfigStatus.UNDEFINED);
    }

    /**
     * @return the state as defined by {@code server.server-status}
     */
    public RunningState getServerState() {
        return asEnumValue(this, SERVER_STATE, RunningState::valueOf, RunningState.UNDEFINED);
    }

    /**
     * @return the state as defined by {@code server.suspend-state}
     */
    public SuspendState getSuspendState() {
        return asEnumValue(this, SUSPEND_STATE, SuspendState::valueOf, SuspendState.UNDEFINED);
    }

    /**
     * @return the state as defined by {@code server.running-mode}
     */
    public RunningMode getRunningMode() {
        return asEnumValue(this, RUNNING_MODE, RunningMode::valueOf, RunningMode.UNDEFINED);
    }

    /**
     * If this method returns {@code true} it's safe to read the server related attributes like "host", "server-state"
     * or "suspend-state".
     */
    public boolean isStarted() {
        return getServerConfigStatus() == ServerConfigStatus.STARTED || getServerState() == RunningState.RUNNING;
    }

    public boolean isStarting() {
        return getServerState() == RunningState.STARTING;
    }

    public boolean isRunning() {
        return getServerState() == RunningState.RUNNING && !isSuspended();
    }

    public boolean isAdminMode() {
        return getRunningMode() == RunningMode.ADMIN_ONLY;
    }

    public boolean isSuspended() {
        return getSuspendState() == SuspendState.SUSPENDED;
    }

    public boolean isStopped() {
        return getServerConfigStatus() == ServerConfigStatus.STOPPED || getServerConfigStatus() == ServerConfigStatus.DISABLED;
    }

    public boolean isFailed() {
        return getServerConfigStatus() == ServerConfigStatus.FAILED;
    }

    public boolean needsRestart() {
        return getServerState() == RunningState.RESTART_REQUIRED;
    }

    public boolean needsReload() {
        return getServerState() == RunningState.RELOAD_REQUIRED;
    }

    /**
     * @return the {@code /host=&lt;host&gt;/server-config=&lt;server&gt;} address or {@link ResourceAddress#ROOT} if
     * either host or server-config is undefined.
     */
    public ResourceAddress getServerConfigAddress() {
        return isStandalone() ? ResourceAddress.ROOT : new ResourceAddress().add(HOST, getHost())
                .add(SERVER_CONFIG, getName());
    }

    /**
     * @return the {@code /host=&lt;host&gt;/server=&lt;server&gt;} address or {@link ResourceAddress#ROOT} if either
     * host or server is undefined.
     */
    public ResourceAddress getServerAddress() {
        return isStandalone() ? ResourceAddress.ROOT : new ResourceAddress().add(HOST, getHost())
                .add(SERVER, getName());
    }

    /**
     * Adds the {@code server} attributes to this instance. Existing attributes will be overwritten.
     */
    public void addServerAttributes(final ModelNode modelNode) {
        modelNode.asPropertyList().forEach(property -> get(property.getName()).set(property.getValue()));
    }
}
