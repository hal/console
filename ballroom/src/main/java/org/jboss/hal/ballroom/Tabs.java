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
package org.jboss.hal.ballroom;

import elemental.dom.Element;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.UIConstants;

import java.util.HashMap;
import java.util.Map;

import static jsinterop.annotations.JsPackage.GLOBAL;
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
                    .innerText(title)
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
        fillPane(pane, first, rest);

        return this;
    }

    private void fillPane(Element pane, Element first, Element... rest) {
        pane.appendChild(first);
        if (rest != null) {
            for (Element element : rest) {
                pane.appendChild(element);
            }
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
                fillPane(pane, first, rest);
            }
        }
    }
}
