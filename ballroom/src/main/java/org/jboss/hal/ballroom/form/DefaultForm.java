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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import elemental.html.UListElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.LazyElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.*;

/**
 * An abstract form with some reasonable UI defaults. Please note that all form items and help texts must be setup
 * before this form is added {@linkplain #asElement() as an element} to the DOM.
 *
 * @author Harald Pehl
 */
public class DefaultForm<T> extends LazyElement implements Form<T>, SecurityContextAware {

    private final static Constants CONSTANTS = GWT.create(Constants.class);
    private final static String NOT_INITIALIZED = "Form element not initialized. Please add this form to the DOM before calling any of the form operations like view(), edit(), save(), cancel() or reset()";

    private final String id;
    private final StateMachine stateMachine;
    private final LinkedHashMap<State, Element> panels;
    private final LinkedHashMap<String, FormItem> formItems;
    private final LinkedHashMap<String, String> helpTexts;
    private final List<FormValidation> formValidations;

    private T model;
    private SecurityContext securityContext;

    private Element buttons;
    private FormLinks formLinks;
    private DivElement errorPanel;
    private SpanElement errorMessage;
    private UListElement errorMessages;
    private EventListener exitEditWithEsc;

    // accessible in subclasses
    protected SaveCallback saveCallback;
    protected ResetCallback resetCallback;
    protected CancelCallback cancelCallback;


    // ------------------------------------------------------ initialization

    public DefaultForm(final String id, final StateMachine stateMachine, final SecurityContext securityContext) {
        this.stateMachine = stateMachine;
        this.securityContext = securityContext;

        this.id = id;
        this.securityContext = securityContext;
        this.panels = new LinkedHashMap<>();
        this.formItems = new LinkedHashMap<>();
        this.helpTexts = new LinkedHashMap<>();
        this.formValidations = new ArrayList<>();
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
    protected Element createElement() {

        Element section = Browser.getDocument().createElement("section"); //NON-NLS
        section.setId(id);

        formLinks = new FormLinks(id, stateMachine, helpTexts,
                event -> edit(getModel()),
                event -> reset());
        section.appendChild(formLinks.asElement());

        if (stateMachine.supports(VIEW)) {
            panels.put(READONLY, viewPanel());
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
        return new Elements.Builder()
                .div().id(IdBuilder.build(id, "view")).css(form, formHorizontal)
                .p().innerText(Names.NYI).end()
                .end().build();
    }

    private Element editPanel() {
        // @formatter:off
        Elements.Builder errorPanelBuilder = new Elements.Builder()
            .div().css(alert, alertDanger).rememberAs("errorPanel")
                .span().css(pfIcon("error-circle-o")).end()
                .span().rememberAs("errorMessage").end()
                .ul().rememberAs("errorMessages").end()
            .end();
        // @formatter:on
        errorMessage = errorPanelBuilder.referenceFor("errorMessage");
        errorMessages = errorPanelBuilder.referenceFor("errorMessages");
        errorPanel = errorPanelBuilder.build();
        clearErrors();

        Element editPanel = new Elements.Builder()
                .div().id(IdBuilder.build(id, "edit")).css(form, formHorizontal).end()
                .build();
        editPanel.appendChild(errorPanel);
        for (FormItem formItem : formItems.values()) {
            formItem.identifyAs(id, "edit", formItem.getName());
            editPanel.appendChild(formItem.asElement(EDITING));
        }

        // @formatter:off
        buttons = new Elements.Builder()
            .div().css(formGroup, editButtons)
                .div().css(offset(labelColumns), column(inputColumns))
                    .div().css(formButtons, pullRight)
                        .button().css(btn, btnHal, btnDefault).on(click, event -> cancel())
                            .innerText(CONSTANTS.cancel())
                        .end()
                        .button().css(btn, btnHal, btnPrimary).on(click, event -> save())
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

    protected void hideButtons() {
        Elements.setVisible(buttons, false);
    }


    // ------------------------------------------------------ form operations

    @Override
    public final void view(final T model) {
        if (model == null) {
            throw new NullPointerException("Model must not be null in " + formId() + ".view(T)");
        }
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        execute(VIEW);
    }

    @Override
    public final void edit(final T model) {
        if (model == null) {
            throw new NullPointerException("Model must not be null in " + formId() + ".edit(T)");
        }
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        this.model = model;
        execute(EDIT);
    }

    @Override
    public final void save() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        boolean valid = validate();
        if (valid) {
            if (saveCallback != null) {
                saveCallback.onSave(getChangedValues());
            }
            execute(SAVE);
        }
    }

    @Override
    public void setSaveCallback(final SaveCallback saveCallback) {
        this.saveCallback = saveCallback;
    }

    private Map<String, Object> getChangedValues() {
        Map<String, Object> changed = new HashMap<>();
        for (Map.Entry<String, FormItem> entry : formItems.entrySet()) {
            if (entry.getValue().isModified()) {
                changed.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return changed;
    }

    @Override
    public final void cancel() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        clearErrors();
        if (cancelCallback != null) {
            cancelCallback.onCancel();
        }
        execute(CANCEL);
    }

    @Override
    public void setCancelCallback(final CancelCallback cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    @Override
    public final void reset() {
        if (!initialized()) {
            throw new IllegalStateException(NOT_INITIALIZED);
        }
        if (resetCallback != null) {
            resetCallback.onReset();
        }
        execute(RESET);
    }

    @Override
    public void setResetCallback(final ResetCallback resetCallback) {
        this.resetCallback = resetCallback;
    }

    private String formId() {
        return "form(" + id + ")"; //NON-NLS
    }


    // ------------------------------------------------------ state transition

    private void execute(final Operation operation) {
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
        switch (state) {
            case READONLY:
                if (exitEditWithEsc != null && panels.get(EDITING) != null) {
                    panels.get(EDITING).removeEventListener("keyup", exitEditWithEsc); //NON-NLS
                }
                break;

            case EDITING:
                if (!formItems.isEmpty()) {
                    Scheduler.get().scheduleDeferred(() -> formItems.values().iterator().next().setFocus(true));
                }
                if (exitEditWithEsc != null && panels.get(EDITING) != null) {
                    // Exit *this* edit state by pressing ESC
                    panels.get(EDITING).setOnkeyup(exitEditWithEsc);
                }
                break;
        }

        formLinks.switchTo(state, securityContext);
        for (Element panel : panels.values()) {
            Elements.setVisible(panel, false);
        }
        Elements.setVisible(panels.get(state), true);
    }


    // ------------------------------------------------------ security

    @Override
    public void updateSecurityContext(final SecurityContext securityContext) {
        this.securityContext = securityContext;
        applySecurity();
    }

    private void applySecurity() {
        if (stateMachine.current() == EDITING && !securityContext.isWritable()) {
            execute(CANCEL);
        }
        formLinks.switchTo(stateMachine.current(), securityContext);
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

    public FormItem getFormItem(String name) {
        return formItems.get(name);
    }

    public Iterable<FormItem> getFormItems() {
        return ImmutableList.copyOf(formItems.values());
    }

    public boolean isModified() {
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
        if (stateMachine.current() == EDITING) {
            showErrors(singletonList(message));
        }
    }

    @Override
    public void invalidate(final String field, final String message) {
        if (stateMachine.current() == EDITING) {
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
        Elements.removeChildrenFrom(errorMessages);
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

    private void showErrors(List<String> messages) {
        if (!messages.isEmpty()) {
            if (messages.size() == 1) {
                errorMessage.setInnerText(messages.get(0));
                Elements.removeChildrenFrom(errorMessages);
            } else {
                errorMessage.setInnerText(CONSTANTS.formErrors());
                for (String message : messages) {
                    errorMessages.appendChild(new Elements.Builder().li().innerText(message).end().build());
                }
            }
            Elements.setVisible(errorPanel, true);
        }
    }

    public void clearValues() {
        for (FormItem formItem : formItems.values()) {
            formItem.clearValue();
            formItem.resetMetaData();
        }
    }
}
