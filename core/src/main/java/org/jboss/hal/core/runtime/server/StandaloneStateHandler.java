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
package org.jboss.hal.core.runtime.server;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.dispatch.ProcessStateEvent;
import org.jboss.hal.dmr.dispatch.ServerState;

/**
 * @author Harald Pehl
 */
public class StandaloneStateHandler implements ProcessStateEvent.ProcessStateHandler {

    private final Environment environment;

    @Inject
    public StandaloneStateHandler(final Environment environment, final EventBus eventBus) {
        this.environment = environment;
        eventBus.addHandler(ProcessStateEvent.getType(), this);
    }

    @Override
    public void onProcessState(final ProcessStateEvent event) {
        if (environment.isStandalone() && !event.getProcessState().isEmpty()) {
            ServerState.State state = event.getProcessState().first().getState();
            if (state == ServerState.State.RELOAD_REQUIRED) {
                // StandaloneServer.INSTANCE.set(SERVER_STATE, RELOAD_REQUIRED);
            } else if (state == ServerState.State.RESTART_REQUIRED) {
                // StandaloneServer.INSTANCE.set(SERVER_STATE, RESTART_REQUIRED);
            }
        }
    }
}
