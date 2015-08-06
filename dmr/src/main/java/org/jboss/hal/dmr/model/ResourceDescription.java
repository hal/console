/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.dmr.model;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Represents the result of a read-resource-description operation for one specific resource.
 *
 * @author Harald Pehl
 */
public class ResourceDescription extends ModelNode {

    public final static ResourceDescription EMPTY = new ResourceDescription();

    static final String ACCESS_CONTROL = "access-control";
    static final String NOTIFICATIONS = "notifications";

    public ResourceDescription() {
        super();
    }

    public ResourceDescription(ModelNode description) {
        set(description);
    }

    public boolean hasAttributes() {
        return hasDefined(ATTRIBUTES);
    }

    public List<Property> getAttributes() {
        return get(ATTRIBUTES).asPropertyList();
    }

    public boolean hasAccessControl() {
        return hasDefined(ACCESS_CONTROL);
    }

    public boolean hasChildren() {
        return hasDefined(CHILDREN);
    }

    public boolean hasOperations() {
        return hasDefined(OPERATIONS);
    }

    public boolean hasNotifications() {
        return hasDefined(NOTIFICATIONS);
    }

    /**
     * Looks for the description of a child resource.
     *
     * @param resourceName The name of the child resource
     *
     * @return the description of the child resource or {@link #EMPTY} if no such resource exists.
     */
    public ResourceDescription getChildDescription(String resourceName) {
        return getChildDescription(resourceName, "*");
    }

    /**
     * Looks for the description of a specific child resource.
     *
     * @param resourceName The name of the child resource
     * @param instanceName The name of the instance
     *
     * @return the description of the specific child resource or {@link #EMPTY} if no such resource exists.
     */
    public ResourceDescription getChildDescription(String resourceName, String instanceName) {
        if (hasChildren()) {
            List<Property> children = get("children").asPropertyList();
            for (Property child : children) {
                if (resourceName.equals(child.getName()) && child.getValue().hasDefined(MODEL_DESCRIPTION)) {
                    List<Property> modelDescriptions = child.getValue().get(MODEL_DESCRIPTION).asPropertyList();
                    for (Property modelDescription : modelDescriptions) {
                        if (instanceName.equals(modelDescription.getName())) {
                            return new ResourceDescription(modelDescription.getValue());
                        }
                    }
                }
            }
        }
        return EMPTY;
    }
}
