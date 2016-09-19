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
package org.jboss.hal.client.deployment;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;
import static org.jboss.hal.resources.CSS.clickable;

/**
 * @author Harald Pehl
 */
class ContentPreview extends PreviewContent<Content> {

    private static final String DEPLOYMENTS_DIV = "deploymentsDiv";
    private static final String DEPLOYMENTS_UL = "deploymentsUl";

    private final ContentColumn column;
    private final Places places;
    private final Resources resources;
    private final PreviewAttributes<Content> attributes;
    private final Element deploymentsDiv;
    private final Element deploymentsUl;

    ContentPreview(final ContentColumn column, final Content content, final Places places, final Resources resources) {
        super(content.getName());
        this.column = column;
        this.places = places;
        this.resources = resources;

        if (content.getServerGroupDeployments().isEmpty()) {
            previewBuilder().add(
                    new Alert(Icons.DISABLED, resources.messages().undeployedContent(content.getName()),
                            resources.constants().deploy(), event -> column.deploy(content)));
        }

        attributes = new PreviewAttributes<>(content,
                asList(NAME, RUNTIME_NAME, MANAGED, EXPLODED)).end();
        previewBuilder().addAll(attributes);

        previewBuilder()
                .div().rememberAs(DEPLOYMENTS_DIV)
                .h(2).textContent(resources.constants().deployments()).end()
                .p().innerHtml(resources.messages().deployedTo(content.getName())).end()
                .ul().rememberAs(DEPLOYMENTS_UL).end()
                .end();
        deploymentsDiv = previewBuilder().referenceFor(DEPLOYMENTS_DIV);
        deploymentsUl = previewBuilder().referenceFor(DEPLOYMENTS_UL);
    }

    @Override
    public void update(final Content content) {
        attributes.refresh(content);
        Elements.setVisible(deploymentsDiv, !content.getServerGroupDeployments().isEmpty());
        if (!content.getServerGroupDeployments().isEmpty()) {
            Elements.removeChildrenFrom(deploymentsUl);
            Elements.Builder builder = new Elements.Builder();
            content.getServerGroupDeployments().forEach(sgd -> {
                String serverGroup = sgd.getServerGroup();
                PlaceRequest serverGroupPlaceRequest = places.finderPlace(NameTokens.DEPLOYMENTS, new FinderPath()
                        .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                        .append(Ids.DEPLOYMENT_SERVER_GROUP, Ids.serverGroup(serverGroup))
                        .append(Ids.SERVER_GROUP_DEPLOYMENT, Ids.serverGroupDeployment(serverGroup, content.getName())))
                        .build();
                String serverGroupToken = places.historyToken(serverGroupPlaceRequest);
                // @formatter:off
                builder
                    .li()
                        .a(serverGroupToken).textContent(serverGroup).end()
                        .span().textContent(" (").end()
                        .a().css(clickable)
                            .on(click, event -> column.undeploy(content, serverGroup))
                            .textContent(resources.constants().undeploy())
                        .end()
                        .span().textContent(")").end()
                    .end();
                // @formatter:on
                deploymentsUl.appendChild(builder.build());
            });
        }
    }
}
