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
package org.jboss.hal.core.finder;

import com.google.gwt.inject.client.AsyncProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class ColumnRegistry {

    private static class AsyncColumn {

        final String id;
        final FinderColumn column;
        final AsyncProvider<List> itemProvider;

        private AsyncColumn(final FinderColumn column, final AsyncProvider<List> itemProvider) {
            this.id = column.getId();
            this.column = column;
            this.itemProvider = itemProvider;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) { return true; }
            if (!(o instanceof AsyncColumn)) { return false; }

            AsyncColumn that = (AsyncColumn) o;

            return id.equals(that.id);

        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }


    private final Map<String, AsyncColumn> columns;

    public ColumnRegistry() {
        columns = new HashMap<>();
    }

    public void registerColumn(FinderColumn column, AsyncProvider<List> itemProvider) {
        columns.put(column.getId(), new AsyncColumn(column, itemProvider));
    }

    public FinderColumn getColumn(String id) {
        AsyncColumn asyncColumn = columns.get(id);
        if (asyncColumn != null) {
            return asyncColumn.column;
        }
        return null;
    }

    public AsyncProvider<List> getItemProvider(String id) {
        AsyncColumn asyncColumn = columns.get(id);
        if (asyncColumn != null) {
            return asyncColumn.itemProvider;
        }
        return null;
    }
}
