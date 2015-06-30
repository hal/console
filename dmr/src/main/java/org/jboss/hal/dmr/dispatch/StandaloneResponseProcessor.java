package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public class StandaloneResponseProcessor implements ResponseProcessor {

    private static final String STANDALONE_SERVER = "Standalone Server";

    @Override
    public boolean accepts(ModelNode response) {
        return response.hasDefined(RESPONSE_HEADERS);
    }

    @Override
    public Map<String, ServerState> process(ModelNode response) {
        Map<String, ServerState> serverStates = new HashMap<>();
        List<Property> headers = response.get(RESPONSE_HEADERS).asPropertyList();
        //noinspection Convert2streamapi
        for (Property header : headers) {
            if (PROCESS_STATE.equals(header.getName())) {

                String headerValue = header.getValue().asString();
                if (RESTART_REQUIRED.equals(headerValue)) {
                    ServerState state = new ServerState(STANDALONE_SERVER);
                    state.setRestartRequired(true);
                    serverStates.put(STANDALONE_SERVER, state);

                } else if (RELOAD_REQUIRED.equals(headerValue)) {
                    ServerState state = new ServerState(STANDALONE_SERVER);
                    state.setReloadRequired(true);
                    serverStates.put(STANDALONE_SERVER, state);
                }
            }
        }
        return serverStates;
    }
}
