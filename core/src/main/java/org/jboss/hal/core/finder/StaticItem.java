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

import java.util.ArrayList;
import java.util.List;

/**
 * A customizable finder item useful when you need full control over each and every item.
 *
 * @author Harald Pehl
 */
public class StaticItem {

    public static class Builder {

        private final String title;
        private final List<ItemAction<StaticItem>> actions;
        private String id;
        private PreviewContent previewContent;
        private String nextColumn;

        public Builder(final String title) {
            this.title = title;
            this.actions = new ArrayList<>();
            this.previewContent = new PreviewContent(title);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder action(String title, ItemActionHandler<StaticItem> action) {
            actions.add(new ItemAction<>(title, action));
            return this;
        }

        public Builder action(ItemAction<StaticItem> itemAction) {
            actions.add(itemAction);
            return this;
        }

        public Builder nextColumn(String nextColumn) {
            this.nextColumn = nextColumn;
            return this;
        }

        public Builder onPreview(PreviewContent previewContent) {
            this.previewContent = previewContent;
            return this;
        }

        public StaticItem build() {
            return new StaticItem(this);
        }
    }


    private final String title;
    private final List<ItemAction<StaticItem>> actions;
    private final String nextColumn;
    private final PreviewContent previewContent;
    private String id;

    private StaticItem(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.actions = builder.actions;
        this.nextColumn = builder.nextColumn;
        this.previewContent = builder.previewContent;
    }

    public String getId() {
        return id;
    }

    public List<ItemAction<StaticItem>> getActions() {
        return actions;
    }

    public PreviewContent getPreviewContent() {
        return previewContent;
    }

    public String getTitle() {
        return title;
    }

    public String getNextColumn() {
        return nextColumn;
    }
}
