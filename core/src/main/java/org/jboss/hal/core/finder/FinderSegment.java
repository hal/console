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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Harald Pehl
 */
public class FinderSegment<T> {

    public static class DropdownItem<T> {

        public final T item;
        public final ItemDisplay<T> display;
        public final BreadcrumbItemHandler<T> handler;

        DropdownItem(final T item, final ItemDisplay<T> display, final BreadcrumbItemHandler<T> handler) {
            this.item = item;
            this.display = display;
            this.handler = handler;
        }
    }


    public interface DropdownCallback<T> {

        void onItems(List<DropdownItem<T>> items);
    }


    private final String key;
    private final String value;
    private String breadcrumbKey;
    private String breadcrumbValue;

    private List<T> initialItems;
    private ItemsProvider<T> itemsProvider;
    private ItemRenderer<T> itemRenderer;
    private BreadcrumbItemHandler<T> breadcrumbItemHandler;

    FinderSegment(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    FinderSegment(final FinderColumn<T> column) {
        this.key = column.getId();
        this.breadcrumbKey = column.getTitle();
        this.breadcrumbItemHandler = column.getBreadcrumbItemHandler();

        FinderRow selectedRow = column.selectedRow();
        if (selectedRow != null) {
            this.value = selectedRow.getId();
            this.breadcrumbValue = selectedRow.getDisplay().getTitle();
        } else {
            this.value = null;
            this.breadcrumbValue = null;
        }

        if (column.getBreadcrumbItemHandler() != null) {
            this.initialItems = column.getInitialItems();
            this.itemsProvider = column.getItemsProvider();
            this.itemRenderer = column.getItemRenderer();
        }
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getBreadcrumbKey() {
        return breadcrumbKey;
    }

    public String getBreadcrumbValue() {
        return breadcrumbValue;
    }

    public boolean canProvideValues() {
        return (initialItems != null || itemsProvider != null) && itemRenderer != null;
    }

    public void dropdown(final FinderContext finderContext, DropdownCallback<T> callback) {
        List<DropdownItem<T>> elements = new ArrayList<>();
        if ((initialItems != null && !initialItems.isEmpty())) {
            collectDropdownElements(elements, initialItems);
            callback.onItems(elements);

        } else {
            itemsProvider.get(finderContext, new AsyncCallback<List<T>>() {
                @Override
                public void onFailure(final Throwable caught) {
                    // ignore
                }

                @Override
                public void onSuccess(final List<T> result) {
                    collectDropdownElements(elements, result);
                    callback.onItems(elements);
                }
            });
        }
    }

    private void collectDropdownElements(List<DropdownItem<T>> elements, List<T> items) {
        for (T item : items) {
            ItemDisplay<T> display = itemRenderer.render(item);
            if (display.getId().equals(value)) {
                continue;
            }
            elements.add(new DropdownItem<>(item, display, breadcrumbItemHandler));
        }
    }
}
