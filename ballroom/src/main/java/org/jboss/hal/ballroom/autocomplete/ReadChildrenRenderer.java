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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.jboss.hal.js.JsonArray;
import org.jboss.hal.js.JsonObject;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.ballroom.autocomplete.ItemRenderer.highlight;
import static org.jboss.hal.ballroom.autocomplete.ReadChildrenResult.ADDRESSES;
import static org.jboss.hal.ballroom.autocomplete.ReadChildrenResult.KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.address;
import static org.jboss.hal.resources.CSS.autocompleteSuggestion;

final class ReadChildrenRenderer implements ItemRenderer<JsonObject> {

    @Override
    public String render(JsonObject item, String query) {
        String name = item.get(NAME).asString();
        @NonNls SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<div class=\"" + autocompleteSuggestion + "\" data-val=\"" + name + "\">");
        JsonArray addresses = item.getArray(ADDRESSES);
        if (addresses.length() != 0) {
            for (int i = 0; i < addresses.length(); i++) {
                JsonObject keyValue = addresses.getObject(i);
                builder.appendHtmlConstant(
                        "<span title=\"" + keyValue.getString(KEY) + "\" class=\"" + address + "\">");
                builder.appendEscaped(keyValue.getString(VALUE));
                builder.appendEscaped(" / ");
                builder.appendHtmlConstant("</span>");
            }
        }
        builder.appendHtmlConstant(highlight(query).replace(name, "<b>$1</b>")) //NON-NLS
                .appendHtmlConstant("</div>");
        return builder.toSafeHtml().asString();
    }
}
