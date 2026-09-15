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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import elemental2.promise.Promise;

/** ItemsProvider that requires resource name(s) to be retrieved from parent column(s) */
public final class DependentItemsProvider<T> implements ItemsProvider<T> {

    private final Function<String[], Promise<List<T>>> retrieveItems;
    private final ParentResolver[] parentResolvers;
    private String[] parentNames;

    public DependentItemsProvider(Function<String[], Promise<List<T>>> retrieveItems, String... parentColumnIds) {
        this.retrieveItems = retrieveItems;
        this.parentResolvers = new ParentResolver[parentColumnIds.length];
        for (int i = 0; i < parentResolvers.length; i++) {
            parentResolvers[i] = new ParentResolver(parentColumnIds[i]);
        }
    }

    private void resolve(FinderPath path) {
        parentNames = new String[parentResolvers.length];
        for (int i = 0; i < parentResolvers.length; i++) {
            parentResolvers[i].resolve(path);
            parentNames[i] = parentResolvers[i].getName();
        }
    }

    private boolean present() {
        for (ParentResolver parentResolver : parentResolvers) {
            if (!parentResolver.present()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Promise<List<T>> items(FinderContext context) {
        resolve(context.getPath());
        if (!present()) {
            // shouldn't we throw an error if the parent column is missing?
            return Promise.resolve(Collections.emptyList());
        }
        return retrieveItems.apply(parentNames);
    }

    public static ParentResolver resolver(String parentColumnId) {
        return new ParentResolver(parentColumnId);
    }

    public static ParentResolver resolver(String parentColumnId, FinderPath path) {
        ParentResolver resolver = resolver(parentColumnId);
        resolver.resolve(path);
        return resolver;
    }

    public static class ParentResolver {
        private final String parentColumnId;
        private FinderSegment<?> segment;

        public ParentResolver(String parentColumnId) {
            this.parentColumnId = parentColumnId;
        }

        public void resolve(FinderPath path) {
            segment = path.findColumn(parentColumnId);
        }

        public boolean present() {
            return segment != null;
        }

        public String getName() {
            return present() ? segment.getItemTitle() : null;
        }
    }
}
