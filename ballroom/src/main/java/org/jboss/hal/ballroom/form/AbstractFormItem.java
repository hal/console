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
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.DivElement;
import elemental.html.LabelElement;
import elemental.html.SpanElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Names;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

/**
 * TODO Implement org.jboss.hal.ballroom.form.Form.State#READONLY
 *
 * @author Harald Pehl
 */
public abstract class AbstractFormItem<T> implements FormItem<T> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String EXPRESSION_CONTAINER = "expressionContainer"; //NON-NLS
    private static final String RESTRICTED_ELEMENT = "restrictedElement"; //NON-NLS

    private final EventBus eventBus;
    private final List<FormItemValidation<T>> validationHandlers;
    private boolean required;
    private boolean modified;
    private boolean undefined;
    private boolean restricted;
    private boolean expressionAllowed;

    private final DivElement inputGroupContainer;
    private final LabelElement labelElement;
    private final SpanElement errorText;
    private final SpanElement expressionContainer;
    private final DivElement restrictedContainer;
    final DivElement container;
    final DivElement inputContainer;
    final InputElement<T> inputElement;


    // ------------------------------------------------------ initialization

    AbstractFormItem(String name, String label) {
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
        container = new Elements.Builder().div().css(formGroup).end().build();
        labelElement = new Elements.Builder()
                .label()
                .css(column(labelColumns), controlLabel)
                .innerText(label).build();
        inputContainer = new Elements.Builder()
                .div()
                .css(column(inputColumns)).end().build();
        errorText = new Elements.Builder().span().css(helpBlock).end().build();
        Elements.setVisible(errorText, false);

        // @formatter:off
        Elements.Builder inputGroupBuilder = new Elements.Builder()
            .div().css(inputGroup)
                .span().css(inputGroupBtn).rememberAs(EXPRESSION_CONTAINER)
                    .button().css(btn, btnDefault)
                        .on(click, event -> ResolveExpressionEvent.fire(this, getExpressionValue()))
                        .title(CONSTANTS.expressionResolver())
                        .start("i").css(fontAwesome("link")).end()
                    .end()
                .end()
            .end();
        // @formatter:on
        inputGroupContainer = inputGroupBuilder.build();
        expressionContainer = inputGroupBuilder.referenceFor(EXPRESSION_CONTAINER);

        // @formatter:off
        Elements.Builder restrictedBuilder = new Elements.Builder()
            .div().css(inputGroup)
                .input(text).id(IdBuilder.build(name, Names.RESTRICTED)).css(formControl).rememberAs(RESTRICTED_ELEMENT)
                .span().css(inputGroupAddon)
                    .start("i").css(fontAwesome("lock")).end()
                .end()
            .end();
        // @formatter:on
        elemental.html.InputElement restrictedInput = restrictedBuilder.referenceFor(RESTRICTED_ELEMENT);
        restrictedInput.setReadOnly(true);
        restrictedInput.setValue(CONSTANTS.restricted());
        restrictedContainer = restrictedBuilder.build();

        setId(IdBuilder.build(name));
        setName(name);
        assembleUI();
    }

    /**
     * Assembles the <strong>initial</strong> widgets / containers at creation time based on the default values of this
     * form item.
     */
    void assembleUI() {
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
    public Element asElement(Form.State state) {
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

    @Override
    public void clearValue() {
        inputElement.clearValue();
    }

    void signalChange(final T value) {
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

    @Override
    public boolean isEmpty() {
        return getValue() == null || isNullOrEmpty(getText()) || isUndefined();
    }

    @Override
    public void setId(String id) {
        IdBuilder.set(inputElement.asElement(), id);
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

    @Override
    public void identifyAs(String id, String... additionalIds) {
        String fq = IdBuilder.build(id, additionalIds);
        setId(fq);
        setName(fq);
        asElement(EDITING).getDataset().setAt("formItemGroup", fq); //NON-NLS
        labelElement.getDataset().setAt("formItemLabel", fq); //NON-NLS
        inputElement.asElement().getDataset().setAt("formItemControl", fq); //NON-NLS
    }


    // ------------------------------------------------------ validation

    private void resetValidationHandlers() {
        validationHandlers.clear();
        validationHandlers.addAll(defaultValidationHandlers());
    }

    List<FormItemValidation<T>> defaultValidationHandlers() {
        return singletonList(new RequiredValidation<>(this));
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean requiresValidation() {
        if (isRequired()) {
            return true;
        }
        if (isUndefined()) {
            return false;
        }
        return (isModified() && !validationHandlers.isEmpty());
    }

    @Override
    public void addValidationHandler(FormItemValidation<T> validationHandler) {
        if (validationHandler != null) {
            validationHandlers.add(validationHandler);
        }
    }

    @Override
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

    @Override
    public void clearError() {
        Elements.setVisible(errorText, false);
        container.getClassList().remove(hasError);
    }

    @Override
    public void showError(String message) {
        container.getClassList().add(hasError);
        errorText.setInnerText(message);
        Elements.setVisible(errorText, true);
    }


    // ------------------------------------------------------ expressions

    @Override
    public boolean isExpressionValue() {
        return supportsExpressions() && hasExpressionScheme(getText());
    }

    @Override
    public void setExpressionValue(String expressionValue) {
        if (supportsExpressions()) {
            toggleExpressionSupport(true);
            setText(expressionValue);
        }
    }

    @Override
    public String getExpressionValue() {
        if (supportsExpressions()) {
            return getText();
        }
        return null;
    }

    @Override
    public void addResolveExpressionHandler(ResolveExpressionEvent.Handler handler) {
        eventBus.addHandler(ResolveExpressionEvent.TYPE, handler);
    }

    private boolean hasExpressionScheme(String value) {
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

    private boolean inExpressionState() {
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

    @Override
    public void resetMetaData() {
        setExpressionValue(null);
        setModified(false);
        setUndefined(true);
        clearError();
        // restricted cannot be reset!
    }

    @Override
    public String getLabel() {
        return labelElement.getInnerText();
    }

    @Override
    public void setLabel(final String label) {
        labelElement.setInnerText(label);
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean isUndefined() {
        return undefined;
    }

    @Override
    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    @Override
    public boolean isExpressionAllowed() {
        return expressionAllowed;
    }

    @Override
    public void setExpressionAllowed(final boolean expressionAllowed) {
        this.expressionAllowed = expressionAllowed;
    }

    @Override
    public boolean isRestricted() {
        return restricted;
    }

    @Override
    public void setRestricted(final boolean restricted) {
        if (this.restricted != restricted) {
            this.restricted = restricted;
            toggleRestricted(restricted);
        }
    }

    void toggleRestricted(final boolean on) {
        if (on) {
            container.getClassList().add(hasFeedback);
            Node firstChild = inputContainer.getChildren().item(0);
            inputContainer.removeChild(firstChild);
            inputContainer.appendChild(restrictedContainer);
        } else {
            container.getClassList().remove(hasFeedback);
            inputContainer.removeChild(restrictedContainer);
            if (isExpressionValue()) {
                inputContainer.appendChild(inputGroupContainer);
            } else {
                inputContainer.appendChild(inputElement.asElement());
            }
        }
    }

    InputElement<T> inputElement() {
        return inputElement;
    }
}
