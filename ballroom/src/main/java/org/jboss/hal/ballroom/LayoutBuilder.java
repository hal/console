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

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.CSS;

import static org.jboss.hal.resources.CSS.offset;
import static org.jboss.hal.resources.CSS.row;

/**
 * @author Harald Pehl
 */
public class LayoutBuilder extends Elements.CoreBuilder<LayoutBuilder> {

    public LayoutBuilder() {
        super("hal.layoutBuilder");
    }

    @Override
    protected LayoutBuilder that() {
        return this;
    }

    /**
     * Starts a row. You always need at least one row. Rows should contain columns only.
     */
    public LayoutBuilder row() {
        return div().css(row);
    }

    /**
     * Starts a column. Columns should contain (sub)headers, elements or tabs.
     */
    public LayoutBuilder column() {
        return column(0, 12);
    }

    public LayoutBuilder column(int offset, int columns) {
        return div().css(rowCss(offset, columns));
    }

    /**
     * Adds a h1 header. Needs to closed!
     */
    public LayoutBuilder header(String title) {
        return h(1).textContent(title);
    }

    private String rowCss(int offset, int columns) {
        return offset == 0 ? CSS.column(columns) : offset(offset) + " " + CSS.column(columns);
    }
}