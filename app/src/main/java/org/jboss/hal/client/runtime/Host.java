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
package org.jboss.hal.client.runtime;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.IdBuilder;

import static org.jboss.hal.client.runtime.RunningMode.ADMIN_ONLY;
import static org.jboss.hal.client.runtime.RunningState.RELOAD_REQUIRED;
import static org.jboss.hal.client.runtime.RunningState.RESTART_REQUIRED;
import static org.jboss.hal.client.runtime.SuspendState.PRE_SUSPEND;
import static org.jboss.hal.client.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.client.runtime.SuspendState.SUSPENDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * @author Harald Pehl
 */
public class Host extends NamedNode {

    static String id(final String name) {
        return IdBuilder.build("host", name);
    }

    private final List<String> runningServers;

    public Host(final ModelNode node) {
        super(node.get(NAME).asString(), node);
        this.runningServers = new ArrayList<>();
    }

    public Host(final Property property) {
        super(property);
        this.runningServers = new ArrayList<>();
    }

    public boolean isDomainController() {
        return hasDefined("master") && get("master").asBoolean(); //NON-NLS
    }

    public RunningState getHostState() {
        return asEnumValue(this, HOST_STATE, RunningState::valueOf, RunningState.UNDEFINED);
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

    public boolean isRunning() {
        return getHostState() == RunningState.RUNNING &&
                !EnumSet.of(PRE_SUSPEND, SUSPENDING, SUSPENDED).contains(getSuspendState());
    }

    public boolean isAdminMode() {
        return getRunningMode() == ADMIN_ONLY;
    }

    public boolean isSuspending() {
        return EnumSet.of(PRE_SUSPEND, SUSPENDING, SUSPENDED).contains(getSuspendState());
    }

    public boolean needsRestart() {
        return getHostState() == RESTART_REQUIRED;
    }

    public boolean needsReload() {
        return getHostState() == RELOAD_REQUIRED;
    }

    public boolean hasRunningServers() {
        return !runningServers.isEmpty();
    }

    public void addRunningServer(String server) {
        runningServers.add(server);
    }

    public List<String> getRunningServers() {
        return runningServers;
    }
}
