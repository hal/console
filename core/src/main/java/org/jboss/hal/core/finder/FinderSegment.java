/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;

/** A segment inside a {@link FinderPath}. */
public class FinderSegment<T> {

    /** Separator used in URL tokens. Must be securely encodable in URLs. */
    static final String SEPARATOR = "~";
    private static final Logger logger = LoggerFactory.getLogger(FinderSegment.class);

    private final String columnId;
    private final String itemId;
    private final String columnTitle;
    private final String itemTitle;

    private FinderColumn<T> column;

    FinderSegment(String columnId, String itemId) {
        this(columnId, itemId, columnId, itemId);
    }

    FinderSegment(String columnId, String itemId,
            String columnTitle, String itemTitle) {
        this.columnId = columnId;
        this.itemId = itemId;
        this.columnTitle = columnTitle;
        this.itemTitle = itemTitle;
    }

    FinderSegment(FinderColumn<T> column) {
        this.columnId = column.getId();
        this.columnTitle = column.getTitle();
        this.column = column;

        FinderRow selectedRow = column.selectedRow();
        if (selectedRow != null) {
            this.itemId = selectedRow.getId();
            this.itemTitle = selectedRow.getDisplay().getTitle();
        } else {
            this.itemId = null;
            this.itemTitle = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinderSegment)) {
            return false;
        }

        FinderSegment<?> that = (FinderSegment<?>) o;
        if (!columnId.equals(that.columnId)) {
            return false;
        }
        return itemId.equals(that.itemId);

    }

    @Override
    public int hashCode() {
        int result = columnId.hashCode();
        result = 31 * result + itemId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // Do not change this implementation as the place management relies on it!
        return columnId + SEPARATOR + itemId;
    }

    FinderSegment<T> copy() {
        return new FinderSegment<>(columnId, itemId, columnTitle, itemTitle);
    }

    public void connect(FinderColumn<T> column) {
        this.column = column;
    }

    public String getColumnId() {
        return columnId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getColumnTitle() {
        return columnTitle;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    /**
     * @return {@code true} if this segment was initialized with a column which has an {@linkplain BreadcrumbItemHandler
     *         breadcrumb item handler} and either {@linkplain FinderColumn#getInitialItems() initial items}, an
     *         {@linkplain FinderColumn#getItemsProvider() items provider} or a
     *         {@linkplain FinderColumn#getBreadcrumbItemsProvider() breadcrumb items provider}.
     */
    public boolean supportsDropdown() {
        // noinspection SimplifiableIfStatement
        if (column != null) {
            return ((column.getBreadcrumbItemHandler() != null || column.useFirstActionAsBreadcrumbHandler()) &&
                    ((column.getInitialItems() != null && !column.getInitialItems().isEmpty()) ||
                            column.getItemsProvider() != null ||
                            column.getBreadcrumbItemsProvider() != null));
        }
        return false;
    }

    public void dropdown(FinderContext context, DropdownCallback<T> callback) {
        List<DropdownItem<T>> elements = new ArrayList<>();
        AsyncCallback<List<T>> asyncCallback = new AsyncCallback<List<T>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.error("Cannot provide dropdown items for breadcrumb segment '{}': {}", this,
                        caught.getMessage());
            }

            @Override
            public void onSuccess(List<T> result) {
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
            if (display.getId().equals(itemId)) {
                continue;
            }

            BreadcrumbItemHandler<T> breadcrumbItemHandler = column.getBreadcrumbItemHandler();
            if (breadcrumbItemHandler == null && column.useFirstActionAsBreadcrumbHandler()) {
                List<ItemAction<T>> actions = display.actions();
                if (actions != null && !actions.isEmpty()) {
                    breadcrumbItemHandler = new ItemActionBreadcrumbHandler<>(actions.get(0));
                } else {
                    // noinspection DuplicateStringLiteralInspection
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
                // noinspection DuplicateStringLiteralInspection
                logger.error("Unable to get breadcrumb handler for segment '{}': " +
                        "No handler found for column '{}'", this, column.getId());
            }
        }
    }

    private static class ItemActionBreadcrumbHandler<T> implements BreadcrumbItemHandler<T> {

        private final ItemAction<T> itemAction;

        private ItemActionBreadcrumbHandler(ItemAction<T> itemAction) {
            this.itemAction = itemAction;
        }

        @Override
        public void execute(T item, FinderContext context) {
            itemAction.handler.execute(item);
        }
    }

    public static class DropdownItem<T> {

        public final T item;
        public final ItemDisplay<T> display;
        public final BreadcrumbItemHandler<T> handler;

        DropdownItem(T item, ItemDisplay<T> display, BreadcrumbItemHandler<T> handler) {
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
}
