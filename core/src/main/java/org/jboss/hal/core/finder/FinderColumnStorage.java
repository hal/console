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

import java.util.HashSet;
import java.util.Set;

import org.jboss.hal.resources.Ids;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;

import static elemental2.dom.DomGlobal.window;

class FinderColumnStorage {

    private final String pinnedId;
    private final Storage storage;

    FinderColumnStorage(final String id) {
        this.pinnedId = Ids.build(Ids.STORAGE, id, "pinned");
        this.storage = WebStorageWindow.of(window).localStorage;
    }

    void pinItem(String id) {
        Set<String> items = pinnedItems();
        items.add(id);
        save(items);
    }

    void unpinItem(String id) {
        Set<String> items = pinnedItems();
        items.remove(id);
        save(items);
    }

    Set<String> pinnedItems() {
        Set<String> items = new HashSet<>();
        if (storage != null) {
            String pinnedItems = storage.getItem(this.pinnedId);
            if (pinnedItems != null) {
                // noinspection ResultOfMethodCallIgnored
                Iterables.addAll(items, Splitter.on(',').split(pinnedItems));
            }
        }
        return items;
    }

    private void save(Set<String> items) {
        if (storage != null) {
            storage.setItem(pinnedId, String.join(",", items));
        }
    }
}
