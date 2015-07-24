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
 * A form able to modify a specific model using a well defined state machine.
 *
 * @author Harald Pehl
 */
public interface Form<T> extends IsElement {

    /**
     * Defines the valid states for a form.
     * <pre>
     *         +---------+
     *         |         |
     *   +-----+  EMPTY  <-----+-----+
     *   |     |         |     |     |
     *   |     +----+----+     |     |
     *   |          |         undefine()
     *   |        view()  (no required attributes!)
     *   |          |          |     |
     *   |     +----v----+     |     |
     *   |     |         +-----+     |
     * add()   |  VIEW   |           |
     *   |     |         <-----+     |
     *   |     +-+-----^-+     |     |
     *   |       |     |       |     |
     *   |    edit() cancel() save() |
     *   |       |     |       |     |
     *   |     +-v-----+-+     |     |
     *   |     |         |     |     |
     *   +----->  EDIT   +-----+-----+
     *         |         |
     *         +---------+
     * </pre>
     */
    enum State {
        EMPTY, VIEW, EDIT
    }


    @FunctionalInterface
    interface SaveCallback<T> {

        void onSave(T model, Map<String, Object> changedValues);
    }


    @FunctionalInterface
    interface UndefineCallback<T> {

        void onUndefine(T model);
    }


    @FunctionalInterface
    interface CancelCallback<T> {

        void onCancel(T model);
    }


    /**
     * Creates a new transient model and switches to edit state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code state == EMPTY}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == EDIT}</dd>
     * </dl>
     */
    void add();

    /**
     * Takes the specified model, updates the form with the values from the model and switches to view state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code state == EMPTY}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == VIEW}</dd>
     * </dl>
     *
     * @param model the model to view.
     */
    void view(T model);

    /**
     * Takes the specified model, populate the form fields with the values from the model and switches to edit state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code state == EMPTY || VIEW}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == EDIT}</dd>
     * </dl>
     *
     * @param model the model to edit.
     */
    void edit(T model);

    /**
     * Validates the form and the form fields, updates the model with changed values from the form fields and switches
     * to view state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code state == EDIT}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == VIEW}</dd>
     * </dl>
     */
    void save();

    /**
     * Cancels any modifications to the model and switches to view state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code state == EDIT}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == VIEW}</dd>
     * </dl>
     */
    void cancel();

    /**
     * Undefines the model and switches to empty state.
     * <dl>
     * <dt>Precondition</dt>
     * <dd>{@code (state == VIEW || EDIT) && (fields.noneMatch(field -&gt; field.isRequired()))}</dd>
     * <dt>Postcondition</dt>
     * <dd>{@code state == EMPTY}</dd>
     * </dl>
     */
    void undefine();

    /**
     * Creates a new model.
     *
     * @return a new model ready to be used in edit state.
     */
    T newModel();

    /**
     * @return the changed values made during edit state.
     */
    Map<String, Object> getChangedValues();

    /**
     * @return the current model.
     */
    T getModel();

    /**
     * @return an unique identifier for this form.
     */
    String getId();

    /**
     * @return the current state.
     */
    State getState();

    /**
     * Invalidates this form.
     *
     * @param message an error message
     */
    void invalidate(String message);

    /**
     * Invalidates the specified form item.
     *
     * @param message an error message
     */
    void invalidate(String formItem, String message);

    /**
     * Clears all error markers.
     */
    void clearErrors();

    /**
     * Clears the error marker for the specified form item
     *
     * @param formItem the form item
     */
    void clearError(String formItem);
}
