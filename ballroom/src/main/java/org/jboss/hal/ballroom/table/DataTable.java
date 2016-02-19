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
package org.jboss.hal.ballroom.table;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.TableElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
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

    private final String id;
    private final SecurityContext securityContext;
    private final Options<T> options;
    private final TableElement tableElement;
    private Api<T> api;

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public DataTable(final String id, final SecurityContext securityContext, final Options<T> options) {
        this.id = id;
        this.securityContext = securityContext;
        this.options = options;
        this.tableElement = new Elements.Builder()
                .start("table").id(id).css(dataTable, table, tableStriped, tableBordered, hover).end().build();
    }

    public Element asElement() {
        return tableElement;
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
        }
    }


    // ------------------------------------------------------ API access

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

    public void show() {
        Element wrapper = Browser.getDocument().getElementById(id + "_wrapper");
        Elements.setVisible(wrapper, true);
    }

    public void hide() {
        Element wrapper = Browser.getDocument().getElementById(id + "_wrapper");
        Elements.setVisible(wrapper, false);
    }
}
