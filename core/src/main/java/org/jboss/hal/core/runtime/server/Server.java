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

import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.runtime.RunningMode;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Combination of the two resources {@code server-config} and {@code server}. Make sure to check {@link #isStarted()}
 * before reading server related attributes.
 *
 * @author Harald Pehl
 */
public class Server extends NamedNode {

    public static final Server STANDALONE = new Server(Ids.STANDALONE_HOST, Ids.STANDALONE_SERVER, new ModelNode(),
            true);

    private final boolean standalone;
    private Version managementVersion;
    private boolean bootErrors;

    public Server(final String host, final ModelNode node) {
        this(host, node.get(NAME).asString(), node, false);
    }

    public Server(final String host, final Property property) {
        this(host, property.getName(), property.getValue(), false);
    }

    private Server(final String host, final String server, final ModelNode modelNode, final boolean standalone) {
        super(server, modelNode);
        this.standalone = standalone;
        this.managementVersion = ManagementModel.parseVersion(modelNode);
        this.bootErrors = false;
        get(HOST).set(host);
        if (standalone) {
            get(STATUS).set(ServerConfigStatus.STARTED.name().toLowerCase());
            get(SERVER_STATE).set(RunningState.RUNNING.name());
        }
    }

    /**
     * Unique server identifier containing the host and server name.
     */
    public String getId() {
        return Ids.hostServer(getHost(), getName());
    }

    /**
     * @return {@code <host-name>}-{@code <server-name>}
     */
    public String getFqName() {
        return getHost() + "-" + getName();
    }

    public boolean isStandalone() {
        return standalone;
    }

    public Version getManagementVersion() {
        return managementVersion;
    }

    public boolean hasBootErrors() {
        return bootErrors;
    }

    public void setBootErrors(final boolean bootErrors) {
        this.bootErrors = bootErrors;
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

    /**
     * @return {@code true} if the {@link #getServerConfigStatus()} == {@link
     * ServerConfigStatus#FAILED}, {@code false} otherwise. Does not take {@link #hasBootErrors()} into account!
     */
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
     * @return the {@code /host=&lt;host&gt;/server-config=&lt;server&gt;} address or {@link ResourceAddress#root()} if
     * either host or server-config is undefined.
     */
    public ResourceAddress getServerConfigAddress() {
        return isStandalone() ? ResourceAddress.root() : new ResourceAddress().add(HOST, getHost())
                .add(SERVER_CONFIG, getName());
    }

    /**
     * @return the {@code /host=&lt;host&gt;/server=&lt;server&gt;} address or {@link ResourceAddress#root()} if either
     * host or server is undefined.
     */
    public ResourceAddress getServerAddress() {
        return isStandalone() ? ResourceAddress.root() : new ResourceAddress().add(HOST, getHost())
                .add(SERVER, getName());
    }

    /**
     * Adds the {@code server} attributes to this instance. Existing attributes will be overwritten.
     */
    public void addServerAttributes(final ModelNode modelNode) {
        modelNode.asPropertyList().forEach(property -> get(property.getName()).set(property.getValue()));
        managementVersion = ManagementModel.parseVersion(modelNode);
    }
}
