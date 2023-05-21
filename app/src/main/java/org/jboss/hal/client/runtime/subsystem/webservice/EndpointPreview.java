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
package org.jboss.hal.client.runtime.subsystem.webservice;

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
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
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_CONFIGURATION_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AVERAGE_PROCESSING_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENDPOINT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FAULT_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX_PROCESSING_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIN_PROCESSING_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TOTAL_PROCESSING_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WSDL_URL;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

class EndpointPreview extends PreviewContent<DeploymentResource> {

    private EmptyState noStatistics;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final PreviewAttributes<DeploymentResource> previewAttributes;
    private final PreviewAttributes<DeploymentResource> totalProcessingTimeAttribute;
    private final PreviewAttributes<DeploymentResource> responseAttribute;
    private final HTMLElement statSection;
    private final GroupedBar processingTime;
    private final Donut requests;
    private final LabelBuilder lblBuilder = new LabelBuilder();

    EndpointPreview(FinderPathFactory finderPathFactory, Places places,
            DeploymentResource deploymentResource, Dispatcher dispatcher, StatementContext statementContext,
            Resources resources) {
        super(abbreviateFqClassName(deploymentResource.getName()), deploymentResource.getPath());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        noStatistics = new EmptyState.Builder(Ids.WEBSERVICES_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.WEBSERVICES))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), () -> enableStatistics(deploymentResource),
                        Constraint.writable(WEBSERVICES_CONFIGURATION_TEMPLATE, STATISTICS_ENABLED))
                .build();
        Elements.setVisible(noStatistics.element(), false);

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

        previewBuilder().addAll(previewAttributes)
                .add(noStatistics)
                .add(statSection = section()
                        .add(h(2, resources.constants().processingTime()))
                        .add(processingTime)
                        .addAll(totalProcessingTimeAttribute)
                        .add(h(2, resources.constants().request() + " / " + resources.constants().response()))
                        .add(requests)
                        .addAll(responseAttribute)
                        .element());
    }

    private PreviewAttributes.PreviewAttribute msAttribute(String attribute, ModelNode model) {
        long value = model.get(attribute).asLong();
        return new PreviewAttributes.PreviewAttribute(lblBuilder.label(attribute), value + " ms"); // NON-NLS
    }

    @Override
    public void update(DeploymentResource item) {
        // the endpoint name (last value of address) contains %3A character separator
        // however the dispatcher.execute call performs a http call, then the url is encoded, the %3A becomes %253A
        // in the endpointa name, causing a HTTP 500 address not found.
        // Then, read the parent resoource and interate over the endpoint results to match the endpoint name.
        String endpointName = item.getAddress().lastValue();
        Operation opEndpoint = new Operation.Builder(item.getAddress().getParent(), READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ENDPOINT)
                .param(INCLUDE_RUNTIME, true)
                .build();
        ResourceAddress configurationAddress = WEBSERVICES_RUNTIME_TEMPLATE.resolve(statementContext);
        Operation opStatistics = new Operation.Builder(configurationAddress, READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();

        dispatcher.execute(new Composite(opEndpoint, opStatistics), (CompositeResult compositeResult) -> {
            ModelNode endpointResult = compositeResult.step(0).get(RESULT);
            ModelNode statisticsResult = compositeResult.step(1).get(RESULT);

            for (Property prop : endpointResult.asPropertyList()) {
                if (prop.getName().equals(endpointName)) {
                    ModelNode node = prop.getValue();
                    DeploymentResource n = new DeploymentResource(item.getAddress(), node);
                    previewAttributes.refresh(n);

                    boolean statsAvailable = node.get(REQUEST_COUNT).asLong() > 0;
                    boolean statsEnabled = statisticsResult.asBoolean(statsAvailable);
                    if (statsEnabled) {
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
                    }

                    Elements.setVisible(noStatistics.element(), !statsEnabled);
                    Elements.setVisible(statSection, statsEnabled);
                    break;
                }
            }
        });
    }

    private void enableStatistics(DeploymentResource item) {
        ResourceAddress address = WEBSERVICES_CONFIGURATION_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(item));
    }
}
