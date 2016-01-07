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
package org.jboss.hal.ballroom.layout;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.tab.Tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class LayoutBuilder {

    private static final String TABS = "tabs";
    private static final String TAB = "tab";
    private static final String PANES = "panes";

    private final Elements.Builder eb;
    private int offset;
    private int columns;
    private Tabs tabs;

    public LayoutBuilder() {
        // start the top level "row" div
        this.eb = new Elements.Builder().div().css(row);
        this.offset = 0;
        this.columns = 12;
    }

    public LayoutBuilder startRow() {
        return startRow(0, 12);
    }

    public LayoutBuilder startRow(int offset, int columns) {
        assertNoTabs();
        this.offset = offset;
        this.columns = columns;
        eb.div().css(rowCss());
        return this;
    }

    public LayoutBuilder endRow() {
        assertNoTabs();
        eb.end();
        return this;
    }

    public LayoutBuilder header(String title) {
        assertNoTabs();
        eb.h(1).innerText(title).end();
        return this;
    }

    public LayoutBuilder subheader(String title) {
        assertNoTabs();
        eb.h(3).innerText(title).end();
        return this;
    }

    public LayoutBuilder add(Element first, Element... rest) {
        assertNoTabs();

        List<Element> elements = new ArrayList<>();
        elements.add(first);
        if (rest != null) {
            Collections.addAll(elements, rest);
        }
        for (Element element : elements) {
            eb.add(element);
        }
        return this;
    }

    /**
     * Enters the tab mode.
     */
    public LayoutBuilder startTabs() {
        if (tabs != null) {
            throw new IllegalStateException("Nested tabs are not supported");
        }
        tabs = new Tabs();
        return this;
    }

    /**
     * Adds a tab. The specified elements are added to the pane.
     */
    public LayoutBuilder tab(String id, String title, Element first, Element... rest) {
        if (tabs == null) {
            throw new IllegalStateException("Not in tab mode");
        }
        tabs.add(id, title, first, rest);
        return this;
    }

    /**
     * Exits the tab mode.
     */
    public LayoutBuilder endTabs() {
        eb.add(tabs.asElement());
        tabs = null;
        return this;
    }

    private String rowCss() {
        return offset == 0 ? column(columns) : offset(offset) + " " + column(columns);
    }

    private void assertNoTabs() {
        if (tabs != null) {
            throw new IllegalStateException("Not allowed inside tabs");
        }
    }

    public Element build() {
        // close and return the top level "row" div
        return eb.end().build();
    }
}
