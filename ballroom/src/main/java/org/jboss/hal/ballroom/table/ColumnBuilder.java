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

/**
 * Builder for a {@link Column}.
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 */
public class ColumnBuilder<T> {

    private final String name;
    private final String title;
    private final Column.RenderCallback<T, ?> render;

    private boolean orderable;
    private boolean searchable;
    private String type;
    private String width;

    public ColumnBuilder(final String name, final String title, Column.RenderCallback<T, ?> render) {
        this.name = name;
        this.title = title;
        this.render = render;
        this.orderable = true;
        this.searchable = true;
    }

    public ColumnBuilder<T> orderable(boolean orderable) {
        this.orderable = orderable;
        return this;
    }

    public ColumnBuilder<T> searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public ColumnBuilder<T> type(String type) {
        this.type = type;
        return this;
    }

    public ColumnBuilder<T> width(String width) {
        this.width = width;
        return this;
    }

    public Column<T> build() {
        Column<T> column = new Column<>();
        column.name = name;
        column.title = title;
        column.render = render;
        column.orderable = orderable;
        column.searchable = searchable;
        if (type != null) {
            column.type = type;
        }
        if (width != null) {
            column.width = width;
        }
        return column;
    }
}
