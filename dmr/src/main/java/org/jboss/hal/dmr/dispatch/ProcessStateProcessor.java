package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public interface ProcessStateProcessor {

    String RESPONSE_HEADERS = "response-headers";
    String PROCESS_STATE = "process-state";
    String RESTART_REQUIRED = "restart-required";
    String RELOAD_REQUIRED = "reload-required";

    boolean accepts(ModelNode response);

    ProcessState process(ModelNode response);

    ProcessStateProcessor NOOP = new ProcessStateProcessor() {
        @Override
        public boolean accepts(final ModelNode response) {
            return false;
        }

        @Override
        public ProcessState process(final ModelNode response) {
            return ProcessState.EMPTY;
        }
    };
}
