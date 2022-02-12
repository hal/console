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
package org.jboss.hal.client.configuration;

import java.util.Iterator;

import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

class SocketBindingGroupPreview extends PreviewContent<NamedNode> {

    private final PreviewAttributes<NamedNode> attributes;

    @SuppressWarnings("HardCodedStringLiteral")
    SocketBindingGroupPreview(NamedNode socketBinding, Places places) {
        super(socketBinding.getName());

        attributes = new PreviewAttributes<>(socketBinding)
                .append(model -> {
                    String defaultInterface = model.get(DEFAULT_INTERFACE).asString();
                    PlaceRequest interfacePlaceRequest = places.finderPlace(NameTokens.CONFIGURATION,
                            new FinderPath()
                                    .append(Ids.CONFIGURATION, Ids.asId(Names.INTERFACES))
                                    .append(Ids.INTERFACE, defaultInterface))
                            .build();
                    String token = places.historyToken(interfacePlaceRequest);
                    return new PreviewAttribute(Names.DEFAULT_INTERFACE, defaultInterface, token);
                })
                .append(PORT_OFFSET)
                .append(model -> {
                    if (model.hasDefined(INCLUDES)) {
                        SafeHtmlBuilder html = new SafeHtmlBuilder();
                        for (Iterator<ModelNode> iterator = model.get(INCLUDES).asList().iterator(); iterator.hasNext();) {
                            String sbg = iterator.next().asString();
                            PlaceRequest sbgPlaceRequest = places.finderPlace(NameTokens.CONFIGURATION,
                                    new FinderPath()
                                            .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS))
                                            .append(Ids.SOCKET_BINDING_GROUP, sbg))
                                    .build();
                            String token = places.historyToken(sbgPlaceRequest);
                            html.appendHtmlConstant("<a href=\"").appendHtmlConstant(token).appendHtmlConstant("\">")
                                    .appendEscaped(sbg).appendHtmlConstant("</a>");
                            if (iterator.hasNext()) {
                                html.appendEscaped(", ");
                            }
                        }
                        return new PreviewAttribute(Names.INCLUDES_ATTRIBUTE, html.toSafeHtml());
                    } else {
                        return new PreviewAttribute(Names.INCLUDES_ATTRIBUTE, Names.NOT_AVAILABLE);
                    }
                });
        previewBuilder().addAll(attributes);

        PreviewAttributes<NamedNode> ports = new PreviewAttributes<>(socketBinding, Names.PORTS)
                .append(model -> new PreviewAttribute(Names.HTTP, port(model, HTTP)))
                .append(model -> new PreviewAttribute(Names.HTTPS, port(model, HTTPS)))
                .append(model -> new PreviewAttribute(Names.MANAGEMENT, port(model, MANAGEMENT_HTTP)))
                .append(model -> new PreviewAttribute(Names.SECURE_MANAGEMENT, port(model, MANAGEMENT_HTTPS)));
        previewBuilder().addAll(ports);
    }

    private String port(ModelNode modelNode, String subresource) {
        ModelNode port = failSafeGet(modelNode, SOCKET_BINDING + "/" + subresource + "/" + PORT);
        return port.isDefined() ? port.asString() : Names.NOT_AVAILABLE;
    }

    @Override
    public void update(final NamedNode item) {
        attributes.setVisible(INCLUDES, item.hasDefined(INCLUDES));
    }
}
