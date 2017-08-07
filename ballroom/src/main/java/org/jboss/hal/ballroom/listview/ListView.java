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
import org.jboss.hal.ballroom.DataProvider;
import org.jboss.hal.ballroom.Display;
import org.jboss.hal.ballroom.PageInfo;
import org.jboss.hal.resources.CSS;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.listPf;
import static org.jboss.hal.resources.CSS.listPfStacked;

/**
 * PatternFly list view. The list view should not manage data by itself. Instead use a {@link DataProvider} and add the
 * list view as a display to the data provider:
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
public class ListView<T> implements Display<T>, IsElement<HTMLElement> {

    private final ItemRenderer<T> itemRenderer;
    private final boolean multiselect;
    private final HTMLElement root;
    private final Map<String, ListItem<T>> currentListItems;
    private SelectHandler<T> selectHandler;

    public ListView(String id, ItemRenderer<T> itemRenderer, boolean stacked, boolean multiselect) {
        this.itemRenderer = itemRenderer;
        this.multiselect = multiselect;
        this.currentListItems = new HashMap<>();

        HtmlContentBuilder<HTMLDivElement> div = div().id(id).css(listPf);
        if (stacked) {
            div.css(listPfStacked);
        }
        this.root = div.asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        currentListItems.clear();
        Elements.removeChildrenFrom(root);
        for (T item : items) {
            ListItem<T> listItem = new ListItem<>(this, item, multiselect, itemRenderer.render(item));
            currentListItems.put(listItem.id, listItem);
            root.appendChild(listItem.asElement());
        }
    }

    /** Selects the item and fires a selection event if the item is in the visible range. */
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
            for (ListItem<T> otherItem : currentListItems.values()) {
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
        if (element != null && element.id != null && currentListItems.containsKey(element.id)) {
            return currentListItems.get(element.id).item;
        }
        return null;
    }

    public List<T> selectedItems() {
        List<T> selected = new ArrayList<>();
        NodeList<Element> nodes = root.querySelectorAll("." + CSS.active);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof HTMLElement) {
                HTMLElement element = (HTMLElement) nodes.item(i);
                if (element.id != null && currentListItems.containsKey(element.id)) {
                    selected.add(currentListItems.get(element.id).item);
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

    protected List<ItemAction<T>> allowedActions(List<ItemAction<T>> actions) {
        return actions;
    }

    private ListItem<T> listItem(T item) {
        String id = itemRenderer.render(item).getId();
        return currentListItems.get(id);
    }
}
