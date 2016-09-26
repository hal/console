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

import elemental.dom.Element;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.UIConstants;

import static java.util.Arrays.asList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Tabs implements IsElement {

    @JsFunction
    @FunctionalInterface
    public interface SelectHandler {

        void onSelect();
    }


    @JsType(isNative = true)
    static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void tab(String command);

        public native void on(String event, SelectHandler handler);
    }


    private static final String TABS = "tabs";
    private static final String TAB = "tab";
    private static final String PANES = "panes";

    private final Element root;
    private final Element tabs;
    private final Element panes;
    private final Map<Integer, String> indexToId;
    private final Map<String, Element> paneElements;

    public Tabs() {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div()
                .ul().css(nav, navTabs, navTabsPf, navTabsHal)
                    .attr(UIConstants.ROLE, UIConstants.TABLIST)
                    .rememberAs(TABS).end()
                .div().css(tabContent).rememberAs(PANES).end()
            .end();
        // @formatter:on

        root = builder.build();
        tabs = builder.referenceFor(TABS);
        panes = builder.referenceFor(PANES);
        indexToId = new HashMap<>();
        paneElements = new HashMap<>();
    }

    @Override
    public Element asElement() {
        return root;
    }

    public Tabs add(String id, String title, Element first, Element... rest) {
        return add(id, title, elements(first, rest));
    }

    public Tabs add(String id, String title, Iterable<Element> elements) {
        int size = tabs.getChildElementCount();
        if (size != panes.getChildElementCount()) {
            throw new IllegalStateException(
                    "Unbalanced containers: tabs(" + size + ") != panes(" + panes.getChildElementCount() + ")");
        }
        indexToId.put(size, id);

        // @formatter:off
        Element tab = new Elements.Builder()
            .li().attr(UIConstants.ROLE, "presentation") //NON-NLS
                .a("#" + id).aria(UIConstants.CONTROLS, id).attr(UIConstants.ROLE, TAB).data(UIConstants.TOGGLE, TAB)
                    .on(click, event -> {
                        event.preventDefault();
                        showTab(id);
                    })
                    .textContent(title)
                .end()
            .end()
        .build();

        Element pane = new Elements.Builder()
            .div()
                .id(id)
                .css(tabPane)
                .attr(UIConstants.ROLE, "tabpanel") //NON-NLS
            .end()
        .build();
        // @formatter:on

        tabs.appendChild(tab);
        panes.appendChild(pane);
        paneElements.put(id, pane);
        if (tabs.getChildren().getLength() == 1) {
            tab.getClassList().add(active);
        }
        if (panes.getChildren().getLength() == 1) {
            pane.getClassList().add(active);
        }
        fillPane(pane, elements);

        return this;
    }

    private List<Element> elements(Element first, Element... rest) {
        List<Element> elements = new ArrayList<>();
        elements.add(first);
        if (rest != null) {
            elements.addAll(asList(rest));
        }
        return elements;
    }

    private void fillPane(Element pane, Iterable<Element> elements) {
        for (Element element : elements) {
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

    public void setContent(final int index, Element first, Element... rest) {
        setContent(indexToId.get(index), first, rest);
    }

    public void setContent(final String id, Element first, Element... rest) {
        if (id != null) {
            Element pane = paneElements.get(id);
            if (pane != null) {
                Elements.removeChildrenFrom(pane);
                fillPane(pane, elements(first, rest));
            }
        }
    }

    public void onShow(final String id, final SelectHandler handler) {
        if (id != null) {
            Bridge.select("a[href='#" + id + "']").on("shown.bs.tab", handler); //NON-NLS
        }
    }
}
