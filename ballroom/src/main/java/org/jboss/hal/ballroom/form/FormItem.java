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

import com.google.gwt.core.client.GWT;
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
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.DivElement;
import elemental.html.LabelElement;
import elemental.html.SpanElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.GridSpec;
import org.jboss.hal.resources.HalConstants;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.text;

/**
 * @author Harald Pehl
 */
public abstract class FormItem<T>
        implements IsElement, HasEnabled, Focusable, HasName, HasValue<T>, HasText /* for expression support */,
        GridSpec {

    static final HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final EventBus eventBus;
    private final List<FormItemValidation<T>> validationHandlers;

    private boolean required;
    private boolean modified;
    private boolean undefined;
    private boolean restricted;
    private boolean expressionAllowed;

    protected final DivElement container;
    protected final DivElement inputContainer;
    protected final DivElement inputGroupContainer;
    protected final LabelElement labelElement;
    protected final InputElement<T> inputElement;
    protected final SpanElement errorText;
    protected final SpanElement expressionContainer;
    protected final DivElement restrictedContainer;


    // ------------------------------------------------------ initialization

    public FormItem(String name, String label) {
        this.inputElement = newInputElement();

        this.required = false;
        this.modified = false;
        this.undefined = true;
        this.restricted = false;
        this.expressionAllowed = true;

        this.eventBus = new SimpleEventBus();
        this.validationHandlers = new LinkedList<>();
        resetValidationHandlers();

        // create basic elements
        container = new Elements.Builder().div().css("form-group").end().build();
        labelElement = new Elements.Builder()
                .label()
                .css("col-" + COLUMN_DISCRIMINATOR + "-" + LABEL_COLUMNS + " control-label")
                .innerText(label).build();
        inputContainer = new Elements.Builder()
                .div()
                .css("col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS).end().build();
        errorText = new Elements.Builder().span().css("help-block").end().build();
        Elements.setVisible(errorText, false);

        // @formatter:off
        Elements.Builder inputGroupBuilder = new Elements.Builder()
            .div().css("input-group")
                .span().css("input-group-btn").rememberAs("expressionContainer")
                    .button().css("btn btn-default")
                        .on(click, event -> ResolveExpressionEvent.fire(this, getExpressionValue()))
                        .title("Expression Resolver")
                        .start("i").css("fa fa-link").end()
                    .end()
                .end()
            .end();
        // @formatter:on
        inputGroupContainer = inputGroupBuilder.build();
        expressionContainer = inputGroupBuilder.referenceFor("expressionContainer");

        // @formatter:off
        Elements.Builder restrictedBuilder = new Elements.Builder()
            .div().css("input-group")
                .input(text).id(Id.generate(name, "restricted")).css("form-control").rememberAs("restrictedElement")
                .span().css("input-group-addon")
                    .start("i").css("fa fa-lock").end()
                .end()
            .end();
        // @formatter:on
        elemental.html.InputElement restrictedInput = restrictedBuilder.referenceFor("restrictedElement");
        restrictedInput.setReadOnly(true);
        restrictedInput.setValue(CONSTANTS.restricted());
        restrictedContainer = restrictedBuilder.build();

        setId(Id.generate(name));
        setName(name);
        assembleUI();
    }

    /**
     * Assembles the <strong>initial</strong> widgets / containers at creation time based on the default values of this
     * form item.
     */
    protected void assembleUI() {
        inputContainer.appendChild(inputElement.asElement());
        inputContainer.appendChild(errorText);
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


    // ------------------------------------------------------ state, name & text

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

    public void identifyAs(String id, String... additionalIds) {
        String fq = Id.generate(id, additionalIds);
        setId(fq);
        setName(fq);
        asElement().getDataset().setAt("formItemGroup", fq);
        labelElement.getDataset().setAt("formItemLabel", fq);
        inputElement.asElement().getDataset().setAt("formItemControl", fq);
    }


    // ------------------------------------------------------ validation

    protected void resetValidationHandlers() {
        validationHandlers.clear();
        validationHandlers.addAll(defaultValidationHandlers());
    }

    protected List<FormItemValidation<T>> defaultValidationHandlers() {
        return singletonList(new RequiredValidation<>(this));
    }

    @SuppressWarnings("SimplifiableIfStatement")
    protected boolean requiresValidation() {
        if (isRequired()) {
            return true;
        }
        if (isUndefined()) {
            return false;
        }
        return (isModified() && !validationHandlers.isEmpty());
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
                inputGroupContainer.insertBefore(inputElement.asElement(), expressionContainer);
                inputContainer.insertBefore(inputGroupContainer, errorText);
            } else {
                inputGroupContainer.removeChild(inputElement.asElement());
                inputContainer.removeChild(inputGroupContainer);
                inputContainer.insertBefore(inputElement.asElement(), errorText);
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

    public boolean isExpressionAllowed() {
        return expressionAllowed;
    }

    public void setExpressionAllowed(final boolean expressionAllowed) {
        this.expressionAllowed = expressionAllowed;
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
            inputContainer.appendChild(restrictedContainer);
        } else {
            container.getClassList().remove("has-feedback");
            inputContainer.removeChild(restrictedContainer);
            if (isExpressionValue()) {
                inputContainer.appendChild(inputGroupContainer);
            } else {
                inputContainer.appendChild(inputElement.asElement());
            }
        }
    }

    protected InputElement<T> inputElement() {
        return inputElement;
    }
}
