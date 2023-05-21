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
package org.jboss.hal.client.deployment;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.bag;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.deployment.ServerGroupDeploymentColumn.SERVER_GROUP_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.Icons.flag;

class ContentPreview extends PreviewContent<Content> {

    private final ContentColumn column;
    private final Places places;
    private final Resources resources;
    private final AuthorisationDecision authorisationDecision;
    private final PreviewAttributes<Content> attributes;
    private final HTMLElement deploymentsDiv;
    private final HTMLElement deploymentsUl;
    private final HTMLElement undeployedContentDiv;
    private final HTMLElement infoExplodedDiv;

    ContentPreview(ContentColumn column, Content content, Environment environment, Places places,
            Metadata serverGroupMetadata, Resources resources) {
        super(content.getName());
        this.column = column;
        this.places = places;
        this.resources = resources;
        this.authorisationDecision = AuthorisationDecision.from(environment, serverGroupMetadata.getSecurityContext());

        if (!content.isManaged()) {
            previewBuilder().add(new Alert(Icons.INFO, resources.messages().cannotBrowseUnmanaged()).element());
        }
        previewBuilder().add(
                infoExplodedDiv = new Alert(Icons.INFO, resources.messages().cannotDownloadExploded()).element());

        LabelBuilder labelBuilder = new LabelBuilder();
        attributes = new PreviewAttributes<>(content, asList(NAME, RUNTIME_NAME));
        attributes.append(model -> {
            String label = String.join(", ", labelBuilder.label(MANAGED), labelBuilder.label(EXPLODED));
            Iterable<HTMLElement> elements = bag()
                    .add(span()
                            .title(labelBuilder.label(MANAGED))
                            .css(flag(failSafeBoolean(model, MANAGED)), marginRight5))
                    .add(span()
                            .title(labelBuilder.label(EXPLODED))
                            .css(flag(failSafeBoolean(model, EXPLODED))))
                    .elements();
            return new PreviewAttribute(label, elements);
        });
        if (!content.isManaged()) {
            attributes.append(model -> {
                String pathValue = model.get(CONTENT).asList().get(0).get(PATH).asString();
                return new PreviewAttributes.PreviewAttribute(labelBuilder.label(PATH), pathValue);
            });
        }
        previewBuilder().addAll(attributes);

        HTMLElement p;
        previewBuilder()
                .add(h(2).textContent(resources.constants().deployments()))
                .add(deploymentsDiv = div()
                        .add(p().innerHtml(resources.messages().deployedTo(content.getName())))
                        .add(deploymentsUl = ul().element()).element())
                .add(undeployedContentDiv = div()
                        .add(p = p()
                                .add(span()
                                        .innerHtml(resources.messages().undeployedContent(content.getName())))
                                .element())
                        .element());
        if (authorisationDecision.isAllowed(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))) {
            p.appendChild(a().css(clickable, marginLeft5).on(click, event -> column.deploy(content))
                    .textContent(resources.constants().deploy()).element());
        }
    }

    @Override
    public void update(Content content) {
        attributes.refresh(content);

        boolean undeployed = content.getServerGroupDeployments().isEmpty();
        boolean unmanaged = !content.isManaged();
        boolean exploded = content.isExploded();
        Elements.setVisible(deploymentsDiv, !undeployed);
        Elements.setVisible(undeployedContentDiv, undeployed);
        Elements.setVisible(infoExplodedDiv, !unmanaged && exploded);
        if (!undeployed) {
            Elements.removeChildrenFrom(deploymentsUl);
            content.getServerGroupDeployments().forEach(sgd -> {
                String serverGroup = sgd.getServerGroup();
                PlaceRequest serverGroupPlaceRequest = places.finderPlace(NameTokens.DEPLOYMENTS, new FinderPath()
                        .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                        .append(Ids.DEPLOYMENT_SERVER_GROUP, Ids.serverGroup(serverGroup))
                        .append(Ids.SERVER_GROUP_DEPLOYMENT, Ids.serverGroupDeployment(serverGroup, content.getName())))
                        .build();
                String serverGroupToken = places.historyToken(serverGroupPlaceRequest);
                HTMLElement li = li()
                        .add(a(serverGroupToken).textContent(serverGroup)).element();
                if (authorisationDecision.isAllowed(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))) {
                    li.appendChild(span().textContent(" (").element());
                    li.appendChild(a().css(clickable)
                            .on(click, event -> column.undeploy(content, serverGroup))
                            .textContent(resources.constants().undeploy()).element());
                    li.appendChild(span().textContent(")").element());
                }
                deploymentsUl.appendChild(li);
            });
        }
    }
}
