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

import java.util.Collection;

import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class StaticTypeahead extends Typeahead {

    public StaticTypeahead(final Collection<String> values) {
        options = initOptions();

        int index = 0;
        JsArrayOf<JsJsonObject> jsValues = JsArrayOf.create();
        for (String value : values) {
            JsJsonObject object = JsJsonObject.create();
            object.put(Ids.SUGGEST_HANDLER_ID, index);
            object.put(Ids.SUGGEST_HANDLER_VALUE, value);
            jsValues.push(object);
            index++;
        }

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = data -> data.getString(NAME).split(WHITESPACE);
        bloodhoundOptions.queryTokenizer = query -> query.split(WHITESPACE);
        bloodhoundOptions.identify = data -> String.valueOf(data.getNumber(Ids.SUGGEST_HANDLER_ID));
        bloodhoundOptions.local = jsValues;
        bloodhound = new Bloodhound(bloodhoundOptions);

        dataset = new Dataset();
        dataset.async = false;
        dataset.limit = jsValues.length();
        dataset.display = data -> data.getString(Ids.SUGGEST_HANDLER_VALUE);
        dataset.source = (query, syncCallback, asyncCallback) -> {
            if (SHOW_ALL_VALUE.equals(query)) {
                syncCallback.sync(jsValues);
            } else {
                bloodhound.search(query, syncCallback, asyncCallback);
            }
        };
        dataset.templates = initTemplates();
    }
}
