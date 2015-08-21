package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.ServerState.State;

import java.util.List;

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
    public ProcessState process(ModelNode response) {
        ProcessState processState = new ProcessState();
        List<Property> headers = response.get(RESPONSE_HEADERS).asPropertyList();
        //noinspection Convert2streamapi
        for (Property header : headers) {
            if (PROCESS_STATE.equals(header.getName())) {

                String headerValue = header.getValue().asString();
                if (RESTART_REQUIRED.equals(headerValue)) {
                    ServerState state = new ServerState(null, STANDALONE_SERVER, State.RESTART_REQUIRED);
                    processState.add(state);

                } else if (RELOAD_REQUIRED.equals(headerValue)) {
                    ServerState state = new ServerState(null, STANDALONE_SERVER, State.RELOAD_REQUIRED);
                    processState.add(state);
                }
            }
        }
        return processState;
    }
}
