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
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class CompositeRrdParser {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(CompositeRrdParser.class);

    private final Composite composite;

    CompositeRrdParser(final Composite composite) {
        this.composite = composite;
    }

    public Set<RrdResult> parse(CompositeResult compositeResult) throws ParserException {
        int index = 0;
        Set<RrdResult> overallResults = new HashSet<>();

        for (ModelNode step : compositeResult) {
            if (step.isFailure()) {
                throw new ParserException("Failed step 'step-" + (index + 1) + "' in composite rrd result: " + step
                        .getFailureDescription());
            }

            ModelNode stepResult = step.get(RESULT);

            if (stepResult.getType() == ModelType.LIST) {
                // multiple rrd results each with its own address
                for (ModelNode modelNode : stepResult.asList()) {
                    ModelNode result = modelNode.get(RESULT);
                    if (result.isDefined()) {
                        ResourceAddress operationAddress = operationAddress(index);
                        ResourceAddress resultAddress = new ResourceAddress(modelNode.get(ADDRESS));
                        ResourceAddress resolvedAddress = adjustAddress(operationAddress, resultAddress);

                        Set<RrdResult> results = new SingleRrdParser().parse(resolvedAddress, result);
                        overallResults.addAll(results);
                    }
                }

            } else {
                // a single rrd result
                ResourceAddress address = operationAddress(index);
                Set<RrdResult> results = new SingleRrdParser().parse(address, stepResult);
                overallResults.addAll(results);
            }
            index++;
        }

        return overallResults;
    }

    private ResourceAddress operationAddress(int index) {
        List<ModelNode> steps = composite.get(STEPS).asList();
        if (index >= steps.size()) {
            throw new ParserException(
                    "Cannot get operation at index " + index + " from composite " + composite);
        }
        ModelNode operation = steps.get(index);
        return new ResourceAddress(operation.get(ADDRESS));
    }

    private ResourceAddress adjustAddress(ResourceAddress operationAddress, ResourceAddress resultAddress) {
        // For wildcard rrd operations against running servers like /host=master/server=server-one/interfaces=*
        // the result does *not* contain fully qualified addresses. But since we need fq addresses in the
        // registries this method fixes this special case.

        ResourceAddress resolved = resultAddress;
        List<Property> operationSegments = operationAddress.asPropertyList();
        List<Property> resultSegments = resultAddress.asPropertyList();
        if (operationSegments.size() > 2 &&
                operationSegments.size() == resultSegments.size() + 2 &&
                HOST.equals(operationSegments.get(0).getName()) &&
                SERVER.equals(operationSegments.get(1).getName())) {
            resolved = new ResourceAddress()
                    .add(HOST, operationSegments.get(0).getValue().asString())
                    .add(SERVER, operationSegments.get(1).getValue().asString())
                    .add(resultAddress);
            logger.debug("Adjust result address '{}' -> '{}'", resultAddress, resolved);
        }
        return resolved;
    }
}
