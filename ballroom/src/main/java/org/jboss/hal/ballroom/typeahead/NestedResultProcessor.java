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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
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
 *     "addresses": [
 *         {
 *             "key": "socket-binding-group",
 *             "value": "standard-sockets"
 *         }
 *     ]
 * }
 * </pre>
 */
// TODO Rename this to NestedResultProcessor and change the JSON as stated above
class NestedResultProcessor extends AbstractResultProcessor<NestedResultProcessor.Result>
        implements ResultProcessor {

    static class Result {

        final String name;
        final LinkedHashMap<String, String> addresses;

        Result(final String name) {
            this.name = name;
            this.addresses = new LinkedHashMap<>();
        }

        @Override
        public String toString() {
            return name + " " + addresses;
        }
    }


    static final String ADDRESSES = "addresses";
    static final String KEY = "key";
    static final String VALUE = "value";
    private static final Logger logger = LoggerFactory.getLogger(NestedResultProcessor.class);

    private final Operation operation;

    NestedResultProcessor(final Operation operation) {
        this.operation = operation;
    }

    @Override
    protected List<Result> processToModel(final String query, final ModelNode result) {
        List<Result> models = new ArrayList<>();

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

            // turn the addresses into a list of models
            for (ResourceAddress address : addresses) {
                Result model = new Result(address.lastValue());
                for (Property property : address.getParent().asPropertyList()) {
                    model.addresses.put(property.getName(), property.getValue().asString());
                }
                models.add(model);
            }
        }

        return models;
    }

    @Override
    protected JsArrayOf<JsJsonObject> asJson(final List<Result> models) {
        JsArrayOf<JsJsonObject> objects = JsArrayOf.create();
        for (Result model : models) {
            JsJsonObject object = JsJsonObject.create();
            object.put(NAME, model.name);
            JsJsonArray jsonGroups = (JsJsonArray) JsJsonArray.create();
            int i = 0;
            for (Map.Entry<String, String> entry : model.addresses.entrySet()) {
                JsJsonObject keyValue = JsJsonObject.create();
                keyValue.put(KEY, entry.getKey());
                keyValue.put(VALUE, entry.getValue());
                jsonGroups.set(i, keyValue);
                i++;
            }
            object.put(ADDRESSES, jsonGroups);
            objects.push(object);
        }
        return objects;
    }

    private boolean match(String query, ResourceAddress address) {
        return !Strings.isNullOrEmpty(query) &&
                (SuggestHandler.SHOW_ALL_VALUE.equals(query) || address.lastValue().contains(query));
    }
}
