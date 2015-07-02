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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.ButtonElement;
import elemental.html.DivElement;
import elemental.html.LabelElement;
import elemental.html.SpanElement;
import org.jboss.hal.ballroom.Elements;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.IsElement;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;

/**
 * @author Harald Pehl
 */
public abstract class FormItem<T>
        implements IsElement, HasEnabled, Focusable, HasName, HasValue<T>, HasText /* for expression support */,
        FormLayout {

    public static final String STYLE_NAME = "hal-FormItem";

    private final EventBus eventBus;
    private final List<FormItemValidation<T>> validationHandlers;

    private boolean required;
    private boolean modified;
    private boolean undefined;
    private boolean restricted;

    protected final DivElement container;
    protected final DivElement inputContainer;
    protected final DivElement inputGroupContainer;
    protected final LabelElement labelElement;
    protected final InputElement<T> inputElement;
    protected final SpanElement errorText;
    protected final SpanElement expressionContainer;
    protected final ButtonElement expressionButton;
    protected final RestrictedElement restrictedElement;
    protected final SpanElement restrictedIcon;


    // ------------------------------------------------------ initialization

    public FormItem(String name, String label) {
        this.inputElement = newInputElement();

        this.required = false;
        this.modified = false;
        this.undefined = true;
        this.restricted = false;

        this.eventBus = new SimpleEventBus();
        this.validationHandlers = new LinkedList<>();
        resetValidationHandlers();

        // create UI elements
        Document document = Browser.getDocument();
        container = document.createDivElement();
        container.setClassName(STYLE_NAME + " form-group");

        labelElement = document.createLabelElement();
        labelElement.setClassName("col-" + COLUMN_DISCRIMINATOR + "-" + LABEL_COLUMNS + " control-label");
        labelElement.setInnerText(label);

        inputContainer = document.createDivElement();
        inputContainer.setClassName("col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS);

        inputGroupContainer = document.createDivElement();
        inputGroupContainer.setClassName("input-group");

        errorText = document.createSpanElement();
        errorText.setClassName("help-block");
        Elements.setVisible(errorText, false);

        expressionContainer = document.createSpanElement();
        expressionContainer.setClassName("input-group-btn");
        expressionButton = document.createButtonElement();
        expressionButton.setClassName("btn btn-default");
        expressionButton.setInnerHTML("<i class=\"fa fa-link\"></i>");
        expressionButton.setOnclick(event -> ResolveExpressionEvent.fire(this, getExpressionValue()));
        expressionButton.setTitle("Expression Resolver");

        restrictedElement = new RestrictedElement();
        restrictedIcon = document.createSpanElement();
        restrictedIcon.setClassName("fa fa-lock form-control-feedback");
        Elements.setVisible(restrictedIcon, false);

        setId(Id.generate(name));
        setName(name);
        assembleUI();
    }

    /**
     * Assembles the <strong>initial</strong> widgets / containers at creation time based on the default values of this
     * form item.
     */
    protected void assembleUI() {
        expressionContainer.appendChild(expressionButton);
        inputGroupContainer.appendChild(expressionContainer);
        inputContainer.appendChild(inputElement.asElement());
        inputContainer.appendChild(errorText);
        inputContainer.appendChild(restrictedIcon);
        container.appendChild(labelElement);
        container.appendChild(inputContainer);
    }

    /**
     * Subclasses must create and return an input element with proper styles attached to it.
     * Subclasses should register a value change handler on the input element to update the modified / undefined flags
     * and signal changed values using the {@link #signalChange(Object)} method.
     *
     * @return a new input element for this form item
     */
    protected abstract InputElement<T> newInputElement();

    @Override
    public Element asElement() {
        return container;
    }


    // ------------------------------------------------------ state, id, name & text

    @Override
    public T getValue() {
        return inputElement.getValue();
    }

    @Override
    public void setValue(final T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(final T value, final boolean fireEvent) {
        toggleExpressionSupport(false);
        inputElement.setValue(value);
        if (fireEvent) {
            signalChange(value);
        }
    }

    public void clearValue() {
        inputElement.clearValue();
    }

    protected void signalChange(final T value) {
        ValueChangeEvent.fire(this, value);
    }

    @Override
    public void fireEvent(final GwtEvent<?> gwtEvent) {
        eventBus.fireEvent(gwtEvent);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> valueChangeHandler) {
        return eventBus.addHandler(ValueChangeEvent.getType(), valueChangeHandler);
    }

    /**
     * @return if this form item has no value.
     */
    public boolean isEmpty() {
        return getValue() == null || isNullOrEmpty(getText()) || isUndefined();
    }

    public void setId(String id) {
        Id.set(inputElement.asElement(), id);
        labelElement.setHtmlFor(id);
    }

    @Override
    public String getName() {
        return inputElement.getName();
    }

    @Override
    public void setName(final String name) {
        inputElement.setName(name);
    }

    @Override
    public String getText() {
        return inputElement.getText();
    }

    @Override
    public void setText(final String text) {
        inputElement.setText(text);
    }


    // ------------------------------------------------------ validation

    protected void resetValidationHandlers() {
        validationHandlers.clear();
        validationHandlers.addAll(defaultValidationHandlers());
    }

    protected List<FormItemValidation<T>> defaultValidationHandlers() {
        return singletonList(new RequiredValidation<>(this));
    }

    /**
     * Skips validation if this form item is not required and undefined
     */
    protected boolean requiresValidation() {
        return isRequired() || isModified() || !isUndefined();
    }

    public void addValidationHandler(FormItemValidation<T> validationHandler) {
        if (validationHandler != null) {
            validationHandlers.add(validationHandler);
        }
    }

    public final boolean validate() {
        if (requiresValidation()) {
            for (FormItemValidation<T> validationHandler : validationHandlers) {
                ValidationResult result = validationHandler.validate(getValue());
                if (!result.isValid()) {
                    showError(result.getMessage());
                    return false;
                }
            }
        }
        clearError();
        return true;
    }

    void clearError() {
        Elements.setVisible(errorText, false);
        container.getClassList().remove("has-error");
    }

    void showError(String message) {
        container.getClassList().add("has-error");
        errorText.setInnerText(message);
        Elements.setVisible(errorText, true);
    }


    // ------------------------------------------------------ expressions

    public boolean isExpressionValue() {
        return supportsExpressions() && hasExpressionScheme(getText());
    }

    public void setExpressionValue(String expressionValue) {
        if (supportsExpressions()) {
            toggleExpressionSupport(true);
            setText(expressionValue);
        }
    }

    public String getExpressionValue() {
        if (supportsExpressions()) {
            return getText();
        }
        return null;
    }

    public abstract boolean supportsExpressions();

    boolean hasExpressionScheme(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    boolean toggleExpressionSupport(boolean on) {
        // only change the UI if expressions are supported and switch is necessary
        if (supportsExpressions() && !isRestricted() && on != inExpressionState()) {
            if (on) {
                inputContainer.removeChild(inputElement.asElement());
                inputGroupContainer.appendChild(inputElement.asElement());
                inputContainer.appendChild(inputGroupContainer);
            } else {
                inputGroupContainer.removeChild(inputElement.asElement());
                inputContainer.removeChild(inputGroupContainer);
                inputContainer.appendChild(inputElement.asElement());
            }
            return true;
        }
        return false;
    }

    boolean inExpressionState() {
        return inputContainer.contains(inputGroupContainer);
    }


    // ------------------------------------------------------ input element delegates

    @Override
    public boolean isEnabled() {
        return inputElement.isEnabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        inputElement.setEnabled(enabled);
    }

    @Override
    public int getTabIndex() {
        return inputElement.getTabIndex();
    }

    @Override
    public void setTabIndex(final int index) {
        inputElement.setTabIndex(index);
    }

    @Override
    public void setAccessKey(final char accessKey) {
        inputElement.setAccessKey(accessKey);
    }

    @Override
    public void setFocus(final boolean focus) {
        inputElement.setFocus(focus);
    }


    // ------------------------------------------------------ properties

    public void resetMetaData() {
        setExpressionValue(null);
        setModified(false);
        setUndefined(true);
        clearError();

        // restricted cannot be reset!
    }

    public String getLabel() {
        return labelElement.getInnerText();
    }

    public void setLabel(final String label) {
        labelElement.setInnerText(label);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isModified() {
        return modified;
    }

    protected void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isUndefined() {
        return undefined;
    }

    protected void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(final boolean restricted) {
        this.restricted = restricted;
        toggleRestricted(restricted);
    }

    protected void toggleRestricted(final boolean on) {
        if (on) {
            container.getClassList().add("has-feedback");
            Node firstChild = inputContainer.getChildren().item(0);
            inputContainer.removeChild(firstChild);
            inputContainer.appendChild(restrictedElement.asElement());
        } else {
            container.getClassList().remove("has-feedback");
            inputContainer.removeChild(restrictedElement.asElement());
            if (isExpressionValue()) {
                inputContainer.appendChild(inputGroupContainer);
            } else {
                inputContainer.appendChild(inputElement.asElement());
            }
        }
        Elements.setVisible(restrictedIcon, on);
    }

    protected InputElement<T> inputElement() {
        return inputElement;
    }
}
