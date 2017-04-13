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
package org.jboss.hal.client.runtime.server;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.server.ServerColumn.serverConfigTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class ServerPreview extends RuntimePreview<Server> {

    private static final String START_LINK = "start-link";
    private static final String STOP_LINK = "stop-link";
    private static final String RESUME_LINK = "resume-link";
    private static final String BOOT_ERRORS_LINK = "boot-errors-link";

    private final ServerActions serverActions;
    private final Element startLink;
    private final Element stopLink;
    private final Element reloadLink;
    private final Element restartLink;
    private final Element resumeLink;
    private final Element bootErrorsLink;
    private final Element[] links;
    private final PreviewAttributes<Server> attributes;

    ServerPreview(final ServerActions serverActions, final Server server,
            final PlaceManager placeManager, final Places places, final FinderPathFactory finderPathFactory,
            final Resources resources) {
        super(server.getName(), null, resources);
        this.serverActions = serverActions;

        // @formatter:off
        previewBuilder()
            .div().rememberAs(ALERT_CONTAINER)
                .span().rememberAs(ALERT_ICON).end()
                .span().rememberAs(ALERT_TEXT).end()
                .span().textContent(" ").end()
                .a().rememberAs(START_LINK).css(clickable, alertLink)
                    .on(click, event -> serverActions.start(server))
                    .data(UIConstants.CONSTRAINT, Constraint.executable(serverConfigTemplate(server), START).data())
                    .textContent(resources.constants().start())
                .end()
                .a().rememberAs(STOP_LINK).css(clickable, alertLink)
                    .on(click, event -> serverActions.stop(server))
                    .data(UIConstants.CONSTRAINT, Constraint.executable(serverConfigTemplate(server), STOP).data())
                    .textContent(resources.constants().stop())
                .end()
                .a().rememberAs(RELOAD_LINK).css(clickable, alertLink)
                    .on(click, event -> serverActions.reload(server))
                    .data(UIConstants.CONSTRAINT, Constraint.executable(serverConfigTemplate(server), RELOAD).data())
                    .textContent(resources.constants().reload())
                .end()
                .a().rememberAs(RESTART_LINK).css(clickable, alertLink)
                    .on(click, event -> serverActions.restart(server))
                    .data(UIConstants.CONSTRAINT, Constraint.executable(serverConfigTemplate(server), RESTART).data())
                    .textContent(resources.constants().restart())
                .end()
                .a().rememberAs(RESUME_LINK).css(clickable, alertLink)
                    .on(click, event -> serverActions.resume(server))
                    .data(UIConstants.CONSTRAINT, Constraint.executable(serverConfigTemplate(server), RESUME).data())
                    .textContent(resources.constants().resume())
                .end()
                .a().rememberAs(BOOT_ERRORS_LINK).css(clickable, alertLink)
                    .on(click, event-> {
                        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_BOOT_ERRORS)
                                .with(HOST, server.getHost())
                                .with(SERVER, server.getName())
                                .build();
                        placeManager.revealPlace(placeRequest);
                    })
                    .textContent(resources.constants().view())
                .end()
            .end();
        // @formatter:on

        alertContainer = previewBuilder().referenceFor(ALERT_CONTAINER);
        alertIcon = previewBuilder().referenceFor(ALERT_ICON);
        alertText = previewBuilder().referenceFor(ALERT_TEXT);
        startLink = previewBuilder().referenceFor(START_LINK);
        stopLink = previewBuilder().referenceFor(STOP_LINK);
        reloadLink = previewBuilder().referenceFor(RELOAD_LINK);
        restartLink = previewBuilder().referenceFor(RESTART_LINK);
        resumeLink = previewBuilder().referenceFor(RESUME_LINK);
        bootErrorsLink = previewBuilder().referenceFor(BOOT_ERRORS_LINK);
        links = new Element[]{startLink, stopLink, reloadLink, restartLink, resumeLink, bootErrorsLink};

        if (server.isStandalone()) {
            this.attributes = new PreviewAttributes<>(server, asList(STATUS, RUNNING_MODE, SERVER_STATE, SUSPEND_STATE))
                    .end();
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
                    .append(AUTO_START)
                    .append(SOCKET_BINDING_PORT_OFFSET)
                    .append(STATUS)
                    .append(RUNNING_MODE)
                    .append(SERVER_STATE)
                    .append(SUSPEND_STATE)
                    .end();
        }
        previewBuilder().addAll(this.attributes);

        update(server);
    }

    @Override
    public void update(final Server server) {
        ServerStatusSwitch sss = new ServerStatusSwitch(serverActions) {
            @Override
            protected void onPending(final Server server) {
                pending(resources.messages().serverPending(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onBootErrors(final Server server) {
                error(resources.messages().serverBootErrors(server.getName()));
                disableAllLinksBut(bootErrorsLink);
            }

            @Override
            protected void onFailed(final Server server) {
                error(resources.messages().serverFailed(server.getName()));
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(startLink);
                }
            }

            @Override
            protected void onAdminMode(final Server server) {
                adminOnly(resources.messages().serverAdminMode(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onStarting(final Server server) {
                adminOnly(resources.messages().serverAdminMode(server.getName()));
                disableAllLinks();
            }

            @Override
            protected void onSuspended(final Server server) {
                suspended(resources.messages().serverSuspended(server.getName()));
                disableAllLinksBut(resumeLink);
            }

            @Override
            protected void onNeedsReload(final Server server) {
                needsReload(resources.messages().serverNeedsReload(server.getName()));
                disableAllLinksBut(reloadLink);
            }

            @Override
            protected void onNeedsRestart(final Server server) {
                needsRestart(resources.messages().serverNeedsRestart(server.getName()));
                disableAllLinksBut(restartLink);
            }

            @Override
            protected void onRunning(final Server server) {
                running(resources.messages().serverRunning(server.getName()));
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(stopLink);
                }
            }

            @Override
            protected void onStopped(final Server server) {
                alertContainer.setClassName(alert + " " + alertInfo);
                alertIcon.setClassName(Icons.STOPPED);
                alertText.setInnerHTML(resources.messages().serverStopped(server.getName()).asString());
                if (server.isStandalone()) {
                    disableAllLinks();
                } else {
                    disableAllLinksBut(startLink);
                }
            }

            @Override
            protected void onUnknown(final Server server) {
                unknown(resources.messages().serverUndefined(server.getName()));
                disableAllLinks();
            }
        };
        sss.accept(server);

        attributes.refresh(server);
        attributes.setVisible(PROFILE, server.isStarted());
        attributes.setVisible(RUNNING_MODE, server.isStarted());
        attributes.setVisible(SERVER_STATE, server.isStarted());
        attributes.setVisible(SUSPEND_STATE, server.isStarted());
    }

    private void disableAllLinks() {
        for (Element l : links) {
            // Do not simply hide the links, but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            l.getClassList().add(hidden);
        }
    }

    private void disableAllLinksBut(Element link) {
        for (Element l : links) {
            if (l == link) {
                continue;
            }
            // Do not simply hide the links, but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            l.getClassList().add(hidden);
        }
    }
}
