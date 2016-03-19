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
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.spi.Footer;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for finder columns. Manages both sync and async columns behind a split point.
 *
 * @author Harald Pehl
 */
public class ColumnRegistry {

    interface LookupCallback {

        void found(FinderColumn column);

        void error(String failure);
    }


    private final MetadataProcessor metadataProcessor;
    private final RequiredResources requiredResources;
    private final Provider<Progress> progress;
    private final Map<String, FinderColumn> columns;
    private final Map<String, AsyncProvider> asyncColumns;
    private final Map<String, FinderColumn> resolvedColumns;

    @Inject
    public ColumnRegistry(MetadataProcessor metadataProcessor, RequiredResources requiredResources,
            @Footer Provider<Progress> progress) {
        this.metadataProcessor = metadataProcessor;
        this.requiredResources = requiredResources;
        this.progress = progress;
        this.columns = new HashMap<>();
        this.asyncColumns = new HashMap<>();
        this.resolvedColumns = new HashMap<>();
    }

    public void registerColumn(FinderColumn column) {
        columns.put(column.getId(), column);
    }

    public void registerColumn(String id, AsyncProvider column) {
        asyncColumns.put(id, column);
    }

    @SuppressWarnings("unchecked")
    void lookup(String id, LookupCallback callback) {
        if (resolvedColumns.containsKey(id)) {
            callback.found(resolvedColumns.get(id));

        } else if (columns.containsKey(id)) {
            FinderColumn column = columns.get(id);
            if (requiredResources.getResources(id).isEmpty()) {
                resolve(id, column);
                callback.found(column);

            } else {
                // process the required resource attached to this column
                metadataProcessor.process(id, progress.get(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        //noinspection HardCodedStringLiteral
                        callback.error("Unable to load required resources for column '" + id + "': " + throwable
                                .getMessage());
                    }

                    @Override
                    public void onSuccess(final Void aVoid) {
                        resolve(id, column);
                        callback.found(column);
                    }
                });
            }

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
                    columns.put(id, column);
                    lookup(id, callback);
                }
            });

        } else {
            //noinspection HardCodedStringLiteral
            callback.error("Unknown column '" + id + "'. Please make sure to register all columns, before using them.");
        }
    }

    private void resolve(String id, FinderColumn column) {
        columns.remove(id);
        resolvedColumns.put(id, column);
    }
}
