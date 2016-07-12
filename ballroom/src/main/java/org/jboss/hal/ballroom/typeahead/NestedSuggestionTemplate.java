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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.js.json.JsJsonArray;
import elemental.js.json.JsJsonObject;
import elemental.json.JsonObject;

import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.ADDRESSES;
import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.KEY;
import static org.jboss.hal.ballroom.typeahead.NestedResultProcessor.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.CSS.ttNested;

/**
 * @author Harald Pehl
 */
final class NestedSuggestionTemplate implements Templates.SuggestionTemplate {

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String render(final JsJsonObject data) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<div class=\"" + ttNested + "\">");
        JsJsonArray addresses = (JsJsonArray) data.get(ADDRESSES);
        if (addresses.length() != 0) {
            for (int i = 0; i < addresses.length(); i++) {
                JsonObject keyValue = addresses.getObject(i);
                builder.appendHtmlConstant("<span title=\"" + keyValue.getString(KEY) + "\">");
                builder.appendEscaped(keyValue.getString(VALUE));
                builder.appendEscaped(" / ");
                builder.appendHtmlConstant("</span>");
            }
        }
        builder.appendHtmlConstant("<span>").appendEscaped(data.getString(NAME)).appendHtmlConstant("</span>");
        builder.appendHtmlConstant("</div>");
        return builder.toSafeHtml().asString();
    }
}
