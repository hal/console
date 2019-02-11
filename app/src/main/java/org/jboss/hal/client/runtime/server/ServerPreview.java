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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttributeFunction;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.server.ServerColumn.serverConfigTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.*;

class ServerPreview extends RuntimePreview<Server> {

    private static final AddressTemplate SELECTED_SERVER = AddressTemplate.of("{selected.host}/{selected.server}");
    private static final String ID_OPEN_PORTS = "open-ports";
    private static final String ID_HEADER_OPEN_PORTS = "h2-open-ports";

    private final ServerActions serverActions;
    private Dispatcher dispatcher;
    private EventBus eventBus;
    private Provider<Progress> progress;
    private StatementContext statementContext;
    private final HTMLElement startLink;
    private final HTMLElement stopLink;
    private final HTMLElement reloadLink;
    private final HTMLElement restartLink;
    private final HTMLElement resumeLink;
    private final HTMLElement bootErrorsLink;
    private final HTMLElement[] links;
    private final HTMLElement serverUrl;
    private final PreviewAttributes<Server> attributes;
    private HTMLElement ulOpenPorts = ul().id(ID_OPEN_PORTS).css(listGroup).get();
    private HTMLElement headerOpenPorts = h(2, resources.constants().openPorts()).id(ID_HEADER_OPEN_PORTS).get();

    ServerPreview(ServerActions serverActions,
            Server server,
            Dispatcher dispatcher,
            EventBus eventBus,
            Provider<Progress> progress,
            StatementContext statementContext,
            PlaceManager placeManager,
            Places places,
            FinderPathFactory finderPathFactory,
            Resources resources) {
        super(server.getName(), null, resources);
        this.serverActions = serverActions;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.statementContext = statementContext;

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
        if (server.isRunning() || server.needsRestart() || server.needsReload()) {
            previewBuilder().add(this.headerOpenPorts);
            previewBuilder().add(this.ulOpenPorts);
        }
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

        attributes.refresh(server);
        attributes.setVisible(PROFILE, server.isStarted());
        attributes.setVisible(URL, server.isStarted());
        attributes.setVisible(RUNNING_MODE, server.isStarted());
        attributes.setVisible(SERVER_STATE, server.isStarted());
        attributes.setVisible(SUSPEND_STATE, server.isStarted());

        boolean displayOpenPorts = server.isRunning() || server.needsRestart() || server.needsReload();
        if (displayOpenPorts) {
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(flowContext -> {
                ResourceAddress address = SELECTED_SERVER.resolve(statementContext);
                Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> flowContext.push(result.get(0).asString()))
                        .toCompletable();
            });

            tasks.add(flowContext -> {
                String socketBnding = flowContext.pop();
                ResourceAddress address = SELECTED_SERVER.resolve(statementContext)
                        .add(SOCKET_BINDING_GROUP, socketBnding)
                        .add(SOCKET_BINDING, "*");
                ModelNode select = new ModelNode();
                select.add("bound-port").add(NAME);
                ModelNode where = new ModelNode();
                where.set("bound", true);
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, select)
                        .param(WHERE, where)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            ModelNode openPortsModel = new ModelNode();
                            result.asList().forEach(m -> {
                                ModelNode sbModel = m.get(RESULT);
                                openPortsModel.add(sbModel.get(NAME).asString(), sbModel.get("bound-port").asInt());
                                flowContext.push(openPortsModel);
                            });
                        })
                        .toCompletable();
            });

            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                        @Override
                        public void onSuccess(FlowContext flowContext) {
                            ModelNode openPorts = flowContext.pop();
                            buildOpenPortsElement(openPorts);
                        }
                    });
            serverActions.readUrl(server, serverUrl);
        }
        Elements.setVisible(headerOpenPorts, displayOpenPorts);
        Elements.setVisible(ulOpenPorts, displayOpenPorts);
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

    private void buildOpenPortsElement(ModelNode ports) {
        Elements.removeChildrenFrom(ulOpenPorts);
        LabelBuilder labelBuilder = new LabelBuilder();
        ports.asPropertyList().forEach(prop -> {
            String label = labelBuilder.label(prop.getName());
            HTMLLIElement liState = li().css(listGroupItem)
                    .add(span().css(key).textContent(label))
                    .add(span().css(CSS.value).textContent(prop.getValue().asString()).get())
                    .get();
            ulOpenPorts.appendChild(liState);
        });
    }
}
