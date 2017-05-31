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
import elemental2.dom.HTMLElement;
import elemental2.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.CSS;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listViewPf;

/**
 * @author Harald Pehl
 */
public class ListView<T> implements IsElement {

    private final String id;
    private final boolean multiselect;
    private final ItemRenderer<T> itemRenderer;
    private final Map<String, ListItem<T>> items;
    private final HTMLElement root;
    private SelectHandler<T> selectHandler;

    public ListView(final String id, final ItemRenderer<T> itemRenderer) {
        this(id, false, itemRenderer);
    }

    public ListView(final String id, final boolean multiselect, final ItemRenderer<T> itemRenderer) {
        this.id = id;
        this.multiselect = multiselect;
        this.itemRenderer = itemRenderer;
        this.items = new HashMap<>();
        this.root = div().id(id).css(listGroup, listViewPf).asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public void setItems(Iterable<T> items) {
        this.items.clear();
        Elements.removeChildrenFrom(root);
        for (T item : items) {
            ListItem<T> listItem = new ListItem<>(this, item, multiselect, itemRenderer.render(item));
            this.items.put(listItem.id, listItem);
            root.appendChild(listItem.asElement());
        }
    }

    /**
     * Select the item and fires a selection event
     */
    public void selectItem(T item) {
        ListItem<T> listItem = getItem(item);
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
            for (ListItem<T> otherItem : items.values()) {
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
        if (element != null && element.id != null && items.containsKey(element.id)) {
            return items.get(element.id).item;
        }
        return null;
    }

    public List<T> selectedItems() {
        List<T> selected = new ArrayList<>();
        NodeList<Element> nodes = root.querySelectorAll("." + CSS.active);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof HTMLElement) {
                HTMLElement element = (HTMLElement) nodes.item(i);
                if (element.id != null && items.containsKey(element.id)) {
                    selected.add(items.get(element.id).item);
                }
            }
        }
        return selected;
    }

    public void enableAction(T item, String actionId) {
        ListItem<T> listItem = getItem(item);
        if (listItem != null) {
            listItem.enableAction(actionId);
        }
    }

    public void disableAction(T item, String actionId) {
        ListItem<T> listItem = getItem(item);
        if (listItem != null) {
            listItem.disableAction(actionId);
        }
    }

    private ListItem<T> getItem(T item) {
        String itemId = itemRenderer.render(item).getId();
        return items.get(itemId);
    }
}
