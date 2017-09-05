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

import elemental2.dom.HTMLAnchorElement;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.a;
import static java.util.Arrays.asList;
import static org.jboss.hal.core.Strings.abbreviateFqClassName;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WSDL_URL;

class EndpointPreview extends PreviewContent<DeploymentResource> {

    private enum CountStatus {
        REQUEST, FAULT
    }

    private Dispatcher dispatcher;
    private PreviewAttributes<DeploymentResource> previewAttributes;
    private Donut requests;

    EndpointPreview(final DeploymentResource deploymentResource, final Dispatcher dispatcher,
            final Resources resources) {
        super(abbreviateFqClassName(deploymentResource.getName()), deploymentResource.getPath());
        this.dispatcher = dispatcher;

        getHeaderContainer().title = deploymentResource.getName();
        getHeaderContainer().textContent = abbreviateFqClassName(deploymentResource.getName());

        previewAttributes = new PreviewAttributes<>(deploymentResource,
                asList("average-processing-time", "class", "context", "min-processing-time", "max-processing-time",
                        "total-processing-time", "name", "response-count", "type"
                ));
        previewAttributes
                .append(model -> {
                    String value = model.get(WSDL_URL).asString();
                    HTMLAnchorElement anchor = a(value).attr("target", "_blank").asElement();
                    anchor.textContent = value;
                    return new PreviewAttributes.PreviewAttribute("WSDL URL", anchor);
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

    @Override
    public void update(final DeploymentResource item) {
        Operation operation = new Operation.Builder(item.getAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            DeploymentResource n = new DeploymentResource(item.getAddress(), result);
            previewAttributes.refresh(n);

            Map<String, Long> txUpdates = new HashMap<>(7);
            long request = result.get("request-count").asLong();
            long fault = result.get("fault-count").asLong();
            txUpdates.put(CountStatus.REQUEST.name(), request);
            txUpdates.put(CountStatus.FAULT.name(), fault);
            requests.update(txUpdates);
        });
    }


}
