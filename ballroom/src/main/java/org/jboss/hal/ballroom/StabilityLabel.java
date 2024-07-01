/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import org.jboss.elemento.IsElement;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.CSS.marginRight5;

public class StabilityLabel implements IsElement<HTMLElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    public static SafeHtml stabilityLevelHtml(StabilityLevel stability, boolean compact) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        String name = compact ? String.valueOf(stability.label.toUpperCase().charAt(0)) : stability.label;
        String stabilityClass = CSS.stability + "-" + stability.name().toLowerCase();
        html.appendHtmlConstant("<span class=\"" + CSS.label + " " + stabilityClass + " " + CSS.marginRight5 + "\" " +
                "title=\"" + CONSTANTS.stabilityLevel() + ": " + stability.name().toLowerCase() + "\">" +
                "<i class=\"fa fa-flask\"></i>")
                .appendEscaped(" " + name)
                .appendHtmlConstant("</span>");
        return html.toSafeHtml();
    }

    private final HTMLElement root;

    public StabilityLabel(StabilityLevel stability) {
        this(stability, false);
    }

    public StabilityLabel(StabilityLevel stability, boolean compact) {
        String name = compact ? String.valueOf(stability.label.toUpperCase().charAt(0)) : stability.label;
        String stabilityClass = CSS.stability + "-" + stability.label;
        root = span().css(CSS.label, stabilityClass, marginRight5)
                .title(CONSTANTS.stabilityLevel() + ": " + stability.name().toLowerCase())
                .add(i().css(stability.icon))
                .add(" ")
                .add(name)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
