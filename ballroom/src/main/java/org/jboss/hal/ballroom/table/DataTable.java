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

import java.util.List;
import java.util.function.Function;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JQuery;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.form.Form;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.table;
import static org.jboss.hal.ballroom.table.RefreshMode.RESET;
import static org.jboss.hal.resources.CSS.dataTable;
import static org.jboss.hal.resources.CSS.hover;
import static org.jboss.hal.resources.CSS.table;
import static org.jboss.hal.resources.CSS.tableBordered;
import static org.jboss.hal.resources.CSS.tableStriped;

/**
 * Table element which implements the DataTables plugin for jQuery. Using the data table consists of multiple steps:
 * <ol>
 * <li>Create an instance passing an id and an {@linkplain Options options} instance</li>
 * <li>Call {@link #attach()} <strong>after</strong> the data table element was added to the DOM</li>
 * <li>Call any of the {@link Table} methods</li>
 * </ol>
 * <p>
 * Sample which uses a {@code FooBar} as row type:
 * <pre>
 * class FooBar {
 *     final String foo;
 *     final String bar;
 *
 *     FooBar() {
 *         this.foo = "Foo-" + String.valueOf(Random.nextInt(12345));
 *         this.bar = "Bar-" + String.valueOf(Random.nextInt(12345));
 *     }
 * }
 *
 * Options<FooBar> options = new OptionsBuilder&lt;FooBarBaz&gt;()
 *     .button("Add Row", (event, api) -> api.row.add(new FooBar()).draw("full-reset"))
 *     .column("foo", "Foo", (cell, type, row, meta) -> row.foo)
 *     .column("bar", "Bar", (cell, type, row, meta) -> row.baz)
 *     .options();
 * DataTable&lt;FooBar&gt; dataTable = new DataTable&lt;&gt;("sample", SecurityContext.RWX, options);
 * </pre>
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/">https://datatables.net/</a>
 */
public class DataTable<T> implements Table<T> {

    @JsType(isNative = true)
    static class Bridge<T> {

        @JsMethod(namespace = GLOBAL, name = "$")
        native static <T> Bridge<T> select(@NonNls String selector);

        @JsMethod(name = "DataTable")
        native Api<T> dataTable(Options options);
    }


    // ------------------------------------------------------ instance & lifecycle

    private static final String DESELECT = "deselect";
    private static final String ROW = "row";
    private static final String SELECT = "select";
    private static final String WRAPPER_SUFFIX = "_wrapper";

    private final String id;
    private final Options<T> options;
    private final HTMLTableElement tableElement;
    private Api<T> api;

    public DataTable(final String id, final Options<T> options) {
        this.id = id;
        this.options = options;
        this.tableElement = table().id(id).css(dataTable, table, tableStriped, tableBordered, hover).asElement();
        for (Api.Button<T> button : options.buttons.buttons) {
            button.table = this;
        }
    }

    @Override
    public HTMLElement asElement() {
        return api == null ? tableElement : (HTMLElement) DomGlobal.document.getElementById(id + WRAPPER_SUFFIX);
    }

    /**
     * Initialized the {@link Api} instance using the {@link Options} given at constructor argument. Make sure to call
     * this method before using any of the API methods. It's safe to call the methods multiple times (the
     * initialization will happen only once).
     */
    @Override
    public void attach() {
        if (api == null) {
            options.id = id;
            api = Bridge.<T>select("#" + id).dataTable(options);
        }
    }


    // ------------------------------------------------------ DataTable API access

    /**
     * Getter for the {@link Api} instance.
     *
     * @return The data tables API
     *
     * @throws IllegalStateException if the API wasn't initialized using {@link #attach()}
     */
    private Api<T> api() {
        if (api == null) {
            throw new IllegalStateException(
                    "DataTable('" + id + "') is not attached. Call DataTable.attach() before using any of the API methods!");
        }
        return api;
    }

    protected JQuery buttonElement(int index) {
        return api().button(index).node();
    }


    // ------------------------------------------------------ 'higher' level API

    @Override
    public void show() {
        HTMLElement wrapper = (HTMLElement) DomGlobal.document.getElementById(id + WRAPPER_SUFFIX);
        Elements.setVisible(wrapper, true);
    }

    @Override
    public void hide() {
        HTMLElement wrapper = (HTMLElement) DomGlobal.document.getElementById(id + WRAPPER_SUFFIX);
        Elements.setVisible(wrapper, false);
    }

    @Override
    public void enableButton(final int index, final boolean enable) {
        api().button(index).enable(enable);
    }

    /**
     * Binds a form to the table and takes care to view or clear the form upon selection changes
     */
    @Override
    public void bindForm(final Form<T> form) {
        onSelectionChange(table -> {
            if (table.hasSelection()) {
                form.view(table.selectedRow());
            } else {
                form.clear();
            }
        });
    }

    @Override
    public void bindForms(final Iterable<Form<T>> forms) {
        onSelectionChange(table -> {
            if (table.hasSelection()) {
                T selectedRow = table.selectedRow();
                for (Form<T> form : forms) {
                    form.view(selectedRow);
                }
            } else {
                for (Form<T> form : forms) {
                    form.clear();
                }
            }
        });
    }

    @Override
    public void clear() {
        api().clear();
    }

    @Override
    public List<T> getRows() {
        SelectorModifier selectorModifier = new SelectorModifierBuilder().page(SelectorModifier.Page.all).build();
        return JsHelper.asList(api().rows(selectorModifier).data().toArray());
    }

    @Override
    public void onSelectionChange(final SelectionChangeHandler<T> handler) {
        api().on(SELECT, (event, api, type) -> {
            if (ROW.equals(type)) {
                handler.onSelectionChanged(DataTable.this);
            }
        });
        api().on(DESELECT, (event, api, type) -> {
            if (ROW.equals(type)) {
                handler.onSelectionChanged(DataTable.this);
            }
        });
    }

    @Override
    public T selectedRow() {
        return api().selectedRow();
    }

    @Override
    public List<T> selectedRows() {
        return api().selectedRows();
    }

    @Override
    public void select(final T data) {
        select(data, null);
    }

    /**
     * Selects the row with the specified data.
     *
     * @param data       the data
     * @param identifier a function which must return an unique identifier for a given row.
     */
    @Override
    public void select(final T data, final Function<T, String> identifier) {
        if (data != null && identifier != null) {
            String id1 = identifier.apply(data);
            Api.RowSelection<T> rows = (idx, d, tr) -> {
                if (d != null) {
                    String id2 = identifier.apply(d);
                    return (id1 != null && id2 != null) && id1.equals(id2);
                }
                return false;
            };
            api().rows(rows).select();
        }
    }

    /**
     * Replaces the existing data with the new one.
     *
     * @param data the new data
     */
    @Override
    public void update(final Iterable<T> data) {
        update(data, RESET, null);
    }

    @Override
    public void update(final Iterable<T> data, final RefreshMode mode) {
        update(data, mode, null);
    }

    @Override
    public void update(final Iterable<T> data, final Function<T, String> identifier) {
        update(data, RESET, identifier);
    }

    /**
     * Replaces the existing data with the new one. If necessary, restores the current selection based on the specified
     * function.
     *
     * @param data       the new data
     * @param identifier a function which must return an unique identifier for a given row. Used to restore the
     *                   selection after replacing the data.
     */
    @Override
    public void update(final Iterable<T> data, final RefreshMode mode, final Function<T, String> identifier) {
        List<T> selection = api().selectedRows();
        api().clear().add(data).refresh(mode);
        if (identifier != null) {
            if (!selection.isEmpty()) {
                Api.RowSelection<T> rows = (index, d1, tr) -> {
                    if (d1 != null) {
                        String id1 = identifier.apply(d1);
                        return selection.stream().anyMatch(d2 -> {
                            if (d2 != null) {
                                String id2 = identifier.apply(d2);
                                return (id1 != null && id2 != null) && id1.equals(id2);
                            }
                            return false;
                        });
                    }
                    return false;
                };
                api().rows(rows).select();
            }
        }
    }
}
