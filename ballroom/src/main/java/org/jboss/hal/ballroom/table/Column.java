/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.table;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Options for a column in a data table.
 *
 * @param <T> the row type
 *
 * @see <a href="https://datatables.net/reference/option/columns">https://datatables.net/reference/option/columns</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Column<T> {

    public String name;
    public String title;
    public RenderCallback<T, String> render;
    public boolean orderable;
    public boolean searchable;
    public String className;
    public String type;
    public String width;

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Meta {

        public int row;
        public int col;
    }

    /**
     * Function to render the data of a column
     *
     * @param <T> the row type
     * @param <C> the column type
     *
     * @see <a href=
     *      "https://datatables.net/reference/option/columns.render">https://datatables.net/reference/option/columns.render</a>
     */
    @JsFunction
    @FunctionalInterface
    public interface RenderCallback<T, C> {

        /**
         * Render function
         *
         * @param cell the data for the cell
         * @param type the type call data requested - this will be "filter", "display", "type" or "sort".
         * @param row the full data source for the row
         * @param meta an object that contains additional information about the cell being requested
         *
         * @return the return value from the function is what will be used for the data requested
         */
        C render(C cell, String type, T row, Meta meta);
    }
}
