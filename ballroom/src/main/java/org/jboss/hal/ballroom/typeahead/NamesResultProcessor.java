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
import java.util.List;

import com.google.gwt.core.shared.GWT;
import elemental.js.json.JsJsonFactory;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Processes the result of a single READ_CHILDREN_NAMES operation. Returns an array of JSON objects defined as
 * <pre>
 * {
 *     "name": "foo"
 * }
 * </pre>
 */
class NamesResultProcessor implements ResultProcessor {

    @Override
    public List<JsonObject> process(final String query, final ModelNode result) {
        List<JsonObject> objects = new ArrayList<>();
        JsonFactory jsonFactory = GWT.isScript() ? new JsJsonFactory() : new JreJsonFactory();
        for (ModelNode child : result.asList()) {
            String value = child.asString();
            if (SuggestHandler.SHOW_ALL_VALUE.equals(query) || value.contains(query)) {
                JsonObject object = jsonFactory.createObject();
                object.put(NAME, value);
                objects.add(object);
            }
        }
        return objects;
    }
}
