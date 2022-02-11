/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;

class CompositeReadChildrenProcessor extends ReadChildrenProcessor implements ResultProcessor {

    @Override
    public List<ReadChildrenResult> processToModel(final String query, final ModelNode nodes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReadChildrenResult> processToModel(final String query, final CompositeResult compositeResult) {
        List<ResourceAddress> addresses = new ArrayList<>();
        for (ModelNode step : compositeResult) {
            if (!step.isFailure()) {
                ModelNode stepResult = step.get(RESULT);
                for (ModelNode modelNode : stepResult.asList()) {
                    ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                    if (match(query, address)) {
                        addresses.add(address);
                    }
                }
            }
        }
        return results(addresses);
    }
}
