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

import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listViewPf;

/**
 * @author Harald Pehl
 */
public class ListView<T> implements IsElement, SecurityContextAware {

    private final String id;
    private final boolean multiselect;
    private final ItemRenderer<T> itemRenderer;
    private final Map<String, ListItem<T>> items;
    private final Element root;
    private SelectHandler<T> selectHandler;

    public ListView(final String id, final ItemRenderer<T> itemRenderer) {
        this(id, false, itemRenderer);
    }

    public ListView(final String id, final boolean multiselect, final ItemRenderer<T> itemRenderer) {
        this.id = id;
        this.multiselect = multiselect;
        this.itemRenderer = itemRenderer;
        this.items = new HashMap<>();
        this.root = new Elements.Builder().div().id(id).css(listGroup, listViewPf).end().build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void setItems(List<T> items) {
        this.items.clear();
        Elements.removeChildrenFrom(root);
        for (T item : items) {
            ListItem<T> listItem = new ListItem<>(this, item, multiselect, itemRenderer.render(item));
            this.items.put(listItem.id, listItem);
            root.appendChild(listItem.asElement());
        }
    }

    public void onSelect(SelectHandler<T> selectHandler) {
        this.selectHandler = selectHandler;
    }

    public boolean hasSelection() {
        return !selectedItems().isEmpty();
    }

    public T selectedItem() {
        Element element = root.querySelector("." + CSS.active);
        if (element != null && element.getId() != null && items.containsKey(element.getId())) {
            return items.get(element.getId()).item;
        }
        return null;
    }

    public List<T> selectedItems() {
        List<T> selected = new ArrayList<>();
        NodeList nodes = root.querySelectorAll("." + CSS.active);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element element = (Element) nodes.item(i);
                if (element.getId() != null && items.containsKey(element.getId())) {
                    selected.add(items.get(element.getId()).item);
                }
            }
        }
        return selected;
    }

    void select(ListItem<T> item, boolean select) {
        if (select) {
            item.root.getClassList().add(active);
        } else {
            item.root.getClassList().remove(active);
        }
        if (!multiselect) {
            // deselect all other items
            for (ListItem<T> otherItem : items.values()) {
                if (otherItem == item) {
                    continue;
                }
                otherItem.root.getClassList().remove(active);
            }
        }
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
