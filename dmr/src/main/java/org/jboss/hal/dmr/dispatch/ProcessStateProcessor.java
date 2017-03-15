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

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.dmr.dispatch.ServerState.State;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROCESS_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;

/**
 * @author Harald Pehl
 */
public class ProcessStateProcessor implements ResponseHeadersProcessor {

    private final EventBus eventBus;

    @Inject
    public ProcessStateProcessor(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void process(final Header[] headers) {
        ProcessState processState = new ProcessState();
        for (Header header : headers) {
            if (header.getHeader().hasDefined(PROCESS_STATE)) {
                String processStateValue = header.getHeader().get(PROCESS_STATE).asString();
                if (RESTART_REQUIRED.equals(processStateValue)) {
                    ServerState state = new ServerState(header.getHost(), header.getServer(), State.RESTART_REQUIRED);
                    processState.add(state);

                } else if (RELOAD_REQUIRED.equals(processStateValue)) {
                    ServerState state = new ServerState(header.getHost(), header.getServer(), State.RELOAD_REQUIRED);
                    processState.add(state);
                }
            }
        }
        if (!processState.isEmpty()) {
            eventBus.fireEvent(new ProcessStateEvent(processState));
        }
    }
}
