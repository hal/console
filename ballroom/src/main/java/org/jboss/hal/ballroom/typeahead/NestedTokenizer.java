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

import java.util.HashSet;
import java.util.Set;

import elemental.js.json.JsJsonObject;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.ADDRESSES;
import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.KEY;
import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.VALUE;
import static org.jboss.hal.ballroom.typeahead.Typeahead.WHITESPACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
final class NestedTokenizer implements DataTokenizer {

    @Override
    public String[] tokenize(final JsJsonObject data) {
        Set<String> tokens = new HashSet<>();
        tokens.addAll(asList(data.getString(NAME).split(WHITESPACE)));
        JsonArray addresses = data.getArray(ADDRESSES);
        for (int i = 0; i < addresses.length(); i++) {
            JsonObject keyValue = addresses.getObject(i);
            tokens.addAll(asList(keyValue.getString(KEY).split(WHITESPACE)));
            tokens.addAll(asList(keyValue.getString(VALUE).split(WHITESPACE)));
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}
