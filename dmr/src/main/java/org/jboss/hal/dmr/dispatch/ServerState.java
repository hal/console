package org.jboss.hal.dmr.dispatch;

import com.google.common.base.Strings;

/**
 * @author Heiko Braun
 * @date 1/18/12
 */
public class ServerState {

    public enum State {
        RELOAD_REQUIRED, RESTART_REQUIRED
    }

    private final String host;
    private final String server;
    private final State state;

    public ServerState(final String host, final String server, State state) {
        this.host = host;
        this.server = server;
        this.state = state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ServerState)) { return false; }

        ServerState that = (ServerState) o;

        if (!host.equals(that.host)) { return false; }
        if (!server.equals(that.server)) { return false; }
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
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

    public String getHost() {
        return host;
    }

    public State getState() {
        return state;
    }
}
