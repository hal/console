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
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NOT_AVAILABLE;
import static org.jboss.hal.resources.Names.ROLE;

/**
 * Describes a column in a finder.
 *
 * @author Harald Pehl
 */
public class FinderColumn<T extends FinderItem> implements IsElement, SecurityContextAware {

    public static class Builder<T extends FinderItem> {

        private final String id;
        private final String title;
        private final List<T> initialItems;
        private final List<ActionStruct<T>> columnActions;
        private boolean showCount;
        private boolean withFilter;

        public Builder(final String id, final String title) {
            this.id = id;
            this.title = title;
            this.initialItems = new ArrayList<>();
            this.columnActions = new ArrayList<>();
            this.showCount = false;
            this.withFilter = false;
        }

        public Builder<T> initialItem(T item) {
            initialItems.add(item);
            return this;
        }

        public Builder<T> initialItems(List<T> items) {
            initialItems.addAll(items);
            return this;
        }

        public Builder<T> columnAction(String title, ColumnAction<T> action) {
            this.columnActions.add(new ActionStruct<>(title, action));
            return this;
        }

        public Builder<T> columnAction(Element content, ColumnAction<T> action) {
            this.columnActions.add(new ActionStruct<>(content, action));
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

        public FinderColumn<T> build() {
            return new FinderColumn<>(this);
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String HEADER_ELEMENT = "headerElement";

    private final String id;
    private final Element root;

    FinderColumn(final Builder<T> builder) {
        this.id = builder.id;

        // header
        Elements.Builder eb = new Elements.Builder()
                .div().id(id).css(finderColumn, column(2))
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
        eb.end(); // </header>

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
                .span().id(iconId).css(inputGroupAddon, fontAwesome("search")).end()
            .end();
            // @formatter:on
        }

        // items
        eb.ul();
        for (T item : builder.initialItems) {
            eb.add(item.asElement());
        }
        eb.end().end(); // </ul> && </div>

        root = eb.build();
        if (builder.showCount && !builder.initialItems.isEmpty()) {
            Element headerElement = eb.referenceFor(HEADER_ELEMENT);
            String titleWithSize = builder.title + "(" + builder.initialItems.size() + ")";
            headerElement.setInnerText(titleWithSize);
            headerElement.setTitle(titleWithSize);
        }
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

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof FinderColumn)) { return false; }

        FinderColumn<?> that = (FinderColumn<?>) o;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
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

    }
}
