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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Combination of the two resources {@code server-config} and {@code server}. Make sure to check {@link #isStarted()}
 * before reading server related attributes.
 *
 * @author Harald Pehl
 */
public class Server extends NamedNode {

    /**
     * Status as defined by {@code server-config.status}
     */
    public enum ServerConfigStatus {
        STARTED, STOPPED, UNDEFINED
    }


    /**
     * State as defined by {@code server.server-state}
     */
    public enum ServerState {
        STARTING, RUNNING, RESTART_REQUIRED, RELOAD_REQUIRED, UNDEFINED
    }


    private static final String STANDALONE_SERVER = "standalone.server";
    private static final String STANDALONE_HOST = "standalone.host";
    public static final Server STANDALONE = new Server(STANDALONE_SERVER, STANDALONE_HOST,
            ServerConfigStatus.STARTED, ServerState.RUNNING);


    private Server(String server, String host, ServerConfigStatus serverConfigStatus, ServerState serverState) {
        super(server, new ModelNode());
        get(HOST).set(host);
        get(STATUS).set(serverConfigStatus.name().toLowerCase());
        get(SERVER_STATE).set(serverState.name());
    }

    public Server(final ModelNode node) {
        super(node.get(NAME).asString(), node);
    }

    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public String getServerGroup() {
        if (hasDefined("group")) { // first try to read from server-config
            return get("group").asString();
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
        if (hasDefined(STATUS)) {
            return ServerConfigStatus.valueOf(get(STATUS).asString().toUpperCase());
        }
        return ServerConfigStatus.UNDEFINED;
    }

    /**
     * If this method returns {@code true} it's safe to read the server related attributes like "host", "server-state"
     * or "suspend-state".
     */
    public boolean isStarted() {
        return getServerConfigStatus() == ServerConfigStatus.STARTED || getServerState() == ServerState.RUNNING;
    }

    /**
     * @return the state as defined by {@code server.server-status}
     */
    public ServerState getServerState() {
        if (hasDefined(SERVER_STATE)) {
            return ServerState.valueOf(get(SERVER_STATE).asString().toUpperCase());
        }
        return ServerState.UNDEFINED;
    }

    public boolean isStandalone() {
        return STANDALONE_SERVER.equals(getName()) && STANDALONE_HOST.equals(getHost());
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
}
