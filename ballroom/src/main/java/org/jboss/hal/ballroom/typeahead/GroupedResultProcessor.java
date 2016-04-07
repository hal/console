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
package org.jboss.hal.ballroom.typeahead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import elemental.js.json.JsJsonFactory;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;

/**
 * Processes the result of a single READ_CHILDREN operation with one or multiple wildcards or the result of
 * a composite operation with one or more of those operations. Returns an array of json objects defined as
 * <pre>
 * {
 *     "name": "http",
 *     "groups": [
 *         "socket-binding-group => standard-sockets"
 *     ]
 * }
 * </pre>
 */
class GroupedResultProcessor implements ResultProcessor {

    static final String GROUPS = "groups";
    private static final Logger logger = LoggerFactory.getLogger(GroupedResultProcessor.class);

    private final Operation operation;

    GroupedResultProcessor(final Operation operation) {
        this.operation = operation;
    }

    @Override
    public List<JsonObject> process(final String query, final ModelNode result) {
        // first collect all addresses from the result
        List<ResourceAddress> addresses = new ArrayList<>();
        if (operation instanceof Composite) {
            CompositeResult compositeResult = new CompositeResult(result);
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

        } else {
            for (ModelNode modelNode : result.asList()) {
                ResourceAddress address = new ResourceAddress(modelNode.get(ADDRESS));
                if (match(query, address)) {
                    addresses.add(address);
                }
            }
        }

        if (!addresses.isEmpty()) {
            // it's expected that all addresses are of the same type (same length of segments)
            Iterator<ResourceAddress> iterator = addresses.iterator();
            int length = iterator.next().asList().size();
            while (iterator.hasNext()) {
                if (iterator.next().asList().size() != length) {
                    //noinspection HardCodedStringLiteral
                    logger.error("Different address types in result processor for operation {}", operation);
                    return Collections.emptyList();
                }
            }

            // turn the list of addresses into a list of lists of string segments w/o the last segment (name)
            // and store the names in an extra list
            List<List<String>> segments = Lists.transform(addresses, address -> {
                        List<String> addresSegments = new ArrayList<>();
                        for (Property property : address.getParent().asPropertyList()) {
                            addresSegments.add(property.getName() + " => " + property.getValue().asString());
                        }
                        return addresSegments;
                    });
            List<String> names = Lists.transform(addresses, ResourceAddress::lastValue);

            // find matching segments (if the segments of the first two addresses match we assume that
            // all other segments match as well)
            Set<Integer> commonIndices = new HashSet<>();
            if (segments.size() > 1) {
                List<String> first = segments.get(0);
                List<String> second = segments.get(0);
                for (int i = 0; i < first.size(); i++) {
                    if (first.get(i).equals(second.get(i))) {
                        commonIndices.add(i);
                    }
                    i++;
                }
            }

            // build final list w/o matching segments
            List<List<String>> groups = Lists.transform(segments, addressSegments -> {
                List<String> stripped = new ArrayList<>();
                for (int i = 0; i < addressSegments.size(); i++) {
                    if (commonIndices.contains(i)) {
                        continue;
                    }
                    stripped.add(addressSegments.get(i));
                }
                return stripped;
            });

            // finally build json object
            List<JsonObject> objects = new ArrayList<>();
            JsonFactory jsonFactory = GWT.isScript() ? new JsJsonFactory() : new JreJsonFactory();
            for (int i = 0; i < names.size(); i++) {
                JsonObject object = jsonFactory.createObject();
                object.put(NAME, names.get(i));
                JsonArray jsonGroups = jsonFactory.createArray();
                List<String> group = groups.get(i);
                for (int j = 0; j < group.size(); j++) {
                    jsonGroups.set(j, group.get(j));
                }
                object.put(GROUPS, jsonGroups);
                objects.add(object);
            }
            return objects;
        }
        return Collections.emptyList();
    }

    private boolean match(String query, ResourceAddress address) {
        return (SuggestHandler.SHOW_ALL_VALUE.equals(query) || address.lastValue().contains(query));
    }
}
