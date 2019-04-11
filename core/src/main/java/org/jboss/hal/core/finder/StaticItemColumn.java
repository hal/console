/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.finder;

import java.util.List;

import elemental2.dom.HTMLElement;

/** Finder column for {@link StaticItem}. */
public class StaticItemColumn extends FinderColumn<StaticItem> {

    public static class StaticItemDisplay implements ItemDisplay<StaticItem> {

        private final StaticItem item;

        public StaticItemDisplay(StaticItem item) {
            this.item = item;
        }

        @Override
        public String getId() {
            return item.getId() != null ? item.getId() : ItemDisplay.super.getId();
        }

        @Override
        public String getTitle() {
            return item.getTitle();
        }

        @Override
        public HTMLElement element() {
            if (item.getSubtitle() != null) {
                return ItemDisplay.withSubtitle(item.getTitle(), item.getSubtitle());
            }
            return null;
        }

        @Override
        public List<ItemAction<StaticItem>> actions() {
            return item.getActions();
        }

        @Override
        public String getFilterData() {
            return item.getKeywords().isEmpty() ? null : String.join(" ", item.getKeywords());
        }

        @Override
        public String nextColumn() {
            return item.getNextColumn();
        }
    }

    public StaticItemColumn(Finder finder, String id, String title, List<StaticItem> items) {
        super(new Builder<StaticItem>(finder, id, title)
                .itemRenderer(StaticItemDisplay::new)
                .initialItems(items)
                .onPreview(StaticItem::getPreviewContent)
                .withFilter(items.stream().anyMatch(i -> !i.getKeywords().isEmpty())));
    }

    public StaticItemColumn(Finder finder, String id, String title, ItemsProvider<StaticItem> itemsProvider) {
        super(new Builder<StaticItem>(finder, id, title)
                .itemRenderer(StaticItemDisplay::new)
                .itemsProvider(itemsProvider)
                .onPreview(StaticItem::getPreviewContent));
    }
}
