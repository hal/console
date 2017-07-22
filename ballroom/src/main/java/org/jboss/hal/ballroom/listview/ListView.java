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
package org.jboss.hal.ballroom.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.resources.CSS;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.listPf;
import static org.jboss.hal.resources.CSS.listPfStacked;

/**
 * PatternFly list view. Please note that the list view does not hold data. Instead use a {@link DataProvider} and
 * add it as a display to the data provider:
 *
 * <pre>
 * DataProvider dataProvider = ...;
 * ListView listView = ...;
 *
 * dataProvider.addDisplay(listView);
 * dataProvider.setItems(...);
 * </pre>
 *
 * @see <a href="http://www.patternfly.org/pattern-library/content-views/list-view/">http://www.patternfly.org/pattern-library/content-views/list-view/</a>
 */
public class ListView<T> implements Display<T>, IsElement {

    public static class Builder<T> {

        private final String id;
        private final ItemRenderer<T> itemRenderer;
        private final String[] contentWidths;
        private boolean multiselect;
        private boolean stacked;

        public Builder(String id, ItemRenderer<T> itemRenderer) {
            this.id = id;
            this.itemRenderer = itemRenderer;
            this.contentWidths = new String[]{"60%", "40%"};
            this.multiselect = false;
            this.stacked = true;
        }

        public Builder<T> multiselect(boolean multiselect) {
            this.multiselect = multiselect;
            return this;
        }

        public Builder<T> stacked(boolean stacked) {
            this.stacked = stacked;
            return this;
        }

        public Builder<T> contentWidths(String main, String additional) {
            contentWidths[0] = main;
            contentWidths[1] = additional;
            return this;
        }

        public ListView<T> build() {
            return new ListView<>(this);
        }
    }


    private final boolean multiselect;
    private final String[] contentWidths;
    private final ItemRenderer<T> itemRenderer;
    private final HTMLElement root;
    private final Map<String, ListItem<T>> currentItems;
    private SelectHandler<T> selectHandler;

    private ListView(Builder<T> builder) {
        this.multiselect = builder.multiselect;
        this.contentWidths = builder.contentWidths;
        this.itemRenderer = builder.itemRenderer;
        this.currentItems = new HashMap<>();

        HtmlContentBuilder<HTMLDivElement> div = div().id(builder.id).css(listPf);
        if (builder.stacked) {
            div.css(listPfStacked);
        }
        this.root = div.asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    /**
     * Select the item and fires a selection event if the item is in the visible range.
     */
    public void selectItem(T item) {
        ListItem<T> listItem = listItem(item);
        if (listItem != null) {
            select(listItem, true);
        }
    }

    void select(ListItem<T> item, boolean select) {
        if (select) {
            item.asElement().classList.add(active);
            if (selectHandler != null) {
                selectHandler.onSelect(item.item);
            }
        } else {
            item.asElement().classList.remove(active);
        }
        if (!multiselect) {
            // deselect all other items
            for (ListItem<T> otherItem : currentItems.values()) {
                if (otherItem == item) {
                    continue;
                }
                otherItem.asElement().classList.remove(active);
            }
        }
    }

    public void onSelect(SelectHandler<T> selectHandler) {
        this.selectHandler = selectHandler;
    }

    public boolean hasSelection() {
        return !selectedItems().isEmpty();
    }

    public T selectedItem() {
        HTMLElement element = (HTMLElement) root.querySelector("." + CSS.active);
        if (element != null && element.id != null && currentItems.containsKey(element.id)) {
            return currentItems.get(element.id).item;
        }
        return null;
    }

    public List<T> selectedItems() {
        List<T> selected = new ArrayList<>();
        NodeList<Element> nodes = root.querySelectorAll("." + CSS.active);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof HTMLElement) {
                HTMLElement element = (HTMLElement) nodes.item(i);
                if (element.id != null && currentItems.containsKey(element.id)) {
                    selected.add(currentItems.get(element.id).item);
                }
            }
        }
        return selected;
    }

    public void enableAction(T item, String actionId) {
        ListItem<T> listItem = listItem(item);
        if (listItem != null) {
            listItem.enableAction(actionId);
        }
    }

    public void disableAction(T item, String actionId) {
        ListItem<T> listItem = listItem(item);
        if (listItem != null) {
            listItem.disableAction(actionId);
        }
    }

    @Override
    public void setItems(Iterable<T> items, int visible, int total) {
        currentItems.clear();
        Elements.removeChildrenFrom(root);
        for (T item : items) {
            ListItem<T> listItem = new ListItem<>(this, item, multiselect, contentWidths, itemRenderer.render(item));
            currentItems.put(listItem.id, listItem);
            root.appendChild(listItem.asElement());
        }
    }

    private ListItem<T> listItem(T item) {
        String id = itemRenderer.render(item).getId();
        return currentItems.get(id);
    }
}
