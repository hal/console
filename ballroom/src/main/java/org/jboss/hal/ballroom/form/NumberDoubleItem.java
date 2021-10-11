/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.number;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.formControl;

public class NumberDoubleItem extends AbstractFormItem<Double> {

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER
     */
    private static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
     */
    private static final long MAX_SAFE_LONG = 9007199254740991L;

    private static final Logger logger = LoggerFactory.getLogger(NumberDoubleItem.class);
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final HTMLInputElement inputElement;
    private long min;
    private long max;

    public NumberDoubleItem(String name, String label, String unit, long min, long max, SafeHtml helpText) {
        super(name, label, unit, helpText);
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
                        Double value = Double.parseDouble(stringValue);
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
        remember(bind(inputElement, keyup, event -> {
            toggleExpressionSupport(inputElement.value);
            inputElement.focus();
        }));
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
    protected List<FormItemValidation<Double>> defaultValidationHandlers() {
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


    private static class NumberReadOnlyAppearance extends ReadOnlyAppearance<Double> {

        NumberReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HELP, HINT, RESTRICTED));
        }

        @Override
        protected String name() {
            return "NumberSelectReadOnlyAppearance";
        }
    }


    private static class NumberEditingAppearance extends EditingAppearance<Double> {

        NumberEditingAppearance(HTMLInputElement inputElement) {
            super(EnumSet.allOf(Decoration.class), inputElement);
        }

        @Override
        protected String name() {
            return "NumberSelectEditingAppearance";
        }

        @Override
        public void showValue(Double value) {
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


    class NumberValidation implements FormItemValidation<Double> {

        @Override
        public ValidationResult validate(Double value) {
            if (!isExpressionValue() && !isEmpty()) {
                try {
                    Double.parseDouble(inputElement.value);
                    return ValidationResult.OK;
                } catch (NumberFormatException e) {
                    return ValidationResult.invalid(CONSTANTS.notANumber());
                }
            }
            return ValidationResult.OK;
        }
    }


    class RangeValidation implements FormItemValidation<Double> {

        @Override
        public ValidationResult validate(Double value) {
            if (!isExpressionValue() && !isEmpty()) {
                if (value < min || value > max) {
                    return ValidationResult.invalid(MESSAGES.invalidRange(value, min, max));
                }
            }
            return ValidationResult.OK;
        }
    }
}
