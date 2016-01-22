/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.tab;

import elemental.dom.Element;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
public class Tabs implements IsElement {

    @JsType(isNative = true)
    static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void tab(String command);
    }


    private static final String TABS = "tabs";
    private static final String TAB = "tab";
    private static final String PANES = "panes";

    private final Element root;
    private final Element tabs;
    private final Element panes;
    private final Map<Integer, String> indexToId;

    public Tabs() {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div()
                .ul().css(nav, navTabs, navTabsPf, navTabsHal).attr(ROLE, "tablist").rememberAs(TABS).end() //NON-NLS
                .div().css(tabContent).rememberAs(PANES).end()
            .end();
        // @formatter:on

        root = builder.build();
        tabs = builder.referenceFor(TABS);
        panes = builder.referenceFor(PANES);
        indexToId = new HashMap<>();
    }

    @Override
    public Element asElement() {
        return root;
    }

    public Tabs add(String id, String title, Element first, Element... rest) {
        int size = tabs.getChildElementCount();
        if (size != panes.getChildElementCount()) {
            throw new IllegalStateException(
                    "Unbalanced containers: tabs(" + size + ") != panes(" + panes.getChildElementCount() + ")");
        }
        indexToId.put(size, id);

        // @formatter:off
        Element tab = new Elements.Builder()
            .li().attr(ROLE, "presentation") //NON-NLS
                .a("#" + id).aria(CONTROLS, id).attr(ROLE, TAB).data(TOGGLE, TAB)
                    .on(click, event -> {
                        event.preventDefault();
                        showTab(id);
                    })
                    .innerText(title)
                .end()
            .end()
        .build();

        Element pane = new Elements.Builder()
            .div()
                .id(id)
                .css(tabPane)
                .attr(ROLE, "tabpanel") //NON-NLS
            .end()
        .build();
        // @formatter:on

        tabs.appendChild(tab);
        panes.appendChild(pane);
        if (tabs.getChildren().getLength() == 1) {
            tab.getClassList().add(active);
        }
        if (panes.getChildren().getLength() == 1) {
            pane.getClassList().add(active);
        }

        List<Element> elements = new ArrayList<>();
        elements.add(first);
        if (rest != null) {
            Collections.addAll(elements, rest);
        }
        for (Element element : elements) {
            pane.appendChild(element);
        }

        return this;
    }

    public void showTab(final int index) {
        showTab(indexToId.get(index));
    }

    public void showTab(final String id) {
        if (id != null) {
            Bridge.select("#" + id).tab("show"); //NON-NLS
        }
    }
}
