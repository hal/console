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
package org.jboss.hal.client.runtime;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.ApplicationReadyEvent.ApplicationReadyHandler;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.Result;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent;
import org.jboss.hal.core.runtime.group.ServerGroupResultEvent.ServerGroupResultHandler;
import org.jboss.hal.core.runtime.host.HostResultEvent;
import org.jboss.hal.core.runtime.host.HostResultEvent.HostResultHandler;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActionEvent;
import org.jboss.hal.core.runtime.server.ServerActionEvent.ServerActionHandler;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.core.runtime.server.ServerResultEvent.ServerResultHandler;
import org.jboss.hal.dmr.dispatch.ProcessStateEvent;
import org.jboss.hal.dmr.dispatch.ServerState;
import org.jboss.hal.dmr.dispatch.ServerState.State;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;

/**
 * Handles {@link org.jboss.hal.dmr.dispatch.ProcessState} events and emits {@linkplain org.jboss.hal.spi.Message
 * messages} if necessary. Messages are emitted only if there was no message in the last {@value #MESSAGE_TIMEOUT} ms
 * and if the server was not restarted recently (a server restart resets the timeout).
 * <p>
 * In standalone mode the message contains an action link to reload / restart the server. Whereas in domain mode
 * there's no direct way to reload / restart the affected servers (there might be just too many of them). Instead the
 * message contains a link to the topology.
 */
public class ProcessStateHandler implements ApplicationReadyHandler, ProcessStateEvent.ProcessStateHandler,
        HostResultHandler, ServerGroupResultHandler, ServerActionHandler, ServerResultHandler {

    /**
     * Omit multiple messages and wait MESSAGE_TIMEOUT ms until the message is emitted again.
     */
    private static final long MESSAGE_TIMEOUT = 2 * 60 * 1000;

    private final Environment environment;
    private final EventBus eventBus;
    private final PlaceManager placeManager;
    private final Places places;
    private final Finder finder;
    private final ServerActions serverActions;
    private final Provider<Progress> progress;
    private final Resources resources;
    private boolean applicationReady;
    private long lastMessage;

    @Inject
    public ProcessStateHandler(final Environment environment,
            final EventBus eventBus,
            final PlaceManager placeManager,
            final Places places,
            final Finder finder,
            final ServerActions serverActions,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        this.environment = environment;
        this.eventBus = eventBus;
        this.placeManager = placeManager;
        this.places = places;
        this.finder = finder;
        this.serverActions = serverActions;
        this.progress = progress;
        this.resources = resources;
        this.applicationReady = false;
        resetTimeout();

        this.eventBus.addHandler(ApplicationReadyEvent.getType(), this);
        this.eventBus.addHandler(ProcessStateEvent.getType(), this);
        this.eventBus.addHandler(HostResultEvent.getType(), this);
        this.eventBus.addHandler(ServerGroupResultEvent.getType(), this);
        this.eventBus.addHandler(ServerActionEvent.getType(), this);
        this.eventBus.addHandler(ServerResultEvent.getType(), this);
    }

    @Override
    public void onApplicationReady(final ApplicationReadyEvent event) {
        applicationReady = true;
    }

    @Override
    public void onProcessState(final ProcessStateEvent event) {
        // adjust the static standalone server's state
        if (environment.isStandalone()) {
            ServerState serverState = event.getProcessState().first();
            if (serverState.getState() == State.RELOAD_REQUIRED) {
                Server.STANDALONE.get(SERVER_STATE).set(RELOAD_REQUIRED);
            } else if (serverState.getState() == State.RESTART_REQUIRED) {
                Server.STANDALONE.get(SERVER_STATE).set(RESTART_REQUIRED);
            }
        }

        if (shouldProcess() && !event.getProcessState().isEmpty()) {
            startTimeout();

            if (environment.isStandalone()) {
                ServerState serverState = event.getProcessState().first();
                if (serverState.getState() == State.RELOAD_REQUIRED) {
                    MessageEvent.fire(eventBus, Message.warning(UIConstants.RELOAD_MESSAGE_ID,
                            resources.messages().serverConfigurationChanged(),
                            resources.constants().reload(),
                            () -> serverActions.reload(Server.STANDALONE)));

                } else if (serverState.getState() == State.RESTART_REQUIRED) {
                    MessageEvent.fire(eventBus, Message.warning(UIConstants.RESTART_MESSAGE_ID,
                            resources.messages().serverConfigurationChanged(),
                            resources.constants().restart(),
                            () -> serverActions.restart(Server.STANDALONE)));
                }

            } else {
                FinderPath path = new FinderPath().append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.TOPOLOGY));
                PlaceRequest place = places.finderPlace(NameTokens.RUNTIME, path).build();
                MessageEvent.fire(eventBus, Message.warning(UIConstants.DOMAIN_CHANGED_MESSAGE_ID,
                        resources.messages().domainConfigurationChanged(),
                        Names.TOPOLOGY, () -> placeManager.revealPlace(place)));
            }
        }
    }

    @Override
    public void onHostResult(final HostResultEvent event) {
        if (event.getResult() == Result.SUCCESS) {
            resetTimeout();
        }
    }

    @Override
    public void onServerGroupResult(final ServerGroupResultEvent event) {
        if (event.getResult() == Result.SUCCESS) {
            resetTimeout();
        }
    }

    @Override
    public void onServerAction(final ServerActionEvent event) {
        // show the progress bar in the footer when we want to reload in standalone and we're not on the runtime tab
        if (environment.isStandalone() && event.getAction() == Action.RELOAD &&
                !NameTokens.RUNTIME.equals(placeManager.getCurrentPlaceRequest().getNameToken())) {
            progress.get().reset();
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {
        progress.get().finish(); // finish in any case
        if (event.getResult() == Result.SUCCESS) {
            resetTimeout();
        }
        if (environment.isStandalone() && NameTokens.RUNTIME
                .equals(placeManager.getCurrentPlaceRequest().getNameToken())) {
            FinderColumn column = finder.getColumn(Ids.STANDALONE_SERVER_COLUMN);
            if (column != null) {
                column.refresh(RESTORE_SELECTION);
            }
        }
    }

    private boolean shouldProcess() {
        return applicationReady && lastMessage + MESSAGE_TIMEOUT < System.currentTimeMillis();
    }

    private void startTimeout() {
        lastMessage = System.currentTimeMillis();
    }

    private void resetTimeout() {
        lastMessage = 0;
    }
}
