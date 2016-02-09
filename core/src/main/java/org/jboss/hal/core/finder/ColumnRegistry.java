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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.hal.core.finder.ColumnRegistry.LookupResult.*;

/**
 * @author Harald Pehl
 */
public class ColumnRegistry {

    public enum LookupResult {ASYNC, READY, UNKNOWN}


    @FunctionalInterface
    public interface ColumnReadyCallback {

        void ready(FinderColumn column);
    }


    private static final Logger logger = LoggerFactory.getLogger(ColumnRegistry.class);

    private final Map<String, FinderColumn> columns;
    private final Map<String, AsyncProvider> asyncColumns;

    public ColumnRegistry() {
        columns = new HashMap<>();
        asyncColumns = new HashMap<>();
    }

    public void registerColumn(FinderColumn column) {
        columns.put(column.getId(), column);
    }

    public <C extends FinderColumn> void registerColumn(String id, AsyncProvider<C> column) {
        asyncColumns.put(id, column);
    }

    public FinderColumn getColumn(String id) {
        return columns.get(id);
    }

    public LookupResult lookup(String id) {
        if (columns.containsKey(id)) {
            return READY;
        } else if (asyncColumns.containsKey(id)) {
            return ASYNC;
        } else {
            return UNKNOWN;
        }
    }

    @SuppressWarnings("unchecked")
    public void loadColumn(String id, ColumnReadyCallback callback) {
        if (asyncColumns.containsKey(id)) {
            AsyncProvider<FinderColumn> asyncProvider = asyncColumns.get(id);
            asyncProvider.get(new AsyncCallback<FinderColumn>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    logger.error("Unable to load column {}: {}", id, throwable.getMessage()); //NON-NLS
                }

                @Override
                public void onSuccess(final FinderColumn column) {
                    asyncColumns.remove(id);
                    registerColumn(column);
                    callback.ready(column);
                }
            });
        }
    }
}
