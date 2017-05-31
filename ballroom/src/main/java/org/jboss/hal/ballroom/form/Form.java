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

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.spi.Callback;

/**
 * A form bound to a model using well defined states and operations. The form contains a list of form items which are
 * used to view and modify the attributes of the model. Form items can be bound or unbound. Bound form items show the
 * attributes of the model (text input, check boxes or select boxes), whereas unbound form items have no relation to
 * the model (static text or buttons).
 *
 * @param <T> The model for this form
 *
 * @author Harald Pehl
 */
@JsType(namespace = "hal.ui")
public interface Form<T> extends IsElement, Attachable {

    // ------------------------------------------------------ states and operations


    enum State {
        /**
         * No model is bound to the form
         */
        EMPTY,

        /**
         * The model is shown in read-only mode
         */
        READONLY,

        /**
         * The model is shown in edit mode
         */
        EDITING
    }


    enum Operation {
        /**
         * Takes the specified model and updates the read-only state with the values from the model.
         */
        VIEW,

        /**
         * Clears this form by removing the model reference and by clearing all bound form fields. Does not modify the
         * model!
         */
        CLEAR,

        /**
         * Resets the model and updates the bound form field.
         */
        RESET,

        /**
         * Takes the specified model and populates the bound form fields with the values from the model.
         */
        EDIT,

        /**
         * Validates the form and its fields and upon successful validation persists the changes to the model and
         * calls the save callback.
         */
        SAVE,

        /**
         * Cancels any modifications to the model and calls the cancel callback.
         */
        CANCEL,

        /**
         * Removes the model and calls the remove callback.
         */
        REMOVE
    }


    // ------------------------------------------------------ callbacks


    @FunctionalInterface
    interface SaveCallback<T> {

        void onSave(Form<T> form, Map<String, Object> changedValues);
    }


    @FunctionalInterface
    interface CancelCallback<T> {

        void onCancel(Form<T> form);
    }


    /**
     * Callback to prepare the reset operation. Use this callback if the reset operation is behind some kind of
     * confirmation dialog. If the user confirms to reset, it's expected that you use an implementation of {@link
     * FinishReset} to conclude the reset operation.
     */
    @FunctionalInterface
    interface PrepareReset<T> {

        void beforeReset(Form<T> form);
    }


    /**
     * Callback to be used after the reset operation has been successfully executed. This callback takes care of calling
     * {@link Form#reset()}. You just need to place your business logic into {@link #afterReset(Form)}.
     */
    abstract class FinishReset<T> implements Callback {

        private final Form<T> form;

        protected FinishReset(final Form<T> form) {this.form = form;}

        @Override
        public void execute() {
            form.reset();
            afterReset(form);
        }

        public abstract void afterReset(Form<T> form);
    }


    /**
     * Callback to prepare the remove operation. Use this callback if the remove operation is behind some kind of
     * confirmation dialog. If the user confirms to remove, it's expected that you use an implementation of {@link
     * FinishRemove} to conclude the remove operation.
     */
    @FunctionalInterface
    interface PrepareRemove<T> {

        void beforeRemove(Form<T> form);
    }


    /**
     * Callback to be used after the remove operation has been successfully executed. This callback takes care of
     * calling {@link Form#remove()}. You just need to place your business logic into {@link #afterRemove(Form)}.
     */
    abstract class FinishRemove<T> implements Callback {

        private final Form<T> form;

        protected FinishRemove(final Form<T> form) {this.form = form;}

        @Override
        public void execute() {
            form.remove();
            afterRemove(form);
        }

        public abstract void afterRemove(Form<T> form);
    }


    // ------------------------------------------------------ form API

    @JsProperty
    boolean isUndefined();

    @JsProperty
    boolean isTransient();

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

    @JsIgnore
    void setSaveCallback(SaveCallback<T> saveCallback);

    /**
     * Cancels any modifications to the model.
     */
    void cancel();

    @JsIgnore
    void setCancelCallback(CancelCallback<T> cancelCallback);

    @JsIgnore
    void setPrepareReset(PrepareReset<T> prepareReset);

    /**
     * Resets the model.
     */
    @JsIgnore
    void reset();

    @JsIgnore
    void setPrepareRemove(PrepareRemove<T> removeCallback);

    /**
     * Removes the model.
     */
    @JsIgnore
    void remove();

    /**
     * @return an unique identifier for this form.
     */
    @JsIgnore
    String getId();

    /**
     * @return the current model.
     */
    @JsProperty
    T getModel();

    /**
     * @return the state machine which access to the current state.
     */
    @JsIgnore
    StateMachine getStateMachine();

    @JsIgnore
    <F> FormItem<F> getFormItem(String name);

    /**
     * @return return all form items.
     */
    @JsIgnore
    Iterable<FormItem> getFormItems();

    /**
     * @return only those form items which are bound to the model.
     */
    @JsIgnore
    Iterable<FormItem> getBoundFormItems();

    @JsIgnore
    Map<String, Object> getUpdatedModel();


        /**
         * Makes it possible to validate the form as a whole or to check fields which depend on other fields.
         */
    @JsIgnore
    void addFormValidation(FormValidation<T> formValidation);
}
