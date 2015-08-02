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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.LazyElement;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.LayoutSpec;
import org.jboss.hal.resources.HalConstants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.State.*;

/**
 * An abstract form which implements a subset or all of the states described in {@link Form}.
 * <p>
 * Please note that all form items and help texts must be added to this form before it is added {@linkplain #asElement()
 * as element} to the DOM.
 *
 * @author Harald Pehl
 */
public abstract class AbstractForm<T> extends LazyElement implements Form<T>, LayoutSpec {

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final String id;
    private final EnumSet<State> supportedStates;
    private final LinkedHashMap<State, Element> panels;
    private final LinkedHashMap<String, FormItem> formItems;
    private final LinkedHashMap<String, String> helpTexts;
    private final List<FormValidation> formValidations;

    private T model;
    private State state;
    private State lastState;

    private FormLinks formLinks;
    private DivElement errorPanel;
    private SpanElement errorMessage;
    private EventListener exitEditWithEsc;

    // accessible in subclasses
    protected SaveCallback<T> saveCallback;
    protected UndefineCallback<T> undefineCallback;
    protected CancelCallback<T> cancelCallback;


    // ------------------------------------------------------ initialization

    protected AbstractForm(final String id, final State firstSupportedState, final State... otherSupportedStates) {

        EnumSet<State> supportedStates = EnumSet.of(firstSupportedState, otherSupportedStates);
        validateStates(supportedStates);

        this.id = id;
        this.supportedStates = supportedStates;
        this.panels = new LinkedHashMap<>();
        this.formItems = new LinkedHashMap<>();
        this.helpTexts = new LinkedHashMap<>();
        this.formValidations = new ArrayList<>();
    }

    private void validateStates(final EnumSet<State> supportedStates) {
        if (supportedStates.contains(EMPTY)) {
            // EMPTY requires all three states!
            if (!supportedStates.contains(VIEW) && !supportedStates.contains(EDIT)) {
                throw new IllegalStateException(
                        "Illegal state combination: " + EMPTY + " without " + VIEW + " and " + EDIT);
            }
        }
    }

    protected void addFormItem(FormItem formItem, FormItem... formItems) {
        for (FormItem item : Lists.asList(formItem, formItems)) {
            this.formItems.put(item.getName(), item);
        }
    }

    protected void addHelp(String label, String description) {
        helpTexts.put(label, description);
    }

    public void addFormValidation(FormValidation formValidation) {
        formValidations.add(formValidation);
    }


    // ------------------------------------------------------ ui setup

    @Override
    protected Element lazyElement() {

        Element section = Browser.getDocument().createElement("section");
        section.setId(id);

        formLinks = new FormLinks(id, supportedStates, helpTexts,
                event -> add(),
                event -> edit(getModel()),
                event -> undefine());
        section.appendChild(formLinks.asElement());

        if (supports(EMPTY)) {
            panels.put(EMPTY, emptyPanel());
        }
        if (supports(VIEW)) {
            panels.put(VIEW, viewPanel());
        }
        if (supports(EDIT)) {
            panels.put(EDIT, editPanel());
        }
        for (Element element : panels.values()) {
            section.appendChild(element);
        }

        if (supports(EDIT) && (supports(VIEW) || supports(EMPTY))) {
            exitEditWithEsc = event -> {
                if (event instanceof KeyboardEvent) {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) event;
                    if (keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.ESC &&
                            getState() == EDIT &&
                            panels.get(EDIT) != null &&
                            Elements.isVisible(panels.get(EDIT))) {
                        keyboardEvent.preventDefault();
                        cancel();
                    }
                }
            };
        }

        if (supports(EMPTY)) {
            switchToEmptyState();
            changeState(EMPTY);
        } else if (supports(VIEW)) {
            switchToViewState();
            changeState(VIEW);
        } else {
            switchToEditState();
            changeState(EDIT);
        }
        return section;
    }

    protected Element emptyPanel() {
        return new Elements.Builder()
                .div().id(Id.generate(id, "empty")).css("form form-horizontal")
                .p().innerText("The model is undefined. Click 'Add' to create a new model.").end()
                .end().build();
    }

    protected Element viewPanel() {
        return new Elements.Builder()
                .div().id(Id.generate(id, "view")).css("form form-horizontal")
                .p().innerText("View panel not yet implemented.").end()
                .end().build();
    }

    protected Element editPanel() {
        // @formatter:off
        Elements.Builder errorPanelBuilder = new Elements.Builder()
            .div().css("alert alert-danger").rememberAs("errorPanel")
                .span().css("pficon-layered")
                    .span().css("pficon pficon-error-octagon").end()
                    .span().css("pficon pficon-error-exclamation").end()
                .end()
                .span().rememberAs("errorMessage").end()
            .end();
        // @formatter:on
        errorMessage = errorPanelBuilder.referenceFor("errorMessage");
        errorPanel = errorPanelBuilder.build();
        clearErrors();

        Element editPanel = new Elements.Builder()
                .div().id(Id.generate(id, "edit")).css("form form-horizontal").end()
                .build();
        editPanel.appendChild(errorPanel);
        for (FormItem formItem : formItems.values()) {
            editPanel.appendChild(formItem.asElement());
        }

        // @formatter:off
        Element buttons = new Elements.Builder()
            .div().css("form-group edit-buttons")
                .div().css("col-" + COLUMN_DISCRIMINATOR + "-offset-" + LABEL_COLUMNS + " col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS)
                    .div().css("pull-right form-buttons")
                        .button().css("btn btn-form btn-default").on(click, event -> cancel())
                            .innerText(CONSTANTS.cancel())
                        .end()
                        .button().css("btn btn-form btn-primary").on(click, event -> save())
                            .innerText(CONSTANTS.save())
                        .end()
                    .end()
                .end()
            .end()
        .build();
        // @formatter:on
        editPanel.appendChild(buttons);

        return editPanel;
    }


    // ------------------------------------------------------ add

    /**
     * Implements the transition from {@link State#EMPTY EMPTY} &rarr; {@link State#EDIT EDIT}.
     * <ol>
     * <li>Create a new model: {@code this.model = newModel()}</li>
     * <li>Prepare edit state: {@link #prepareEditState()}</li>
     * <li>Switch to edit state: {@link #switchToEditState()}</li>
     * <li>Change the state to {@link State#EDIT}</li>
     * </ol>
     * Please make sure to fulfill this contract in case you override this method.
     */
    @Override
    public void add() {
        assertState(EMPTY);

        this.model = newModel();
        prepareEditState();
        switchToEditState();
        changeState(EDIT);
    }


    // ------------------------------------------------------ view

    /**
     * Implements the transition from {@link State#EMPTY EMPTY} &rarr; {@link State#VIEW VIEW}.
     * <ol>
     * <li>Store the model for later use: {@code this.model = model}</li>
     * <li>Prepare view state: {@link #prepareViewState()}</li>
     * <li>Switch to view state: {@link #switchToViewState()}</li>
     * <li>Change the state to {@link State#VIEW}</li>
     * </ol>
     * Please make sure to fulfill this contract in case you override this method.
     *
     * @param model the model to view
     */
    @Override
    public void view(final T model) {
        assertState(EMPTY, VIEW);

        if (model == null) {
            if (supportsOnly(VIEW)) {
                // Model can be null if this is a view only form.
                // In this case just create a new one
                this.model = newModel();
            } else {
                // Not ok for (add-)view-edit forms
                throw new NullPointerException("Model must not be null in view(T)");
            }
        }

        this.model = model;
        prepareViewState();
        switchToViewState();
        changeState(VIEW);
    }

    /**
     * Gives subclasses a way to prepare the view state. Using {@link #model} is safe in this method.
     */
    protected void prepareViewState() {}

    /**
     * Flips the deck panel so that the view panel is visible
     */
    protected void switchToViewState() {
        if (exitEditWithEsc != null) {
            Browser.getDocument().removeEventListener("keyup", exitEditWithEsc);
        }
        switchTo(VIEW);
    }


    // ------------------------------------------------------ edit

    /**
     * Implements the transition from [{@link State#EMPTY EMPTY}, {@link State#VIEW VIEW}] &rarr; {@link
     * State#EDIT EDIT}.
     * <ol>
     * <li>Store the model for later use: {@code this.model = model}</li>
     * <li>Prepare edit state: {@link #prepareEditState()}</li>
     * <li>Switch to edit state: {@link #switchToEditState()}</li>
     * <li>Change the state to {@link State#EDIT}</li>
     * </ol>
     * Please make sure to fulfill this contract in case you override this method.
     *
     * @param model the model to edit
     */
    @Override
    public void edit(final T model) {
        assertState(EMPTY, VIEW);

        if (model == null) {
            if (supportsOnly(EDIT)) {
                // Model can be null if this is a edit only form.
                // In this case just create a new one
                this.model = newModel();
            } else {
                // Not ok for (add-)view-edit forms
                throw new NullPointerException("Model must not be null in edit(T)");
            }
        }

        this.model = model;
        prepareEditState();
        switchToEditState();
        changeState(EDIT);
    }

    /**
     * Gives subclasses a way to prepare the edit state. Using {@link #model} is safe in this method.
     */
    protected void prepareEditState() {}

    /**
     * Flips the deck panel so that the edit panel is visible
     */
    protected void switchToEditState() {
        switchTo(EDIT);

        if (!formItems.isEmpty()) {
            formItems.values().iterator().next().setFocus(true);
        }

        if (exitEditWithEsc != null) {
            // Exit *this* edit state by pressing ESC
            Browser.getDocument().setOnkeyup(exitEditWithEsc);
        }
    }


    // ------------------------------------------------------ save, cancel

    /**
     * Implements the transition from {@link State#EDIT EDIT} &rarr; {@link State#VIEW VIEW} in case the user
     * presses 'save'.
     * <ol>
     * <li>Validate the form and input fields: {@link #validate()}</li>
     * <li>Update the model with the changed values: {@link #updateModel(Map)}</li>
     * <li>Prepare view state: {@link #prepareViewState()}</li>
     * <li>Switch to view state: {@link #switchToViewState()}</li>
     * <li>Call registered save callbacks: {@link SaveCallback#onSave(Object,
     * Map)}</li>
     * <li>Change the state to {@link State#VIEW}</li>
     * </ol>
     * Please make sure to stick with this contract in case you override this method.
     */
    @Override
    public void save() {
        assertState(EDIT);

        boolean valid = validate();
        if (valid) {
            updateModel(getChangedValues());
            if (saveCallback != null) {
                saveCallback.onSave(getModel(), getChangedValues());
            }
            prepareViewState();
            switchToViewState();
            changeState(VIEW);
        }
    }

    /**
     * This method is called when the user saves the model. Subclasses need to update the model with the specified
     * changed values.
     *
     * @param changedValues the changed values
     */
    protected abstract void updateModel(final Map<String, Object> changedValues);

    /**
     * Implements the transition from {@link State#EDIT EDIT} &rarr; {@link State#VIEW VIEW} in case the user
     * presses 'cancel'.
     * <ol>
     * <li>Clear errors: {@link #clearErrors()}</li>
     * <li>Call registered cancel callbacks: {@link CancelCallback#onCancel(Object)}</li>
     * <li>Depending whether the last state was {@link State#EMPTY EMPTY} or {@link State#VIEW VIEW}</li>
     * <ol>
     * <li>Prepare the empty or view state</li>
     * <li>Switch to empty or view state</li>
     * <li>Change the state to {@link State#EMPTY} or {@link State#VIEW}</li>
     * </ol>
     * </ol>
     * Please make sure to fulfill this contract in case you override this method.
     */
    @Override
    public void cancel() {
        assertState(EDIT);

        clearErrors();
        if (cancelCallback != null) {
            cancelCallback.onCancel(getModel());
        }
        if (lastState == EMPTY) {
            prepareEmptyState();
            switchToEmptyState();
            changeState(EMPTY);
        } else {
            prepareViewState();
            switchToViewState();
            changeState(VIEW);
        }
    }


    // ------------------------------------------------------ undefine

    /**
     * Implements the transition from [{@link State#VIEW VIEW}, {@link State#EDIT EDIT}] &rarr; {@link
     * State#EMPTY EMPTY}.
     * <ol>
     * <li>Check whether this form can 'undefine' the model</li>
     * <li>Undefine the model: {@link #undefineModel()}</li>
     * <li>Call registered undefine callbacks: {@link org.jboss.hal.ballroom.form.Form.UndefineCallback#onUndefine(Object)}</li>
     * <li>Prepare the empty state: {@link #prepareEmptyState()}</li>
     * <li>Switch to view state: {@link #switchToEmptyState()}</li>
     * <li>Change the state to {@link State#EMPTY}</li>
     * </ol>
     * Please make sure to stick with this contract in case you override this method.
     */
    @Override
    public void undefine() {
        if (undefinePossible()) {
            assertState(VIEW, EDIT);

            undefineModel();
            if (undefineCallback != null) {
                undefineCallback.onUndefine(getModel());
            }
            prepareEmptyState();
            switchToEmptyState();
            changeState(EMPTY);
        }
    }

    /**
     * This method is called when the user undefined the model.
     */
    protected abstract void undefineModel();

    /**
     * Checks whether this form supports the transition to state {@link State#EMPTY}.
     *
     * @return {@code false} if one of the form fields is required, {@code true} otherwise.
     */
    protected boolean undefinePossible() {
        for (FormItem formItem : formItems.values()) {
            if (formItem.isRequired()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gives subclasses a way to prepare the empty state.
     */
    protected void prepareEmptyState() {}

    /**
     * Flips the deck panel so that the empty panel is visible
     */
    protected void switchToEmptyState() {
        if (exitEditWithEsc != null) {
            Browser.getDocument().removeEventListener("keyup", exitEditWithEsc);
        }
        switchTo(EMPTY);
    }


    // ------------------------------------------------------ state transition

    private void switchTo(State state) {
        if (supports(state)) {
            formLinks.switchTo(state);
            for (Element panel : panels.values()) {
                Elements.setVisible(panel, false);
            }
            Elements.setVisible(panels.get(state), true);
        }
    }

    private void changeState(State state) {
        this.lastState = this.state;
        this.state = state;
    }


    // ------------------------------------------------------ properties

    @Override
    public T getModel() {
        return model;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public State getState() {
        return state;
    }

    public boolean modified() {
        for (FormItem formItem : formItems.values()) {
            if (formItem.isModified()) {
                return true;
            }
        }
        return false;
    }


    // ------------------------------------------------------ validation

    @Override
    public void invalidate(final String message) {
        if (state == EDIT) {
            showErrors(singletonList(message));
        }
    }

    @Override
    public void invalidate(final String field, final String message) {
        if (state == EDIT) {
            FormItem formItem = formItems.get(field);
            if (formItem != null) {
                formItem.showError(message);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean validate() {
        boolean valid = true;

        // validate form items
        for (FormItem formItem : formItems.values()) {
            if (!formItem.validate()) {
                valid = false;
            }
        }

        // validate form on its own
        List<String> messages = new ArrayList<>();
        for (FormValidation validationHandler : formValidations) {
            ValidationResult validationResult = validationHandler.validate(formItems.values());
            if (!validationResult.isValid()) {
                messages.add(validationResult.getMessage());
            }
        }
        if (!messages.isEmpty()) {
            valid = false;
            showErrors(messages);
        }
        if (valid) {
            clearErrors();
        }

        return valid;
    }

    @Override
    public void clearErrors() {
        for (FormItem formItem : formItems.values()) {
            formItem.clearError();
        }
        errorMessage.setInnerText("");
        Elements.setVisible(errorPanel, false);
    }

    @Override
    public void clearError(final String formItem) {
        for (FormItem item : formItems.values()) {
            if (item.getName().equals(formItem)) {
                item.clearError();
            }
        }
    }

    protected void showErrors(List<String> messages) {
        errorMessage.setInnerText(Joiner.on(", ").join(messages));
        Elements.setVisible(errorPanel, true);
    }

    protected void reset() {
        if (state == VIEW) {
            for (FormItem formItem : formItems.values()) {
                formItem.clearValue();
                formItem.resetMetaData();
            }
        }
    }


    // ------------------------------------------------------ helper methods

    private boolean supports(State state) {
        return supportedStates.contains(state);
    }

    private boolean supportsOnly(State state) {
        return supportedStates.size() == 1 && supports(state);
    }

    private void assertState(State... state) {
        for (State st : state) {
            if (this.state == st) {
                return;
            }
        }
        if (state.length == 1) {
            throw new IllegalStateException("Illegal form state: Expected " + state[0] + ", but got " + this.state);
        } else {
            throw new IllegalStateException(
                    "Illegal form state: Expected one of [" + Joiner.on(", ").join(state) + "], but got " + this.state);
        }
    }
}
