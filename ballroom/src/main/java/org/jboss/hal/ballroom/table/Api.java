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

import java.util.Collections;
import java.util.List;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JQuery;

import static org.jboss.hal.ballroom.JsHelper.asList;
import static org.jboss.hal.resources.CSS.columnAction;

/**
 * Subset of the DataTables API.
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/reference/api/">https://datatables.net/reference/api/</a>
 */
@JsType(isNative = true)
public class Api<T> {

    // We cannot have both a property and a method named equally.
    // That's why the API defines the property "row" and the method "rows"
    @JsProperty Row<T> row;


    // ------------------------------------------------------ internal API

    @JsOverlay
    final Api<T> add(Iterable<T> data) {
        if (data != null) {
            for (T d : data) {
                row.add(d);
            }
        }
        return this;
    }

    native Api<T> clear();


    // ------------------------------------------------------ API a-z

    public native Api<T> button(int index);

    public native Api<T> data();

    public native Api<T> draw(String paging);

    /**
     * Disables or enables the button selected with {@link #button(int)}
     */
    public native Api<T> enable(boolean enable);

    public native Options<T> init();

    /**
     * Returns the jQuery object for the button selected with {@link #button(int)}
     */
    public native JQuery node();

    /**
     * Adds a selection callback. Currently restricted to the "select" and "deselect" event.
     *
     * @param event    must be "select" or "deselect"
     * @param callback the select callback
     */
    public native Api<T> on(String event, SelectCallback callback);

    public native Api<T> off(String event);

    /**
     * Select all rows, but apply the specified modifier (e.g. to return only selected rows). Chain the {@link #data()}
     * to get the actual data.
     */
    public native Api<T> rows(SelectorModifier selectorModifier);

    /**
     * Select rows by tr element. Chain the {@link #data()} to get the actual data.
     */
    public native Api<T> rows(Element tr);

    /**
     * Select rows by using a function. Chain the {@link #data()} to get the actual data.
     */
    public native Api<T> rows(RowSelection<T> selection);

    /**
     * Selects the row(s) that have been found by the {@link #rows(RowSelection)}, {@link #rows(Element)} or {@link
     * #rows(SelectorModifier)} selector methods.
     */
    public native Api<T> select();

    public native JsArrayOf<T> toArray();


    // ------------------------------------------------------ overlay methods

    @JsOverlay
    public final Api<T> refresh(RefreshMode mode) {
        Api<T> api = draw(mode.mode());
        Options<T> options = api.init();
        ColumnActions<T> columnActions = options.columnActions;
        if (columnActions != null && !columnActions.isEmpty()) {
            Element table = Browser.getDocument().getElementById(options.id);
            if (table != null) {
                Elements.stream(table.querySelectorAll("." + columnAction)).forEach(link -> {
                    ColumnAction<T> columnAction = columnActions.get(link.getId());
                    if (columnAction != null) {
                        link.setOnclick(event -> {
                            event.stopPropagation();
                            Element e = link; // find enclosing tr
                            while (e != null && e != Browser.getDocument() && !"TR".equals(e.getTagName())) { //NON-NLS
                                e = e.getParentElement();
                            }
                            if (e != null) {
                                JsArrayOf<T> array = rows(e).data().toArray();
                                if (!array.isEmpty()) {
                                    columnAction.action(array.get(0));
                                }
                            }
                        });
                    }
                });
            }
        }
        return api;
    }

    @JsOverlay
    public final T selectedRow() {
        List<T> rows = selectedRows();
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    @JsOverlay
    public final List<T> selectedRows() {
        SelectorModifier selectorModifier = new SelectorModifierBuilder().selected().build();
        JsArrayOf<T> selection = rows(selectorModifier).data().toArray();
        if (selection == null || selection.isEmpty()) {
            return Collections.emptyList();
        }
        return asList(selection);
    }
}