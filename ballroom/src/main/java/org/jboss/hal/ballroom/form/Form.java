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
package org.jboss.hal.ballroom.form;

import org.jboss.gwt.elemento.core.IsElement;

import java.util.Map;

/**
 * A form using well defined states and operations.
 *
 * @author Harald Pehl
 */
public interface Form<T> extends IsElement {

    enum State {
        READONLY, EDITING
    }


    enum Operation {
        VIEW, ADD, EDIT, CANCEL, SAVE, RESET
    }


    @FunctionalInterface
    interface SaveCallback<T> {

        void onSave(Form<T> form, Map<String, Object> changedValues);
    }


    @FunctionalInterface
    interface ResetCallback<T> {

        void onReset(Form<T> form);
    }


    @FunctionalInterface
    interface CancelCallback<T> {

        void onCancel(Form<T> form);
    }


    /**
     * Takes the specified model and updates the read-only state with the values from the model.
     *
     * @param model the model to view.
     */
    void view(T model);

    /**
     * Takes the transient model and enters the editing state.
     *
     * @param model the transient model
     */
    void add(T model);

    /**
     * Takes the specified model and populates the editing state with the values from the model.
     *
     * @param model the model to edit.
     */
    void edit(T model);

    /**
     * Validates the form and its fields and calls the save callback with the changed values.
     */
    void save();

    void setSaveCallback(SaveCallback<T> saveCallback);

    /**
     * Cancels any modifications to the model.
     */
    void cancel();

    void setCancelCallback(CancelCallback<T> cancelCallback);

    /**
     * Resets the model.
     */
    void reset();

    void setResetCallback(ResetCallback<T> resetCallback);

    /**
     * @return an unique identifier for this form.
     */
    String getId();

    /**
     * @return the current model.
     */
    T getModel();

    @SuppressWarnings("unchecked")
    <I> FormItem<I> getFormItem(String name);

    Iterable<FormItem> getFormItems();
}
