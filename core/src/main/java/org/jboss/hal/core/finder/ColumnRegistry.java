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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.spi.Footer;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    @NonNls private static final Logger logger = LoggerFactory.getLogger(ColumnRegistry.class);

    private final MetadataProcessor metadataProcessor;
    private final RequiredResources requiredResources;
    private final Provider<Progress> progress;
    // Do not use columns directly instead use providers to ensure late initialization. Background: Some columns use
    // Environment.isStandalone() in their constructors. If they were referenced by instance this flag would not yet
    // be initialized.
    private final Map<String, Provider<?>> columns;
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

    public <C extends FinderColumn<T>, T> void registerColumn(String id, Provider<C> column) {
        columns.put(id, column);
    }

    public void registerColumn(String id, AsyncProvider column) {
        asyncColumns.put(id, column);
    }

    void lookup(String id, LookupCallback callback) {
        if (resolvedColumns.containsKey(id)) {
            callback.found(resolvedColumns.get(id));

        } else {
            logger.debug("Try to lookup column '{}'", id);
            if (!requiredResources.getResources(id).isEmpty()) {
                // first of all process the required resources attached to this column
                logger.debug("Column '{}' has the following required resources attached to it: {}", id,
                        requiredResources.getResources(id));
                metadataProcessor.process(id, progress.get(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        //noinspection HardCodedStringLiteral
                        callback.error("Unable to load required resources for column '" + id +
                                ((throwable != null) ? "': " + throwable.getMessage() : "'"));
                    }

                    @Override
                    public void onSuccess(final Void aVoid) {
                        lookupInternal(id, callback);
                    }
                });

            } else {
                logger.debug("No required resources attached to column '{}'", id);
                lookupInternal(id, callback);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void lookupInternal(String id, LookupCallback callback) {
        if (columns.containsKey(id)) {
            // this is a regular column: we're ready to go
            FinderColumn column = (FinderColumn) columns.get(id).get();
            resolve(id, column);
            callback.found(column);

        } else if (asyncColumns.containsKey(id)) {
            // the column sits behind a split point: load it asynchronously
            logger.debug("Load async column '{}'", id);
            AsyncProvider<FinderColumn> asyncProvider = asyncColumns.get(id);
            asyncProvider.get(new AsyncCallback<FinderColumn>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    callback.error("Unable to load column '" + id + "': " + throwable.getMessage()); //NON-NLS
                }

                @Override
                public void onSuccess(final FinderColumn column) {
                    resolve(id, column);
                    callback.found(column);
                }
            });

        } else {
            //noinspection HardCodedStringLiteral
            callback.error("Unknown column '" + id + "'. Please make sure to register all columns, before using them.");
        }
    }

    private void resolve(String id, FinderColumn column) {
        logger.info("Successfully resolved column '{}'", id);
        columns.remove(id);
        asyncColumns.remove(id);
        resolvedColumns.put(id, column);
    }
}
