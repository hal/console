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

import java.util.function.Function;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.ballroom.autocomplete.ItemRenderer.highlight;
import static org.jboss.hal.resources.CSS.autocompleteSuggestion;

public final class StringRenderer<T> implements ItemRenderer<T> {

    private final Function<T, String> toString;

    public StringRenderer(Function<T, String> toString) {
        this.toString = toString;
    }

    @Override
    public String render(T item, String query) {
        String itm = toString.apply(item);
        @NonNls SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant(
                "<div class=\"" + autocompleteSuggestion + "\" data-val=\"" + itm + "\">")
                .appendHtmlConstant(highlight(query).replace(itm, "<b>$1</b>")) //NON-NLS
                .appendHtmlConstant("</div>");
        return builder.toSafeHtml().asString();
    }
}
