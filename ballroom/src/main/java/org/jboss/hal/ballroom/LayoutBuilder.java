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

import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.hal.resources.CSS;

import elemental2.dom.HTMLDivElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.resources.CSS.*;

/** Helper methods to create {@code <div/>} elements for bootstrap grids. */
public final class LayoutBuilder {

    public static HtmlContentBuilder<HTMLDivElement> row() {
        return div().css(row);
    }

    public static HtmlContentBuilder<HTMLDivElement> column() {
        return column(0, 12);
    }

    public static HtmlContentBuilder<HTMLDivElement> column(int columns) {
        return column(0, columns);
    }

    public static HtmlContentBuilder<HTMLDivElement> column(int offset, int columns) {
        return div().css(rowCss(offset, columns));
    }

    private static String rowCss(int offset, int columns) {
        return offset == 0
                ? CSS.column(columns, columnLg, columnMd, columnSm)
                : offset(offset, columnLg, columnMd, columnSm) + " " + CSS
                        .column(columns, columnLg, columnMd, columnSm);
    }

    private LayoutBuilder() {
    }
}
