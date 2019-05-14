/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;

@JsType(namespace = "hal.ui")
public interface Table<T> extends IsElement, Attachable {

    @JsIgnore
    void show();

    @JsIgnore
    void hide();

    @JsIgnore
    void enableButton(int index, boolean enable);

    void bindForm(Form<T> form);

    @JsIgnore
    void bindForms(Iterable<Form<T>> forms);

    void clear();

    @JsIgnore
    List<T> getRows();

    void onSelectionChange(SelectionChangeHandler<T> handler);

    @JsIgnore
    default boolean hasSelection() {
        return !selectedRows().isEmpty();
    }

    @JsProperty(name = "selectedRow")
    T selectedRow();

    @JsIgnore
    List<T> selectedRows();

    @JsIgnore
    void select(T data);

    @JsIgnore
    void select(T data, Function<T, String> identifier);

    @JsIgnore
    void update(Iterable<T> data);

    @JsIgnore
    void update(Iterable<T> data, RefreshMode mode);

    @JsIgnore
    void update(Iterable<T> data, Function<T, String> identifier);

    @JsIgnore
    void update(Iterable<T> data, RefreshMode mode, Function<T, String> identifier);


    /**
     * Convenience handler when a <em>row</em> selection <em>or</em> deselection takes place.
     *
     * @param <T> the row type
     */
    @JsFunction
    @FunctionalInterface
    interface SelectionChangeHandler<T> {

        /**
         * Called when a selection changed. That is when a row is selected <em>or</em> deselected.
         *
         * @param table the table instance
         */
        void onSelectionChanged(Table<T> table);
    }
}