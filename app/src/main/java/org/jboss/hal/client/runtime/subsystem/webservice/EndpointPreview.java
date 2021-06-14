/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import org.jboss.hal.ballroom.chart.GroupedBar;
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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

class EndpointPreview extends PreviewContent<DeploymentResource> {

    private Dispatcher dispatcher;
    private PreviewAttributes<DeploymentResource> previewAttributes;
    private PreviewAttributes<DeploymentResource> totalProcessingTimeAttribute;
    private PreviewAttributes<DeploymentResource> responseAttribute;
    private GroupedBar processingTime;
    private Donut requests;
    private LabelBuilder lblBuilder = new LabelBuilder();

    EndpointPreview(FinderPathFactory finderPathFactory, Places places,
            DeploymentResource deploymentResource, Dispatcher dispatcher, Resources resources) {
        super(abbreviateFqClassName(deploymentResource.getName()), deploymentResource.getPath());
        this.dispatcher = dispatcher;

        getHeaderContainer().title = deploymentResource.getName();
        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));

        FinderPath path = finderPathFactory.deployment(deploymentResource.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(deploymentResource.getPath())
                .title(resources.messages().goTo(Names.DEPLOYMENTS)).element());

        previewAttributes = new PreviewAttributes<>(deploymentResource, asList(CLASS, TYPE, CONTEXT));
        previewAttributes.append(model -> {
            String value = model.get(WSDL_URL).asString();
            return new PreviewAttributes.PreviewAttribute(lblBuilder.label(WSDL_URL), value, value,
                    Ids.asId(deploymentResource.getName()));
        });

        processingTime = new GroupedBar.Builder(resources.constants().milliseconds())
                .add(MIN_PROCESSING_TIME, resources.constants().minimum(), PatternFly.colors.green)
                .add(AVERAGE_PROCESSING_TIME, resources.constants().average(), PatternFly.colors.blue)
                .add(MAX_PROCESSING_TIME, resources.constants().maximum(), PatternFly.colors.orange)
                .responsive(true)
                .horizontal()
                .build();
        registerAttachable(processingTime);
        totalProcessingTimeAttribute = new PreviewAttributes<>(deploymentResource, (String) null);
        totalProcessingTimeAttribute.append(model -> msAttribute(TOTAL_PROCESSING_TIME, model));

        requests = new Donut.Builder(Names.REQUESTS)
                .add(REQUEST_COUNT, Names.REQUESTS, PatternFly.colors.green)
                .add(FAULT_COUNT, resources.constants().failed(), PatternFly.colors.red)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(requests);
        responseAttribute = new PreviewAttributes<>(deploymentResource, null, singletonList(RESPONSE_COUNT));

        previewBuilder()
                .addAll(previewAttributes)
                .add(h(2, resources.constants().processingTime()))
                .add(processingTime)
                .addAll(totalProcessingTimeAttribute)
                .add(h(2, resources.constants().request() + " / " + resources.constants().response()))
                .add(requests)
                .addAll(responseAttribute);
    }

    private PreviewAttributes.PreviewAttribute msAttribute(String attribute, ModelNode model) {
        long value = model.get(attribute).asLong();
        return new PreviewAttributes.PreviewAttribute(lblBuilder.label(attribute), value + " ms"); //NON-NLS
    }

    @Override
    public void update(DeploymentResource item) {
        // the endpoint name (last value of address) contains %3A character separator
        // however the dispatcher.execute call performs a http call, then the url is encoded, the %3A becomes %253A
        // in the endpointa name, causing a HTTP 500 address not found.
        // Then, read the parent resoource and interate over the endpoint results to match the endpoint name.
        String endpointName = item.getAddress().lastValue();
        Operation operation = new Operation.Builder(item.getAddress().getParent(), READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ENDPOINT)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            for (Property prop : result.asPropertyList()) {
                if (prop.getName().equals(endpointName)) {
                    ModelNode node = prop.getValue();
                    DeploymentResource n = new DeploymentResource(item.getAddress(), node);
                    previewAttributes.refresh(n);

                    Map<String, Long> processingTimes = new HashMap<>();
                    processingTimes.put(MIN_PROCESSING_TIME, node.get(MIN_PROCESSING_TIME).asLong());
                    processingTimes.put(AVERAGE_PROCESSING_TIME, node.get(AVERAGE_PROCESSING_TIME).asLong());
                    processingTimes.put(MAX_PROCESSING_TIME, node.get(MAX_PROCESSING_TIME).asLong());
                    processingTime.update(processingTimes);
                    totalProcessingTimeAttribute.refresh(n);

                    Map<String, Long> metricUpdates = new HashMap<>(7);
                    metricUpdates.put(REQUEST_COUNT, node.get(REQUEST_COUNT).asLong());
                    metricUpdates.put(FAULT_COUNT, node.get(FAULT_COUNT).asLong());
                    requests.update(metricUpdates);
                    responseAttribute.refresh(n);
                    break;
                }
            }
        });
    }
}
