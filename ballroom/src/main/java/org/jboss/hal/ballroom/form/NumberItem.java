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

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Strings;
import elemental2.dom.HTMLInputElement;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.number;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.formControl;

/**
 * @author Harald Pehl
 */
public class NumberItem extends AbstractFormItem<Long> {

    private static class NumberReadOnlyAppearance extends ReadOnlyAppearance<Long> {

        NumberReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HINT, RESTRICTED));
        }

        @Override
        protected String name() {
            return "NumberSelectReadOnlyAppearance";
        }
    }


    private static class NumberEditingAppearance extends EditingAppearance<Long> {

        NumberEditingAppearance(HTMLInputElement inputElement) {
            super(EnumSet.allOf(Decoration.class), inputElement);
        }

        @Override
        protected String name() {
            return "NumberSelectEditingAppearance";
        }

        @Override
        public void showValue(final Long value) {
            inputElement.value = String.valueOf(value);
        }

        @Override
        public void showExpression(final String expression) {
            inputElement.value = expression;
        }

        @Override
        public void clearValue() {
            inputElement.value = "";
        }
    }


    class NumberValidation implements FormItemValidation<Long> {

        @Override
        public ValidationResult validate(final Long value) {
            if (!isExpressionValue()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Long.parseLong(inputElement.value);
                    return ValidationResult.OK;
                } catch (NumberFormatException e) {
                    return ValidationResult.invalid(CONSTANTS.notANumber());
                }
            }
            return ValidationResult.OK;
        }
    }


    class RangeValidation implements FormItemValidation<Long> {

        @Override
        public ValidationResult validate(final Long value) {
            if (!isExpressionValue()) {
                if (value < min || value > max) {
                    return ValidationResult.invalid(MESSAGES.invalidRange(value, min, max));
                }
            }
            return ValidationResult.OK;
        }
    }


    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER
     */
    public static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
     */
    public static final long MAX_SAFE_LONG = 9007199254740991L;

    private final HTMLInputElement inputElement;
    private long min;
    private long max;

    public NumberItem(final String name, final String label, final String unit, long min, long max) {
        super(name, label, unit);
        setRange(min, max);

        // read-only appearance
        addAppearance(Form.State.READONLY, new NumberReadOnlyAppearance());

        // editing appearance - type="number" not possible because of expression support
        inputElement = input(text).css(formControl).asElement();

        remember(bind(inputElement, change, event -> {
            String stringValue = inputElement.value;
            if (isExpressionAllowed() && hasExpressionScheme(stringValue)) {
                modifyExpressionValue(stringValue);
            } else {
                if (!Strings.isNullOrEmpty(stringValue)) {
                    try {
                        Long value = Long.parseLong(stringValue);
                        modifyValue(value);
                    } catch (NumberFormatException e) {
                        // at least mark as modified and undefined
                        setModified(true);
                        setUndefined(false);
                    }
                }
            }
        }));
        remember(bind(inputElement, keyup, event -> {
            toggleExpressionSupport(inputElement.value);
            inputElement.focus();
        }));

        addAppearance(Form.State.EDITING, new NumberEditingAppearance(inputElement));
    }

    private void setRange(long min, long max) {
        this.min = Math.max(MIN_SAFE_LONG, min);
        this.max = Math.min(MAX_SAFE_LONG, max);
    }

    @Override
    public boolean isEmpty() {
        return isExpressionValue() ? Strings.isNullOrEmpty(getExpressionValue()) : getValue() == null;
    }

    @Override
    protected List<FormItemValidation<Long>> defaultValidationHandlers() {
        return asList(new RequiredValidation<>(this), new NumberValidation(), new RangeValidation());
    }

    @Override
    public boolean supportsExpressions() {
        return isExpressionAllowed();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void setExpressionAllowed(final boolean expressionAllowed) {
        super.setExpressionAllowed(expressionAllowed);
        if (!expressionAllowed) {
            inputElement.type = number.name();
        } else {
            inputElement.type = text.name();
        }
    }
}
