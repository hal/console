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
package org.jboss.hal.core.deployment;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBDEPLOYMENT;

/**
 * Model node for resources under {@code /deployment=foo/subsystem=*} resp. {@code
 * /deployment=foo/subdeployment=bar/subsystem=*}
 */
public class DeploymentResource extends NamedNode {

    private final ResourceAddress address;
    private String path;
    private String deployment;
    private String subdeployment;

    public DeploymentResource(ResourceAddress address, ModelNode modelNode) {
        this(address.lastValue(), address, modelNode);
    }

    public DeploymentResource(String name, ResourceAddress address, ModelNode modelNode) {
        super(name, modelNode);
        this.address = address;

        address.asList().forEach(segment -> {
            if (segment.hasDefined(DEPLOYMENT)) {
                deployment = segment.get(DEPLOYMENT).asString();
            }
            if (segment.hasDefined(SUBDEPLOYMENT)) {
                subdeployment = segment.get(SUBDEPLOYMENT).asString();
            }
        });
        this.path = subdeployment != null ? deployment + "/" + subdeployment : deployment;
    }

    public ResourceAddress getAddress() {
        return address;
    }

    public String getDeployment() {
        return deployment;
    }

    public String getSubdeployment() {
        return subdeployment;
    }

    /**
     * Returns {@code deployment}/{@code subdeployment} if {@code subdeployment != null}, {@code deployment} otherwise.
     * Should not be used to build DMR operations, but rather in the UI.
     */
    public String getPath() {
        return path;
    }
}
