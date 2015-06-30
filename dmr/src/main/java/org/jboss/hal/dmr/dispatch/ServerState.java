package org.jboss.hal.dmr.dispatch;

/**
 * @author Heiko Braun
 * @date 1/18/12
 */
public class ServerState {

    private String name;
    private boolean reloadRequired;
    private boolean restartRequired;

    public ServerState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isReloadRequired() {
        return reloadRequired;
    }

    public void setReloadRequired(boolean reloadRequired) {
        this.reloadRequired = reloadRequired;
    }

    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
    }
}
