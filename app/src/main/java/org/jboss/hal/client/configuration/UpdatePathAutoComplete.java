/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.configuration.ProfileSelectionEvent;
import org.jboss.hal.core.configuration.ProfileSelectionEvent.ProfileSelectionHandler;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;

/** Updates the {@link PathsAutoComplete} upon profile selections and server stop / start. */
public class UpdatePathAutoComplete implements ProfileSelectionHandler, ServerResultHandler {

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public UpdatePathAutoComplete(EventBus eventBus, Environment environment, Dispatcher dispatcher,
            StatementContext statementContext) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        eventBus.addHandler(ProfileSelectionEvent.getType(), this);
        eventBus.addHandler(ServerResultEvent.getType(), this);
    }

    @Override
    public void onProfileSelection(ProfileSelectionEvent event) {
        PathsAutoComplete.updateOperation(environment, dispatcher, statementContext);
    }

    @Override
    public void onServerResult(ServerResultEvent event) {
        PathsAutoComplete.updateOperation(environment, dispatcher, statementContext);
    }
}
