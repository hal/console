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

import java.util.EnumSet;
import java.util.List;

import org.jboss.elemento.EventCallbackFn;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.change;
import static org.jboss.elemento.EventType.keyup;
import static org.jboss.elemento.InputType.number;
import static org.jboss.elemento.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.formControl;

public class NumberItem extends AbstractFormItem<Long> {

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER
     */
    public static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
     */
    public static final long MAX_SAFE_LONG = 9007199254740991L;

    private static final Logger logger = LoggerFactory.getLogger(NumberItem.class);
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final HTMLInputElement inputElement;
    private long min;
    private long max;

    public NumberItem(String name, String label, String unit, long min, long max) {
        super(name, label, unit);
        setRange(min, max);

        // read-only appearance
        addAppearance(Form.State.READONLY, new NumberReadOnlyAppearance());

        // editing appearance - type="number" not possible because of expression support
        inputElement = input(text).css(formControl).element();
        addAppearance(Form.State.EDITING, new NumberEditingAppearance(inputElement));
    }

    @Override
    public void attach() {
        super.attach();
        remember(bind(inputElement, change, event -> {
            String stringValue = inputElement.value;
            logger.debug("value changed to {}", stringValue);
            if (isExpressionAllowed() && hasExpressionScheme(stringValue)) {
                modifyExpressionValue(stringValue);
            } else {
                if (!Strings.isNullOrEmpty(stringValue)) {
                    try {
                        Long value = Long.parseLong(stringValue.trim());
                        modifyValue(value);
                        logger.debug("modified value to {}", value);
                    } catch (NumberFormatException ignored) {
                        // at least mark as modified and defined
                        setModified(true);
                        setUndefined(false);
                    }
                } else {
                    modifyValue(null);
                }
            }
        }));
        EventCallbackFn<Event> keyUpCallback = (__ -> {
            toggleExpressionSupport(inputElement.value);
            inputElement.focus();
        });
        remember(bind(inputElement, keyup.getName(), (e) -> keyUpCallback.onEvent(Js.cast(e))));
    }

    private void setRange(long min, long max) {
        this.min = Math.max(MIN_SAFE_LONG, min);
        this.max = Math.min(MAX_SAFE_LONG, max);
    }

    @Override
    public boolean isEmpty() {
        String numberValue = getValue() == null ? "" : getValue().toString();
        return Strings.isNullOrEmpty(isExpressionValue() ? getExpressionValue() : numberValue);
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
    public void setExpressionAllowed(boolean expressionAllowed) {
        super.setExpressionAllowed(expressionAllowed);
        if (!expressionAllowed) {
            inputElement.type = number.name();
        } else {
            inputElement.type = text.name();
        }
    }

    private static class NumberReadOnlyAppearance extends ReadOnlyAppearance<Long> {

        NumberReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HINT, RESTRICTED, STABILITY));
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
        public void showValue(Long value) {
            inputElement.value = String.valueOf(value);
        }

        @Override
        public void showExpression(String expression) {
            inputElement.value = expression;
        }

        @Override
        public void clearValue() {
            inputElement.value = "";
        }
    }

    class NumberValidation implements FormItemValidation<Long> {

        @Override
        public ValidationResult validate(Long value) {
            // use the inputElement.value instead of isEmpty because the isEmpty evaluates the getValue() method
            // the attach method already bind numbers only values, so this validation would always returns numbers only
            if (!isExpressionValue() && inputElement.value != null) {
                try {
                    Long.parseLong(inputElement.value.trim());
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
        public ValidationResult validate(Long value) {
            if (!isExpressionValue() && !isEmpty()) {
                if (value < min || value > max) {
                    return ValidationResult.invalid(MESSAGES.invalidRange(value, min, max));
                }
            }
            return ValidationResult.OK;
        }
    }
}
