/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.jboss.elemento.Elements;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.LazyElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.HandlerRegistration;

import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLFieldSetElement;
import elemental2.dom.HTMLHRElement;
import elemental2.dom.HTMLLegendElement;
import elemental2.dom.HTMLUListElement;
import jsinterop.base.Js;

import static java.util.stream.Collectors.toList;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.hr;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;
import static org.jboss.elemento.EventType.keyup;
import static org.jboss.elemento.Key.Escape;
import static org.jboss.hal.ballroom.form.Form.Operation.CANCEL;
import static org.jboss.hal.ballroom.form.Form.Operation.CLEAR;
import static org.jboss.hal.ballroom.form.Form.Operation.EDIT;
import static org.jboss.hal.ballroom.form.Form.Operation.REMOVE;
import static org.jboss.hal.ballroom.form.Form.Operation.RESET;
import static org.jboss.hal.ballroom.form.Form.Operation.SAVE;
import static org.jboss.hal.ballroom.form.Form.Operation.VIEW;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.alertDanger;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnHal;
import static org.jboss.hal.resources.CSS.btnPrimary;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.editing;
import static org.jboss.hal.resources.CSS.faAngleDown;
import static org.jboss.hal.resources.CSS.fieldSectionTogglePf;
import static org.jboss.hal.resources.CSS.fieldsSectionHeaderPf;
import static org.jboss.hal.resources.CSS.fieldsSectionPf;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.form;
import static org.jboss.hal.resources.CSS.formButtons;
import static org.jboss.hal.resources.CSS.formGroup;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.formSection;
import static org.jboss.hal.resources.CSS.halFormOffset;
import static org.jboss.hal.resources.CSS.helpBlock;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.CSS.readonly;
import static org.jboss.hal.resources.CSS.separator;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;

/**
 * A generic form with some reasonable UI defaults. Please note that all form items and help texts must be setup before this
 * form is added {@linkplain #element() as an element} to the DOM.
 * <p>
 * The form consists of {@linkplain FormLinks links} and three sections:
 * <ul>
 * <li>empty</li>
 * <li>read-only</li>
 * <li>editing</li>
 * </ul>
 */
public abstract class AbstractForm<T> extends LazyElement implements Form<T> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    private static final String MODEL_MUST_NOT_BE_NULL = "Model must not be null in ";
    private static final String MODEL_MUST_NOT_BE_UNDEFINED = "Model must not be undefined in ";
    private static final String NOT_INITIALIZED = "Form element not initialized. Please add this form to the DOM before calling any of the form operations";

    private final String id;
    private final StateMachine stateMachine;
    private final LinkedHashMap<State, HTMLElement> panels;
    // Contains *all* form items. Do not use this field directly.
    // Instead use getFormItems() or getBoundFormItems()
    private final LinkedHashMap<String, FormItem> formItems;
    private final Set<String> unboundItems;
    private final LinkedHashMap<String, SafeHtml> helpTexts;
    private final List<FormValidation> formValidations;
    private boolean separateOptionalFields;

    private T model;
    private final EmptyState emptyState;

    protected FormLinks<T> formLinks;
    private HTMLDivElement errorPanel;
    private HTMLElement errorMessage;
    private HTMLUListElement errorMessages;
    private EventCallbackFn<Event> escCallback;
    private HandlerRegistration escRegistration;

    // accessible in subclasses
    protected final DataMapping<T> dataMapping;
    protected SaveCallback<T> saveCallback;
    protected CancelCallback<T> cancelCallback;
    protected PrepareReset<T> prepareReset;
    protected PrepareRemove<T> prepareRemove;

    // ------------------------------------------------------ initialization

    public AbstractForm(String id, StateMachine stateMachine, DataMapping<T> dataMapping,
            EmptyState emptyState) {
        this.id = id;
        this.stateMachine = stateMachine;
        this.dataMapping = dataMapping;
        this.emptyState = emptyState;

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
            if (item instanceof AbstractFormItem) {
                ((AbstractFormItem) item).setForm(this);
            }
        }
    }

    protected void separateOptionalFields(boolean separateOptionalFields) {
        this.separateOptionalFields = separateOptionalFields;
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
    protected HTMLElement createElement() {
        HTMLElement section = section().id(id).css(formSection).element();

        formLinks = new FormLinks<>(this, stateMachine, helpTexts,
                event -> edit(getModel()),
                event -> {
                    if (prepareReset != null) {
                        prepareReset.beforeReset(this);
                    } else {
                        reset();
                    }
                },
                event -> {
                    if (prepareRemove != null) {
                        prepareRemove.beforeRemove(this);
                    } else {
                        remove();
                    }
                });
        section.appendChild(formLinks.element());

        errorPanel = div().css(alert, alertDanger)
                .add(span().css(Icons.ERROR))
                .add(errorMessage = span().element())
                .add(errorMessages = ul().element()).element();
        clearErrors();

        if (stateMachine.supports(EMPTY)) {
            panels.put(EMPTY, emptyState.element());
        }
        if (stateMachine.supports(READONLY)) {
            panels.put(READONLY, viewPanel());
        }
        if (stateMachine.supports(EDITING)) {
            panels.put(EDITING, editPanel());
        }
        for (HTMLElement element : panels.values()) {
            section.appendChild(element);
        }

        if (stateMachine.supports(EDIT)) {
            escCallback = (Event event) -> {
                if (Escape.match(event) &&
                        stateMachine.current() == EDITING &&
                        panels.get(EDITING) != null &&
                        Elements.isVisible(panels.get(EDITING))) {
                    event.preventDefault();
                    cancel();
                }
            };
        }

        State current = stateMachine.current();
        if (current != null) {
            flip(current);
        } else {
            flip(panels.keySet().iterator().next());
        }
        return section;
    }

    private HTMLElement viewPanel() {
        HTMLDivElement viewPanel = div()
                .id(Ids.build(id, READONLY.name().toLowerCase()))
                .css(form, formHorizontal, readonly).element();
        for (Iterator<FormItem> iterator = getFormItems().iterator(); iterator.hasNext();) {
            FormItem formItem = iterator.next();
            viewPanel.appendChild(formItem.element(READONLY));
            if (iterator.hasNext()) {
                HTMLHRElement hr = hr().css(separator).element();
                viewPanel.appendChild(hr);
            }
        }
        return viewPanel;
    }

    private HTMLElement editPanel() {
        HTMLElement editPanel = div()
                .id(Ids.build(id, EDITING.name().toLowerCase()))
                .css(form, formHorizontal, editing).element();
        editPanel.appendChild(errorPanel);
        boolean hasRequiredField = false;
        boolean hasOptionalField = false;
        for (FormItem formItem : getFormItems()) {
            if (formItem.isRequired() || !separateOptionalFields) {
                editPanel.appendChild(formItem.element(EDITING));
            }
            hasRequiredField = hasRequiredField || formItem.isRequired();
            hasOptionalField = hasOptionalField || !formItem.isRequired();
        }
        if (hasRequiredField) {
            editPanel.appendChild(div().css(formGroup)
                    .add(div().css(halFormOffset)
                            .add(span().css(helpBlock).innerHtml(MESSAGES.requiredHelp())))
                    .element());
        }
        // if separateOptionalFields=true and there are non-required attributes, they are placed in a collapsible
        // panel
        if (separateOptionalFields && hasOptionalField) {
            List<HTMLElement> optionalFields = new ArrayList<>();
            HTMLFieldSetElement fieldsetElement = fieldset().css(fieldsSectionPf).element();
            HTMLElement expanderElement = span().css(fontAwesome("angle-right"), fontAwesome("angle-down"),
                    fieldSectionTogglePf).element();
            // as we add fa-angle-right and fa-angle-down, remove the later so the angle-right becomes visible
            expanderElement.classList.remove(faAngleDown);
            HTMLLegendElement legend = legend().css(fieldsSectionHeaderPf)
                    .add(expanderElement)
                    .add(a().css(fieldSectionTogglePf, clickable)
                            .textContent(CONSTANTS.optionalFields())
                            .on(click, event -> {
                                // toggle the fa-angle-down to show either angle-right or angle-down
                                expanderElement.classList.toggle(faAngleDown);
                                optionalFields.forEach(field -> setVisible(field,
                                        expanderElement.classList.contains(faAngleDown)));
                            }))
                    .element();
            fieldsetElement.appendChild(legend);
            for (FormItem formItem : getFormItems()) {
                if (!formItem.isRequired()) {
                    HTMLElement field = formItem.element(EDITING);
                    optionalFields.add(field);
                    fieldsetElement.appendChild(field);
                    setVisible(field, false);
                }
            }
            editPanel.appendChild(fieldsetElement);
        }
        HTMLElement buttons = div().css(formGroup, formButtons)
                .add(div().css(halFormOffset)
                        .add(div().css(pullRight)
                                .add(button()
                                        .css(btn, btnHal, btnDefault)
                                        .textContent(CONSTANTS.cancel())
                                        .on(click, event -> cancel()))
                                .add(button()
                                        .css(btn, btnHal, btnPrimary)
                                        .textContent(CONSTANTS.save())
                                        .on(click, event -> save()))))
                .element();
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
     * Executes the {@link Operation#VIEW} operation and calls {@link DataMapping#populateFormItems(Object, Form)} if the form
     * is not {@linkplain #isUndefined() undefined}.
     *
     * @param model the model to view.
     */
    @Override
    public final void view(T model) {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }

        this.model = model;
        stateExec(VIEW, isUndefined() ? EMPTY : READONLY);
        if (!isUndefined()) {
            dataMapping.populateFormItems(model, this);
        }
    }

    /**
     * Removes the model reference, executes the {@link Operation#CLEAR} operation and calls
     * {@link DataMapping#clearFormItems(Form)}.
     */
    @Override
    public void clear() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = null;
        stateExec(CLEAR);
        clearErrors();
        dataMapping.clearFormItems(this);
    }

    /**
     * Executes the {@link Operation#EDIT} operation and calls {@link DataMapping#newModel(Object, Form)} if the model is
     * {@linkplain #isTransient() transitive} otherwise {@link DataMapping#populateFormItems(Object, Form)}.
     *
     * @param model the model to edit.
     */
    @Override
    public final void edit(T model) {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        stateExec(EDIT);
        clearErrors();
        if (isTransient()) {
            dataMapping.newModel(model, this);
        } else {
            dataMapping.populateFormItems(model, this);
            for (FormItem formItem : getBoundFormItems()) {
                formItem.setModified(false);
            }
        }
    }

    /**
     * Upon successful validation, executes the {@link Operation#SAVE} operation, calls
     * {@link DataMapping#persistModel(Object, Form)} and finally calls the registered {@linkplain SaveCallback save callback}
     * (if any).
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
    public void setSaveCallback(SaveCallback<T> saveCallback) {
        this.saveCallback = saveCallback;
    }

    protected Map<String, Object> getChangedValues() {
        Map<String, Object> changed = new HashMap<>();
        for (FormItem formItem : getBoundFormItems()) {
            if (formItem.isModified()) {
                if (formItem.isExpressionValue()) {
                    changed.put(formItem.getName(), formItem.getExpressionValue());
                } else {
                    changed.put(formItem.getName(), formItem.getValue());
                }
            }
        }
        return changed;
    }

    /**
     * Executes the {@link Operation#CANCEL} operation and calls the registered {@linkplain CancelCallback cancel callback} (if
     * any).
     */
    @Override
    public final void cancel() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        if (getModel() == null) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_NULL + formId() + ".cancel()");
        }
        stateExec(CANCEL);
        dataMapping.populateFormItems(model, this); // restore persisted model
        if (cancelCallback != null) {
            cancelCallback.onCancel(this);
        }
    }

    @Override
    public void setCancelCallback(CancelCallback<T> cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    @Override
    public void setPrepareReset(PrepareReset<T> prepareReset) {
        this.prepareReset = prepareReset;
    }

    /**
     * Executes the {@link Operation#RESET} operation.
     */
    @Override
    public final void reset() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        if (isUndefined()) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_UNDEFINED + formId() + ".reset()");
        }
        stateExec(RESET);
    }

    @Override
    public void setPrepareRemove(PrepareRemove<T> removeCallback) {
        this.prepareRemove = removeCallback;
    }

    /**
     * Removes the model reference and executes the {@link Operation#REMOVE} operation.
     */
    @Override
    public void remove() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        if (isUndefined()) {
            throw new NullPointerException(MODEL_MUST_NOT_BE_UNDEFINED + formId() + ".remove()");
        }
        this.model = null;
        stateExec(REMOVE);
    }

    private String formId() {
        return "form(" + id + ")"; // NON-NLS
    }

    // ------------------------------------------------------ state transition

    private void stateExec(Operation operation) {
        stateExec(operation, null);
    }

    private <C> void stateExec(Operation operation, C context) {
        stateMachine.execute(operation, context);
        prepare(stateMachine.current());
        flip(stateMachine.current());
    }

    protected void prepare(State state) {
        switch (state) {
            case EMPTY:
                formLinks.setVisible(false, false, false, false);
                prepareEmptyState();
                break;
            case READONLY:
                formLinks.setVisible(model != null && stateMachine.supports(EDIT),
                        model != null && stateMachine.supports(RESET),
                        model != null && stateMachine.supports(REMOVE),
                        true);
                prepareViewState();
                break;
            case EDITING:
                formLinks.setVisible(false, false, false, true);
                prepareEditState();
                break;
            default:
                break;
        }
    }

    /**
     * Gives subclasses a way to prepare the empty state. Called after the state has changed, but before the UI flips to the new
     * state.
     */
    protected void prepareEmptyState() {
    }

    /**
     * Gives subclasses a way to prepare the view state. Called after the state has changed, but before the UI flips to the new
     * state.
     */
    protected void prepareViewState() {
    }

    /**
     * Gives subclasses a way to prepare the edit state. Called after the state has changed, but before the UI flips to the new
     * state.
     */
    protected void prepareEditState() {
    }

    protected void flip(State state) {
        // exit with ESC handler
        switch (state) {
            case EMPTY:
            case READONLY:
                if (escRegistration != null && panels.get(EDITING) != null) {
                    escRegistration.removeHandler();
                }
                break;

            case EDITING:
                if (!Iterables.isEmpty(getFormItems())) {
                    setTimeout((o) -> getFormItems().iterator().next().setFocus(true), MEDIUM_TIMEOUT);
                }
                if (escCallback != null && panels.get(EDITING) != null && escRegistration == null) {
                    // Exit *this* edit state by pressing ESC
                    escRegistration = bind(panels.get(EDITING), keyup.getName(), ((e) -> escCallback.onEvent(Js.cast(e))));
                }
                break;
            default:
                break;
        }

        panels.values().stream()
                .filter(panel -> panel != panels.get(state))
                .forEach(panel -> setVisible(panel, false));
        setVisible(panels.get(state), true);
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

    // ------------------------------------------------------ validation

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean validate() {
        boolean valid = true;
        clearErrors();

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
        return valid;
    }

    private void clearErrors() {
        for (FormItem formItem : getFormItems()) {
            formItem.clearError();
        }
        errorMessage.textContent = "";
        Elements.removeChildrenFrom(errorMessages);
        setVisible(errorPanel, false);
    }

    private void showErrors(List<String> messages) {
        if (!messages.isEmpty()) {
            if (messages.size() == 1) {
                errorMessage.textContent = messages.get(0);
                Elements.removeChildrenFrom(errorMessages);
            } else {
                errorMessage.textContent = CONSTANTS.formErrors();
                for (String message : messages) {
                    errorMessages.appendChild(li().textContent(message).element());
                }
            }
            setVisible(errorPanel, true);
        }
    }
}
