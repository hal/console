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
package org.jboss.hal.client.runtime.configurationchanges;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeDate;

class ConfigurationChange extends NamedNode {

    private final Date date;
    private final boolean composite;
    private final String operationNames;
    private final String addressSegments;

    ConfigurationChange(ModelNode model) {
        super(model.hasDefined(DOMAIN_UUID) ? model.get(DOMAIN_UUID).asString() : model.get(OPERATION_DATE).asString(),
                model);
        this.date = failSafeDate(model, OPERATION_DATE);

        Set<String> operationNames = new HashSet<>();
        Set<String> addressSegments = new HashSet<>();
        List<ModelNode> operations = model.get(OPERATIONS).asList();
        if (!operations.isEmpty()) {
            composite = COMPOSITE.equals(operations.get(0).get(OPERATION).asString());
            if (composite) {
                for (ModelNode operation : operations) {
                    for (ModelNode step : operation.get(STEPS).asList()) {
                        operationNames.add(step.get(OPERATION).asString());
                        for (Property property : step.get(ADDRESS).asPropertyList()) {
                            addressSegments.add(property.getName());
                            addressSegments.add(property.getValue().asString());
                        }
                    }
                }

            } else {
                for (ModelNode operation : operations) {
                    operationNames.add(operation.get(OPERATION).asString());
                    for (Property property : operation.get(ADDRESS).asPropertyList()) {
                        addressSegments.add(property.getName());
                        addressSegments.add(property.getValue().asString());
                    }
                }
            }

        } else {
            composite = false;
        }
        this.operationNames = String.join(" ", operationNames);
        this.addressSegments = String.join(" ", addressSegments);

        int length = get(OPERATIONS).asString().length();
        get(HAL_LENGTH).set(length);
    }

    Date getOperationDate() {
        return date;
    }

    int getOperationsLength() {
        return get(HAL_LENGTH).asInt();
    }

    String getOperationNames() {
        return operationNames;
    }

    String getAddressSegments() {
        return addressSegments;
    }

    String getAccessMechanism() {
        return get(ACCESS_MECHANISM).asString();
    }

    String getRemoteAddress() {
        return get(REMOTE_ADDRESS).asString();
    }

    boolean isSuccess() {
        return get(OUTCOME).asString().equals(SUCCESS);
    }

    String getOutcome() {
        return get(OUTCOME).asString();
    }

    boolean isComposite() {
        return composite;
    }

    List<ModelNode> changes() {
        List<ModelNode> changes;
        if (composite) {
            changes = get(OPERATIONS).asList().get(0).get(STEPS).asList();
        } else {
            changes = get(OPERATIONS).asList();
        }
        return changes;
    }
}
