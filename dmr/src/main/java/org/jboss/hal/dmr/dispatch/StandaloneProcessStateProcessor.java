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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.ServerState.State;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROCESS_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public class StandaloneProcessStateProcessor implements ProcessStateProcessor {

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
                    ServerState state = new ServerState("", Names.STANDALON_SERVER, State.RESTART_REQUIRED);
                    processState.add(state);

                } else if (RELOAD_REQUIRED.equals(headerValue)) {
                    ServerState state = new ServerState("", Names.STANDALON_SERVER, State.RELOAD_REQUIRED);
                    processState.add(state);
                }
            }
        }
        return processState;
    }
}
