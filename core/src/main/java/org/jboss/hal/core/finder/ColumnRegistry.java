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
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class ColumnRegistry {

    public interface LookupCallback {

        void found(FinderColumn column);

        void error(String failure);
    }


    private final Map<String, FinderColumn> columns;
    private final Map<String, AsyncProvider> asyncColumns;

    public ColumnRegistry() {
        columns = new HashMap<>();
        asyncColumns = new HashMap<>();
    }

    public void registerColumn(FinderColumn column) {
        columns.put(column.getId(), column);
    }

    public void registerColumn(String id, AsyncProvider column) {
        asyncColumns.put(id, column);
    }

    @SuppressWarnings("unchecked")
    void lookup(String id, LookupCallback callback) {
        if (columns.containsKey(id)) {
            callback.found(columns.get(id));

        } else if (asyncColumns.containsKey(id)) {
            AsyncProvider<FinderColumn> asyncProvider = asyncColumns.get(id);
            asyncProvider.get(new AsyncCallback<FinderColumn>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    callback.error("Unable to load column '" + id + "': " + throwable.getMessage()); //NON-NLS
                }

                @Override
                public void onSuccess(final FinderColumn column) {
                    asyncColumns.remove(id);
                    registerColumn(column);
                    callback.found(column);
                }
            });

        } else {
            //noinspection HardCodedStringLiteral
            callback.error("Unknown column '" + id + "'. Please make sure to register all columns, before using them.");
        }
    }
}
