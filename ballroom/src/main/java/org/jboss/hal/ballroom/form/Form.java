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
package org.jboss.hal.ballroom.form;

import java.util.Map;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;

/**
 * A form using well defined states and operations.
 *
 * @param <T> The model for this form
 *
 * @author Harald Pehl
 */
public interface Form<T> extends IsElement, Attachable {

    enum State {
        READONLY, EDITING
    }


    enum Operation {
        ADD, VIEW, CLEAR, RESET, EDIT, SAVE, CANCEL
    }


    @FunctionalInterface
    interface ResetCallback<T> {

        void onReset(Form<T> form);
    }


    @FunctionalInterface
    interface SaveCallback<T> {

        void onSave(Form<T> form, Map<String, Object> changedValues);
    }


    @FunctionalInterface
    interface CancelCallback<T> {

        void onCancel(Form<T> form);
    }


    /**
     * Takes a new transient model and enters the editing state.
     *
     * @param model the transient model
     */
    void add(T model);

    /**
     * Takes the specified model and updates the read-only state with the values from the model.
     *
     * @param model the model to view.
     */
    void view(T model);

    /**
     * Clears this form by removing the model reference and by clearing all form fields.
     */
    void clear();

    /**
     * Resets the model.
     */
    void reset();

    void setResetCallback(ResetCallback<T> resetCallback);

    /**
     * Takes the specified model and populates the editing state with the values from the model.
     *
     * @param model the model to edit.
     */
    void edit(T model);

    /**
     * Validates the form and its fields and upon successful validation persists the changes to the model and
     * calls the save callback.
     */
    boolean save();

    void setSaveCallback(SaveCallback<T> saveCallback);

    /**
     * Cancels any modifications to the model.
     */
    void cancel();

    void setCancelCallback(CancelCallback<T> cancelCallback);

    /**
     * @return an unique identifier for this form.
     */
    String getId();

    /**
     * @return the current model.
     */
    T getModel();

    /**
     * @return the state machine which access to the current state.
     */
    StateMachine getStateMachine();

    @SuppressWarnings("unchecked")
    <F> FormItem<F> getFormItem(String name);

    Iterable<FormItem> getFormItems();

    Iterable<FormItem> getBoundFormItems();
}
