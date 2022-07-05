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

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.resources.Ids;

class TopologyStatus {

    enum Status {
        LOADING, OK, TIMEOUT, FAILURE, UNKNOWN
    }

    private final Map<String, Status> host;
    private final Map<String, Status> serverGroup;
    private final Map<String, Status> server;

    TopologyStatus() {
        host = new HashMap<>();
        serverGroup = new HashMap<>();
        server = new HashMap<>();
    }

    void reset() {
        host.clear();
        serverGroup.clear();
        server.clear();
    }

    void host(String name, Status status) {
        host.put(name, status);
    }

    Status host(String name) {
        return host.getOrDefault(name, Status.UNKNOWN);
    }

    void serverGroup(String name, Status status) {
        serverGroup.put(name, status);
    }

    Status serverGroup(String name) {
        return serverGroup.getOrDefault(name, Status.UNKNOWN);
    }

    void server(String host, String serverGroup, String name, Status status) {
        server.put(serverId(host, serverGroup, name), status);
    }

    Status server(String host, String serverGroup, String name) {
        return server.getOrDefault(serverId(host, serverGroup, name), Status.UNKNOWN);
    }

    private String serverId(String host, String serverGroup, String server) {
        return Ids.build(host, serverGroup, server);
    }
}
