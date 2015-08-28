package org.jboss.hal.core.dispatch;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.core.dispatch.ServerState.State;

import java.util.List;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public class DomainProcessStateProcessor implements ProcessStateProcessor {

    private static final String SERVER_GROUPS = "server-groups";
    private static final String RESPONSE = "response";

    @Override
    public boolean accepts(ModelNode response) {
        return response.hasDefined(SERVER_GROUPS);
    }

    @Override
    public ProcessState process(ModelNode response) {
        ProcessState processState = new ProcessState();

        List<Property> serverGroups = response.get(SERVER_GROUPS).asPropertyList();
        for (Property serverGroup : serverGroups) {
            ModelNode serverGroupValue = serverGroup.getValue();

            //  server group
            if (serverGroupValue.hasDefined("host")) {
                List<Property> hosts = serverGroupValue.get("host").asPropertyList();
                for (Property host : hosts) {
                    // host
                    ModelNode hostValue = host.getValue();
                    parseHost(host.getName(), hostValue, processState);
                }
            }
        }
        return processState;
    }

    private static void parseHost(final String hostName, ModelNode hostValue, ProcessState processState) {
        List<Property> servers = hostValue.asPropertyList();

        for (Property server : servers) {
            // server
            ModelNode serverResponse = server.getValue().get(RESPONSE);

            if (serverResponse.hasDefined(RESPONSE_HEADERS)) {
                List<Property> headers = serverResponse.get(RESPONSE_HEADERS).asPropertyList();
                //noinspection Convert2streamapi
                for (Property header : headers) {
                    if (PROCESS_STATE.equals(header.getName())) {
                        String headerValue = header.getValue().asString();

                        if (RESTART_REQUIRED.equals(headerValue)) {
                            ServerState serverState = new ServerState(hostName, server.getName(), State.RESTART_REQUIRED);
                            processState.add(serverState);

                        } else if (RELOAD_REQUIRED.equals(headerValue)) {
                            ServerState serverState = new ServerState(hostName, server.getName(), State.RELOAD_REQUIRED);
                            processState.add(serverState);
                        }
                    }
                }
            }
        }
    }
}
