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
package org.jboss.hal.client.runtime;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jboss.elemento.ElementsBag;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.host.HostPreviewAttributes;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerPreviewAttributes;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.base.Strings;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTO_START;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISCONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAST_CONNECTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELEASE_CODENAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELEASE_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_DEFAULT_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_PORT_OFFSET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;

class TopologyAttributes {

    private final Places places;
    private final HostActions hostActions;
    private final ServerActions serverActions;
    private final Resources resources;
    private final PreviewAttributes<Host> hostAttributes;
    private final PreviewAttributes<ServerGroup> serverGroupAttributes;
    private final PreviewAttributes<Server> serverAttributes;
    private final HTMLElement hostAttributesSection;
    private final HTMLElement serverGroupAttributesSection;
    private final HTMLElement serverAttributesSection;
    private final HTMLElement serverUrl;

    TopologyAttributes(
            Places places,
            FinderPathFactory finderPathFactory,
            HostActions hostActions,
            ServerActions serverActions,
            Resources resources) {
        this.places = places;
        this.hostActions = hostActions;
        this.serverActions = serverActions;
        this.resources = resources;

        LabelBuilder labelBuilder = new LabelBuilder();
        hostAttributes = new PreviewAttributes<>(new Host(new ModelNode()), Names.HOST)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, ModelNode::isDefined,
                            m -> finderPathFactory.runtimeHostPath(m.getAddressName()));
                    return new PreviewAttributes.PreviewAttribute(resources.constants().name(), model.getName(), token);
                })
                .append(RELEASE_CODENAME)
                .append(RELEASE_VERSION)
                .append(PRODUCT_NAME)
                .append(PRODUCT_VERSION)
                .append(HOST_STATE)
                .append(RUNNING_MODE)
                .append(model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(MANAGEMENT_VERSION),
                        model.getManagementVersion().toString()))
                .append(model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(LAST_CONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getLastConnected())
                                : Names.NOT_AVAILABLE))
                .append(model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(DISCONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getDisconnected())
                                : Names.NOT_AVAILABLE));
        hostAttributesSection = section().addAll(hostAttributes).element();

        serverGroupAttributes = new PreviewAttributes<>(new ServerGroup("", new ModelNode()), Names.SERVER_GROUP)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, ModelNode::isDefined,
                            m -> finderPathFactory.runtimeServerGroupPath(m.getName()));
                    return new PreviewAttributes.PreviewAttribute(resources.constants().name(), model.getName(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, ModelNode::isDefined,
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                    .append(Ids.PROFILE, m.getProfile()));
                    return new PreviewAttributes.PreviewAttribute(Names.PROFILE, model.getProfile(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, ModelNode::isDefined,
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS))
                                    .append(Ids.SOCKET_BINDING_GROUP, model.get(SOCKET_BINDING_GROUP).asString()));
                    return new PreviewAttributes.PreviewAttribute(Names.SOCKET_BINDING_GROUP,
                            model.get(SOCKET_BINDING_GROUP).asString(),
                            token);
                })
                .append(SOCKET_BINDING_PORT_OFFSET)
                .append(SOCKET_BINDING_DEFAULT_INTERFACE);
        serverGroupAttributesSection = section().addAll(serverGroupAttributes).element();

        serverUrl = span().textContent(Names.NOT_AVAILABLE).element();
        serverAttributes = new PreviewAttributes<>(new Server("", new ModelNode()), Names.SERVER)
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeServerPath(model.getHost(), model.getName()));
                    return new PreviewAttributes.PreviewAttribute(resources.constants().name(), model.getName(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeHostPath(model.getHost()));
                    return new PreviewAttributes.PreviewAttribute(Names.HOST, model.getHost(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.RUNTIME, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> finderPathFactory.runtimeServerGroupPath(model.getServerGroup()));
                    return new PreviewAttributes.PreviewAttribute(Names.SERVER_GROUP, model.getServerGroup(), token);
                })
                .append(model -> {
                    String token = lazyToken(NameTokens.CONFIGURATION, model, m -> !Strings.isNullOrEmpty(m.getHost()),
                            m -> new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                                    .append(Ids.PROFILE, model.get(PROFILE_NAME).asString()));
                    return new PreviewAttributes.PreviewAttribute(Names.PROFILE, model.get(PROFILE_NAME).asString(), token);
                })
                .append(model -> new PreviewAttributes.PreviewAttribute(Names.URL, serverUrl))
                .append(AUTO_START)
                .append(SOCKET_BINDING_PORT_OFFSET)
                .append(STATUS)
                .append(RUNNING_MODE)
                .append(SERVER_STATE)
                .append(SUSPEND_STATE);
        serverAttributesSection = section().addAll(serverAttributes).element();
    }

    private <T extends NamedNode> String lazyToken(String tlc, T model,
            Predicate<T> defined, Function<T, FinderPath> path) {
        String token = "";
        if (defined.test(model)) {
            PlaceRequest placeRequest = places.finderPlace(tlc, path.apply(model)).build();
            token = places.historyToken(placeRequest);
        }
        return token;
    }

    void addTo(ElementsBag previewBuilder) {
        previewBuilder
                .add(hostAttributesSection)
                .add(serverGroupAttributesSection)
                .add(serverAttributesSection);

    }

    void hideAll() {
        setVisible(serverGroupAttributesSection, false);
        setVisible(hostAttributesSection, false);
        setVisible(serverAttributesSection, false);
    }

    void refreshHost(Host host) {
        HostPreviewAttributes.refresh(host, hostAttributes, hostActions);
        setVisible(serverGroupAttributesSection, false);
        setVisible(hostAttributesSection, true);
        setVisible(serverAttributesSection, false);
    }

    void refreshServerGroup(ServerGroup serverGroup) {
        serverGroupAttributes.refresh(serverGroup);
        setVisible(serverGroupAttributesSection, true);
        setVisible(hostAttributesSection, false);
        setVisible(serverAttributesSection, false);
    }

    void refreshServer(Server server) {
        if (server.hasBootErrors()) {
            PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.SERVER_BOOT_ERRORS)
                    .with(HOST, server.getHost())
                    .with(SERVER, server.getName())
                    .build();
            String token = places.historyToken(placeRequest);
            serverAttributes.setDescription(resources.messages().serverBootErrorsAndLink(server.getName(), token));
        } else {
            serverAttributes.hideDescription();
        }

        ServerPreviewAttributes.refresh(server, serverAttributes);
        setVisible(serverGroupAttributesSection, false);
        setVisible(hostAttributesSection, false);
        setVisible(serverAttributesSection, true);

        if (server.isStarted()) {
            serverActions.readUrl(server, serverUrl);
        }
    }
}
