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
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.client.deployment.Deployment.Status;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.deployment.DeploymentPreview.LAST_DISABLED_AT;
import static org.jboss.hal.client.deployment.DeploymentPreview.LAST_ENABLED_AT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class ServerGroupDeploymentPreview extends PreviewContent<ServerGroupDeployment> {

    ServerGroupDeploymentPreview(final ServerGroupDeploymentColumn column, final ServerGroupDeployment sgd,
            final Places places, final Resources resources) {
        super(sgd.getName());

        Deployment deployment = sgd.getDeployment();
        if (deployment != null) {
            if (deployment.getStatus() == Status.FAILED) {
                previewBuilder().add(new Alert(Icons.ERROR, resources.messages().deploymentFailed(sgd.getName())));
            } else if (deployment.getStatus() == Status.STOPPED) {
                previewBuilder().add(new Alert(Icons.STOPPED, resources.messages().deploymentStopped(sgd.getName()),
                        resources.constants().enable(), event -> column.enable(sgd)));
            } else if (deployment.getStatus() == Status.OK) {
                previewBuilder().add(new Alert(Icons.OK, resources.messages().deploymentActive(sgd.getName()),
                        resources.constants().disable(), event -> column.disable(sgd)));
            } else {
                previewBuilder()
                        .add(new Alert(Icons.UNKNOWN, resources.messages().deploymentUnknownState(sgd.getName()),
                                resources.constants().disable(), event -> column.disable(sgd)));
            }
        } else {
            if (sgd.isEnabled()) {
                previewBuilder().add(new Alert(Icons.OK, resources.messages().deploymentEnabled(sgd.getName()),
                        resources.constants().disable(), event -> column.disable(sgd)));
            } else {
                previewBuilder()
                        .add(new Alert(Icons.DISABLED, resources.messages().deploymentDisabled(sgd.getName()),
                                resources.constants().enable(), event -> column.enable(sgd)));
            }
        }

        // main attributes
        PreviewAttributes<ServerGroupDeployment> attributes = new PreviewAttributes<>(sgd,
                asList(NAME, RUNTIME_NAME));
        attributes.append(model -> {
            PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, new FinderPath()
                    .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(resources.constants().contentRepository()))
                    .append(Ids.CONTENT, Ids.content(model.getName())))
                    .build();
            return new PreviewAttribute(resources.constants().providedBy(), model.getName(),
                    places.historyToken(placeRequest));
        });
        attributes.append(MANAGED);
        attributes.append(EXPLODED);
        attributes.append(ENABLED);
        if (deployment != null) {
            attributes.append(model -> new PreviewAttribute(new LabelBuilder().label(STATUS),
                    deployment.getStatus().name()));
            attributes.append(model -> new PreviewAttribute(LAST_ENABLED_AT, deployment.getEnabledTime()));
            attributes.append(model -> new PreviewAttribute(LAST_DISABLED_AT, deployment.getDisabledTime()));
        }
        attributes.end();
        previewBuilder().addAll(attributes);

        // sub-deployments
        if (deployment != null && deployment.hasSubdeployments()) {
            previewBuilder().h(2).textContent(Names.SUBDEPLOYMENTS).end().ul();
            deployment.getSubdeployments().forEach(
                    subdeployment -> previewBuilder().li().textContent(subdeployment.getName()).end());
            previewBuilder().end();
        }

        // reference server
        if (deployment == null) {
            previewBuilder().h(2).textContent(resources.constants().noReferenceServer()).end();
            String serverGroup = sgd.getServerGroup();
            PlaceRequest serverGroupPlaceRequest = places.finderPlace(NameTokens.RUNTIME, new FinderPath()
                    .append(Ids.DOMAIN_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                    .append(Ids.SERVER_GROUP, Ids.serverGroup(serverGroup)))
                    .build();
            String serverGroupHistoryToken = places.historyToken(serverGroupPlaceRequest);
            LabelBuilder labelBuilder = new LabelBuilder();
            previewBuilder().p().innerHtml(resources.messages().noReferenceServerPreview(sgd.getName(),
                    labelBuilder.label(STATUS), labelBuilder.label(LAST_ENABLED_AT),
                    serverGroup, serverGroupHistoryToken))
                    .end();
        }
    }
}
