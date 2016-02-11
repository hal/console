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
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.core.finder.Finder.DATA_BREADCRUMB;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.Names.ROLE;

/**
 * Describes and renders a column in a finder. A column has a unique id, a title, a number of optional column actions
 * and an {@link ItemRenderer} which defines how the items of this column are rendered.
 *
 * @param <T> The columns type.
 * @author Harald Pehl
 */
public class FinderColumn<T> implements IsElement, SecurityContextAware {

    // TODO Make the ItemRenderer optional with a default implementation
    // TODO Let the FinderColumn override the renderer
    public static class Builder<T> {

        private final Finder finder;
        private final String id;
        private final String title;
        private final ItemRenderer<T> itemRenderer;
        private final List<ColumnAction<T>> columnActions;
        private boolean showCount;
        private boolean withFilter;
        private PreviewCallback<T> previewCallback;
        private List<T> items;
        private ItemsProvider<T> itemsProvider;

        public Builder(final Finder finder, final String id, final String title,
                final ItemRenderer<T> itemRenderer) {
            this.finder = finder;
            this.id = id;
            this.title = title;
            this.itemRenderer = itemRenderer;
            this.columnActions = new ArrayList<>();
            this.showCount = false;
            this.withFilter = false;
            this.items = new ArrayList<>();
        }

        public Builder<T> columnAction(String title, ColumnActionHandler<T> action) {
            columnActions.add(new ColumnAction<>(title, action));
            return this;
        }

        public Builder<T> columnAction(Element element, ColumnActionHandler<T> action) {
            columnActions.add(new ColumnAction<>(element, action));
            return this;
        }

        public Builder<T> withColumnAdd(ColumnActionHandler<T> action) {
            Element element = new Elements.Builder().span().css(CSS.pfIcon("add-circle-o")).end().build();
            return columnAction(element, action);
        }

        public Builder<T> withColumnRefresh(ColumnActionHandler<T> action) {
            Element element = new Elements.Builder().span().css(CSS.fontAwesome("refresh")).end().build();
            return columnAction(element, action);
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

    private final Finder finder;
    private final String id;
    private final String title;
    private final boolean showCount;
    private final ItemRenderer<T> itemRenderer;
    private final List<T> initialItems;
    private final ItemsProvider<T> itemsProvider;
    private final PreviewCallback<T> previewCallback;
    private final Map<String, FinderRow<T>> rows;

    private final Element root;
    private final Element headerElement;
    private final InputElement filterElement;
    private final Element ulElement;
    private final Element noItems;


    protected FinderColumn(final Builder<T> builder) {
        this.finder = builder.finder;
        this.id = builder.id;
        this.title = builder.title;
        this.showCount = builder.showCount;
        this.itemRenderer = builder.itemRenderer;
        this.initialItems = builder.items;
        this.itemsProvider = builder.itemsProvider;
        this.previewCallback = builder.previewCallback;
        this.rows = new HashMap<>();

        // header
        Elements.Builder eb = new Elements.Builder()
                .div().id(id).data(DATA_BREADCRUMB, title).css(finderColumn, column(2))
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
                    .on(keyup, (this::onFilter))
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

    private void onFilter(final Event event) {
        int matched = 0;
        String filter = filterElement.getValue();
        for (Element li : Elements.children(ulElement)) {
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

    boolean containsItem(String itemId) {
        return rows.containsKey(itemId);
    }

    void selectItem(String itemId) {
        for (Map.Entry<String, FinderRow<T>> entry : rows.entrySet()) {
            entry.getValue().markSelected(itemId.equals(entry.getKey()));
        }
    }

    FinderRow<T> getSelectedRow(String id) {
        Element activeItem = ulElement.querySelector("li." + CSS.active); //NON-NLS
        if (activeItem != null && rows.containsKey(activeItem.getId())) {
            return rows.get(activeItem.getId());
        }
        return null;
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

        for (T item : items) {
            FinderRow<T> row = new FinderRow<>(finder, this, item,
                    itemRenderer.render(item), previewCallback);
            rows.put(row.getId(), row);
            ulElement.appendChild(row.asElement());
        }
        updateHeader(items.size());

        if (items.isEmpty()) {
            ulElement.appendChild(noItems);
        }

        if (callback != null) {
            callback.onSuccess(this);
        }
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        // TODO Check column actions
        for (FinderRow<T> row : rows.values()) {
            row.onSecurityContextChange(securityContext);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
