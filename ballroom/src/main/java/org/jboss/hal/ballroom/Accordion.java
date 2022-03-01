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

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

import static elemental2.dom.DomGlobal.document;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.resources.CSS.collapse;
import static org.jboss.hal.resources.CSS.in;
import static org.jboss.hal.resources.CSS.panel;
import static org.jboss.hal.resources.CSS.panelBody;
import static org.jboss.hal.resources.CSS.panelCollapse;
import static org.jboss.hal.resources.CSS.panelDefault;
import static org.jboss.hal.resources.CSS.panelGroup;
import static org.jboss.hal.resources.CSS.panelHeading;
import static org.jboss.hal.resources.CSS.panelTitle;

public class Accordion implements IsElement {

    private final String id;
    private final HTMLDivElement root;

    public Accordion(String id) {
        this.id = id;
        root = div()
                .id(id)
                .css(panelGroup)
                .aria("multiselectable", UIConstants.TRUE)
                .attr(UIConstants.ROLE, UIConstants.TABLIST).element();
    }

    public void add(String id, String title, HTMLElement first, HTMLElement... rest) {
        boolean firstPanel = root.childElementCount == 0;
        String headerId = Ids.build(id, "header");

        // @formatter:off
        HTMLDivElement body;
        HTMLDivElement div = div().css(panel, panelDefault)
                .add(div().css(panelHeading).id(headerId)
                        .add(h(4).css(panelTitle)
                                .add(a(UIConstants.HASH + id)
                                        .data(UIConstants.TOGGLE, UIConstants.COLLAPSE)
                                        .data("parent", UIConstants.HASH + this.id)
                                        .aria(UIConstants.CONTROLS, id)
                                        .aria(UIConstants.EXPANDED, String.valueOf(firstPanel))
                                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                                        .textContent(title))))
                .add(div().id(id).css(panelCollapse, collapse, firstPanel ? in : null).aria("labelledby", headerId)
                        .add(body = div().css(panelBody).element()))
                .element();

        fillBody(body, first, rest);
        root.appendChild(div);
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
    public HTMLElement element() {
        return root;
    }

    public void showPanel(String id) {
        Api.select(UIConstants.HASH + id).collapse("show"); // NON-NLS
    }

    public void hidePanel(String id) {
        Api.select(UIConstants.HASH + id).collapse("hide"); // NON-NLS
    }

    public void togglePanel(String id) {
        Api.select(UIConstants.HASH + id).collapse("toggle"); // NON-NLS
    }

    public void setContent(String id, Element first, Element... rest) {
        if (id != null) {
            Element body = document.querySelector(UIConstants.HASH + id + " > ." + panelBody);
            if (body != null) {
                Elements.removeChildrenFrom(body);
                fillBody(body, first, rest);
            }
        }
    }

    @JsType(isNative = true)
    static class Api {

        @JsMethod(namespace = GLOBAL, name = "$")
        public static native Api select(String selector);

        public native void collapse(String command);
    }
}
