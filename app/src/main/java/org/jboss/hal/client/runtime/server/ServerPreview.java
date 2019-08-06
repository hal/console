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
package org.jboss.hal.client.runtime.server;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttributeFunction;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerPreviewAttributes;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.server.ServerColumn.serverConfigTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

class ServerPreview extends RuntimePreview<Server> {

    private final ServerActions serverActions;
    private final HTMLElement startLink;
    private final HTMLElement stopLink;
    private final HTMLElement reloadLink;
    private final HTMLElement restartLink;
    private final HTMLElement resumeLink;
    private final HTMLElement bootErrorsLink;
    private final HTMLElement[] links;
    private final HTMLElement serverUrl;
    private final PreviewAttributes<Server> attributes;

    ServerPreview(ServerActions serverActions,
            Server server,
            PlaceManager placeManager,
            Places places,
            FinderPathFactory finderPathFactory,
            Resources resources) {
        super(server.getName(), null, resources);
        this.serverActions = serverActions;

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().get())
                        .add(alertText = span().get())
                        .add(span().textContent(" "))

                        .add(startLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.start(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), START).data())
                                .textContent(resources.constants().start())
                                .get())
                        .add(stopLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.stop(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), STOP).data())
                                .textContent(resources.constants().stop())
                                .get())
                        .add(reloadLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.reload(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RELOAD).data())
                                .textContent(resources.constants().reload())
                                .get())
                        .add(restartLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.restart(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RESTART).data())
                                .textContent(resources.constants().restart())
                                .get())
                        .add(resumeLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.resume(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RESUME).data())
                                .textContent(resources.constants().resume())
                                .get())
                        .add(bootErrorsLink = a().css(clickable, alertLink)
                                .on(click, event -> {
                                    PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(
                                            NameTokens.SERVER_BOOT_ERRORS)
                                            .with(HOST, server.getHost())
                                            .with(SERVER, server.getName())
                                            .build();
                                    placeManager.revealPlace(placeRequest);
                                })
                                .textContent(resources.constants().view())
                                .get())
                        .get());

        links = new HTMLElement[]{startLink, stopLink, reloadLink, restartLink, resumeLink, bootErrorsLink};

        serverUrl = span().textContent(Names.NOT_AVAILABLE).get();
        PreviewAttributeFunction<Server> previewFunction = model -> new PreviewAttribute(Names.URL, serverUrl);
        if (server.isStandalone()) {
            this.attributes = new PreviewAttributes<>(server)
                    .append(previewFunction)
                    .append(STATUS)
                    .append(RUNNING_MODE)
                    .append(SERVER_STATE)
                    .append(SUSPEND_STATE);
        } else {
            this.attributes = new PreviewAttributes<>(server)
                    .append(model -> {
                        String host = model.getHost();
                        String token = places.historyToken(
                                places.finderPlace(NameTokens.RUNTIME, finderPathFactory.runtimeHostPath(host))
                                        .build());
                        return new PreviewAttribute(Names.HOST, host, token);
                    })
                    .append(model -> {
                        String serverGroup = model.getServerGroup();
                        String token = places.historyToken(places.finderPlace(NameTokens.RUNTIME,
                                finderPathFactory.runtimeServerGroupPath(serverGroup)).build());
                        return new PreviewAttribute(Names.SERVER_GROUP, serverGroup, token);
                    })
                    .append(model -> {
                        String profile = model.get(PROFILE_NAME).asString();
                        PlaceRequest profilePlaceRequest = places
                                .finderPlace(NameTokens.CONFIGURATION, new FinderPath()
                                        .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                        .append(Ids.PROFILE, profile))
                                .build();
                        String token = places.historyToken(profilePlaceRequest);
                        return new PreviewAttribute(Names.PROFILE, profile, token);
                    })
                    .append(previewFunction)
                    .append(AUTO_START)
                    .append(SOCKET_BINDING_PORT_OFFSET)
                    .append(STATUS)
                    .append(RUNNING_MODE)
                    .append(SERVER_STATE)
                    .append(SUSPEND_STATE);
        }
        previewBuilder().addAll(this.attributes);

        update(server);
    }

    @Override
    public void update(Server server) {
        ServerStatusSwitch sss = new ServerStatusSwitch(serverActions) {
            @Override
            protected void onPending(Server server) {
                pending(resources.messages().serverPending(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onBootErrors(Server server) {
                error(resources.messages().serverBootErrors(server.getName()));
                disableAllLinksBut(bootErrorsLink);
            }

            @Override
            protected void onFailed(Server server) {
                error(resources.messages().serverFailed(server.getName()));
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(startLink);
                }
            }

            @Override
            protected void onAdminMode(Server server) {
                adminOnly(resources.messages().serverAdminMode(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onStarting(Server server) {
                adminOnly(resources.messages().serverAdminMode(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onSuspended(Server server) {
                suspended(resources.messages().serverSuspended(server.getName()));
                disableAllLinksBut(resumeLink);
            }

            @Override
            protected void onNeedsReload(Server server) {
                needsReload(resources.messages().serverNeedsReload(server.getName()));
                disableAllLinksBut(reloadLink);
            }

            @Override
            protected void onNeedsRestart(Server server) {
                needsRestart(resources.messages().serverNeedsRestart(server.getName()));
                disableAllLinksBut(restartLink);
            }

            @Override
            protected void onRunning(Server server) {
                running(resources.messages().serverRunning(server.getName()));
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(stopLink);
                }
            }

            @Override
            protected void onStopped(Server server) {
                alertContainer.className = alert + " " + alertInfo;
                alertIcon.className = Icons.STOPPED;
                alertText.innerHTML = resources.messages().serverStopped(server.getName()).asString();
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(startLink);
                }
            }

            @Override
            protected void onUnknown(Server server) {
                unknown(resources.messages().serverUndefined(server.getName()));
                disableAllLinks();
            }
        };
        sss.accept(server);

        ServerPreviewAttributes.refresh(server, attributes);
        if (server.isStarted()) {
            serverActions.readUrl(server, serverUrl);
        }
    }

    private void disableAllLinks() {
        for (HTMLElement l : links) {
            // Do not simply hide the links, but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            l.classList.add(hidden);
        }
    }

    private void disableAllLinksBut(HTMLElement link) {
        for (HTMLElement l : links) {
            if (l == link) {
                continue;
            }
            // Do not simply hide the links, but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            l.classList.add(hidden);
        }
    }
}
