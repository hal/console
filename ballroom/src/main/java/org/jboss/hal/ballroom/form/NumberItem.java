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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalMessages;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
public class NumberItem extends FormItem<Long> {

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER
     */
    public static final long MIN_SAFE_NUMBER = -9007199254740991l;

    /**
     * As defined by https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
     */
    public static final long MAX_SAFE_NUMBER = 9007199254740991l;

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);
    private final static HalMessages MESSAGES = GWT.create(HalMessages.class);

    private long min;
    private long max;

    public NumberItem(final String name, final String label) {
        this(name, label, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public NumberItem(final String name, final String label, long min, long max) {
        super(name, label);
        setRange(min, max);
    }

    @Override
    protected List<FormItemValidation<Long>> defaultValidationHandlers() {
        return asList(new RequiredValidation<>(this), new NumberValidation(), new RangeValidation());
    }

    @Override
    protected InputElement<Long> newInputElement() {
        NumberElement number = new NumberElement();
        number.setClassName("form-control");
        number.element.setOnchange(event -> {
            String newText = inputElement.getText();
            Long newValue = inputElement().getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(newText));
            signalChange(newValue);
        });
        // toggle expression support on the fly
        number.element.setOnkeyup(event -> {
            if (toggleExpressionSupport(isExpressionValue())) {
                setFocus(true);
            }
        });
        return number;
    }

    public void setRange(long min, long max) {
        this.min = Math.max(MIN_SAFE_NUMBER, min);
        this.max = Math.min(MAX_SAFE_NUMBER, max);    }

    @Override
    public boolean supportsExpressions() {
        return isExpressionAllowed();
    }


    class NumberValidation implements FormItemValidation<Long> {

        @Override
        public ValidationResult validate(final Long value) {
            if (!isExpressionValue()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Long.parseLong(getText());
                    return ValidationResult.OK;
                } catch (NumberFormatException e) {
                    return ValidationResult.invalid(CONSTANTS.not_a_number());
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
                    return ValidationResult.invalid(MESSAGES.invalid_range(value, min, max));
                }
            }
            return ValidationResult.OK;
        }
    }


    static class NumberElement extends InputElement<Long> {

        final elemental.html.InputElement element;

        NumberElement() {
            element = Browser.getDocument().createInputElement();
            // type="number" not possible because of expression support
            element.setType("text");
        }

        @Override
        public int getTabIndex() {
            return element.getTabIndex();
        }

        @Override
        public void setAccessKey(final char c) {
            element.setAccessKey(String.valueOf(c));
        }

        @Override
        public void setFocus(final boolean b) {
            if (b) {
                element.focus();
            } else {
                element.blur();
            }
        }

        @Override
        public void setTabIndex(final int i) {
            element.setTabIndex(i);
        }

        @Override
        public boolean isEnabled() {
            return !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setDisabled(!b);
        }

        @Override
        public Long getValue() {
            try {
                return Long.parseLong(element.getValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        void setValue(final Long value) {
            element.setValue(String.valueOf(value));
        }

        @Override
        void clearValue() {
            element.setValue("");
        }

        @Override
        public void setName(final String s) {
            element.setName(s);
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public String getText() {
            return element.getValue();
        }

        @Override
        public void setText(final String s) {
            element.setValue(s);
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
