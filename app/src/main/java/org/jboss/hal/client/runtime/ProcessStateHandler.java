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
package org.jboss.hal.client.runtime;

import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerResultEvent;
import org.jboss.hal.dmr.dispatch.ProcessStateEvent;
import org.jboss.hal.dmr.dispatch.ServerState;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.dmr.dispatch.ServerState.State.RELOAD_REQUIRED;
import static org.jboss.hal.dmr.dispatch.ServerState.State.RESTART_REQUIRED;

/**
 * Handles {@link org.jboss.hal.dmr.dispatch.ProcessState} events and emits {@linkplain org.jboss.hal.spi.Message
 * messages} if necessary.
 *
 * @author Harald Pehl
 */
public class ProcessStateHandler implements ProcessStateEvent.ProcessStateHandler,
        ServerResultEvent.ServerResultHandler {

    private static final long MESSAGE_TIMEOUT = 5 * 60 * 1000; // 5 minutes

    private final Environment environment;
    private final EventBus eventBus;
    private final ServerActions serverActions;
    private final FinderPathFactory finderPathFactory;
    private final Places places;
    private final Resources resources;
    private long lastMessage;

    @Inject
    public ProcessStateHandler(final Environment environment, final EventBus eventBus,
            final ServerActions serverActions, final FinderPathFactory finderPathFactory, final Places places,
            final Resources resources) {
        this.environment = environment;
        this.eventBus = eventBus;
        this.serverActions = serverActions;
        this.finderPathFactory = finderPathFactory;
        this.places = places;
        this.resources = resources;

        this.eventBus.addHandler(ServerResultEvent.getType(), this);
        this.lastMessage = 0;
    }

    @Override
    public void onProcessState(final ProcessStateEvent event) {
        if (!event.getProcessState().isEmpty() && showAgain()) {
            resetTimeout();
            SafeHtml message = null;
            String actionTitle = null;
            Message.Action action = null;
            @NonNls SafeHtmlBuilder html = new SafeHtmlBuilder();

            if (environment.isStandalone()) {
                FinderPath path = finderPathFactory.runtimeServerPath();
                PlaceRequest placeRequest = places.finderPlace(NameTokens.RUNTIME, path).build();
                html.appendHtmlConstant("<a href=\">").appendHtmlConstant(places.historyToken(placeRequest))
                        .appendEscaped(Names.SERVER).appendHtmlConstant("</a>");
                ServerState serverState = event.getProcessState().first();
                if (serverState.getState() == RELOAD_REQUIRED) {
                    actionTitle = resources.constants().reload();
                } else if (serverState.getState() == RESTART_REQUIRED) {
                    actionTitle = resources.constants().restart();
                }
            } else {
                html.appendHtmlConstant("<ul>");
                event.getProcessState().forEach(serverState -> {
                    FinderPath hostPath = finderPathFactory.runtimeHostPath(serverState.getHost());
                    FinderPath serverPath = finderPathFactory
                            .runtimeServerPath(serverState.getHost(), serverState.getServer());
                    PlaceRequest hostPlace = places.finderPlace(NameTokens.RUNTIME, hostPath).build();
                    PlaceRequest serverPlace = places.finderPlace(NameTokens.RUNTIME, serverPath).build();
                    html.appendHtmlConstant("<li>")
                            .appendEscaped(Names.HOST).appendEscaped(" ").appendEscaped(serverState.getHost())
                            .appendEscaped(Names.SERVER).appendEscaped(" ").appendEscaped(serverState.getServer())
                            .appendHtmlConstant("</li>");
                });
                html.appendHtmlConstant("</ul>");
            }

            MessageEvent.fire(eventBus, Message.warning(message, actionTitle, action, true));
        }
    }

    @Override
    public void onServerResult(final ServerResultEvent event) {

    }

    private boolean showAgain() {
        return lastMessage + MESSAGE_TIMEOUT < System.currentTimeMillis();
    }

    private void resetTimeout() {
        lastMessage = System.currentTimeMillis();
    }
}
