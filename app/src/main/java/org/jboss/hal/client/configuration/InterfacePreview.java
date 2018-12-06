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
package org.jboss.hal.client.configuration;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

class InterfacePreview extends PreviewContent<NamedNode> {

    private final Dispatcher dispatcher;
    private final Places places;
    private final HTMLElement links;

    InterfacePreview(NamedNode interfce, Dispatcher dispatcher, Places places) {
        super(interfce.getName());
        this.dispatcher = dispatcher;
        this.places = places;
        this.links = span().get();

        PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(interfce)
                .append(INET_ADDRESS)
                .append(model -> new PreviewAttributes.PreviewAttribute(Names.SOCKET_BINDING_GROUPS, links));
        previewBuilder().addAll(attributes);
    }

    @Override
    public void update(final NamedNode item) {
        Operation operation = new Operation.Builder(new ResourceAddress().add(SOCKET_BINDING_GROUP, "*"), QUERY)
                .param(SELECT, new ModelNode().add(NAME))
                .param(WHERE, new ModelNode().set(DEFAULT_INTERFACE, item.getName()))
                .build();
        dispatcher.execute(operation, result -> {
            List<String> socketBindingGroups = result.asList().stream()
                    .filter(modelNode -> !modelNode.isFailure())
                    .map(modelNode -> failSafeGet(modelNode, RESULT + "/" + NAME))
                    .filter(ModelNode::isDefined)
                    .map(ModelNode::asString)
                    .sorted()
                    .collect(toList());
            if (!socketBindingGroups.isEmpty()) {
                @NonNls SafeHtmlBuilder html = new SafeHtmlBuilder();
                for (Iterator<String> iterator = socketBindingGroups.iterator(); iterator.hasNext(); ) {
                    String sbg = iterator.next();
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
                links.innerHTML = html.toSafeHtml().asString();
            } else {
                links.textContent = Names.NOT_AVAILABLE;
            }
        });
    }
}
