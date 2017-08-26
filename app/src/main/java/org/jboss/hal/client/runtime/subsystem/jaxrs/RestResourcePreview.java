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
package org.jboss.hal.client.runtime.subsystem.jaxrs;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Splitter;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.CSSProperties.MarginBottomUnionType;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromSafeConstant;
import static elemental2.dom.DomGlobal.document;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.core.Strings.abbreviateFqClassName;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.resources.CSS.*;

class RestResourcePreview extends PreviewContent<RestResource> {

    RestResourcePreview(RestResource restResource, FinderPathFactory finderPathFactory, Places places, Resources r) {
        super(abbreviateFqClassName(restResource.getName()), restResource.getPath());

        getHeaderContainer().title = restResource.getName();
        getHeaderContainer().textContent = abbreviateFqClassName(restResource.getName());

        FinderPath path = finderPathFactory.deployment(restResource.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(restResource.getPath())
                .title(r.messages().goTo(Names.DEPLOYMENTS))
                .asElement());
        getLeadElement().style.marginBottom = MarginBottomUnionType.of(0);

        List<ModelNode> resourcePaths = failSafeList(restResource, REST_RESOURCE_PATHS);
        if (!resourcePaths.isEmpty()) {
            previewBuilder().add(h(2, Names.RESOURCE_PATHS));
            resourcePaths.stream()
                    .collect(groupingBy(node -> node.get(RESOURCE_PATH).asString()))
                    .forEach((resourcePath, resources) -> {
                        HTMLElement body;
                        previewBuilder().add(
                                div().css(CSS.panel, panelDefault)
                                        .add(div().css(panelHeading)
                                                .add(h(3, resourcePath).css(panelTitle)))
                                        .add(body = div().css(panelBody, restResources).asElement()));
                        for (Iterator<ModelNode> iterator = resources.iterator(); iterator.hasNext(); ) {
                            ModelNode resource = iterator.next();
                            if (resource.hasDefined(CONSUMES)) {
                                appendMediaTypes(body, resource, CONSUMES, "&rarr;");
                            }
                            if (resource.hasDefined(PRODUCES)) {
                                appendMediaTypes(body, resource, PRODUCES, "&larr;");
                            }
                            if (resource.hasDefined(RESOURCE_METHODS)) {
                                List<String> resourceMethods = failSafeList(resource, RESOURCE_METHODS).stream()
                                        .map(ModelNode::asString)
                                        .collect(toList());
                                HTMLElement p;
                                body.appendChild(p = p().asElement());
                                for (Iterator<String> rmIterator = resourceMethods.iterator(); rmIterator.hasNext(); ) {
                                    String resourceMethod = rmIterator.next();
                                    if (resourceMethod.contains(" ")) {
                                        List<String> parts = Splitter.on(' ').limit(2).splitToList(resourceMethod);
                                        if (parts.size() == 2) {
                                            p.appendChild(strong().textContent(parts.get(0)).asElement());
                                            p.appendChild(document.createTextNode(" " + parts.get(1)));
                                        } else {
                                            p.appendChild(document.createTextNode(resourceMethod));
                                        }
                                    } else {
                                        p.appendChild(document.createTextNode(resourceMethod));
                                    }
                                    if (rmIterator.hasNext()) {
                                        p.appendChild(br().asElement());
                                    }
                                }
                            }
                            if (resource.hasDefined(JAVA_METHOD)) {
                                body.appendChild(pre().css(prettyPrint, langJava)
                                        .style("white-space:pre-wrap") //NON-NLS
                                        .textContent(resource.get(JAVA_METHOD).asString())
                                        .asElement());
                            }
                            if (iterator.hasNext()) {
                                body.appendChild(hr().asElement());
                            }
                        }
                    });
        }

        List<ModelNode> subResourceLocators = failSafeList(restResource, SUB_RESOURCE_LOCATORS);
        if (!subResourceLocators.isEmpty()) {
            previewBuilder().add(h(2, Names.SUB_RESOURCE_LOCATORS));
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void appendMediaTypes(HTMLElement body, ModelNode resource, String type, @NonNls String arrow) {
        String consumes = failSafeList(resource, type).stream()
                .map(ModelNode::asString)
                .collect(joining(", "));
        body.appendChild(p()
                .add(span().title(type)
                        .style("cursor:help")
                        .innerHtml(fromSafeConstant(arrow)))
                .add(" " + consumes)
                .asElement());
    }

    @Override
    public void attach() {
        super.attach();
        PatternFly.prettyPrint();
    }
}
