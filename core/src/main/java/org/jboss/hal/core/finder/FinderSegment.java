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

        public void onSelect(FinderContext context) {
            handler.execute(item, context);
        }

        public String getTitle() {
            return display.getTitle();
        }
    }


    @FunctionalInterface
    public interface DropdownCallback<T> {

        void onItems(List<DropdownItem<T>> items);
    }


    private static class ItemActionBreadcrumbHandler<T> implements BreadcrumbItemHandler<T> {

        private final ItemAction<T> itemAction;

        private ItemActionBreadcrumbHandler(final ItemAction<T> itemAction) {this.itemAction = itemAction;}

        @Override
        public void execute(final T item, final FinderContext context) {
            itemAction.handler.execute(item);
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(FinderSegment.class);

    private final String key;
    private final String value;
    private final String breadcrumbKey;
    private final String breadcrumbValue;

    private FinderColumn<T> column;

    FinderSegment(final String key, final String value) {
        this(key, value, key, value);
    }

    FinderSegment(final String key, final String value, final String breadcrumbKey,
            final String breadcrumbValue) {
        this.key = key;
        this.value = value;
        this.breadcrumbKey = breadcrumbKey;
        this.breadcrumbValue = breadcrumbValue;
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

    public void connect(FinderColumn<T> column) {
        this.column = column;
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

    /**
     * @return {@code true} if this segment was initialized with a column which has an {@linkplain
     * BreadcrumbItemHandler breadcrumb item handler} and either {@linkplain FinderColumn#getInitialItems() initial
     * items}, an {@linkplain FinderColumn#getItemsProvider() items provider} or a {@linkplain
     * FinderColumn#getBreadcrumbItemsProvider() breadcrumb items provider}.
     */
    public boolean supportsDropdown() {
        //noinspection SimplifiableIfStatement
        if (column != null) {
            return ((column.getBreadcrumbItemHandler() != null || column.useFirstActionAsBreadcrumbHandler()) &&
                    ((column.getInitialItems() != null && !column.getInitialItems().isEmpty()) ||
                            column.getItemsProvider() != null ||
                            column.getBreadcrumbItemsProvider() != null
                    ));
        }
        return false;
    }

    public void dropdown(final FinderContext context, DropdownCallback<T> callback) {
        List<DropdownItem<T>> elements = new ArrayList<>();
        AsyncCallback<List<T>> asyncCallback = new AsyncCallback<List<T>>() {
            @Override
            public void onFailure(final Throwable caught) {
                logger.error("Cannot provide dropdown items for breadcrumb segment '{}': {}", this, //NON-NLS
                        caught.getMessage());
            }

            @Override
            public void onSuccess(final List<T> result) {
                collectDropdownElements(elements, result);
                callback.onItems(elements);
            }
        };

        // check the different ways to provide breadcrumb items in this order
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

            BreadcrumbItemHandler<T> breadcrumbItemHandler = column.getBreadcrumbItemHandler();
            if (breadcrumbItemHandler == null && column.useFirstActionAsBreadcrumbHandler()) {
                List<ItemAction<T>> actions = display.actions();
                if (actions != null && !actions.isEmpty()) {
                    breadcrumbItemHandler = new ItemActionBreadcrumbHandler<>(actions.get(0));
                } else {
                    //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
                    logger.error("Unable to get breadcrumb handler for segment '{}': " +
                            "Column '{}' was specified to use first item action as breadcrumb handler, " +
                            "but no actions were found.", this, column.getId());
                }
            }

            if (breadcrumbItemHandler != null) {
                elements.add(new DropdownItem<>(item, display, breadcrumbItemHandler));
            } else {
                // This method only gets called when supportsDropdown() returned true.
                // So there has to be a handler
                //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
                logger.error("Unable to get breadcrumb handler for segment '{}': " +
                        "No handler found for column '{}'", this, column.getId());
            }
        }
    }
}
