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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Focusable;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form.State;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent.ResolveExpressionHandler;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.Deprecation;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ballroom.form.Decoration.*;

/**
 * Base class for all form item implementations. Contains central logic for handling (default) values, various flags,
 * validation, expressions and event handling. All UI and DOM related code can be found in {@linkplain Appearance
 * appearances}.
 * <p>
 * A form item carries three different values:
 * <ol>
 * <li>{@linkplain #getValue() value}: The value of this form item which has the type {@code T}</li>
 * <li>{@linkplain #getExpressionValue() expression value}: The expression value of this form item (if expressions are
 * {@linkplain #supportsExpressions() supported}). The expression value is <em>always</em> a string.</li>
 * <li>default value: The default value of this form item (if any) which has the type {@code T}</li>
 * </ol>
 * <p>
 * The value and the expression value are mutual exclusive. Only one of them is allowed to be non-null.
 *
 * @param <T> The type of the form item's value.
 *
 * @author Harald Pehl
 */
public abstract class AbstractFormItem<T> implements FormItem<T> {

    @FunctionalInterface
    interface ExpressionCallback {

        void resolveExpression(String expression);
    }


    static class ExpressionContext {

        final String expression;
        final ExpressionCallback callback;

        ExpressionContext(final String expression, final ExpressionCallback callback) {
            this.expression = expression;
            this.callback = callback;
        }
    }


    // all form items share the same event bus
    private static final EventBus EVENT_BUS = new SimpleEventBus();


    private String name;
    private final String label;
    private final String hint;
    private T value;
    private T defaultValue;
    private String expressionValue;

    private boolean required;
    private boolean modified;
    private boolean undefined;
    private boolean restricted;
    private boolean enabled;
    private boolean expressionAllowed;
    private Deprecation deprecation;

    private Form form;
    private final Map<State, Appearance<T>> appearances;
    private SuggestHandler suggestHandler;
    private final List<FormItemValidation<T>> validationHandlers;
    private final List<ResolveExpressionHandler> resolveExpressionHandlers;
    private final List<com.google.web.bindery.event.shared.HandlerRegistration> handlers;

    AbstractFormItem(final String name, final String label, final String hint) {
        this.name = name;
        this.label = label;
        this.hint = hint;
        this.value = null;
        this.defaultValue = null;
        this.expressionValue = null;

        this.required = false;
        this.modified = false;
        this.undefined = true;
        this.restricted = false;
        this.enabled = true;
        this.expressionAllowed = true;
        this.deprecation = null;

        this.appearances = new HashMap<>();
        this.suggestHandler = null;
        this.validationHandlers = new LinkedList<>();
        this.validationHandlers.addAll(defaultValidationHandlers());
        this.resolveExpressionHandlers = new LinkedList<>();
        this.handlers = new ArrayList<>();
    }

    protected void addAppearance(State state, Appearance<T> appearance) {
        appearances.put(state, appearance);
        appearance.setLabel(label);
        if (hint != null) {
            appearance.apply(HINT, hint);
        }
    }

    /**
     * Store the event handler registration to remove them in {@link #detach()}.
     */
    protected void remember(com.google.web.bindery.event.shared.HandlerRegistration handler) {
        handlers.add(handler);
    }


    // ------------------------------------------------------ element and appearance

    @Override
    public HTMLElement asElement(final State state) {
        if (appearances.containsKey(state)) {
            return appearances.get(state).asElement();
        } else {
            throw new IllegalStateException("Unknown state in FormItem.asElement(" + state + ")");
        }
    }

    /**
     * Calls {@code SuggestHandler.attach()} in case there was one registered. If you override this method, please
     * call {@code super.attach()} to keep this behaviour.
     */
    @Override
    public void attach() {
        if (form != null) {
            // if there's a back reference use it to attach only the appearances which are supported by the form
            for (Map.Entry<State, Appearance<T>> entry : appearances.entrySet()) {
                State state = entry.getKey();
                if (form.getStateMachine().supports(state)) {
                    Appearance<T> appearance = entry.getValue();
                    appearance.attach();
                }
            }
            if (form.getStateMachine().supports(State.EDITING) && suggestHandler instanceof Attachable) {
                ((Attachable) suggestHandler).attach();
            }

        } else {
            appearances.values().forEach(Appearance::attach);
            if (suggestHandler instanceof Attachable) {
                ((Attachable) suggestHandler).attach();
            }
        }
    }

    @Override
    public void detach() {
        if (suggestHandler instanceof Attachable) {
            ((Attachable) suggestHandler).detach();
        }
        appearances.values().forEach(Appearance::detach);
        for (com.google.web.bindery.event.shared.HandlerRegistration handler : handlers) {
            handler.removeHandler();
        }
        handlers.clear();
    }

    private void apply(Decoration decoration) {
        apply(decoration, null);
    }

    private <C> void apply(Decoration decoration, C context) {
        appearances.values().forEach(a -> a.apply(decoration, context));
    }

    private void unapply(Decoration decoration) {
        appearances.values().forEach(a -> a.unapply(decoration));
    }

    private Optional<Appearance<T>> appearance(State state) {
        if (appearances.containsKey(state)) {
            return Optional.of(appearances.get(state));
        }
        return Optional.empty();
    }


    // ------------------------------------------------------ id, value & name

    @Override
    public String getId(final State state) {
        return appearance(state).map(Appearance::getId).orElse(null);
    }

    @Override
    public void setId(String id) {
        appearances.values().forEach(a -> a.setId(id));
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(final T value) {
        setValue(value, false);
    }

    /**
     * Sets the form item's value and shows the value in the appearances. Sets the expression value to {@code null}.
     * Does not touch the {@code modified} and {@code undefined} flags. Should be called from business code like form
     * mapping.
     */
    @Override
    public void setValue(final T value, final boolean fireEvent) {
        this.value = value;
        this.expressionValue = null;

        appearances.values().forEach(a -> {
            a.showValue(value);
            if (isEmpty() && defaultValue != null) {
                a.apply(DEFAULT, a.asString(defaultValue));
            } else {
                a.unapply(DEFAULT);
            }
            if (supportsExpressions()) {
                a.unapply(EXPRESSION);
            }
        });

        if (fireEvent) {
            signalChange(value);
        }
    }

    /**
     * Assigns a new value to the internal value and adjusts the {@code modified} and {@code undefined} flags.
     * Should be called from change handlers. Does not update any appearances nor apply / unapply decorations.
     */
    protected void modifyValue(T newValue) {
        this.value = newValue;
        this.expressionValue = null;

        setModified(true);
        setUndefined(isEmpty());
        signalChange(newValue);
    }

    /**
     * Sets the value and expression value to {@code null}, {@linkplain #clearError() clears any error marker} and
     * shows the default value (if any). Does not touch the {@code modified} and {@code undefined} flags. Should be
     * called from business code like form mapping.
     */
    @Override
    public void clearValue() {
        this.value = null;
        this.expressionValue = null;

        appearances.values().forEach((a) -> {
            a.clearValue();
            a.unapply(INVALID);
            if (supportsExpressions()) {
                a.unapply(EXPRESSION);
            }
        });
        markDefaultValue(defaultValue != null);
    }

    /**
     * Stores the default value for later use. The default value will be used in {@link #setValue(Object)} (if the
     * value is null or empty) and {@link #clearValue()}. Calling this method will <strong>not</strong> immediately
     * show the default value.
     */
    @Override
    public void assignDefaultValue(final T defaultValue) {
        this.defaultValue = defaultValue;
    }

    private void markDefaultValue(final boolean on) {
        if (on) {
            appearances.values().forEach(a -> a.apply(DEFAULT, a.asString(defaultValue)));
        } else {
            unapply(DEFAULT);
        }
    }

    @Override
    public void mask() {
        apply(SENSITIVE);
    }

    @Override
    public void unmask() {
        unapply(SENSITIVE);
    }

    private void signalChange(final T value) {
        ValueChangeEvent.fire(this, value);
    }

    @Override
    public void fireEvent(final GwtEvent<?> gwtEvent) {
        EVENT_BUS.fireEvent(gwtEvent);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> valueChangeHandler) {
        return EVENT_BUS.addHandler(ValueChangeEvent.getType(), valueChangeHandler);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
        appearances.values().forEach(a -> a.setName(name));
    }


    // ------------------------------------------------------ validation

    List<FormItemValidation<T>> defaultValidationHandlers() {
        return singletonList(new RequiredValidation<>(this));
    }

    @SuppressWarnings("SimplifiableIfStatement")
    final boolean requiresValidation() {
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

    void removeValidationHandler(FormItemValidation<T> validationHandler) {
        if (validationHandler != null) {
            validationHandlers.remove(validationHandler);
        }
    }

    @Override
    public boolean validate() {
        if (requiresValidation()) {
            for (FormItemValidation<T> validationHandler : validationHandlers) {
                ValidationResult result = validationHandler.validate(value);
                if (!result.isValid()) {
                    showError(result.getMessage());
                    return false;
                }
            }
        }
        clearError();
        return true;
    }

    /**
     * Clears any error markers. This method {@linkplain Appearance#unapply(Decoration) unapplies} the {@linkplain
     * Decoration#INVALID INVALID} decoration.
     */
    @Override
    public void clearError() {
        unapply(INVALID);
    }

    /**
     * Shows the specified error message. This method {@linkplain Appearance#apply(Decoration, Object) applies} the
     * {@linkplain Decoration#INVALID INVALID} decoration using the error message as context.
     */
    @Override
    public void showError(String message) {
        apply(INVALID, message);
    }


    // ------------------------------------------------------ expressions

    @Override
    public boolean isExpressionAllowed() {
        return expressionAllowed;
    }

    @Override
    public void setExpressionAllowed(final boolean expressionAllowed) {
        this.expressionAllowed = expressionAllowed;
    }

    @Override
    public boolean isExpressionValue() {
        return supportsExpressions() && hasExpressionScheme(expressionValue);
    }

    @Override
    public String getExpressionValue() {
        return expressionValue;
    }

    /**
     * Sets the form item's expression value, applies the {@link Decoration#EXPRESSION} decoration and shows the
     * expression value in the appearances. Sets the value to {@code null}. Does not touch the {@code modified} and
     * {@code undefined} flags. Should be called from business code like form mapping.
     */
    @Override
    public void setExpressionValue(final String expressionValue) {
        this.value = null;
        this.expressionValue = expressionValue;

        appearances.values().forEach(a -> {
            a.unapply(DEFAULT);
            a.showExpression(expressionValue);
        });
        toggleExpressionSupport(expressionValue);
    }

    /**
     * Assigns a new value to the internal expression value and adjusts the {@code modified} and {@code undefined}
     * flags. Does not update any appearances nor apply / unapply decorations. Should be called from change handlers.
     */
    protected void modifyExpressionValue(String newExpressionValue) {
        this.value = null;
        this.expressionValue = newExpressionValue;

        setModified(true);
        setUndefined(isEmpty());
    }

    @Override
    public void addResolveExpressionHandler(ResolveExpressionHandler handler) {
        resolveExpressionHandlers.add(handler);
    }

    void toggleExpressionSupport(String expressionValue) {
        // TODO Find a way how to use the expression resolver in modals
        if (!isModal()) {
            if (supportsExpressions() && hasExpressionScheme(expressionValue)) {
                applyExpressionValue(expressionValue);
            } else {
                unapply(EXPRESSION);
            }
        }
    }

    void applyExpressionValue(String expressionValue) {
        ExpressionContext expressionContext = new ExpressionContext(expressionValue,
                expression -> {
                    ResolveExpressionEvent ree = new ResolveExpressionEvent(expression);
                    resolveExpressionHandlers.forEach(handler -> handler.onResolveExpression(ree));
                });
        apply(EXPRESSION, expressionContext);
    }

    boolean hasExpressionScheme(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    boolean isModal() {
        // extra method to support unit tests
        return Dialog.isOpen() || Wizard.isOpen();
    }


    // ------------------------------------------------------ suggestion handler

    @Override
    public void registerSuggestHandler(final SuggestHandler suggestHandler) {
        this.suggestHandler = suggestHandler;
        this.suggestHandler.setFormItem(this);
        apply(SUGGESTIONS, suggestHandler);
    }

    public void onSuggest(final String suggestion) {
        // nop
    }


    // ------------------------------------------------------ flags and properties

    @Override
    public boolean isRestricted() {
        return restricted;
    }

    @Override
    public void setRestricted(final boolean restricted) {
        if (this.restricted != restricted) {
            this.restricted = restricted;
            if (restricted) {
                apply(RESTRICTED);
            } else {
                unapply(RESTRICTED);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                apply(ENABLED);
            } else {
                unapply(ENABLED);
            }
        }
    }

    @Override
    public int getTabIndex() {
        Optional<Appearance<T>> appearance = appearance(State.EDITING);
        return appearance.map(Focusable::getTabIndex).orElse(-1);
    }

    @Override
    public void setTabIndex(final int index) {
        Optional<Appearance<T>> appearance = appearance(State.EDITING);
        appearance.ifPresent((Appearance<T> a) -> a.setTabIndex(index));
    }

    @Override
    public void setAccessKey(final char accessKey) {
        Optional<Appearance<T>> appearance = appearance(State.EDITING);
        appearance.ifPresent((Appearance<T> a) -> a.setAccessKey(accessKey));
    }

    @Override
    public void setFocus(final boolean focus) {
        Optional<Appearance<T>> appearance = appearance(State.EDITING);
        appearance.ifPresent((Appearance<T> a) -> a.setFocus(focus));
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(final String label) {
        appearances.values().forEach(a -> a.setLabel(label));
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        if (this.required != required) {
            this.required = required;
            if (required) {
                apply(REQUIRED);
            } else {
                unapply(REQUIRED);
            }
        }
    }

    @Override
    public final boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public final boolean isUndefined() {
        return undefined;
    }

    @Override
    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    @Override
    public boolean isDeprecated() {
        return deprecation != null && deprecation.isDefined();
    }

    @Override
    public void setDeprecated(final Deprecation deprecation) {
        this.deprecation = deprecation;
        if (deprecation != null && deprecation.isDefined()) {
            apply(DEPRECATED, deprecation);
        } else {
            unapply(DEPRECATED);
        }
    }

    void setForm(final Form form) {
        this.form = form;
    }
}
