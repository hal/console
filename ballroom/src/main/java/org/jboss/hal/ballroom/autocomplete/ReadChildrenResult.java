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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.hal.js.JsonArray;
import org.jboss.hal.js.JsonObject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

class ReadChildrenResult {

    static final String ADDRESSES = "addresses";
    static final String KEY = "key";

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

    JsonObject asJson() {
        JsonObject object = JsonObject.create();
        object.put(NAME, name);
        JsonArray addresses = JsonArray.create();
        int i = 0;
        for (Map.Entry<String, String> entry : this.addresses.entrySet()) {
            JsonObject keyValue = JsonObject.create();
            keyValue.put(KEY, entry.getKey());
            keyValue.put(VALUE, entry.getValue());
            addresses.set(i, keyValue);
            i++;
        }
        object.put(ADDRESSES, addresses);
        return object;
    }
}
