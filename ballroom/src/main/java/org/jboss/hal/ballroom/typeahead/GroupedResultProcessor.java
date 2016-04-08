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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import elemental.js.json.JsJsonArray;
import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
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
class GroupedResultProcessor extends AbstractResultProcessor<GroupedResultProcessor.Grouped>
        implements ResultProcessor {

    static class Grouped {

        final String name;
        final List<String> groups;

        Grouped(final String name) {
            this.name = name;
            this.groups = new ArrayList<>();
        }
    }


    static final String GROUPS = "groups";
    private static final Logger logger = LoggerFactory.getLogger(GroupedResultProcessor.class);

    private final Operation operation;

    GroupedResultProcessor(final Operation operation) {
        this.operation = operation;
    }

    @Override
    protected List<Grouped> processToModel(final String query, final ModelNode result) {
        List<Grouped> models = new ArrayList<>();

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
            // but backup the names in an extra list for later use
            List<List<String>> segments = Lists.transform(addresses, address -> {
                List<String> addresSegments = new ArrayList<>();
                for (Property property : address.getParent().asPropertyList()) {
                    addresSegments.add(property.getName() + " => " + property.getValue().asString());
                }
                return addresSegments;
            });
            List<String> names = Lists.transform(addresses, ResourceAddress::lastValue);

            // find matching segments (if the segments of the first two addresses match we assume that
            // all other segments match as well) and build final list w/o matching segments
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
            List<List<String>> groupedSegments = Lists.transform(segments, addressSegments -> {
                List<String> stripped = new ArrayList<>();
                for (int i = 0; i < addressSegments.size(); i++) {
                    if (commonIndices.contains(i)) {
                        continue;
                    }
                    stripped.add(addressSegments.get(i));
                }
                return stripped;
            });

            // build model list
            for (int i = 0; i < names.size(); i++) {
                Grouped model = new Grouped(names.get(i));
                model.groups.addAll(groupedSegments.get(i));
                models.add(model);
            }
        }

        return models;
    }

    @Override
    protected JsArrayOf<JsJsonObject> asJson(final List<Grouped> models) {
        JsArrayOf<JsJsonObject> objects = JsArrayOf.create();
        for (Grouped model : models) {
            JsJsonObject object = JsJsonObject.create();
            object.put(NAME, model.name);
            JsJsonArray jsonGroups = (JsJsonArray) JsJsonArray.create();
            int i = 0;
            for (String group : model.groups) {
                jsonGroups.set(i, group);
                i++;
            }
            object.put(GROUPS, jsonGroups);
            objects.push(object);
        }
        return objects;
    }

    private boolean match(String query, ResourceAddress address) {
        return !Strings.isNullOrEmpty(query) &&
                (SuggestHandler.SHOW_ALL_VALUE.equals(query) || address.lastValue().contains(query));
    }
}
