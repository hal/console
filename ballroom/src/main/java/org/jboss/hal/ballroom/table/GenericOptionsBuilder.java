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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.table.Button.ActionHandler;
import org.jboss.hal.ballroom.table.Button.Scope;

import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.pullRight;

/**
 * Generic builder for data table {@linkplain Options options} used as a base class for the different option builders.
 *
 * @param <B> the builder type
 * @param <T> the row type
 *
 * @author Harald Pehl
 */
public abstract class GenericOptionsBuilder<B extends GenericOptionsBuilder<B, T>, T> {

    protected List<Button<T>> buttons;
    protected List<Column<T>> columns;
    protected int pageLength;
    protected boolean paging;
    protected boolean searching;
    protected Select select;

    protected GenericOptionsBuilder() {
        this.buttons = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.pageLength = 10;
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

    public B multiselect() {
        this.select = Select.build(true);
        return that();
    }

    public B pageLength(int pageLength) {
        this.pageLength = pageLength;
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
        options.paging = paging;
        options.pageLength = pageLength;
        options.searching = searching;
        options.select = select;
        return options;
    }
}
