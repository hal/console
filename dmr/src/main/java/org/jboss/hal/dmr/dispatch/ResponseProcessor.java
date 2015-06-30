package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.ModelNode;

import java.util.Collections;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public interface ResponseProcessor {

    String RESPONSE_HEADERS = "response-headers";
    String PROCESS_STATE = "process-state";
    String RESTART_REQUIRED = "restart-required";
    String RELOAD_REQUIRED = "reload-required";

    boolean accepts(ModelNode response);

    Map<String, ServerState> process(ModelNode response);

    ResponseProcessor NOOP = new ResponseProcessor() {
        @Override
        public boolean accepts(final ModelNode response) {
            return false;
        }

        @Override
        public Map<String, ServerState> process(final ModelNode response) {
            return Collections.emptyMap();
        }
    };
}
