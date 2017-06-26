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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.UIConstants;

import static java.util.Arrays.asList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Tabs implements IsElement {

    @JsType(isNative = true)
    static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void tab(String command);

        public native void on(String event, JsCallback callback);
    }


    private final HTMLElement root;
    private final HTMLElement tabs;
    private final HTMLElement panes;
    private final Map<Integer, String> indexToId;
    private final Map<String, HTMLElement> paneElements;

    public Tabs() {
        root = div()
                .add(tabs = ul()
                        .css(nav, navTabs, navTabsPf, navTabsHal)
                        .attr(UIConstants.ROLE, UIConstants.TABLIST)
                        .asElement())
                .add(panes = div().css(tabContent).asElement())
                .asElement();

        indexToId = new HashMap<>();
        paneElements = new HashMap<>();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public Tabs add(String id, String title, HTMLElement first, HTMLElement... rest) {
        return add(id, title, elements(first, rest));
    }

    public Tabs add(String id, String title, Iterable<HTMLElement> elements) {
        int size = (int) tabs.childElementCount;
        if (size != (int) panes.childElementCount) {
            throw new IllegalStateException(
                    "Unbalanced containers: tabs(" + size + ") != panes(" + panes.childElementCount + ")");
        }
        indexToId.put(size, id);

        HTMLElement tab = li().attr(UIConstants.ROLE, "presentation") //NON-NLS
                .add(a("#" + id)
                        .aria(UIConstants.CONTROLS, id)
                        .attr(UIConstants.ROLE, UIConstants.TAB)
                        .data(UIConstants.TOGGLE, UIConstants.TAB)
                        .on(click, event -> {
                            event.preventDefault();
                            showTab(id);
                        })
                        .textContent(title))
                .asElement();
        HTMLElement pane = div().id(id).css(tabPane).attr(UIConstants.ROLE, "tabpanel").asElement(); //NON-NLS

        tabs.appendChild(tab);
        panes.appendChild(pane);
        paneElements.put(id, pane);
        if (tabs.childNodes.getLength() == 1) {
            tab.classList.add(active);
        }
        if (panes.childNodes.getLength() == 1) {
            pane.classList.add(active);
        }
        fillPane(pane, elements);

        return this;
    }

    private List<HTMLElement> elements(HTMLElement first, HTMLElement... rest) {
        List<HTMLElement> elements = new ArrayList<>();
        elements.add(first);
        if (rest != null) {
            elements.addAll(asList(rest));
        }
        return elements;
    }

    private void fillPane(HTMLElement pane, Iterable<HTMLElement> elements) {
        for (HTMLElement element : elements) {
            pane.appendChild(element);
        }
    }

    public void showTab(final int index) {
        showTab(indexToId.get(index));
    }

    public void showTab(final String id) {
        if (id != null) {
            Bridge.select("a[href='#" + id + "']").tab("show"); //NON-NLS
        }
    }

    public void setContent(final int index, HTMLElement first, HTMLElement... rest) {
        setContent(indexToId.get(index), first, rest);
    }

    public void setContent(final String id, HTMLElement first, HTMLElement... rest) {
        if (id != null) {
            HTMLElement pane = paneElements.get(id);
            if (pane != null) {
                Elements.removeChildrenFrom(pane);
                fillPane(pane, elements(first, rest));
            }
        }
    }

    public void onShow(final String id, final JsCallback callback) {
        if (id != null) {
            Bridge.select("a[href='#" + id + "']").on("shown.bs.tab", callback); //NON-NLS
        }
    }
}
