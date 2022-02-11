/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

/**
 * Server state used to process state response headers. This duplicates {@code org.jboss.hal.core.runtime.RunningState} from
 * module {@code core} to a certain degree, but since module {@code ballroom} cannot have dependencies to {@code
 * core} this code duplication is necessary.
 */
public class ServerState {

    public enum State {
        RELOAD_REQUIRED, RESTART_REQUIRED
    }

    @Nullable private final String host;
    private final String server;
    private final State state;

    ServerState(@Nullable final String host, final String server, State state) {
        this.host = host;
        this.server = server;
        this.state = state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerState)) {
            return false;
        }

        ServerState that = (ServerState) o;
        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (!server.equals(that.server)) {
            return false;
        }
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + server.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ServerState(");
        if (!Strings.isNullOrEmpty(host)) {
            builder.append(host).append(" / ");
        }
        builder.append(server).append(": ").append(state.name()).append(")");
        return builder.toString();
    }

    public String getServer() {
        return server;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    public State getState() {
        return state;
    }
}
