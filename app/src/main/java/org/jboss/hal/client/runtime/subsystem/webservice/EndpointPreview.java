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
package org.jboss.hal.client.runtime.subsystem.webservice;

import java.util.HashMap;
import java.util.Map;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.hal.core.Strings.abbreviateFqClassName;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class EndpointPreview extends PreviewContent<DeploymentResource> {

    private enum CountStatus {
        REQUEST, FAULT
    }


    private Dispatcher dispatcher;
    private PreviewAttributes<DeploymentResource> previewAttributes;
    private Donut requests;
    private LabelBuilder lblBuilder = new LabelBuilder();

    EndpointPreview(final FinderPathFactory finderPathFactory, final Places places,
            final DeploymentResource deploymentResource, final Dispatcher dispatcher, final Resources resources) {
        super(abbreviateFqClassName(deploymentResource.getName()), deploymentResource.getPath());
        this.dispatcher = dispatcher;

        getHeaderContainer().title = deploymentResource.getName();

        FinderPath path = finderPathFactory.deployment(deploymentResource.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(deploymentResource.getPath())
                .title(resources.messages().goTo(Names.DEPLOYMENTS))
                .asElement());


        previewAttributes = new PreviewAttributes<>(deploymentResource,
                asList("class", "type", "context", "response-count"
                ));
        previewAttributes
                .append(model -> previewAttribute("average-processing-time", model))
                .append(model -> previewAttribute("min-processing-time", model))
                .append(model -> previewAttribute("max-processing-time", model))
                .append(model -> previewAttribute("total-processing-time", model))
                .append(model -> {
                    String value = model.get(WSDL_URL).asString();
                    return new PreviewAttributes.PreviewAttribute("WSDL URL", value, value, true);
                });

        requests = new Donut.Builder(Names.REQUESTS)
                .add(CountStatus.REQUEST.name(), resources.constants().requests(), PatternFly.colors.green)
                .add(CountStatus.FAULT.name(), resources.constants().failed(), PatternFly.colors.red)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(requests);

        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));

        previewBuilder()
                .addAll(previewAttributes)
                .add(requests);
    }

    private PreviewAttributes.PreviewAttribute previewAttribute(String attribute, ModelNode model) {
        Long value = model.get(attribute).asLong();
        return new PreviewAttributes.PreviewAttribute(lblBuilder.label(attribute), value + " ms");
    }

    @Override
    public void update(final DeploymentResource item) {
        String endpointName = item.getAddress().lastValue();
        Operation operation = new Operation.Builder(item.getAddress().getParent(), READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ENDPOINT)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {

            for (Property prop : result.asPropertyList()) {
                if (prop.getName().equals(endpointName)) {
                    DeploymentResource n = new DeploymentResource(item.getAddress(), prop.getValue());
                    previewAttributes.refresh(n);

                    Map<String, Long> metricUpdates = new HashMap<>(7);
                    long request = prop.getValue().get("request-count").asLong();
                    long fault = prop.getValue().get("fault-count").asLong();
                    metricUpdates.put(CountStatus.REQUEST.name(), request);
                    metricUpdates.put(CountStatus.FAULT.name(), fault);
                    requests.update(metricUpdates);
                    break;
                }
            }
        });
    }
}
