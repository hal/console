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

import static org.jboss.hal.ballroom.typeahead.GroupedResultProcessor.GROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.CSS.ttGrouped;

/**
 * @author Harald Pehl
 */
class GroupedSuggestionTemplate implements Templates.SuggestionTemplate {

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String render(final JsJsonObject data) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<div class=\"" + ttGrouped + "\">");
        JsJsonArray groups = (JsJsonArray) data.get(GROUPS);
        for (int i = 0; i < groups.length(); i++) {
            builder.appendHtmlConstant("<span>").appendEscaped(groups.getString(i)).appendHtmlConstant("</span>");
        }
        builder.appendHtmlConstant("<span>").appendEscaped(data.getString(NAME)).appendHtmlConstant("</span>");
        builder.appendHtmlConstant("</div>");
        return builder.toSafeHtml().asString();
    }
}
