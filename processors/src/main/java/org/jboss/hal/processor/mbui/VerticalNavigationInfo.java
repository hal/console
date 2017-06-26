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
package org.jboss.hal.processor.mbui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
public class VerticalNavigationInfo extends MbuiElementInfo {

    public static class Item {

        private final String id;
        private final String name;
        private final String title;
        private final String icon;
        private final List<Content> content;
        private final List<Item> subItems;
        private final Map<String, Item> subItemsById;

        Item(final String id, final String title, final String icon) {
            this.id = id;
            this.name = LOWER_HYPHEN.to(LOWER_CAMEL, id);
            this.title = Handlebars.templateSafeValue(title); // title can be a simple value or an expression
            this.icon = icon;
            this.content = new ArrayList<>();
            this.subItems = new ArrayList<>();
            this.subItemsById = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }

        public List<Item> getSubItems() {
            return subItems;
        }

        void addSubItem(Item subItem) {
            subItems.add(subItem);
            subItemsById.put(subItem.getId(), subItem);
        }

        Item getItem(String id) {
            return subItemsById.get(id);
        }

        public List<Content> getContent() {
            return content;
        }

        public List<Content> getHtmlContent() {
            return content.stream().filter(c -> c.getHtml() != null).collect(toList());
        }

        void addContent(Content content) {
            this.content.add(content);
        }

        Content findContent(final String id) {
            for (Content c : content) {
                if (id.equals(c.getReference())) {
                    return c;
                }
            }
            return null;
        }
    }


    private final List<Item> items;
    private final Map<String, Item> itemsById;

    VerticalNavigationInfo(final String name, final String selector) {
        super(name, selector);
        this.items = new ArrayList<>();
        this.itemsById = new HashMap<>();
    }

    public List<Item> getItems() {
        return items;
    }

    void addItem(Item item) {
        items.add(item);
        itemsById.put(item.getId(), item);
    }

    Item getItem(String id) {
        Item item = itemsById.get(id);
        if (item == null) {
            for (Item itm : items) {
                Item subItem = itm.getItem(id);
                if (subItem != null) {
                    return subItem;
                }
            }
        }
        return item;
    }
}
