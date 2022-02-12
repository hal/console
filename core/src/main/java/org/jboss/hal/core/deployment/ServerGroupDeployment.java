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
package org.jboss.hal.core.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jboss.hal.core.deployment.Deployment.Status;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;

/** A deployed content used in domain mode. */
public class ServerGroupDeployment extends NamedNode {

    private final String serverGroup;
    private Deployment deployment; // might be null if there's no reference server available
    private final List<ServerGroupDeployment> serverGroupDeployments;

    public ServerGroupDeployment(String serverGroup, ModelNode node) {
        super(node);
        this.serverGroup = serverGroup;
        this.serverGroupDeployments = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerGroupDeployment)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ServerGroupDeployment that = (ServerGroupDeployment) o;
        // noinspection SimplifiableIfStatement
        if (!serverGroup.equals(that.serverGroup)) {
            return false;
        }
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverGroup.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    public String getRuntimeName() {
        ModelNode runtimeName = get(RUNTIME_NAME);
        return runtimeName.isDefined() ? runtimeName.asString() : null;
    }

    public void addDeployment(ServerGroupDeployment serverGroupDeployment) {
        serverGroupDeployments.add(serverGroupDeployment);
    }

    public List<ServerGroupDeployment> getServerGroupDeployments() {
        return serverGroupDeployments;
    }

    public boolean isDeployedTo(String serverGroup) {
        return serverGroupDeployments.stream()
                .anyMatch(sgd -> serverGroup.equals(sgd.getServerGroup()));
    }

    public boolean isExploded() {
        return get(EXPLODED).asBoolean(false);
    }

    public boolean isManaged() {
        return get(MANAGED).asBoolean(true);
    }

    public boolean isEnabled() {
        return hasDefined(ENABLED) && get(ENABLED).asBoolean(false);
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public @Nullable Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(@Nullable Deployment deployment) {
        this.deployment = deployment;
    }

    public boolean runningWithReferenceServer() {
        return deployment != null && deployment.getStatus() == Status.OK && deployment.getReferenceServer() != null;
    }

    @Override
    public String toString() {
        return "ServerGroupDeployment{" + getName() + "@" + serverGroup + ", " + (isEnabled() ? ENABLED : DISABLED) + "}";
    }
}
