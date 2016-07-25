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
package org.jboss.hal.meta.processing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.capabilitiy.Capability;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_GROUP;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;

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

        // capabilities
        if (modelNode.hasDefined(CAPABILITIES)) {
            for (ModelNode capabilityNode : modelNode.get(CAPABILITIES).asList()) {
                String capabilityName = capabilityNode.get(NAME).asString();
                AddressTemplate template;
                if (address.size() == 1) {
                    // do not replace "/profile=*" with "{selected.profile}"
                    template = AddressTemplate.of(address.lastName() + "=*");
                } else {
                    // but replace "/profile=*/foo=bar" with "{selected.profile}/foo=*"
                    template = AddressTemplate.of(address, (name, value, first, last, index) -> {
                        String segment;

                        if (first && last) {
                            segment = name + "=*";
                        }
                        switch (name) {
                            case PROFILE:
                                segment = SELECTED_PROFILE.variable();
                                break;
                            case SERVER_GROUP:
                                segment = SELECTED_GROUP.variable();
                                break;
                            default:
                                segment = name + "=" + (last ? "*" : value);
                                break;
                        }
                        return segment;
                    });
                }
                Capability capability = new Capability(capabilityName);
                capability.addTemplate(template);
                rr.capabilities.add(capability);
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
