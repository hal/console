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

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/** A customizable finder item useful when you need full control over each and every item. */
public class StaticItem {

    private final String title;
    private final String subtitle;
    private final List<ItemAction<StaticItem>> actions;
    private final List<String> keywords;
    private final String nextColumn;
    private final PreviewContent<StaticItem> previewContent;
    private String id;

    private StaticItem(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.actions = builder.actions;
        this.keywords = builder.keywords;
        this.nextColumn = builder.nextColumn;
        this.previewContent = builder.previewContent;
    }

    public String getId() {
        return id;
    }

    public List<ItemAction<StaticItem>> getActions() {
        return actions;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public PreviewContent<StaticItem> getPreviewContent() {
        return previewContent;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getNextColumn() {
        return nextColumn;
    }

    public static class Builder {

        private final String title;
        private final List<ItemAction<StaticItem>> actions;
        private final List<String> keywords;
        private String id;
        private String subtitle;
        private PreviewContent<StaticItem> previewContent;
        private String nextColumn;

        public Builder(String title) {
            this.title = title;
            this.actions = new ArrayList<>();
            this.keywords = new ArrayList<>();
            this.previewContent = new PreviewContent<>(title);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder action(String title, ItemActionHandler<StaticItem> action) {
            actions.add(new ItemAction.Builder<StaticItem>().title(title).handler(action).build());
            return this;
        }

        public Builder action(ItemAction<StaticItem> itemAction) {
            actions.add(itemAction);
            return this;
        }

        public Builder keywords(String first, String... rest) {
            keywords.add(first);
            if (rest != null) {
                keywords.addAll(asList(rest));
            }
            return this;
        }

        public Builder nextColumn(String nextColumn) {
            this.nextColumn = nextColumn;
            return this;
        }

        public Builder onPreview(PreviewContent<StaticItem> previewContent) {
            this.previewContent = previewContent;
            return this;
        }

        public StaticItem build() {
            return new StaticItem(this);
        }
    }
}
