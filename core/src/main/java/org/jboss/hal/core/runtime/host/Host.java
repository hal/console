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
package org.jboss.hal.core.runtime.host;

import java.util.Date;

import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.runtime.HasServersNode;
import org.jboss.hal.core.runtime.RunningMode;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.ManagementModel;

import static org.jboss.hal.core.runtime.RunningMode.ADMIN_ONLY;
import static org.jboss.hal.core.runtime.RunningState.RELOAD_REQUIRED;
import static org.jboss.hal.core.runtime.RunningState.RESTART_REQUIRED;
import static org.jboss.hal.core.runtime.RunningState.STARTING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MASTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * For the host we need to distinguish between the address-name (the name which is part of the host address)
 * and the model-node-name (the name which is part of the host model node).
 * When the latter is changed, the former remains unchanged until the host reload has been executed.
 */
public class Host extends HasServersNode {

    public static Host disconnected(String name, Date disconnected, Date lastConnected) {
        return new Host(name, disconnected, lastConnected);
    }

    private final boolean connected;
    private final Date disconnected;
    private final Date lastConnected;
    private final String addressName;
    private final Version managementVersion;

    private Host(String name, Date disconnected, Date lastConnected) {
        super(name, new ModelNode().setEmptyObject());
        this.connected = false;
        this.disconnected = disconnected;
        this.lastConnected = lastConnected;
        this.addressName = name;
        this.managementVersion = Version.UNDEFINED;
    }

    public Host(ModelNode node) {
        super(node.get(NAME).asString(), node);
        this.connected = true;
        this.disconnected = null;
        this.lastConnected = null;
        this.addressName = node.get(NAME).asString();
        this.managementVersion = ManagementModel.parseVersion(node);
    }

    public Host( Property property) {
        super(property.getValue().get(NAME).asString(), property.getValue());
        this.connected = true;
        this.disconnected = null;
        this.lastConnected = null;
        this.addressName = property.getName();
        this.managementVersion = ManagementModel.parseVersion(property.getValue());
    }

    public String getAddressName() {
        return addressName;
    }

    public Version getManagementVersion() {
        return managementVersion;
    }

    public boolean isConnected() {
        return connected;
    }

    public Date getDisconnected() {
        return disconnected;
    }

    public Date getLastConnected() {
        return lastConnected;
    }

    public boolean isDomainController() {
        return hasDefined(MASTER) && get(MASTER).asBoolean();
    }

    public RunningState getHostState() {
        return asEnumValue(this, HOST_STATE, RunningState::valueOf, RunningState.UNDEFINED);
    }

    /**
     * @return the state as defined by {@code server.running-mode}
     */
    public RunningMode getRunningMode() {
        return asEnumValue(this, RUNNING_MODE, RunningMode::valueOf, RunningMode.UNDEFINED);
    }

    public boolean isStarting() {
        return getHostState() == STARTING;
    }

    public boolean isRunning() {
        return getHostState() == RunningState.RUNNING;
    }

    public boolean isAdminMode() {
        return getRunningMode() == ADMIN_ONLY;
    }

    public boolean needsRestart() {
        return getHostState() == RESTART_REQUIRED;
    }

    public boolean needsReload() {
        return getHostState() == RELOAD_REQUIRED;
    }

    public ResourceAddress getAddress() {
        return new ResourceAddress().add(HOST, getAddressName());
    }
}
