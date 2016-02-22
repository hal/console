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
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class CompositeRrdParser {

    private static final Logger logger = LoggerFactory.getLogger(CompositeRrdParser.class);

    private final Composite composite;

    public CompositeRrdParser(final Composite composite) {this.composite = composite;}

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
