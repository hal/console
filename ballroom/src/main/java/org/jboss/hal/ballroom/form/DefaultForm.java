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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.html.DivElement;
import elemental.html.HRElement;
import elemental.html.SpanElement;
import elemental.html.UListElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.LazyElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

/**
 * A generic form with some reasonable UI defaults. Please note that all form items and help texts must be setup
 * before this form is added {@linkplain #asElement() as an element} to the DOM.
 *
 * @author Harald Pehl
 */
public class DefaultForm<T> extends LazyElement implements Form<T>, SecurityContextAware {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_MESSAGES = "errorMessages";
    private static final String MODEL_MUST_NOT_BE_NULL = "Model must not be null in ";
    private static final String NOT_INITIALIZED = "Form element not initialized. Please add this form to the DOM before calling any of the form operations";

    private final String id;
    private final StateMachine stateMachine;
    private final DataMapping<T> dataMapping;
    private final LinkedHashMap<State, Element> panels;
    private final LinkedHashMap<String, FormItem> formItems;
    private final Set<String> unboundItems;
    private final LinkedHashMap<String, SafeHtml> helpTexts;
    private final List<FormValidation> formValidations;

    private T model;
    private SecurityContext securityContext;

    private FormLinks<T> formLinks;
    private DivElement errorPanel;
    private SpanElement errorMessage;
    private UListElement errorMessages;
    private EventListener exitEditWithEsc;

    // accessible in subclasses
    protected SaveCallback<T> saveCallback;
    protected ResetCallback<T> resetCallback;
    protected CancelCallback<T> cancelCallback;


    // ------------------------------------------------------ initialization

    public DefaultForm(final String id, final StateMachine stateMachine, final DataMapping<T> dataMapping,
            final SecurityContext securityContext) {
        this.stateMachine = stateMachine;
        this.dataMapping = dataMapping;
        this.securityContext = securityContext;

        this.id = id;
        this.securityContext = securityContext;
        this.panels = new LinkedHashMap<>();
        this.formItems = new LinkedHashMap<>();
        this.unboundItems = new HashSet<>();
        this.helpTexts = new LinkedHashMap<>();
        this.formValidations = new ArrayList<>();
    }

    protected void addFormItem(FormItem formItem, FormItem... formItems) {
        for (FormItem item : Lists.asList(formItem, formItems)) {
            this.formItems.put(item.getName(), item);
            item.setId(Ids.build(id, item.getName()));
        }
    }

    protected void markAsUnbound(String name) {
        unboundItems.add(name);
    }

    protected void addHelp(String label, SafeHtml description) {
        helpTexts.put(label, description);
    }

    @Override
    public void addFormValidation(FormValidation<T> formValidation) {
        formValidations.add(formValidation);
    }


    // ------------------------------------------------------ ui setup

    @Override
    protected Element createElement() {

        Element section = Browser.getDocument().createElement("section"); //NON-NLS
        section.setId(id);
        section.getClassList().add(formSection);

        formLinks = new FormLinks<>(id, stateMachine, helpTexts,
                event -> edit(getModel()),
                event -> reset());
        section.appendChild(formLinks.asElement());

        // @formatter:off
        Elements.Builder errorPanelBuilder = new Elements.Builder()
            .div().css(alert, alertDanger).rememberAs("errorPanel")
                .span().css(Icons.ERROR).end()
                .span().rememberAs(ERROR_MESSAGE).end()
                .ul().rememberAs(ERROR_MESSAGES).end()
            .end();
        // @formatter:on
        errorMessage = errorPanelBuilder.referenceFor(ERROR_MESSAGE);
        errorMessages = errorPanelBuilder.referenceFor(ERROR_MESSAGES);
        errorPanel = errorPanelBuilder.build();
        clearErrors();

        if (stateMachine.supports(VIEW)) {
            panels.put(READONLY, viewPanel());
        }
        if (stateMachine.supports(ADD)) {
            panels.put(EDITING, editPanel());
        }
        if (stateMachine.supports(EDIT)) {
            panels.put(EDITING, editPanel());
        }
        for (Element element : panels.values()) {
            section.appendChild(element);
        }

        if (stateMachine.supports(EDIT)) {
            exitEditWithEsc = event -> {
                if (event instanceof KeyboardEvent) {
                    KeyboardEvent keyboardEvent = (KeyboardEvent) event;
                    if (keyboardEvent.getKeyCode() == KeyboardEvent.KeyCode.ESC &&
                            stateMachine.current() == EDITING &&
                            panels.get(EDITING) != null &&
                            Elements.isVisible(panels.get(EDITING))) {
                        keyboardEvent.preventDefault();
                        cancel();
                    }
                }
            };
        }

        flip(panels.keySet().iterator().next());
        return section;
    }

    private Element viewPanel() {
        Element viewPanel = new Elements.Builder()
                .div().id(Ids.build(id, READONLY.name().toLowerCase())).css(form, formHorizontal, readonly)
                .end().build();
        for (Iterator<FormItem> iterator = getFormItems().iterator(); iterator.hasNext(); ) {
            FormItem formItem = iterator.next();
            viewPanel.appendChild(formItem.asElement(READONLY));
            if (iterator.hasNext()) {
                HRElement hr = Browser.getDocument().createHRElement();
                hr.getClassList().add(separator);
                viewPanel.appendChild(hr);
            }
        }
        return viewPanel;
    }

    private Element editPanel() {
        Element editPanel = new Elements.Builder()
                .div().id(Ids.build(id, EDITING.name().toLowerCase())).css(form, formHorizontal, editing).end()
                .build();
        editPanel.appendChild(errorPanel);
        boolean hasRequiredField = false;
        for (FormItem formItem : getFormItems()) {
            editPanel.appendChild(formItem.asElement(EDITING));
            hasRequiredField = hasRequiredField || formItem.isRequired();
        }
        if (hasRequiredField) {
            // @formatter:off
            editPanel.appendChild(new Elements.Builder()
                .div().css(formGroup)
                    .div().css(halFormOffset)
                        .span().css(helpBlock)
                            .innerHtml(MESSAGES.requiredHelp())
                        .end()
                    .end()
                .end()
            .build());
            // @formatter:on
        }

        // @formatter:off
        Element buttons = new Elements.Builder()
            .div().css(formGroup, formButtons)
                .div().css(halFormOffset)
                    .div().css(pullRight)
                        .button().css(btn, btnHal, btnDefault).on(click, event -> cancel())
                            .textContent(CONSTANTS.cancel())
                        .end()
                        .button().css(btn, btnHal, btnPrimary).on(click, event -> save())
                            .textContent(CONSTANTS.save())
                        .end()
                    .end()
                .end()
            .end()
        .build();
        // @formatter:on
        editPanel.appendChild(buttons);

        return editPanel;
    }

    @Override
    public void attach() {
        getFormItems().forEach(Attachable::attach);
    }

    @Override
    public void detach() {
        stateMachine.reset();
        getFormItems().forEach(Attachable::detach);
    }

    // ------------------------------------------------------ form operations

    /**
     * Executes the {@link Operation#ADD} operation and calls {@link
     * DataMapping#newModel(Object, Form)}.
     *
     * @param model the transient model
     */
    @Override
    public final void add(final T model) {
        if (model == null) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_NULL + formId() + ".add(T)");
        }
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        stateExec(ADD); // switch state before data mapping!
        clearErrors();
        dataMapping.newModel(model, this);
    }

    /**
     * Executes the {@link Operation#VIEW} operation and calls {@link
     * DataMapping#populateFormItems(Object, Form)}.
     *
     * @param model the model to view.
     */
    @Override
    public final void view(final T model) {
        if (model == null) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_NULL + formId() + ".view(T)");
        }
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        stateExec(VIEW); // switch state before data mapping!
        dataMapping.populateFormItems(model, this);
    }

    /**
     * Removes the model reference, executes the {@link Operation#CLEAR} operation and
     * calls {@link DataMapping#clearFormItems(Form)}.
     */
    @Override
    public void clear() {
        this.model = null;
        stateExec(CLEAR);
        clearErrors();
        dataMapping.clearFormItems(this);
    }

    /**
     * Executes the {@link Operation#RESET} operation, calls {@link
     * DataMapping#resetModel(Object, Form)} and finally calls the registered {@linkplain
     * ResetCallback reset callback} (if any).
     */
    @Override
    public final void reset() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        stateExec(RESET); // switch state before data mapping!
        clearErrors();
        dataMapping.resetModel(model, this);
        if (resetCallback != null) {
            resetCallback.onReset(this);
        }
    }

    @Override
    public void setResetCallback(final ResetCallback<T> resetCallback) {
        this.resetCallback = resetCallback;
    }

    /**
     * Executes the {@link Operation#EDIT} operation and calls {@link
     * DataMapping#populateFormItems(Object, Form)}.
     *
     * @param model the model to edit.
     */
    @Override
    public final void edit(final T model) {
        if (model == null) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_NULL + formId() + ".edit(T)");
        }
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        stateExec(EDIT); // switch state before data mapping!
        clearErrors();
        dataMapping.populateFormItems(model, this);
        getFormItems().forEach(formItem -> formItem.setModified(false));
    }

    /**
     * Upon successful validation, executes the {@link Operation#SAVE} operation,
     * calls {@link DataMapping#persistModel(Object, Form)} and finally calls the registered {@linkplain
     * SaveCallback save callback} (if any).
     */
    @Override
    public final boolean save() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        boolean valid = validate();
        if (valid) {
            stateExec(SAVE); // switch state before data mapping!
            dataMapping.persistModel(model, this);
            if (saveCallback != null) {
                saveCallback.onSave(this, getChangedValues());
            }
        }
        return valid;
    }

    @Override
    public void setSaveCallback(final SaveCallback<T> saveCallback) {
        this.saveCallback = saveCallback;
    }

    protected Map<String, Object> getChangedValues() {
        Map<String, Object> changed = new HashMap<>();
        for (Map.Entry<String, FormItem> entry : formItems.entrySet()) {
            FormItem formItem = entry.getValue();
            if (formItem.isModified()) {
                changed.put(entry.getKey(), formItem.getValue());
            }
        }
        return changed;
    }

    /**
     * Executes the {@link Operation#CANCEL} operation and calls the registered
     * {@linkplain CancelCallback cancel callback} (if any).
     */
    @Override
    public final void cancel() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        stateExec(CANCEL);
        dataMapping.populateFormItems(model, this); // restore persisted model
        if (cancelCallback != null) {
            cancelCallback.onCancel(this);
        }
    }

    @Override
    public void setCancelCallback(final CancelCallback<T> cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    private String formId() {
        return "form(" + id + ")"; //NON-NLS
    }


    // ------------------------------------------------------ state transition

    private void stateExec(final Operation operation) {
        stateMachine.execute(operation);
        prepare(stateMachine.current());
        flip(stateMachine.current());
    }

    private void prepare(State state) {
        switch (state) {
            case READONLY:
                prepareViewState();
                break;
            case EDITING:
                prepareEditState();
                break;
        }
    }

    /**
     * Gives subclasses a way to prepare the view state.
     */
    protected void prepareViewState() {}

    /**
     * Gives subclasses a way to prepare the edit state.
     */
    protected void prepareEditState() {}

    private void flip(State state) {
        getFormItems().forEach(formItem -> formItem.setState(state));

        switch (state) {
            case READONLY:
                if (exitEditWithEsc != null && panels.get(EDITING) != null) {
                    panels.get(EDITING).removeEventListener("keyup", exitEditWithEsc); //NON-NLS
                }
                break;

            case EDITING:
                if (!formItems.isEmpty()) {
                    Browser.getWindow()
                            .setInterval(() -> getFormItems().iterator().next().setFocus(true), SHORT_TIMEOUT);
                }
                if (exitEditWithEsc != null && panels.get(EDITING) != null) {
                    // Exit *this* edit state by pressing ESC
                    panels.get(EDITING).setOnkeyup(exitEditWithEsc);
                }
                break;
        }

        formLinks.switchTo(state, model, securityContext);
        panels.values().stream()
                .filter(panel -> panel != panels.get(state))
                .forEach(panel -> Elements.setVisible(panel, false));
        Elements.setVisible(panels.get(state), true);
    }


    // ------------------------------------------------------ security

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {
        this.securityContext = securityContext;
        applySecurity();
    }

    private void applySecurity() {
        if (stateMachine.current() == EDITING && !securityContext.isWritable()) {
            stateExec(CANCEL);
        }
        formLinks.switchTo(stateMachine.current(), model, securityContext);
        for (Map.Entry<String, FormItem> entry : formItems.entrySet()) {
            entry.getValue().setRestricted(!securityContext.isWritable(entry.getKey()));
        }
    }


    // ------------------------------------------------------ properties

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T getModel() {
        return model;
    }

    @Override
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I> FormItem<I> getFormItem(String name) {
        return formItems.get(name);
    }

    @Override
    public Iterable<FormItem> getBoundFormItems() {
        return formItems.values().stream()
                .filter(formItem -> !unboundItems.contains(formItem.getName()))
                .collect(toList());
    }

    @Override
    public Iterable<FormItem> getFormItems() {
        return ImmutableList.copyOf(formItems.values());
    }

    public boolean isModified() {
        for (FormItem formItem : getFormItems()) {
            if (formItem.isModified()) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------ validation

    @SuppressWarnings("unchecked")
    protected boolean validate() {
        boolean valid = true;

        // validate form items
        for (FormItem formItem : getFormItems()) {
            if (!formItem.validate()) {
                valid = false;
            }
        }

        // validate form on its own
        List<String> messages = new ArrayList<>();
        for (FormValidation validationHandler : formValidations) {
            ValidationResult validationResult = validationHandler.validate(this);
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

    private void clearErrors() {
        for (FormItem formItem : getFormItems()) {
            formItem.clearError();
        }
        errorMessage.setInnerText("");
        Elements.removeChildrenFrom(errorMessages);
        Elements.setVisible(errorPanel, false);
    }

    private void showErrors(List<String> messages) {
        if (!messages.isEmpty()) {
            if (messages.size() == 1) {
                errorMessage.setInnerText(messages.get(0));
                Elements.removeChildrenFrom(errorMessages);
            } else {
                errorMessage.setInnerText(CONSTANTS.formErrors());
                for (String message : messages) {
                    errorMessages.appendChild(new Elements.Builder().li().textContent(message).end().build());
                }
            }
            Elements.setVisible(errorPanel, true);
        }
    }
}
