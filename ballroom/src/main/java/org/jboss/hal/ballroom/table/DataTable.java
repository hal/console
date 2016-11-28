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

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.TableElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.resources.CSS.*;

/**
 * Table element which implements the DataTables plugin for jQuery. Using the data table consists of multiple steps:
 * <ol>
 * <li>Create an instance passing an id, a security context and an {@linkplain Options options} instance</li>
 * <li>Call {@link #attach()} <strong>after</strong> the data table element was attached to the DOM</li>
 * <li>Call any of the API methods using the {@link #api()} getter</li>
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
 *     .build();
 * DataTable&lt;FooBar&gt; dataTable = new DataTable&lt;&gt;("sample", SecurityContext.RWX, options);
 * </pre>
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/">https://datatables.net/</a>
 */
public class DataTable<T> implements IsElement, Attachable {

    @JsType(isNative = true)
    static class Bridge<T> {

        @JsMethod(namespace = GLOBAL, name = "$")
        native static <T> Bridge<T> select(@NonNls String selector);

        @JsMethod(name = "DataTable")
        native Api<T> dataTable(Options options);
    }


    // ------------------------------------------------------ instance & lifecycle

    static final String DESELECT = "deselect";
    static final String ROW = "row";
    static final String SELECT = "select";
    private static final String WRAPPER_SUFFIX = "_wrapper";

    private final String id;
    private final Options<T> options;
    private final TableElement tableElement;
    private Api<T> api;

    public DataTable(final String id, final Options<T> options) {
        this.id = id;
        this.options = options;
        this.tableElement = new Elements.Builder()
                .start("table").id(id).css(dataTable, table, tableStriped, tableBordered, hover).end().build();
    }

    public Element asElement() {
        return api == null ? tableElement : Browser.getDocument().getElementById(id + WRAPPER_SUFFIX);
    }

    /**
     * Initialized the {@link Api} instance using the {@link Options} given at constructor argument. Make sure to call
     * this method before using any of the API methods. It's safe to call the methods multiple times (the
     * initialization will happen only once).
     */
    @Override
    public void attach() {
        if (api == null) {
            // TODO check security context and adjust options if necessary
            api = Bridge.<T>select("#" + id).dataTable(options);
            api.id = id;
            api.columnActions = options.columnActions;
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
    public Api<T> api() {
        if (api == null) {
            throw new IllegalStateException(
                    "DataTable('" + id + "') is not attached. Call DataTable.attach() before using any of the API methods!");
        }
        return api;
    }


    // ------------------------------------------------------ 'higher' level API

    public void show() {
        Element wrapper = Browser.getDocument().getElementById(id + WRAPPER_SUFFIX);
        Elements.setVisible(wrapper, true);
    }

    public void hide() {
        Element wrapper = Browser.getDocument().getElementById(id + WRAPPER_SUFFIX);
        Elements.setVisible(wrapper, false);
    }

    /**
     * Binds a form to the table and takes care to view or clear the form upon selection changes
     */
    public void bindForm(final Form<T> form) {
        api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                form.view(api.selectedRow());
            } else {
                form.clear();
            }
        });
    }

    public void bindForms(final Iterable<Form<T>> forms) {
        api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                T selectedRow = api.selectedRow();
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

    /**
     * Replaces the existing data with the new one. If necessary, restores the current selection based on the specified
     * function.
     *
     * @param data       the new data
     * @param identifier a function which must return an unique identifier for a given row. Used to restore the
     *                   selection after replacing the data
     */
    public <S> void update(final Iterable<T> data, final Function<T, S> identifier) {
        List<T> selection = api().selectedRows();
        api().clear().add(data).refresh(RESET);
        if (!selection.isEmpty()) {
            RowSelection<T> rows = (index, d1, tr) -> selection.stream().anyMatch(
                    d2 -> identifier.apply(d1).equals(identifier.apply(d2)));
            api().rows(rows).select();
        }
    }
}
