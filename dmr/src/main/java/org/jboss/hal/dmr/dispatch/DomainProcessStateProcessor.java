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
package org.jboss.hal.dmr.dispatch;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

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
                            ServerState serverState = new ServerState(hostName, server.getName(), ServerState.State.RESTART_REQUIRED);
                            processState.add(serverState);

                        } else if (RELOAD_REQUIRED.equals(headerValue)) {
                            ServerState serverState = new ServerState(hostName, server.getName(), ServerState.State.RELOAD_REQUIRED);
                            processState.add(serverState);
                        }
                    }
                }
            }
        }
    }
}
