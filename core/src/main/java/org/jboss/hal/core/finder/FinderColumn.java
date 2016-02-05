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
import java.util.List;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.core.finder.Finder.BREADCRUMB_KEY;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.Names.ROLE;

/**
 * Describes a column in a finder.
 *
 * @author Harald Pehl
 */
public class FinderColumn<T> implements IsElement, SecurityContextAware {

    public static class Builder<T> {

        private final String id;
        private final String title;
        private final ItemCallback<T> itemCallback;
        private final List<ActionStruct<T>> columnActions;
        private final List<ActionStruct<T>> itemActions;
        private boolean showCount;
        private boolean withFilter;
        private SelectCallback<T> selectCallback;
        private PreviewCallback<T> previewCallback;

        public Builder(final String id, final String title,
                final ItemCallback<T> itemCallback) {
            this.id = id;
            this.title = title;
            this.itemCallback = itemCallback;
            this.columnActions = new ArrayList<>();
            this.itemActions = new ArrayList<>();
            this.showCount = false;
            this.withFilter = false;
        }

        public Builder<T> columnAction(String title, ColumnAction<T> action) {
            columnActions.add(new ActionStruct<>(title, action));
            return this;
        }

        public Builder<T> columnAction(Element content, ColumnAction<T> action) {
            columnActions.add(new ActionStruct<>(content, action));
            return this;
        }

        public Builder<T> withColumnAdd(ColumnAction<T> action) {
            Element content = new Elements.Builder().span().css(CSS.pfIcon("add-circle-o")).end().build();
            return columnAction(content, action);
        }

        public Builder<T> withColumnRefresh(ColumnAction<T> action) {
            Element content = new Elements.Builder().span().css(CSS.fontAwesome("refresh")).end().build();
            return columnAction(content, action);
        }

        public Builder<T> showCount() {
            this.showCount = true;
            return this;
        }

        public Builder<T> withFilter() {
            this.withFilter = true;
            return this;
        }

        public Builder<T> itemAction(String title, ItemAction<T> action) {
            itemActions.add(new ActionStruct<>(title, action));
            return this;
        }

        public Builder<T> itemAction(Element content, ItemAction<T> action) {
            itemActions.add(new ActionStruct<>(content, action));
            return this;
        }

        public Builder<T> onSelect(SelectCallback<T> selectCallback) {
            this.selectCallback = selectCallback;
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

    private final String id;
    private final String title;
    private final boolean showCount;
    private final ItemCallback<T> itemCallback;
    private final SelectCallback<T> selectCallback;
    private final PreviewCallback<T> previewCallback;
    private final List<FinderItem<T>> items;
    private final List<ActionStruct<T>> itemActions;

    private final Element root;
    private final Element headerElement;
    private final InputElement filterElement;
    private final Element ulElement;

    private Finder finder;

    protected FinderColumn(final Builder<T> builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.showCount = builder.showCount;
        this.itemCallback = builder.itemCallback;
        this.selectCallback = builder.selectCallback;
        this.previewCallback = builder.previewCallback;
        this.items = new ArrayList<>();
        this.itemActions = new ArrayList<>();
        this.itemActions.addAll(builder.itemActions);
        this.finder = null;

        // header
        Elements.Builder eb = new Elements.Builder()
                .div().id(id).css(finderColumn, column(2)).data(BREADCRUMB_KEY, title)
                .header()
                .h(1).innerText(builder.title).title(builder.title).rememberAs(HEADER_ELEMENT).end();

        // column actions
        if (!builder.columnActions.isEmpty()) {
            if (builder.columnActions.size() == 1) {
                addColumnButton(eb, builder.columnActions.get(0));
            } else {
                //noinspection DuplicateStringLiteralInspection
                eb.div().css(btnGroup).attr(ROLE, "group"); //NON-NLS
                for (ActionStruct<T> action : builder.columnActions) {
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

        // items
        eb.ul().rememberAs(UL_ELEMENT).end().end(); // </ul> && </div>

        root = eb.build();
        headerElement = eb.referenceFor(HEADER_ELEMENT);
        filterElement = builder.withFilter ? eb.referenceFor(FILTER_ELEMENT) : null;
        ulElement = eb.referenceFor(UL_ELEMENT);
    }

    private void addColumnButton(final Elements.Builder builder, final ActionStruct<T> action) {
        builder.button()
                .css(btn, btnFinder)
                .on(click, event -> action.columnAction.execute(this));

        if (action.title != null) {
            builder.innerText(action.title);
        } else if (action.content != null) {
            builder.add(action.content);
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

    public void setItems(List<T> items) {
        this.items.clear();
        Elements.removeChildrenFrom(ulElement);
        if (filterElement != null) {
            filterElement.setValue("");
        }

        for (T item : items) {
            // finder might not yet be initialized, thus pass a Provider<Finder>
            FinderItem<T> finderItem = new FinderItem<>(() -> finder, this, item,
                    itemCallback.render(item), itemActions, selectCallback, previewCallback);
            this.items.add(finderItem);
            ulElement.appendChild(finderItem.asElement());
        }

        updateHeader(items.size());
    }

    private void updateHeader(int matched) {
        if (showCount) {
            String titleWithSize;
            if (matched == items.size()) {
                titleWithSize = title + " (" + items.size() + ")";
            } else {
                titleWithSize = title + " (" + matched + " / " + items.size() + ")";
            }
            headerElement.setInnerText(titleWithSize);
            headerElement.setTitle(titleWithSize);
        }
    }

    void setFinder(final Finder finder) {
        this.finder = finder;
    }

    @Override
    public String toString() {
        return "FinderColumn(" + id + ")";
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        // TODO Check column actions
        for (FinderItem<T> item : items) {
            item.onSecurityContextChange(securityContext);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
