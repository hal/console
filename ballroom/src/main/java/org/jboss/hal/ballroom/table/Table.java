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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;

/**
 * @author Harald Pehl
 */
@JsType(namespace = "ui")
public interface Table<T> extends IsElement, Attachable {

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

    @JsIgnore
    void show();

    @JsIgnore
    void hide();

    void enableButton(final int index, final boolean enable);

    void bindForm(final Form<T> form);

    @JsIgnore
    void bindForms(final Iterable<Form<T>> forms);

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
    void select(final T data);

    @JsIgnore
    void select(final T data, final Function<T, String> identifier);

    @JsIgnore
    void update(final Iterable<T> data);

    @JsIgnore
    void update(final Iterable<T> data, final RefreshMode mode);

    @JsIgnore
    void update(final Iterable<T> data, final Function<T, String> identifier);

    @JsIgnore
    void update(final Iterable<T> data, final RefreshMode mode, final Function<T, String> identifier);
}