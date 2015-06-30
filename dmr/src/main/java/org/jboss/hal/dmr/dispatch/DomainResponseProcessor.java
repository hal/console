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
public class DomainResponseProcessor implements ResponseProcessor {

    private static final String SERVER_GROUPS = "server-groups";
    private static final String RESPONSE = "response";

    @Override
    public boolean accepts(ModelNode response) {
        return response.hasDefined(SERVER_GROUPS);
    }

    @Override
    public Map<String, ServerState> process(ModelNode response) {
        Map<String, ServerState> serverStates = new HashMap<>();

        List<Property> serverGroups = response.get(SERVER_GROUPS).asPropertyList();
        for (Property serverGroup : serverGroups) {
            ModelNode serverGroupValue = serverGroup.getValue();

            //  server group
            if (serverGroupValue.hasDefined("host")) {
                List<Property> hosts = serverGroupValue.get("host").asPropertyList();
                for (Property host : hosts) {
                    // host
                    ModelNode hostValue = host.getValue();
                    parseHost(host.getName(), hostValue, serverStates);
                }
            }
        }
        return serverStates;
    }

    private static void parseHost(final String hostName, ModelNode hostValue, Map<String, ServerState> serverStates) {
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
                        String name = "Host: " + hostName + ", server: " + server.getName();

                        if (RESTART_REQUIRED.equals(headerValue)) {
                            ServerState state = new ServerState(name);
                            state.setRestartRequired(true);
                            serverStates.put(name, state);

                        } else if (RELOAD_REQUIRED.equals(headerValue)) {
                            ServerState state = new ServerState(name);
                            state.setReloadRequired(true);
                            serverStates.put(name, state);
                        }
                    }
                }
            }
        }
    }
}
