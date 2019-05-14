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

import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;

import static java.lang.Math.max;

/**
 * Central interface for all timeouts related to server group, host and server lifecycle operations.
 * All timeouts are in seconds.
 */
public interface Timeouts {

    // ------------------------------------------------------ server

    int SERVER_SUSPEND_TIMEOUT = 1;
    int SERVER_RESUME_TIMEOUT = 3;
    int SERVER_START_TIMEOUT = 15;
    int SERVER_STOP_TIMEOUT = 5;
    int SERVER_RELOAD_TIMEOUT = 10;
    int SERVER_RESTART_TIMEOUT = SERVER_STOP_TIMEOUT + SERVER_START_TIMEOUT;
    int SERVER_DESTROY_TIMEOUT = SERVER_STOP_TIMEOUT + 5;
    int SERVER_KILL_TIMEOUT = SERVER_STOP_TIMEOUT + 5;


    // ------------------------------------------------------ server group

    int SERVER_GROUP_DEFAULT_TIMEOUT = 10;

    static int serverGroupTimeout(ServerGroup serverGroup, Action action) {
        int timeout = SERVER_GROUP_DEFAULT_TIMEOUT;

        switch (action) {
            case RELOAD:
                timeout = max(1, serverGroup.getServers(Server::isStarted).size()) * SERVER_RELOAD_TIMEOUT;
                break;
            case RESTART:
                timeout = max(1, serverGroup.getServers(Server::isStarted).size()) * SERVER_RESTART_TIMEOUT;
                break;
            case SUSPEND:
                timeout = max(1, serverGroup.getServers(Server::isStarted).size()) * SERVER_SUSPEND_TIMEOUT;
                break;
            case RESUME:
                timeout = max(1, serverGroup.getServers(Server::isSuspended).size()) * SERVER_RESUME_TIMEOUT;
                break;
            case START:
                timeout = max(1, serverGroup.getServers(server -> server.isStopped() || server.isFailed()).size())
                        * SERVER_START_TIMEOUT;
                break;
            case STOP:
                timeout = max(1, serverGroup.getServers(Server::isStarted).size()) * SERVER_STOP_TIMEOUT;
                break;
            default:
                break;
        }
        return timeout;
    }


    // ------------------------------------------------------ host

    int HOST_DEFAULT_TIMEOUT = 10;
    int HOST_RELOAD_TIMEOUT = 10;
    int HOST_RESTART_TIMEOUT = 20;

    static int hostTimeout(Host host, Action action) {
        int timeout = HOST_DEFAULT_TIMEOUT;

        if (action == Action.RELOAD) {
            timeout = HOST_RELOAD_TIMEOUT + (max(1, host.getServers(Server::isStarted).size()) * SERVER_RELOAD_TIMEOUT);

        } else if (action == Action.RESTART) {
            timeout = HOST_RESTART_TIMEOUT +
                    (max(1, host.getServers(Server::isStarted).size()) * SERVER_RELOAD_TIMEOUT);
        }
        return timeout;
    }
}
