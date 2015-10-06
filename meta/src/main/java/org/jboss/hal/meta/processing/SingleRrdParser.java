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
package org.jboss.hal.meta.processing;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class SingleRrdParser {

    public Set<RrdResult> parse(ResourceAddress address, ModelNode modelNode) throws ParserException {
        Set<RrdResult> results = new HashSet<>();
        if (modelNode.getType() == ModelType.LIST) {
            for (ModelNode nestedNode : modelNode.asList()) {
                ResourceAddress nestedAddress = new ResourceAddress(nestedNode.get(ADDRESS));
                ModelNode nestedResult = nestedNode.get(RESULT);
                parseSingle(nestedAddress, nestedResult, results);
            }
        } else {
            parseSingle(address, modelNode, results);
        }
        return results;
    }

    private void parseSingle(ResourceAddress address, ModelNode modelNode, Set<RrdResult> results) {
        RrdResult rr = new RrdResult(address);

        // resource description
        if (modelNode.hasDefined(DESCRIPTION)) {
            rr.resourceDescription = new ResourceDescription(modelNode);
        }

        // security context
        ModelNode accessControl = modelNode.get(ACCESS_CONTROL);
        if (accessControl.isDefined()) {
            if (accessControl.hasDefined(DEFAULT)) {
                rr.securityContext = new SecurityContext(accessControl.get(DEFAULT));
            }

            // exceptions
            if (accessControl.hasDefined(EXCEPTIONS)) {
                List<Property> exceptions = accessControl.get(EXCEPTIONS).asPropertyList();
                for (Property property : exceptions) {
                    ModelNode exception = property.getValue();
                    ResourceAddress exceptionAddress = new ResourceAddress(exception.get(ADDRESS));
                    RrdResult exceptionRr = new RrdResult(exceptionAddress);
                    exceptionRr.securityContext = new SecurityContext(exception);
                    results.add(exceptionRr);
                }
            }
        }

        // children
        if (modelNode.hasDefined(CHILDREN)) {
            List<Property> children = modelNode.get(CHILDREN).asPropertyList();
            for (Property child : children) {
                String addressKey = child.getName();
                if (child.getValue().hasDefined(MODEL_DESCRIPTION)) {
                    List<Property> modelDescriptions = child.getValue().get(MODEL_DESCRIPTION).asPropertyList();
                    for (Property modelDescription : modelDescriptions) {
                        String addressValue = modelDescription.getName();
                        ModelNode childNode = modelDescription.getValue();
                        ResourceAddress childAddress = new ResourceAddress(address).add(addressKey, addressValue);
                        parseSingle(childAddress, childNode, results);
                    }
                }
            }
        }

        results.add(rr);
    }
}
