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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.client.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBDEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A deployed and assigned content on a specific server.
 *
 * @author Harald Pehl
 */
public class Deployment extends Content {

    public enum Status {
        OK, FAILED, STOPPED, UNDEFINED
    }


    /**
     * Expects a "subsystem" child resource. Modeled as a static helper method to make it usable from both
     * deployments and subdeployments.
     */
    static void parseSubsystems(ModelNode node, List<Subsystem> subsystems) {
        List<Property> properties = node.get(SUBSYSTEM).asPropertyList();
        for (Property property : properties) {
            Subsystem subsystem = new Subsystem(property);
            subsystems.add(subsystem);
        }
    }

    private final Server referenceServer;
    private final List<Subdeployment> subdeployments;
    private final List<Subsystem> subsystems;

    public Deployment(final Server referenceServer, final ModelNode node) {
        super(node);
        this.referenceServer = referenceServer;
        this.subdeployments = new ArrayList<>();
        this.subsystems = new ArrayList<>();

        if (node.hasDefined(SUBSYSTEM)) {
            parseSubsystems(node, subsystems);
        }
        if (node.hasDefined(SUBDEPLOYMENT)) {
            List<Property> properties = node.get(SUBDEPLOYMENT).asPropertyList();
            for (Property property : properties) {
                Subdeployment subdeployment = new Subdeployment(this, property.getName(), property.getValue());
                subdeployments.add(subdeployment);
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Deployment)) { return false; }
        if (!super.equals(o)) { return false; }

        Deployment that = (Deployment) o;
        //noinspection SimplifiableIfStatement
        if (!referenceServer.equals(that.referenceServer)) { return false; }
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + referenceServer.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Deployment(").append(getName());
        if (!isStandalone()) {
            builder.append("@").append(referenceServer.getHost()).append("/").append(referenceServer.getName());
        }
        builder.append(", ")
                .append((isEnabled() ? ENABLED : DISABLED))
                .append(", ")
                .append(getStatus());
        builder.append(")");
        return builder.toString();
    }

    public boolean isStandalone() {
        return referenceServer.isStandalone();
    }

    public Server getReferenceServer() {
        return referenceServer;
    }

    public boolean isEnabled() {
        ModelNode enabled = get("enabled");
        //noinspection SimplifiableConditionalExpression
        return enabled.isDefined() ? enabled.asBoolean() : false;
    }

    public Status getStatus() {
        Status status = Status.UNDEFINED;
        ModelNode statusNode = get("status");
        if (statusNode.isDefined()) {
            try {
                status = Status.valueOf(statusNode.asString());
            } catch (IllegalArgumentException e) {
                // returns UNDEFINED
            }
        }
        return status;
    }

    public String getEnabledTime() {
        ModelNode node = get("enabled-timestamp");
        if (node.isDefined()) {
            return node.asString();
        }
        return null;
    }

    public String getDisabledTime() {
        ModelNode node = get("disabled-timestamp");
        if (node.isDefined()) {
            return node.asString();
        }
        return null;
    }

    public boolean hasSubdeployments() {
        return !subdeployments.isEmpty();
    }

    public List<Subdeployment> getSubdeployments() {
        return subdeployments;
    }

    public List<Subsystem> getSubsystems() {
        return subsystems;
    }
}
