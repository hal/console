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
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import org.jboss.gwt.waiwai.Elements;
import org.jboss.hal.resources.HalConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.jboss.gwt.waiwai.Elements.EventType.click;
import static org.jboss.hal.ballroom.form.Form.State.*;

/**
 * @author Harald Pehl
 */
public abstract class AbstractForm<T> implements Form<T>, FormLayout {

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final String id;
    private final boolean supportsUndefine;
    private final boolean supportsHelp;
    private final Map<State, Integer> stateDeck;

    private T model;
    private State state;
    private EventListener exitEditWithEsc;

    private FormLinks formLinks;
    private DeckPanel deck;
    private DivElement errorPanel;
    private SpanElement errorMessage;

    // accessible in subclasses
    protected final LinkedHashMap<String, FormItem> formItems;
    protected final List<FormValidation> validationHandlers;
    protected SaveCallback<T> saveCallback;
    protected UndefineCallback<T> undefineCallback;
    protected CancelCallback<T> cancelCallback;


    // ------------------------------------------------------ initialization / ui setup

    protected AbstractForm(final String id, final boolean supportsUndefine, final boolean supportsHelp) {

        this.id = id;
        this.supportsUndefine = supportsUndefine;
        this.supportsHelp = supportsHelp;

        this.state = EMPTY;
        this.stateDeck = ImmutableMap.of(EMPTY, 0, VIEW, 1, EDIT, 2);
        this.exitEditWithEsc = event -> {
            if (event instanceof KeyboardEvent) {
                KeyboardEvent keyboardEvent = (KeyboardEvent) event;
                if (keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.ESC && getState() == EDIT && deck.getWidget(1)
                        .isVisible()) {
                    keyboardEvent.preventDefault();
                    cancel();
                }
            }
        };

        this.formItems = new LinkedHashMap<>();
        this.validationHandlers = new ArrayList<>();
    }

    @Override
    public Element asElement() {
        formLinks = new FormLinks.Builder(id, supportsUndefine, supportsHelp)
                .onAdd(event -> add())
                .onEdit(event -> edit(getModel()))
                .onUndefine(event -> undefine())
                .build();

        deck = new DeckPanel();
        deck.add(emptyPanel());
        deck.add(viewPanel());
        deck.add(editPanel());
        switchTo(EMPTY);

        DivElement root = Browser.getDocument().createDivElement();
        root.appendChild(formLinks.asElement());
        root.appendChild(Elements.asElement(deck));
        return root;
    }

    protected Widget emptyPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("form form-horizontal");
        panel.add(new HTMLPanel("<p>The model is undefined. Click 'Add' to create a new model.</p>"));
        return panel;
    }

    protected Widget viewPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("form form-horizontal");
        panel.add(new HTMLPanel("<p>View panel not yet implemented.</p>"));
        return panel;
    }

    protected Widget editPanel() {
        FlowPanel panel = new FlowPanel("form");
        panel.addStyleName("form form-horizontal");

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css("alert alert-danger")
                .span().css("pficon-layered")
                    .span().css("pficon pficon-error-octagon").end()
                    .span().css("pficon pficon-error-exclamation").end()
                .end()
                .span().rememberAs("errorMessage").end()
            .end();
        // @formatter:on
        errorMessage = builder.referenceFor("errorMessage");
        errorPanel = builder.build();
        clearErrors();

        for (FormItem formItem : formItems.values()) {
            Elements.asElement(panel).appendChild(formItem.asElement());
        }

        Elements.asElement(panel).appendChild(buttons());
        return panel;
    }

    private Element buttons() {
        // @formatter:off
        return new Elements.Builder()
            .div().css("col-" + COLUMN_DISCRIMINATOR + "-offset-" + LABEL_COLUMNS + " col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS)
                .div().css("pull-right form-buttons")
                    .button().css("btn btn-default").on(click, event -> cancel())
                        .innerText(CONSTANTS.cancel())
                    .end()
                    .button().css("btn btn-primary").on(click, event -> save())
                        .innerText(CONSTANTS.save())
                    .end()
                .end()
            .end()
            .build();
        // @formatter:on
    }

    private void switchTo(State state) {
        formLinks.switchTo(state);
        deck.showWidget(stateDeck.get(state));
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
        state = EDIT;
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
        if (model == null) { throw new NullPointerException("Model must not be null in view(T)"); }
        assertState(EMPTY);

        this.model = model;
        prepareViewState();
        switchToViewState();
        state = VIEW;
    }

    /**
     * Gives subclasses a way to prepare the view state. Using {@link #model} is safe in this method.
     */
    protected abstract void prepareViewState();

    /**
     * Flips the deck panel so that the view panel is visible
     */
    protected void switchToViewState() {
        Browser.getDocument().removeEventListener("keyup", exitEditWithEsc);
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
        if (model == null) { throw new NullPointerException("Model must not be null in edit(T)"); }
        assertState(EMPTY, VIEW);

        this.model = model;
        prepareEditState();
        switchToEditState();
        state = EDIT;
    }

    /**
     * Gives subclasses a way to prepare the edit state. Using {@link #model} is safe in this method.
     */
    protected abstract void prepareEditState();

    /**
     * Flips the deck panel so that the edit panel is visible
     */
    protected void switchToEditState() {
        switchTo(EDIT);

        if (!formItems.isEmpty()) {
            formItems.values().iterator().next().setFocus(true);
        }

        // Exit *this* edit state by pressing ESC
        Browser.getDocument().setOnkeyup(exitEditWithEsc);
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
            prepareViewState();
            switchToViewState();
            if (saveCallback != null) {
                saveCallback.onSave(getModel(), getChangedValues());
            }
            state = VIEW;
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
     * <li>Prepare the view state: {@link #prepareViewState()}</li>
     * <li>Switch to view state: {@link #switchToViewState()}</li>
     * <li>Call registered cancel callbacks: {@link CancelCallback#onCancel(Object)}</li>
     * <li>Change the state to {@link State#VIEW}</li>
     * </ol>
     * Please make sure to fulfill this contract in case you override this method.
     */
    @Override
    public void cancel() {
        assertState(EDIT);

        clearErrors();
        prepareViewState();
        switchToViewState();
        if (cancelCallback != null) {
            cancelCallback.onCancel(getModel());
        }
        state = VIEW;
    }


    // ------------------------------------------------------ undefine

    /**
     * Implements the transition from [{@link State#VIEW VIEW}, {@link State#EDIT EDIT}] &rarr; {@link
     * State#EMPTY EMPTY}.
     * <ol>
     * <li>Check whether this form supports 'undefine'</li>
     * <li>Undefine the model: {@link #undefineModel()}</li>
     * <li>Prepare the empty state: {@link #prepareEmptyState()}</li>
     * <li>Switch to view state: {@link #switchToEmptyState()}</li>
     * <li>Call registered undefine callbacks: {@link org.jboss.hal.ballroom.form.Form.UndefineCallback#onUndefine(Object)}</li>
     * <li>Change the state to {@link State#EMPTY}</li>
     * </ol>
     * Please make sure to stick with this contract in case you override this method.
     */
    @Override
    public void undefine() {
        if (supportsUndefine()) {
            assertState(VIEW, EDIT);

            undefineModel();
            prepareEmptyState();
            switchToEmptyState();
            if (undefineCallback != null) {
                undefineCallback.onUndefine(getModel());
            }
            state = EMPTY;
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
    protected boolean supportsUndefine() {
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
    protected abstract void prepareEmptyState();

    /**
     * Flips the deck panel so that the empty panel is visible
     */
    protected void switchToEmptyState() {
        Browser.getDocument().removeEventListener("keyup", exitEditWithEsc);
        switchTo(EMPTY);
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
        for (FormValidation validationHandler : validationHandlers) {
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


    // ------------------------------------------------------ form help delegate

    protected void addHelp(String label, String description) {
        if (supportsHelp && formLinks != null) {
            formLinks.addHelpText(label, description);
        }
    }


    // ------------------------------------------------------ helper methods

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
