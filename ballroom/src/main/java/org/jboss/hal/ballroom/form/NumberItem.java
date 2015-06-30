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

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalMessages;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
public class NumberItem extends FormItem<Integer> {

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);
    private final static HalMessages MESSAGES = GWT.create(HalMessages.class);

    private int min;
    private int max;

    public NumberItem(final String name, final String label) {
        this(name, label, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public NumberItem(final String name, final String label, int min, int max) {
        super(name, label);
        setRange(min, max);
    }

    @Override
    protected List<FormItemValidation<Integer>> defaultValidationHandlers() {
        return asList(new RequiredValidation<>(this), new NumberValidation(), new RangeValidation());
    }

    @Override
    protected InputElement<Integer> newInputElement() {
        NumberElement number = new NumberElement();
        number.setClassName("form-control");
        number.element.addValueChangeHandler(valueChangeEvent -> {
            String newText = inputElement.getText();
            Integer newValue = inputElement().getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(newText));
            signalChange(newValue);
        });
        // toggle expression support on the fly
        number.element.addKeyUpHandler(event -> {
            if (toggleExpressionSupport(isExpressionValue())) {
                setFocus(true);
            }
        });
        return number;
    }

    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean supportsExpressions() {
        return true;
    }


    class NumberValidation implements FormItemValidation<Integer> {

        @Override
        public ValidationResult validate(final Integer value) {
            if (!isExpressionValue()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(getText());
                    return ValidationResult.OK;
                } catch (NumberFormatException e) {
                    return ValidationResult.invalid(CONSTANTS.not_a_number());
                }
            }
            return ValidationResult.OK;
        }
    }


    class RangeValidation implements FormItemValidation<Integer> {

        @Override
        public ValidationResult validate(final Integer value) {
            if (!isExpressionValue()) {
                if ((min > Integer.MIN_VALUE && value < min) ||
                        max < Integer.MAX_VALUE && value > max) {
                    return ValidationResult.invalid(MESSAGES.invalid_range(value, min, max));
                }
            }
            return ValidationResult.OK;
        }
    }


    static class NumberElement extends InputElement<Integer> {

        final TextBox element;

        NumberElement() {
            element = new TextBox(); // type="number" not possible because of expression support
        }

        @Override
        public int getTabIndex() {
            return element.getTabIndex();
        }

        @Override
        public void setAccessKey(final char c) {
            element.setAccessKey(c);
        }

        @Override
        public void setFocus(final boolean b) {
            element.setFocus(b);
        }

        @Override
        public void setTabIndex(final int i) {
            element.setTabIndex(i);
        }

        @Override
        public boolean isEnabled() {
            return element.isEnabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setEnabled(b);
        }

        @Override
        public Integer getValue() {
            try {
                return Integer.parseInt(element.getValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        void setValue(final Integer value) {
            element.setValue(String.valueOf(value));
        }

        @Override
        void clearValue() {
            element.setValue("", false); // no events please!
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
        public Widget asWidget() {
            return element;
        }
    }
}
