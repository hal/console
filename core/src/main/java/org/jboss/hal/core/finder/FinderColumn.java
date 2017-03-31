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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.events.Event;
import elemental.events.KeyboardEvent;
import elemental.events.KeyboardEvent.KeyCode;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.dragndrop.DropEventHandler;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.core.finder.Finder.DATA_FILTER;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.UIConstants.GROUP;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TABINDEX;

/**
 * Describes a column in a finder. A column has an unique id, a title, a number of optional column actions
 * and an {@link ItemRenderer} which defines how the items of this column are rendered. All items of a column must have
 * the same type parameter which is the type parameter of this column.
 * <p>
 * The idea is that columns are self-contained and don't need direct references to other columns. References are only
 * provided by id. The {@link ColumnRegistry} will then resolve the id against an existing column.
 * <p>
 * Please do not use constants from {@code ModelDescriptionConstants} for the column ids (it makes refactoring harder).
 * Instead add an id to {@link org.jboss.hal.resources.Ids}.
 * <p>
 * TODO This class is huge! Try to refactor and break into smaller pieces.
 *
 * @param <T> The column and items type.
 *
 * @author Harald Pehl
 */
public class FinderColumn<T> implements IsElement, SecurityContextAware {

    public static class Builder<T> {

        private final Finder finder;
        private final String id;
        private final String title;
        private final List<ColumnAction<T>> columnActions;
        private ItemRenderer<T> itemRenderer;
        private boolean showCount;
        private boolean withFilter;
        private boolean pinnable;
        private PreviewCallback<T> previewCallback;
        private BreadcrumbItemHandler<T> breadcrumbItemHandler;
        private boolean firstActionAsBreadcrumbHandler;
        private List<T> items;
        private ItemsProvider<T> itemsProvider;
        private BreadcrumbItemsProvider<T> breadcrumbItemsProvider;
        private ItemSelectionHandler<T> selectionHandler;

        public Builder(final Finder finder, final String id, final String title) {
            this.finder = finder;
            this.id = id;
            this.title = title;
            this.itemRenderer = item -> () -> String.valueOf(item);
            this.columnActions = new ArrayList<>();
            this.showCount = false;
            this.withFilter = false;
            this.pinnable = false;
            this.items = new ArrayList<>();
        }

        /**
         * Adds a single column action button in the header of the column
         */
        public Builder<T> columnAction(ColumnAction<T> action) {
            columnActions.add(action);
            return this;
        }

        public Builder<T> showCount() {
            this.showCount = true;
            return this;
        }

        public Builder<T> withFilter() {
            this.withFilter = true;
            return this;
        }

        public Builder<T> pinnable() {
            this.pinnable = true;
            return this;
        }

        public Builder<T> initialItems(List<T> items) {
            if (items != null && !items.isEmpty()) {
                this.items.addAll(items);
            }
            return this;
        }

        public Builder<T> itemsProvider(ItemsProvider<T> itemsProvider) {
            this.itemsProvider = itemsProvider;
            return this;
        }

        public Builder<T> breadcrumbItemsProvider(BreadcrumbItemsProvider<T> breadcrumbItemsProvider) {
            this.breadcrumbItemsProvider = breadcrumbItemsProvider;
            return this;
        }

        public Builder<T> itemRenderer(ItemRenderer<T> itemRenderer) {
            this.itemRenderer = itemRenderer;
            return this;
        }

        public Builder<T> onItemSelect(ItemSelectionHandler<T> selectionHandler) {
            this.selectionHandler = selectionHandler;
            return this;
        }

        public Builder<T> onPreview(PreviewCallback<T> previewCallback) {
            this.previewCallback = previewCallback;
            return this;
        }

        /**
         * Sets the handler which is executed when an item in the breadcrumb dropdown is selected. Has precedence over
         * {@link #useFirstActionAsBreadcrumbHandler()}.
         */
        public Builder<T> onBreadcrumbItem(BreadcrumbItemHandler<T> handler) {
            this.breadcrumbItemHandler = handler;
            return this;
        }

        /**
         * Uses the item's first action as breadcrumb item handler. If a custom handler is set using {@link
         * #onBreadcrumbItem(BreadcrumbItemHandler)} this handler will be used instead of the first item action.
         */
        public Builder<T> useFirstActionAsBreadcrumbHandler() {
            this.firstActionAsBreadcrumbHandler = true;
            return this;
        }

        public FinderColumn<T> build() {
            return new FinderColumn<>(this);
        }
    }


    public enum RefreshMode {CLEAR_SELECTION, RESTORE_SELECTION}


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String HIDDEN_COLUMNS_ELEMENT = "hiddenColumnsElement";
    private static final String HEADER_ELEMENT = "headerElement";
    private static final String COLUMN_ACTIONS_ELEMENT = "columnActionsElement";
    private static final String FILTER_ELEMENT = "filterElement";
    private static final String UL_ELEMENT = "ulElement";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(FinderColumn.class);

    private final Finder finder;
    private final String id;
    private final String title;
    private final boolean showCount;
    private final boolean pinnable;
    private final Element columnActions;
    private final List<T> initialItems;
    private final ItemSelectionHandler<T> selectionHandler;
    private ItemsProvider<T> itemsProvider;
    private ItemRenderer<T> itemRenderer;
    private PreviewCallback<T> previewCallback;
    private BreadcrumbItemsProvider<T> breadcrumbItemsProvider;
    private BreadcrumbItemHandler<T> breadcrumbItemHandler;
    private boolean firstActionAsBreadcrumbHandler;

    private final Map<String, FinderRow<T>> rows;
    private final FinderColumnStorage storage;

    private final Element root;
    private final Element hiddenColumns;
    private final Element headerElement;
    private final InputElement filterElement;
    private final Element ulElement;
    private final Element noItems;

    private boolean asElement;


    // ------------------------------------------------------ ui

    protected FinderColumn(final Builder<T> builder) {
        this.finder = builder.finder;
        this.id = builder.id;
        this.title = builder.title;
        this.showCount = builder.showCount;
        this.pinnable = builder.pinnable;
        this.initialItems = builder.items;
        this.itemsProvider = builder.itemsProvider;
        this.itemRenderer = builder.itemRenderer;
        this.selectionHandler = builder.selectionHandler;
        this.previewCallback = builder.previewCallback;
        this.breadcrumbItemsProvider = builder.breadcrumbItemsProvider;
        this.breadcrumbItemHandler = builder.breadcrumbItemHandler;
        this.firstActionAsBreadcrumbHandler = builder.firstActionAsBreadcrumbHandler;
        this.asElement = false;

        this.rows = new HashMap<>();
        this.storage = new FinderColumnStorage(id);

        // header
        // @formatter:off
        Elements.Builder eb = new Elements.Builder()
            .div().id(id)
                .data(DATA_BREADCRUMB, title)
                .css(finderColumn, column(2))
                .attr(TABINDEX, "-1")
                .on(keydown, this::onNavigation)
                    .header()
                        .span().css(CSS.hiddenColumns, fontAwesome("angle-double-left"))
                            .title(CONSTANTS.hiddenColumns())
                            .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                            .data(UIConstants.PLACEMENT, "bottom")
                            .rememberAs(HIDDEN_COLUMNS_ELEMENT)
                            .on(click, event -> finder.revealHiddenColumns(FinderColumn.this))
                        .end()
                        .h(1).textContent(builder.title).title(builder.title).rememberAs(HEADER_ELEMENT).end();
        // @formatter:on

        // column actions
        eb.div().rememberAs(COLUMN_ACTIONS_ELEMENT);
        if (builder.columnActions.size() == 1) {
            if (isAllowed(builder.columnActions.get(0))) {
                eb.add(newColumnButton(builder.columnActions.get(0)));
            }
        } else {
            //noinspection DuplicateStringLiteralInspection
            if (isAllowed(builder.columnActions)) {
                eb.css(btnGroup).attr(ROLE, GROUP);
                for (ColumnAction<T> action : builder.columnActions) {
                    if (isAllowed(action)) {
                        eb.add(newColumnButton(action));
                    }
                }
            }
        }
        eb.end().end(); // </columnActions> && </header>

        // filter box
        if (builder.withFilter) {
            String iconId = Ids.build(id, filter, "icon");
            // @formatter:off
            eb.div().css(inputGroup, filter)
                .input(text)
                    .id(Ids.build(id, filter))
                    .css(formControl)
                    .aria("describedby", iconId)
                    .attr(UIConstants.PLACEHOLDER, CONSTANTS.filter())
                    .on(keydown, this::onNavigation)
                    .on(keyup, this::onFilter)
                    .rememberAs(FILTER_ELEMENT)
                .span().id(iconId).css(inputGroupAddon, fontAwesome("search")).end()
            .end();
            // @formatter:on
        }

        // rows
        eb.ul();
        if (pinnable) {
            eb.css(CSS.pinnable);
        }
        eb.rememberAs(UL_ELEMENT).end().end(); // </ul> && </div>

        // no items marker
        noItems = new Elements.Builder().li().css(empty)
                .span().css(itemText).textContent(CONSTANTS.noItems()).end()
                .end().build();

        root = eb.build();
        hiddenColumns = eb.referenceFor(HIDDEN_COLUMNS_ELEMENT);
        headerElement = eb.referenceFor(HEADER_ELEMENT);
        columnActions = eb.referenceFor(COLUMN_ACTIONS_ELEMENT);
        filterElement = builder.withFilter ? eb.referenceFor(FILTER_ELEMENT) : null;
        ulElement = eb.referenceFor(UL_ELEMENT);
    }

    @SuppressWarnings("Duplicates")
    private Element newColumnButton(final ColumnAction<T> action) {
        Elements.Builder builder = new Elements.Builder();
        if (!action.actions.isEmpty()) {
            // @formatter:off
            builder.div().css(dropdown)
                .button().css(btn, btnFinder, dropdownToggle).id(action.id)
                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                        .aria(UIConstants.EXPANDED, "false"); //NON-NLS
                    if (action.title != null) {
                        builder.textContent(action.title);
                    } else if (action.element != null) {
                        builder.add(action.element);
                    } else {
                        builder.textContent(NOT_AVAILABLE);
                    }
                    builder.span().css(caret).end()
                .end()
                .ul().id(Ids.uniqueId()).css(dropdownMenu)
                        .attr(UIConstants.ROLE, UIConstants.MENU)
                        .aria(UIConstants.LABELLED_BY, action.id);
                    for (ColumnAction<T> liAction : action.actions) {
                        builder.li().attr(UIConstants.ROLE, UIConstants.PRESENTATION);
                            builder.a()
                                .id(liAction.id)
                                .attr(UIConstants.ROLE, UIConstants.MENUITEM)
                                .attr(UIConstants.TABINDEX, "-1")
                                .on(click, event -> {
                                    if (liAction.handler!= null) {
                                        liAction.handler.execute(this);
                                    }
                                });
                                if (liAction.title != null){
                                    builder.textContent(liAction.title);
                                } else if (liAction.element != null) {
                                    builder.add(liAction.element);
                                } else {
                                    builder.textContent(NOT_AVAILABLE);
                                }
                            builder.end()
                        .end();
                    }
                builder.end()
            .end();
            // @formatter:on

        } else {
            builder.button();
            builder.id(action.id)
                    .css(btn, btnFinder)
                    .on(click, event -> {
                        if (action.handler != null) {
                            action.handler.execute(this);
                        }
                    });
            if (action.title != null) {
                builder.textContent(action.title);
            } else if (action.element != null) {
                builder.add(action.element);
            } else {
                builder.textContent(NOT_AVAILABLE);
            }
            builder.end();
        }

        return builder.build();
    }

    private void updateHeader(int matched) {
        if (showCount) {
            String titleWithSize;
            if (matched == rows.size()) {
                titleWithSize = title + " (" + rows.size() + ")";
            } else {
                titleWithSize = title + " (" + matched + " / " + rows.size() + ")";
            }
            headerElement.setInnerText(titleWithSize);
            headerElement.setTitle(titleWithSize);
        }
    }


    // ------------------------------------------------------ event handler

    private void onFilter(final Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        if (keyboardEvent.getKeyCode() == KeyCode.ESC) {
            filterElement.setValue("");
        }

        int matched = 0;
        String filter = filterElement.getValue();
        for (Element li : Elements.children(ulElement)) {
            if (li == noItems) {
                continue;
            }
            Object filterData = li.getDataset().at(DATA_FILTER);
            boolean match = filter == null
                    || filter.trim().length() == 0
                    || filterData == null
                    || String.valueOf(filterData).toLowerCase().contains(filter.toLowerCase());
            Elements.setVisible(li, match);
            if (match) {
                matched++;
            }
        }
        updateHeader(matched);
        if (matched == 0) {
            Elements.lazyAppend(ulElement, noItems);
        } else {
            Elements.failSafeRemove(ulElement, noItems);
        }
    }

    private void onNavigation(Event event) {
        if (hasVisibleElements()) {
            KeyboardEvent keyboardEvent = (KeyboardEvent) event;
            int keyCode = keyboardEvent.getKeyCode();
            switch (keyCode) {

                case KeyCode.UP:
                case KeyCode.DOWN: {
                    Element activeElement = activeElement();
                    if (!Elements.isVisible(activeElement)) {
                        activeElement = null;
                    }
                    Element select = keyCode == KeyCode.UP
                            ? previousVisibleElement(activeElement)
                            : nextVisibleElement(activeElement);
                    if (select != null && select != noItems) {
                        event.preventDefault();
                        event.stopPropagation();

                        select.scrollIntoView(false);
                        row(select).click();
                    }
                    break;
                }

                case KeyCode.LEFT: {
                    Element previousElement = root.getPreviousElementSibling();
                    if (previousElement != null) {
                        FinderColumn previousColumn = finder.getColumn(previousElement.getId());
                        if (previousColumn != null) {
                            event.preventDefault();
                            event.stopPropagation();

                            Elements.setVisible(previousElement, true);
                            finder.reduceTo(previousColumn);
                            finder.selectColumn(previousColumn.getId());
                            FinderRow selectedRow = previousColumn.selectedRow();
                            if (selectedRow != null) {
                                selectedRow.updatePreview();
                                selectedRow.asElement().scrollIntoView(false);
                            }
                            finder.updateContext();
                            finder.updateHistory();
                        }
                    }
                    break;
                }

                case KeyCode.RIGHT: {
                    Element activeElement = activeElement();
                    String nextColumn = row(activeElement).getNextColumn();
                    if (Elements.isVisible(activeElement) && nextColumn != null) {
                        event.preventDefault();
                        event.stopPropagation();

                        finder.reduceTo(this);
                        finder.appendColumn(nextColumn,
                                new AsyncCallback<FinderColumn>() {
                                    @Override
                                    public void onFailure(final Throwable throwable) {
                                        logger.error("Unable to append next column '{}' on keyboard right: {}",
                                                nextColumn, throwable.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(final FinderColumn column) {
                                        if (column.activeElement() == null && column.hasVisibleElements()) {
                                            Element firstElement = column.nextVisibleElement(null);
                                            column.markSelected(firstElement.getId());
                                            column.row(firstElement).updatePreview();
                                        }
                                        finder.updateContext();
                                        finder.updateHistory();
                                        finder.selectColumn(nextColumn);
                                    }
                                });
                    }
                    break;
                }

                case KeyCode.ENTER: {
                    Element activeItem = activeElement();
                    T item = row(activeItem).getItem();
                    ItemActionHandler<T> primaryAction = row(activeItem).getPrimaryAction();
                    if (Elements.isVisible(activeItem) && item != null && primaryAction != null) {
                        event.preventDefault();
                        event.stopPropagation();

                        row(activeItem).click();
                        primaryAction.execute(item);
                    }
                    break;
                }
            }
        }
    }

    protected boolean isVisible() {
        return asElement && Elements.isVisible(root) && root.getParentElement() != null;
    }


    // ------------------------------------------------------ internal API

    void markHiddenColumns(boolean show) {
        Elements.setVisible(hiddenColumns, show);
    }

    private Element activeElement() {return ulElement.querySelector("li." + active);} //NON-NLS

    private boolean hasVisibleElements() {
        for (Element element : Elements.children(ulElement)) {
            if (Elements.isVisible(element) && element != noItems) {
                return true;
            }
        }
        return false;
    }

    private Element previousVisibleElement(Element start) {
        Element element = start == null ? ulElement.getLastElementChild() : start.getPreviousElementSibling();
        while (element != null && !Elements.isVisible(element)) {
            element = element.getPreviousElementSibling();
        }
        return element;
    }

    private Element nextVisibleElement(Element start) {
        Element element = start == null ? ulElement.getFirstElementChild() : start.getNextElementSibling();
        while (element != null && !Elements.isVisible(element)) {
            element = element.getNextElementSibling();
        }
        return element;
    }

    FinderRow<T> row(String itemId) {
        return rows.get(itemId);
    }

    private FinderRow<T> row(Element element) {
        return row(element.getId());
    }

    FinderRow<T> selectedRow() {
        Element activeItem = ulElement.querySelector("li." + active); //NON-NLS
        if (activeItem != null && rows.containsKey(activeItem.getId())) {
            return rows.get(activeItem.getId());
        }
        return null;
    }

    boolean contains(String itemId) {
        return rows.containsKey(itemId);
    }

    void markSelected(String itemId) {
        for (Map.Entry<String, FinderRow<T>> entry : rows.entrySet()) {
            boolean select = itemId.equals(entry.getKey());
            entry.getValue().markSelected(select);
            if (select && selectionHandler != null) {
                selectionHandler.onSelect(entry.getValue().getItem());
            }
        }
    }

    void resetSelection() {
        Element element = activeElement();
        if (element != null) {
            element.getClassList().remove(active);
        }
    }

    boolean isPinnable() {
        return pinnable;
    }

    void unpin(final FinderRow<T> row) {
        row.asElement().getClassList().remove(pinned);
        row.asElement().getClassList().add(unpinned);

        // move row to unpinned section
        ulElement.removeChild(row.asElement());
        NodeList nodes = ulElement.querySelectorAll("." + unpinned);
        if (nodes.getLength() == 0) {
            // no unpinned rows append to bottom
            ulElement.appendChild(row.asElement());
        } else {
            Element before = findPosition(nodes, row);
            if (before != null) {
                ulElement.insertBefore(row.asElement(), before);
            } else {
                ulElement.appendChild(row.asElement());
            }
        }
        adjustPinSeparator();
        storage.unpinItem(row.getId());
    }

    void pin(final FinderRow<T> row) {
        row.asElement().getClassList().remove(unpinned);
        row.asElement().getClassList().add(pinned);

        // move row to pinned section
        ulElement.removeChild(row.asElement());
        NodeList nodes = ulElement.querySelectorAll("." + pinned);
        if (nodes.getLength() == 0) {
            // no pinned rows append to top
            ulElement.insertBefore(row.asElement(), ulElement.getFirstChild());
        } else {
            Element before = findPosition(nodes, row);
            if (before != null) {
                ulElement.insertBefore(row.asElement(), before);
            } else {
                Element firstUnpinned = ulElement.querySelector("." + unpinned);
                if (firstUnpinned != null) {
                    ulElement.insertBefore(row.asElement(), firstUnpinned);
                } else {
                    ulElement.appendChild(row.asElement());
                }
            }
        }
        adjustPinSeparator();
        row.asElement().scrollIntoView(false);
        storage.pinItem(row.getId());
    }

    private Element findPosition(NodeList nodes, FinderRow<T> row) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element currentElement = (Element) nodes.item(i);
            FinderRow<T> currentRow = row(currentElement);
            if (currentRow.getDisplay().getTitle().compareTo(row.getDisplay().getTitle()) > 0) {
                return currentElement;
            }
        }
        return null;
    }

    private void adjustPinSeparator() {
        NodeList nodes = ulElement.querySelectorAll("." + pinned);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (i == nodes.getLength() - 1) {
                element.getClassList().add(last);
            } else {
                element.getClassList().remove(last);
            }
        }
    }

    void setItems(AsyncCallback<FinderColumn> callback) {
        if (!initialItems.isEmpty()) {
            setItems(initialItems, callback);

        } else if (itemsProvider != null) {
            itemsProvider.get(finder.getContext(), new AsyncCallback<List<T>>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    callback.onFailure(throwable);
                }

                @Override
                public void onSuccess(final List<T> items) {
                    setItems(items, callback);
                }
            });

        } else {
            setItems(Collections.emptyList(), callback);
        }
    }

    private void setItems(List<T> items, AsyncCallback<FinderColumn> callback) {
        rows.clear();
        Elements.removeChildrenFrom(ulElement);
        if (filterElement != null) {
            filterElement.setValue("");
        }

        List<T> pinnedItems = new ArrayList<>();
        List<T> unpinnedItems = new ArrayList<>();
        Set<String> pinnedItemIds = storage.pinnedItems();
        if (pinnable && !pinnedItemIds.isEmpty()) {
            for (T item : items) {
                String id = itemRenderer.render(item).getId();
                if (pinnedItemIds.contains(id)) {
                    pinnedItems.add(item);
                } else {
                    unpinnedItems.add(item);
                }
            }
        } else {
            unpinnedItems.addAll(items);
        }
        for (Iterator<T> iterator = pinnedItems.iterator(); iterator.hasNext(); ) {
            T item = iterator.next();
            FinderRow<T> row = new FinderRow<>(finder, this, item, true,
                    itemRenderer.render(item), previewCallback);
            rows.put(row.getId(), row);
            ulElement.appendChild(row.asElement());
            if (!iterator.hasNext()) {
                row.asElement().getClassList().add(last);
            }
        }
        for (T item : unpinnedItems) {
            FinderRow<T> row = new FinderRow<>(finder, this, item, false,
                    itemRenderer.render(item), previewCallback);
            rows.put(row.getId(), row);
            ulElement.appendChild(row.asElement());
        }
        updateHeader(items.size());
        Tooltip.select("#" + id + " [data-" + UIConstants.TOGGLE + "=" + UIConstants.TOOLTIP + "]").init(); //NON-NLS

        if (items.isEmpty()) {
            ulElement.appendChild(noItems);
        }

        if (callback != null) {
            callback.onSuccess(this);
        }
    }

    /**
     * Sometimes you need to reference {@code this} in the column action handler. This is not possible if they're
     * part of the builder which is passed to {@code super()}. In this case you can use this method to add your column
     * actions <strong>after</strong> the call to {@code super()}.
     */
    protected void addColumnAction(ColumnAction<T> columnAction) {
        if (isAllowed(columnAction)) {
            columnActions.appendChild(newColumnButton(columnAction));
            if (columnActions.getChildElementCount() > 1) {
                columnActions.getClassList().add(btnGroup);
                columnActions.setAttribute(ROLE, GROUP);
            }
        }
    }

    protected void addColumnActions(String id, String iconsCss, String title, List<ColumnAction<T>> actions) {
        if (isAllowed(actions)) {
            Element element = new Elements.Builder().span()
                    .css(iconsCss)
                    .title(title)
                    .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                    .data(UIConstants.PLACEMENT, "bottom")
                    .end().build();
            ColumnAction<T> dropdownAction = new ColumnAction.Builder<T>(id)
                    .element(element)
                    .actions(actions)
                    .build();
            columnActions.appendChild(newColumnButton(dropdownAction));
            if (columnActions.getChildElementCount() > 1) {
                columnActions.getClassList().add(btnGroup);
                columnActions.setAttribute(ROLE, GROUP);
            }
        }
    }

    protected void resetColumnActions() {
        Elements.removeChildrenFrom(columnActions);
    }

    /**
     * Sometimes you need to reference {@code this} in the actions created by {@link ItemDisplay#actions()}. This is
     * not possible if they're part of the builder which is passed to {@code super()}. In this case the item renderer
     * can be specified <strong>after</strong> the call to {@code super()} using this setter.
     * <p>
     * However make sure to call the setter <strong>before</strong> the column is used {@link #asElement()} and gets
     * attached to the DOM!
     */
    protected void setItemRenderer(final ItemRenderer<T> itemRenderer) {
        assertNotAsElement("setItemRenderer()");
        this.itemRenderer = itemRenderer;
    }

    ItemRenderer<T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sometimes you need to reference {@code this} in the items provider. This is not possible if the items provider
     * is part of the builder which is passed to {@code super()}. In this case the items provider can be specified
     * <strong>after</strong> the call to {@code super()} using this setter.
     * <p>
     * However make sure to call the setter <strong>before</strong> the column is used {@link #asElement()} and gets
     * attached to the DOM!
     */
    protected void setItemsProvider(final ItemsProvider<T> itemsProvider) {
        assertNotAsElement("setItemsProvider()");
        this.itemsProvider = itemsProvider;
    }

    ItemsProvider<T> getItemsProvider() {
        return itemsProvider;
    }

    List<T> getInitialItems() {
        return initialItems;
    }

    /**
     * Sometimes you need to reference {@code this} in the preview callback. This is not possible if the preview
     * callback is part of the builder which is passed to {@code super()}. In this case the preview callback can be
     * specified <strong>after</strong> the call to {@code super()} using this setter.
     * <p>
     * However make sure to call the setter <strong>before</strong> the column is used {@link #asElement()} and gets
     * attached to the DOM!
     */
    protected void setPreviewCallback(final PreviewCallback<T> previewCallback) {
        this.previewCallback = previewCallback;
    }

    /**
     * Sometimes you need to reference {@code this} in the breadcrumb items provider. This is not possible if the
     * breadcrumb items provider is part of the builder which is passed to {@code super()}. In this case the breadcrumb
     * items provider can be specified <strong>after</strong> the call to {@code super()} using this setter.
     * <p>
     * However make sure to call the setter <strong>before</strong> the column is used {@link #asElement()} and gets
     * attached to the DOM!
     */
    protected void setBreadcrumbItemsProvider(final BreadcrumbItemsProvider<T> breadcrumbItemsProvider) {
        assertNotAsElement("setBreadcrumbItemsProvider()");
        this.breadcrumbItemsProvider = breadcrumbItemsProvider;
    }

    BreadcrumbItemsProvider<T> getBreadcrumbItemsProvider() {
        return breadcrumbItemsProvider;
    }

    BreadcrumbItemHandler<T> getBreadcrumbItemHandler() {
        return breadcrumbItemHandler;
    }

    boolean useFirstActionAsBreadcrumbHandler() {
        return firstActionAsBreadcrumbHandler;
    }

    protected void setOnDrop(DropEventHandler handler) {
        JsHelper.addDropHandler(ulElement, handler);
    }

    private void assertNotAsElement(String method) {
        if (asElement) {
            throw new IllegalStateException("Illegal call to FinderColumn." + method +
                    " after FinderColumn.asElement(). Make sure to setup the column before it's used as an element.");
        }
    }


    // ------------------------------------------------------ public API

    @Override
    public Element asElement() {
        asElement = true;
        return root;
    }

    public void refresh(RefreshMode refreshMode) {
        switch (refreshMode) {
            case CLEAR_SELECTION:
                if (finder.columns() == 1) {
                    refresh(finder::showInitialPreview);
                } else {
                    refresh(() -> finder.selectPreviousColumn(id));
                }
                break;
            case RESTORE_SELECTION:
                FinderRow<T> oldRow = selectedRow();
                refresh(() -> {
                    if (oldRow != null) {
                        FinderRow<T> updatedRow = rows.get(oldRow.getId());
                        if (updatedRow != null) {
                            updatedRow.click();
                            updatedRow.asElement().scrollIntoView(false);
                        } else {
                            finder.selectPreviousColumn(id);
                        }
                    } else {
                        finder.selectPreviousColumn(id);
                    }
                });
                break;
        }
    }

    /**
     * Refreshes and selects and the specified item.
     */
    public void refresh(String selectItemId) {
        refresh(() -> {
            FinderRow<T> row = rows.get(selectItemId);
            if (row != null) {
                row.click();
            } else {
                finder.selectPreviousColumn(id);
            }
        });
    }

    public void refresh(Callback callback) {
        setItems(new AsyncCallback<FinderColumn>() {
            @Override
            public void onFailure(final Throwable throwable) {
                logger.error("Unable to refresh column {}: {}", id, throwable.getMessage());
            }

            @Override
            public void onSuccess(final FinderColumn column) {
                finder.updateContext();
                if (callback != null) {
                    callback.execute();
                }
            }
        });
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    protected Finder getFinder() {
        return finder;
    }


    // ------------------------------------------------------ rbac / security

    private boolean isAllowed(List<ColumnAction<T>> actions) {
        Set<Constraint> constraints = new HashSet<>();
        actions.forEach(a -> {
            constraints.addAll(a.constraints);
            a.actions.forEach(innerA -> constraints.addAll(innerA.constraints));
        });
        return AuthorisationDecision.strict(finder.metadataRegistry()).isAllowed(constraints);
    }

    private boolean isAllowed(ColumnAction<T> action) {
        Set<Constraint> constraints = new HashSet<>();
        constraints.addAll(action.constraints);
        action.actions.forEach(a -> constraints.addAll(a.constraints));
        return AuthorisationDecision.strict(finder.metadataRegistry()).isAllowed(constraints);
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        // TODO Check column actions
        for (FinderRow<T> row : rows.values()) {
            row.onSecurityContextChange(securityContext);
        }
    }
}
