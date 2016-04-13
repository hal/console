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
package org.jboss.hal.ballroom;

import elemental.client.Browser;
import elemental.dom.Element;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.UIConstants;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Accordion implements IsElement {

    @JsType(isNative = true)
    static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void collapse(String command);
    }


    private static final String BODY_ELEMENT = "panelBody";

    private final String id;
    private final Element root;

    public Accordion(final String id) {
        this.id = id;
        root = new Elements.Builder().div()
                .id(id)
                .css(panelGroup)
                .aria("multiselectable", String.valueOf(true))
                .attr(UIConstants.ROLE, UIConstants.TABLIST)
                .build();
    }

    public void add(String id, String title, Element first, Element... rest) {
        boolean firstPanel = root.getChildElementCount() == 0;
        String headerId = IdBuilder.build(id, "header");

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(panel, panelDefault)
                .div().css(panelHeading).id(headerId)
                    .h(4).css(panelTitle)
                        .a("#" + id)
                            .data(UIConstants.TOGGLE, UIConstants.COLLAPSE)
                            .data("parent", "#" + this.id)
                            .aria(UIConstants.CONTROLS, id)
                            .aria(UIConstants.EXPANDED, String.valueOf(firstPanel))
                            .attr(UIConstants.ROLE, UIConstants.BUTTON)
                            .textContent(title)
                        .end()
                    .end()
                .end()
                .div().id(id).css(panelCollapse, collapse, firstPanel ? in : "").aria("labelledby", headerId)
                    .div().css(panelBody).rememberAs(BODY_ELEMENT).end()
                .end()
            .end();
        // @formatter:on

        Element body = builder.referenceFor(BODY_ELEMENT);
        fillBody(body, first, rest);

        root.appendChild(builder.build());
    }

    private void fillBody(Element body, Element first, Element... rest) {
        body.appendChild(first);
        if (rest != null) {
            for (Element element : rest) {
                body.appendChild(element);
            }
        }
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void showPanel(final String id) {
        Bridge.select("#" + id).collapse("show"); //NON-NLS
    }

    public void hidePanel(final String id) {
        Bridge.select("#" + id).collapse("hide"); //NON-NLS
    }

    public void togglePanel(final String id) {
        Bridge.select("#" + id).collapse("toggle"); //NON-NLS
    }

    public void setContent(final String id, Element first, Element... rest) {
        if (id != null) {
            Element body = Browser.getDocument().querySelector("#" + id + " > ." + panelBody);
            if (body != null) {
                Elements.removeChildrenFrom(body);
                fillBody(body, first, rest);
            }
        }
    }
}
