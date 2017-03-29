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
import java.util.List;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.table.Button.ActionHandler;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.config.Settings;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.config.Settings.Key.PAGE_LENGTH;
import static org.jboss.hal.resources.CSS.*;

/**
 * Generic builder for data table {@linkplain Options options} used as a base class for the different option builders.
 *
 * @param <B> the builder type
 * @param <T> the row type
 *
 * @author Harald Pehl
 */
public abstract class GenericOptionsBuilder<B extends GenericOptionsBuilder<B, T>, T> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    protected List<Button<T>> buttons;
    protected List<Column<T>> columns;
    protected boolean keys;
    protected boolean searching;
    protected Select select;
    private ColumnActions<T> columnActions;
    private int pageLength;
    private boolean paging;

    protected GenericOptionsBuilder() {
        this.buttons = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.columnActions = new ColumnActions<>();
        this.pageLength = Settings.INSTANCE.get(PAGE_LENGTH).asInt(Settings.DEFAULT_PAGE_LENGTH);
        this.keys = true;
        this.paging = true;
        this.searching = true;
        this.select = Select.build(false);
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
        if (columns.isEmpty()) {
            throw new IllegalStateException("Empty columns in data table builder!");
        }
    }

    public B button(String text, ActionHandler<T> action) {
        return button(text, null, action);
    }

    public B button(String text, Scope scope, ActionHandler<T> action) {
        Button<T> button = new Button<>();
        button.text = text;
        button.action = action;
        if (scope != null) {
            button.extend = scope.selector();
        }
        return button(button);
    }

    public B button(Button<T> button) {
        buttons.add(button);
        return that();
    }

    public B column(String name, Column.RenderCallback<T, ?> render) {
        return column(new ColumnBuilder<>(name, new LabelBuilder().label(name), render).build());
    }

    public B column(String name, String title, Column.RenderCallback<T, ?> render) {
        return column(new ColumnBuilder<>(name, title, render).build());
    }

    public B column(Column<T> column) {
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
        Column<T> column = new ColumnBuilder<T>(Ids.build("column-action", Ids.uniqueId()), CONSTANTS.action(),
                (cell, type, row, meta) -> {
                    String id = Ids.uniqueId();
                    columnActions.add(id, columnAction);
                    return "<a id=\"" + id + "\" class=\"" + CSS.columnAction + "\">" + link + "</a>";
                })
                .orderable(false)
                .searchable(false)
                .width("10em")
                .build();
        this.columns.add(column);
        return that();
    }

    public B column(Function<ColumnActions<T>, Column<T>> actionColumn) {
        this.columns.add(actionColumn.apply(columnActions));
        return that();
    }

    public B checkboxColumn() {
        Column<T> checkboxColumn = new Column<>();
        checkboxColumn.orderable = false;
        checkboxColumn.className = selectCheckbox;
        checkboxColumn.render = (Column.RenderCallback<T, String>) (cell, type, row, meta) -> null;
        checkboxColumn.width = "40px"; //NON-NLS
        return column(checkboxColumn);
    }

    public B multiselect() {
        this.select = Select.build(true);
        return that();
    }

    public B keys(boolean keys) {
        this.keys = keys;
        return that();
    }

    public B paging(boolean paging) {
        this.paging = paging;
        return that();
    }

    public B searching(boolean searching) {
        this.searching = searching;
        return that();
    }

    @SuppressWarnings({"HardCodedStringLiteral", "unchecked"})
    public Options<T> build() {
        validate();
        Options<T> options = new Options<>();
        if (!buttons.isEmpty()) {
            // override defaults from patternfly.js:77
            options.dom = "<'dataTables_header' f B i>" +
                    "<'table-responsive' t>" +
                    "<'dataTables_footer' p>";
            options.buttons = new Buttons<>();
            options.buttons.dom = new Buttons.Dom();
            options.buttons.dom.container = new Buttons.Dom.Factory();
            options.buttons.dom.container.tag = "div";
            options.buttons.dom.container.className = pullRight + " " + btnGroup;
            options.buttons.dom.button = new Buttons.Dom.Factory();
            options.buttons.dom.button.tag = "button";
            options.buttons.dom.button.className = btn + " " + btnDefault;
            options.buttons.buttons = buttons.toArray(new Button[buttons.size()]);
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
}
