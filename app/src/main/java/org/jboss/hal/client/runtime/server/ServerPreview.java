/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.server;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
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
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.runtime.server.ServerColumn.serverConfigTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTO_START;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNEL_VERSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_INFO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESUME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_PORT_OFFSET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STOP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUMMARY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHERE;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.alertInfo;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;
import static org.jboss.hal.resources.CSS.key;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listGroupItem;
import static org.jboss.hal.resources.CSS.value;

class ServerPreview extends RuntimePreview<Server> {

    private static final AddressTemplate SELECTED_SERVER = AddressTemplate.of("{selected.host}/{selected.server}");
    private static final String ID_OPEN_PORTS = "open-ports";
    private static final String ID_HEADER_OPEN_PORTS = "h2-open-ports";
    private static final String ID_HEADER_CHANNEL_VERSIONS = "h2-channel-versions";
    private static final String ID_CHANNEL_VERSIONS = "channel-versions";

    private final ServerActions serverActions;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final HTMLElement startLink;
    private final HTMLElement stopLink;
    private final HTMLElement reloadLink;
    private final HTMLElement restartLink;
    private final HTMLElement resumeLink;
    private final HTMLElement bootErrorsLink;
    private final HTMLElement[] links;
    private final HTMLElement serverUrl;
    private final PreviewAttributes<Server> attributes;
    private final HTMLElement ulOpenPorts = ul().id(ID_OPEN_PORTS).css(listGroup).element();
    private final HTMLElement headerOpenPorts = h(2, resources.constants().openPorts()).id(ID_HEADER_OPEN_PORTS).element();
    private final HTMLElement headerChannelVersions = h(2, new LabelBuilder().label(CHANNEL_VERSIONS)).id(
            ID_HEADER_CHANNEL_VERSIONS).element();
    private final HTMLElement ulChannelVersions = ul().id(ID_CHANNEL_VERSIONS).css(listGroup).element();

    ServerPreview(ServerActions serverActions,
            Server server,
            Dispatcher dispatcher,
            Provider<Progress> progress,
            StatementContext statementContext,
            PlaceManager placeManager,
            Places places,
            FinderPathFactory finderPathFactory,
            Resources resources) {
        super(server.getName(), null, resources);
        this.serverActions = serverActions;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.statementContext = statementContext;

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().element())
                        .add(alertText = span().element())
                        .add(span().textContent(" "))

                        .add(startLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.start(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), START).data())
                                .textContent(resources.constants().start()).element())
                        .add(stopLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.stop(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), STOP).data())
                                .textContent(resources.constants().stop()).element())
                        .add(reloadLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.reload(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RELOAD).data())
                                .textContent(resources.constants().reload()).element())
                        .add(restartLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.restart(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RESTART).data())
                                .textContent(resources.constants().restart()).element())
                        .add(resumeLink = a().css(clickable, alertLink)
                                .on(click, event -> serverActions.resume(server))
                                .data(UIConstants.CONSTRAINT,
                                        Constraint.executable(serverConfigTemplate(server), RESUME).data())
                                .textContent(resources.constants().resume()).element())
                        .add(bootErrorsLink = a().css(clickable, alertLink)
                                .on(click, event -> {
                                    PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(
                                                    NameTokens.SERVER_BOOT_ERRORS)
                                            .with(HOST, server.getHost())
                                            .with(SERVER, server.getName())
                                            .build();
                                    placeManager.revealPlace(placeRequest);
                                })
                                .textContent(resources.constants().view()).element())
                        .element());

        links = new HTMLElement[]{startLink, stopLink, reloadLink, restartLink, resumeLink, bootErrorsLink};

        serverUrl = span().textContent(Names.NOT_AVAILABLE).element();
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
        setVisible(headerChannelVersions, false);
        setVisible(ulChannelVersions, false);
        previewBuilder().add(headerChannelVersions);
        previewBuilder().add(ulChannelVersions);
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

        boolean displayOpenPorts = server.isRunning() || server.needsRestart() || server.needsReload();
        if (displayOpenPorts) {
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(flowContext -> {
                ResourceAddress address = SELECTED_SERVER.resolve(statementContext);
                Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                        .build();
                return dispatcher.execute(operation)
                        .then(result -> flowContext.resolve(result.get(0).asString()));
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
                        .then(result -> {
                            ModelNode openPortsModel = new ModelNode();
                            result.asList().forEach(m -> {
                                ModelNode sbModel = m.get(RESULT);
                                openPortsModel.add(sbModel.get(NAME).asString(), sbModel.get("bound-port").asInt());
                                flowContext.push(openPortsModel);
                            });
                            return Promise.resolve(flowContext);
                        });
            });

            sequential(new FlowContext(progress.get()), tasks)
                    .then(flowContext -> {
                        ModelNode openPorts = flowContext.pop();
                        buildOpenPortsElement(openPorts);
                        return null;
                    });
            serverActions.readUrl(server, serverUrl);
        }
        Elements.setVisible(headerOpenPorts, displayOpenPorts);
        Elements.setVisible(ulOpenPorts, displayOpenPorts);

        if (displayOpenPorts) {
            dispatcher.execute(new Operation.Builder(server.getServerAddress(), PRODUCT_INFO).build()).then(result -> {
                boolean showChannelVersions = false;
                if (result.isDefined()) {
                    if (!result.asList().isEmpty()) {
                        ModelNode payload = result.asList().get(0);
                        if (payload.hasDefined(SUMMARY)) {
                            ModelNode summary = payload.get(SUMMARY);
                            if (summary.hasDefined(CHANNEL_VERSIONS)) {
                                Elements.removeChildrenFrom(ulChannelVersions);
                                summary.get(CHANNEL_VERSIONS)
                                        .asList()
                                        .forEach(cv -> ulChannelVersions.appendChild(li().css(listGroupItem)
                                                .add(span().css(value).style("margin-left:0")
                                                        .textContent(cv.asString()).element())
                                                .element()));
                                showChannelVersions = true;
                            }
                        }
                    }
                }
                setVisible(headerChannelVersions, showChannelVersions);
                setVisible(ulChannelVersions, showChannelVersions);
                return null;
            });
        }
    }

    private void disableAllLinks() {
        for (HTMLElement l : links) {
            // Do not simply hide the links but add the hidden CSS class.
            // Important when constraints for the links are processed later.
            l.classList.add(hidden);
        }
    }

    private void disableAllLinksBut(HTMLElement link) {
        for (HTMLElement l : links) {
            if (l == link) {
                continue;
            }
            // Do not simply hide the links but add the hidden CSS class.
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
                    .add(span().css(value).textContent(prop.getValue().asString()).element()).element();
            ulOpenPorts.appendChild(liState);
        });
    }
}
