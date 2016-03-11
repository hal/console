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
package org.jboss.hal.core.finder;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for finder columns. Manages both sync and async columns behind a split point.
 *
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
