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
package org.jboss.hal.client.runtime;

import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.resources.Ids;

final class TopologySelectors {

    private static final String DATA_HOST = "data-host";
    private static final String DATA_SERVER_GROUP = "data-server-group";
    private static final String DATA_SERVERS = "data-servers"; // all servers of a host / server group pair
    private static final String DATA_SERVER = "data-server"; // one server of a host / server group pair

    private static final String HOST = "host";
    private static final String SERVER_GROUP = "serverGroup";
    private static final String SERVERS = "servers"; // all servers of a host / server group pair
    private static final String SERVER = "server"; // one server of a host / server group pair

    private TopologySelectors() {
    }

    // ------------------------------------------------------ host

    static String hostDataName() {
        return HOST;
    }

    static String hostDataValue(Host host) {
        return host.getName();
    }

    static String hostSelector(Host host) {
        return "[" + DATA_HOST + "='" + hostDataValue(host) + "']";
    }

    // ------------------------------------------------------ server group

    static String serverGroupDataName() {
        return SERVER_GROUP;
    }

    static String serverGroupDataValue(ServerGroup serverGroup) {
        return serverGroup.getName();
    }

    static String serverGroupSelector(ServerGroup serverGroup) {
        return "[" + DATA_SERVER_GROUP + "='" + serverGroupDataValue(serverGroup) + "']";
    }

    // ------------------------------------------------------ servers

    static String serversDataName() {
        return SERVERS;
    }

    static String serversDataValue(Host host, ServerGroup serverGroup) {
        return Ids.build(host.getName(), serverGroup.getName());
    }

    static String serversSelector(Host host, ServerGroup serverGroup) {
        return "[" + DATA_SERVERS + "='" + serversDataValue(host, serverGroup) + "']";
    }

    // ------------------------------------------------------ server

    static String serverDataName() {
        return SERVER;
    }

    static String serverDataValue(Server server) {
        return server.getId();
    }

    static String serverSelector(Server server) {
        return "[" + DATA_SERVER + "='" + serverDataValue(server) + "']";
    }
}
