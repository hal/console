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
package org.jboss.hal.ballroom.autocomplete;

import java.util.LinkedHashMap;
import java.util.Map;

import elemental.js.json.JsJsonArray;
import elemental.js.json.JsJsonObject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
class ReadChildrenResult {

    static final String ADDRESSES = "addresses";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    final String name;
    final LinkedHashMap<String, String> addresses;

    ReadChildrenResult(final String name) {
        this.name = name;
        this.addresses = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        return name + " " + addresses;
    }

    JsJsonObject asJson() {
        JsJsonObject object = JsJsonObject.create();
        object.put(NAME, name);
        JsJsonArray addresses = (JsJsonArray) JsJsonArray.create();
        int i = 0;
        for (Map.Entry<String, String> entry : this.addresses.entrySet()) {
            JsJsonObject keyValue = JsJsonObject.create();
            keyValue.put(KEY, entry.getKey());
            keyValue.put(VALUE, entry.getValue());
            addresses.set(i, keyValue);
            i++;
        }
        object.put(ADDRESSES, addresses);
        return object;
    }
}
