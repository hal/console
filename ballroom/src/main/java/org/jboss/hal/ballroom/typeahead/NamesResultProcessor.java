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

import java.util.List;

import com.google.common.collect.FluentIterable;
import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.ModelNode;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Processes the result of a single READ_CHILDREN_NAMES operation. Returns an array of JSON objects defined as
 * <pre>
 * {
 *     "name": "foo"
 * }
 * </pre>
 */
class NamesResultProcessor extends AbstractResultProcessor<String> implements ResultProcessor {

    @Override
    protected List<String> processToModel(final String query, final ModelNode result) {
        //noinspection Guava
        return FluentIterable.from(result.asList())
                .transform(ModelNode::asString)
                .filter(name -> !isNullOrEmpty(query) &&
                        (SuggestHandler.SHOW_ALL_VALUE.equals(query) || name.contains(query)))
                .toList();
    }

    @Override
    protected JsArrayOf<JsJsonObject> asJson(final List<String> models) {
        JsArrayOf<JsJsonObject> objects = JsArrayOf.create();
        for (String model : models) {
            JsJsonObject object = JsJsonObject.create();
            object.put(NAME, model);
            objects.push(object);
        }
        return objects;
    }
}
