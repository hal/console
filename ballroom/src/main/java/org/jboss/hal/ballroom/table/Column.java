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
package org.jboss.hal.ballroom.table;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * Options for a column in a data table.
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/reference/option/columns">https://datatables.net/reference/option/columns</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Column<T> {

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
     * @see <a href="https://datatables.net/reference/option/columns.render">https://datatables.net/reference/option/columns.render</a>
     */
    @JsFunction
    @FunctionalInterface
    public interface RenderCallback<T, C> {

        /**
         * Render function
         *
         * @param cell the data for the cell
         * @param type the type call data requested - this will be "filter", "display", "type" or "sort".
         * @param row  the full data source for the row
         * @param meta an object that contains additional information about the cell being requested
         *
         * @return the return value from the function is what will be used for the data requested
         */
        C render(C cell, String type, T row, Meta meta);
    }


    public String name;
    public String title;
    public RenderCallback<T, ?> render;
    public boolean orderable;
    public boolean searchable;
    public String type;
    public String width;
}
