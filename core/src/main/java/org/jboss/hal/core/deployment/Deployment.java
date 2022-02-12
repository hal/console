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
import java.util.Date;
import java.util.List;

import org.jboss.hal.ballroom.Format;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** A deployment on a specific server. */
public class Deployment extends Content {

    public enum Status {
        OK, FAILED, STOPPED, UNDEFINED
    }

    /**
     * Expects a "subsystem" child resource. Modeled as a static helper method to make it usable from both deployments and
     * subdeployments.
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

    public Deployment(Server referenceServer, ModelNode node) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deployment)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Deployment that = (Deployment) o;
        // noinspection SimplifiableIfStatement
        if (!referenceServer.equals(that.referenceServer)) {
            return false;
        }
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
        ModelNode enabled = get(ENABLED);
        // noinspection SimplifiableConditionalExpression
        return enabled.isDefined() ? enabled.asBoolean(false) : false;
    }

    public Status getStatus() {
        return ModelNodeHelper.asEnumValue(this, STATUS, Status::valueOf, Status.UNDEFINED);
    }

    public String getEnabledTime() {
        ModelNode node = get(ENABLED_TIME);
        if (node.isDefined()) {
            return Format.shortDateTime(new Date(node.asLong()));
        }
        return null;
    }

    public String getDisabledTime() {
        ModelNode node = get(DISABLED_TIME);
        if (node.isDefined()) {
            return Format.shortDateTime(new Date(node.asLong()));
        }
        return null;
    }

    public boolean hasSubdeployments() {
        return !subdeployments.isEmpty();
    }

    public List<Subdeployment> getSubdeployments() {
        return subdeployments;
    }

    public boolean hasSubsystem(String name) {
        return subsystems.stream().anyMatch(subsystem -> name.equals(subsystem.getName()));
    }

    public boolean hasNestedSubsystem(String name) {
        for (Subdeployment subdeployment : subdeployments) {
            for (Subsystem subsystem : subdeployment.getSubsystems()) {
                if (name.equals(subsystem.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
