/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.finder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.KeyboardEvent;
import elemental.events.KeyboardEvent.KeyCode;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.*;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.PatternFly.$;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TABINDEX;

/**
 * Describes and renders a column in a finder. A column has a unique id, a title, a number of optional column actions
 * and an {@link ItemRenderer} which defines how the items of this column are rendered.
 * <p>
 * TODO Keep selected row when adding / removing columns to simplify keyboard navigation
 *
 * @param <T> The columns type.
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
        private PreviewCallback<T> previewCallback;
        private List<T> items;
        private ItemsProvider<T> itemsProvider;

        public Builder(final Finder finder, final String id, final String title) {
            this.finder = finder;
            this.id = id;
            this.title = title;
            this.itemRenderer = item -> () -> String.valueOf(item);
            this.columnActions = new ArrayList<>();
            this.showCount = false;
            this.withFilter = false;
            this.items = new ArrayList<>();
        }

        public Builder<T> columnAction(String title, ColumnActionHandler<T> handler) {
            return columnAction(new ColumnAction<>(title, handler));
        }

        public Builder<T> columnAction(Element element, ColumnActionHandler<T> handler) {
            return columnAction(new ColumnAction<>(element, handler));
        }

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

        public Builder<T> itemRenderer(ItemRenderer<T> itemRenderer) {
            this.itemRenderer = itemRenderer;
            return this;
        }

        public Builder<T> onPreview(PreviewCallback<T> previewCallback) {
            this.previewCallback = previewCallback;
            return this;
        }

        public FinderColumn<T> build() {
            return new FinderColumn<>(this);
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String HEADER_ELEMENT = "headerElement";
    private static final String FILTER_ELEMENT = "filterElement";
    private static final String UL_ELEMENT = "ulElement";
    private static final Logger logger = LoggerFactory.getLogger(FinderColumn.class);

    private final Finder finder;
    private final String id;
    private final String title;
    private final boolean showCount;
    private final List<T> initialItems;
    private ItemsProvider<T> itemsProvider;
    private ItemRenderer<T> itemRenderer;
    private final Map<String, FinderRow<T>> rows;
    private final PreviewCallback<T> previewCallback;

    private final Element root;
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
        this.initialItems = builder.items;
        this.itemsProvider = builder.itemsProvider;
        this.itemRenderer = builder.itemRenderer;
        this.rows = new HashMap<>();
        this.previewCallback = builder.previewCallback;
        this.asElement = false;

        // header
        Elements.Builder eb = new Elements.Builder()
                .div().id(id)
                .data(DATA_BREADCRUMB, title)
                .css(finderColumn, column(2))
                .attr(TABINDEX, "-1")
                .on(keydown, this::onNavigation)
                .header()
                .h(1).innerText(builder.title).title(builder.title).rememberAs(HEADER_ELEMENT).end();

        // column actions
        if (!builder.columnActions.isEmpty()) {
            if (builder.columnActions.size() == 1) {
                addColumnButton(eb, builder.columnActions.get(0));
            } else {
                //noinspection DuplicateStringLiteralInspection
                eb.div().css(btnGroup).attr(ROLE, "group"); //NON-NLS
                for (ColumnAction<T> action : builder.columnActions) {
                    addColumnButton(eb, action);
                }
                eb.end();
            }
        }
        eb.end(); // </updateHeader>

        // filter box
        if (builder.withFilter) {
            String iconId = IdBuilder.build(id, filter, "icon");
            // @formatter:off
            eb.div().css(inputGroup, filter)
                .input(text)
                    .id(IdBuilder.build(id, filter))
                    .css(formControl)
                    .aria("describedby", iconId)
                    .attr("placeholder", CONSTANTS.filter())
                    .on(keydown, this::onNavigation)
                    .on(keyup, this::onFilter)
                    .rememberAs(FILTER_ELEMENT)
                .span().id(iconId).css(inputGroupAddon, fontAwesome("search")).end()
            .end();
            // @formatter:on
        }

        // rows
        eb.ul().rememberAs(UL_ELEMENT).end().end(); // </ul> && </div>

        // no items marker
        noItems = new Elements.Builder().li().css(empty)
                .span().css(itemText).innerText(CONSTANTS.noItems()).end()
                .end().build();

        root = eb.build();
        headerElement = eb.referenceFor(HEADER_ELEMENT);
        filterElement = builder.withFilter ? eb.referenceFor(FILTER_ELEMENT) : null;
        ulElement = eb.referenceFor(UL_ELEMENT);
    }

    private void addColumnButton(final Elements.Builder builder, final ColumnAction<T> action) {
        builder.button()
                .css(btn, btnFinder)
                .on(click, event -> action.handler.execute(this));

        if (action.title != null) {
            builder.innerText(action.title);
        } else if (action.element != null) {
            builder.add(action.element);
        } else {
            builder.innerText(NOT_AVAILABLE);
        }

        builder.end(); // </button>
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
            Object filterData = li.getDataset().at(CSS.filter); //NON-NLS
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
            if (!ulElement.contains(noItems)) {
                ulElement.appendChild(noItems);
            }
        } else {
            if (ulElement.contains(noItems)) {
                ulElement.removeChild(noItems);
            }
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

                        markSelected(select.getId());
                        row(select).showPreview();
                        select.scrollIntoView(false);
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

                            finder.reduceTo(previousColumn);
                            finder.selectColumn(previousColumn.getId());
                            FinderRow selectedRow = previousColumn.selectedRow();
                            if (selectedRow != null) {
                                selectedRow.showPreview();
                            }
                            finder.updateContext();
                            finder.publishContext();
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
                                        //noinspection HardCodedStringLiteral
                                        logger.error("Unable to append next column '{}' on keyboard right: {}",
                                                nextColumn, throwable.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(final FinderColumn column) {
                                        if (column.activeElement() == null && column.hasVisibleElements()) {
                                            Element firstElement = column.nextVisibleElement(null);
                                            column.markSelected(firstElement.getId());
                                            column.row(firstElement).showPreview();
                                        }
                                        finder.updateContext();
                                        finder.publishContext();
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


    // ------------------------------------------------------ internal API

    private Element activeElement() {return ulElement.querySelector("li." + CSS.active);} //NON-NLS

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

    Element nextVisibleElement(Element start) {
        Element element = start == null ? ulElement.getFirstElementChild() : start.getNextElementSibling();
        while (element != null && !Elements.isVisible(element)) {
            element = element.getNextElementSibling();
        }
        return element;
    }

    private FinderRow<T> row(Element element) {
        return rows.get(element.getId());
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
            entry.getValue().markSelected(itemId.equals(entry.getKey()));
        }
    }

    void setItems(AsyncCallback<FinderColumn> callback) {
        Element activeElement = activeElement();
        String selectedItem = activeElement != null ? activeElement.getId() : null;

        if (!initialItems.isEmpty()) {
            setItems(initialItems, selectedItem, callback);

        } else if (itemsProvider != null) {
            itemsProvider.get(finder.getContext(), new AsyncCallback<List<T>>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    callback.onFailure(throwable);
                }

                @Override
                public void onSuccess(final List<T> items) {
                    setItems(items, selectedItem, callback);
                }
            });

        } else {
            setItems(Collections.emptyList(), selectedItem, callback);
        }
    }

    private void setItems(List<T> items, String selectedItem, AsyncCallback<FinderColumn> callback) {
        rows.clear();
        Elements.removeChildrenFrom(ulElement);
        if (filterElement != null) {
            filterElement.setValue("");
        }

        for (T item : items) {
            FinderRow<T> row = new FinderRow<>(finder, this, item,
                    itemRenderer.render(item), previewCallback);
            rows.put(row.getId(), row);
            ulElement.appendChild(row.asElement());
        }
        updateHeader(items.size());
        $("#" + id + " [data-toggle=tooltip]").tooltip();

        if (items.isEmpty()) {
            ulElement.appendChild(noItems);
        } else {
            if (contains(selectedItem)) {
                markSelected(selectedItem);
                rows.get(selectedItem).showPreview();
            }
        }

        if (callback != null) {
            callback.onSuccess(this);
        }
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

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        // TODO Check column actions
        for (FinderRow<T> row : rows.values()) {
            row.onSecurityContextChange(securityContext);
        }
    }

    public void refresh() {
        setItems(new AsyncCallback<FinderColumn>() {
            @Override
            public void onFailure(final Throwable throwable) {
                logger.error("Unable to refresh column {}: {}", id, throwable.getMessage()); //NON-NLS
            }

            @Override
            public void onSuccess(final FinderColumn column) {
                finder.updateContext();
                finder.publishContext();
            }
        });
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
