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
package org.jboss.hal.client.runtime.subsystem.web;

import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

class DeploymentPreview extends PreviewContent<DeploymentResource> {

    private Dispatcher dispatcher;
    private PreviewAttributes<DeploymentResource> previewAttributes;

    DeploymentPreview(final Dispatcher dispatcher, DeploymentResource deploymentResource) {
        super(deploymentResource.getPath());
        this.dispatcher = dispatcher;
        previewAttributes = new PreviewAttributes<>(deploymentResource,
                asList("active-sessions", "context-root", "expired-sessions", "max-active-sessions",
                        "rejected-sessions", "server", "session-avg-alive-time", "session-max-alive-time",
                        "sessions-created", "virtual-host"));
        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));
        previewBuilder().addAll(previewAttributes);
    }

    @Override
    public void update(final DeploymentResource item) {
        Operation operation = new Operation.Builder(item.getAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            DeploymentResource n = new DeploymentResource(item.getAddress(), result);
            previewAttributes.refresh(n);
        });
    }

}
