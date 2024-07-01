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
package org.jboss.hal.core.finder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import elemental2.promise.Promise;

/** Registry for finder columns. Manages both sync and async columns behind a split point. */
public class ColumnRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ColumnRegistry.class);

    private final MetadataProcessor metadataProcessor;
    private final RequiredResources requiredResources;
    private final Provider<Progress> progress;
    // Do not use columns directly instead use providers to ensure late initialization. Background: Some columns use
    // Environment.isStandalone() in their constructors. If they were referenced by instance this flag would not yet
    // be initialized.
    private final Map<String, Provider<? extends FinderColumn<?>>> columns;
    private final Map<String, AsyncProvider<? extends FinderColumn<?>>> asyncColumns;
    private final Map<String, FinderColumn<?>> resolvedColumns;

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

    public <C extends FinderColumn<T>, T> void registerColumn(String id, AsyncProvider<C> column) {
        asyncColumns.put(id, column);
    }

    <C extends FinderColumn<T>, T> Promise<C> lookup(String id) {
        Set<String> resources = requiredResources.getResources(id);
        if (resolvedColumns.containsKey(id)) {
            if (resources.stream().anyMatch(r -> r.contains("{selected.host}") || r.contains("{selected.server"))) {
                // Column depends on a selection (e.g. selected.host/server). These columns are processed for the
                // current selection only. If the column is already resolved, the resources need to be processed for
                // the new selection
                logger.debug("Column '{}' has depends on selection: {}", id, resources);
                metadataProcessor.process(id, progress.get());
            }
            // noinspection unchecked
            return Promise.resolve((C) resolvedColumns.get(id));
        } else {
            logger.debug("Try to lookup column '{}'", id);
            if (!resources.isEmpty()) {
                // process the required resources attached to this column
                logger.debug("Column '{}' has the following required resources attached to it: {}", id, resources);
                return metadataProcessor.process(id, progress.get()).then(v -> lookupInternal(id));
            } else {
                logger.debug("No required resources attached to column '{}'", id);
                return lookupInternal(id);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <C extends FinderColumn<T>, T> Promise<C> lookupInternal(String id) {
        if (columns.containsKey(id)) {
            // this is a regular column: we're ready to go
            Provider<C> provider = (Provider<C>) columns.get(id);
            C column = provider.get();
            resolve(id, column);
            return Promise.resolve(column);

        } else if (asyncColumns.containsKey(id)) {
            // the column sits behind a split point: load it asynchronously
            logger.debug("Load async column '{}'", id);
            AsyncProvider<C> asyncProvider = (AsyncProvider<C>) asyncColumns.get(id);
            return new Promise<>((resolve, reject) -> {
                asyncProvider.get(new AsyncCallback<C>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        reject.onInvoke("Unable to load column '" + id + "': " + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(final C column) {
                        resolve(id, column);
                        resolve.onInvoke(column);
                    }
                });
            });

        } else {
            throw new RuntimeException(
                    "Unknown column '" + id + "'. Please make sure to register all columns, before using them.");
        }
    }

    private <C extends FinderColumn<T>, T> void resolve(String id, C column) {
        logger.info("Successfully resolved column '{}'", id);
        columns.remove(id);
        asyncColumns.remove(id);
        resolvedColumns.put(id, column);
    }
}
