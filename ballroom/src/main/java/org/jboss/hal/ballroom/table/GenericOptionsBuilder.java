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
package org.jboss.hal.ballroom.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Settings;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.config.Settings.Key.PAGE_SIZE;
import static org.jboss.hal.resources.CSS.*;

/**
 * Generic builder for data table {@linkplain Options options} used as a base class for the different option builders.
 *
 * @param <B> the builder type
 * @param <T> the row type
 */
public abstract class GenericOptionsBuilder<B extends GenericOptionsBuilder<B, T>, T> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    protected List<Api.Button<T>> buttons;
    protected List<Column<T>> columns;
    protected boolean keys;
    protected boolean searching;
    protected Api.Select select;
    private ColumnActions<T> columnActions;
    private int pageLength;
    private boolean paging;
    private Options<T> options;

    protected GenericOptionsBuilder() {
        this.buttons = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.columnActions = new ColumnActions<>();
        this.pageLength = Settings.INSTANCE.get(PAGE_SIZE).asInt(Settings.DEFAULT_PAGE_SIZE);
        this.keys = true;
        this.paging = true;
        this.searching = true;
        this.select = Api.Select.build(false);
    }

    /**
     * In order to make builders work with inheritance, sub-builders must return a reference to their instance.
     *
     * @return {@code this}
     */
    protected abstract B that();

    /**
     * @throws IllegalStateException if the builder's internal state is not valid
     */
    protected void validate() {
        assertNoOptions();
        if (columns.isEmpty()) {
            throw new IllegalStateException("Empty columns in data table builder!");
        }
    }

    public B button(String text, ButtonHandler<T> handler) {
        return button(new Button<>(text, handler));
    }

    public B button(String text, ButtonHandler<T> handler, Scope scope) {
        return button(new Button<>(text, handler, scope));
    }

    public B button(String text, ButtonHandler<T> handler, Constraint constraint) {
        return button(new Button<>(text, handler, constraint));
    }

    public B button(String text, ButtonHandler<T> handler, Scope scope, Constraint constraint) {
        return button(new Button<>(text, handler, scope, constraint));
    }

    public B button(Button<T> button) {
        assertNoOptions();

        Api.Button<T> apiButton = new Api.Button<>();
        apiButton.text = button.title;
        apiButton.action = (event, api, node, btn) -> button.handler.execute(btn.table);
        if (button.scope != null) {
            apiButton.extend = button.scope.selector();
        }
        if (button.constraint != null) {
            apiButton.constraint = button.constraint.data();
        }

        buttons.add(apiButton);
        return that();
    }

    public B column(String name, Column.RenderCallback<T, ?> render) {
        return column(new ColumnBuilder<>(name, new LabelBuilder().label(name), render).build());
    }

    public B column(String name, String title, Column.RenderCallback<T, ?> render) {
        return column(new ColumnBuilder<>(name, title, render).build());
    }

    public B column(Column<T> column) {
        assertNoOptions();

        this.columns.add(column);
        return that();
    }

    /**
     * Adds an action column named {@link Constants#action()} with the specified link title and action handler.
     *
     * @param link         the link title
     * @param columnAction the action handler which receives the row data as parameter
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public B column(String link, ColumnAction<T> columnAction) {
        return column(link, columnAction, "10em");
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public B column(String link, ColumnAction<T> columnAction, String width) {
        assertNoOptions();

        Column<T> column = new ColumnBuilder<T>(Ids.build("column-action", Ids.uniqueId()), CONSTANTS.action(),
                (cell, type, row, meta) -> {
                    String id = Ids.uniqueId();
                    columnActions.add(id, columnAction);
                    return "<a id=\"" + id + "\" class=\"" + CSS.columnAction + "\">" + link + "</a>";
                })
                .orderable(false)
                .searchable(false)
                .width(width)
                .build();
        this.columns.add(column);
        return that();
    }

    public B column(Function<ColumnActions<T>, Column<T>> actionColumn) {
        assertNoOptions();

        this.columns.add(actionColumn.apply(columnActions));
        return that();
    }

    public B checkboxColumn() {
        assertNoOptions();

        Column<T> checkboxColumn = new Column<>();
        checkboxColumn.orderable = false;
        checkboxColumn.className = selectCheckbox;
        checkboxColumn.render = (Column.RenderCallback<T, String>) (cell, type, row, meta) -> null;
        checkboxColumn.width = "40px"; //NON-NLS
        return column(checkboxColumn);
    }

    public B multiselect() {
        assertNoOptions();

        this.select = Api.Select.build(true);
        return that();
    }

    public B keys(boolean keys) {
        assertNoOptions();

        this.keys = keys;
        return that();
    }

    public B paging(boolean paging) {
        assertNoOptions();

        this.paging = paging;
        return that();
    }

    public B searching(boolean searching) {
        assertNoOptions();

        this.searching = searching;
        return that();
    }

    @SuppressWarnings({"HardCodedStringLiteral", "unchecked"})
    public Options<T> options() {
        if (options != null) {
            return options;
        }

        validate();
        options = new Options<>();
        options.buttonConstraints = new HashMap<>();
        if (!buttons.isEmpty()) {
            // override defaults from patternfly.js:77
            options.dom = "<'dataTables_header' f B i>" +
                    "<'table-responsive' t>" +
                    "<'dataTables_footer' p>";
            options.buttons = new Api.Buttons<>();
            options.buttons.dom = new Api.Buttons.Dom();
            options.buttons.dom.container = new Api.Buttons.Dom.Factory();
            options.buttons.dom.container.tag = "div";
            options.buttons.dom.container.className = pullRight + " " + btnGroup;
            options.buttons.dom.button = new Api.Buttons.Dom.Factory();
            options.buttons.dom.button.tag = "button";
            options.buttons.dom.button.className = btn + " " + btnDefault;
            options.buttons.buttons = buttons.toArray(new Api.Button[buttons.size()]);

            for (int i = 0; i < options.buttons.buttons.length; i++) {
                if (options.buttons.buttons[i].constraint != null) {
                    options.buttonConstraints.put(i, options.buttons.buttons[i].constraint);
                }
            }
        }
        options.columns = columns.toArray(new Column[columns.size()]);
        options.keys = keys;
        options.paging = paging;
        options.pageLength = pageLength;
        options.searching = searching;
        options.select = select;

        // custom options
        options.columnActions = columnActions;
        return options;
    }

    private void assertNoOptions() {
        if (options != null) {
            throw new IllegalStateException("OptionsBuilder.options() already called");
        }
    }
}
