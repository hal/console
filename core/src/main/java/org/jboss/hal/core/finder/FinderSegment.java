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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    private static final Logger logger = LoggerFactory.getLogger(FinderSegment.class);

    private final String key;
    private String value;
    private String breadcrumbKey;
    private String breadcrumbValue;

    private FinderColumn<T> column;

    FinderSegment(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    FinderSegment(final FinderColumn<T> column) {
        this.key = column.getId();
        this.breadcrumbKey = column.getTitle();
        this.column = column;

        FinderRow selectedRow = column.selectedRow();
        if (selectedRow != null) {
            this.value = selectedRow.getId();
            this.breadcrumbValue = selectedRow.getDisplay().getTitle();
        } else {
            this.value = null;
            this.breadcrumbValue = null;
        }
    }

    @Override
    public String toString() {
        // Do not change this implementation as the place management relies on it!
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

    public void setValues(String value, String breadcrumbValue) {
        this.value = value;
        this.breadcrumbValue = breadcrumbValue;
    }

    /**
     * @return {@code true} if this segment was initialized with a column which has an {@linkplain
     * BreadcrumbItemHandler breadcrumb item handler} and either {@linkplain FinderColumn#getInitialItems() initial
     * items}, an {@linkplain FinderColumn#getItemsProvider() items provider} or a {@linkplain
     * FinderColumn#getBreadcrumbItemsProvider() breadcrumb items provider}.
     */
    public boolean supportsDropdown() {
        return column != null && column.getBreadcrumbItemHandler() != null &&
                (
                        (column.getInitialItems() != null && !column.getInitialItems().isEmpty()) ||
                                column.getItemsProvider() != null ||
                                column.getBreadcrumbItemsProvider() != null
                );
    }

    public void dropdown(final FinderContext context, DropdownCallback<T> callback) {
        List<DropdownItem<T>> elements = new ArrayList<>();
        AsyncCallback<List<T>> asyncCallback = new AsyncCallback<List<T>>() {
            @Override
            public void onFailure(final Throwable caught) {
                logger.error("Cannot provide dropdown items for breadcrumb segment {}={}: {}", key, value, //NON-NLS
                        caught.getMessage());
            }

            @Override
            public void onSuccess(final List<T> result) {
                collectDropdownElements(elements, result);
                callback.onItems(elements);
            }
        };

        if (column.getBreadcrumbItemsProvider() != null) {
            column.getBreadcrumbItemsProvider().get(context, asyncCallback);

        } else if (column.getInitialItems() != null && !column.getInitialItems().isEmpty()) {
            collectDropdownElements(elements, column.getInitialItems());
            callback.onItems(elements);

        } else if (column.getItemsProvider() != null) {
            column.getItemsProvider().get(context, asyncCallback);
        }
    }

    private void collectDropdownElements(List<DropdownItem<T>> elements, List<T> items) {
        for (T item : items) {
            ItemDisplay<T> display = column.getItemRenderer().render(item);
            if (display.getId().equals(value)) {
                continue;
            }
            elements.add(new DropdownItem<>(item, display, column.getBreadcrumbItemHandler()));
        }
    }
}
