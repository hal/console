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

import org.jboss.hal.ballroom.form.InputElement.Context;

import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;

/**
 * @author Harald Pehl
 */
public class CheckBoxItem extends AbstractFormItem<Boolean> {

    public CheckBoxItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<Boolean> newInputElement(Context<?> context) {
        CheckBoxElement checkBox = new CheckBoxElement();
        checkBox.element.setOnchange(event -> {
            Boolean newValue = inputElement().getValue();
            setModified(true);
            setUndefined(false);
            signalChange(newValue);
        });
        return checkBox;
    }

    @Override
    void markDefaultValue(final boolean on, final Boolean defaultValue) {
        super.markDefaultValue(on, defaultValue);
        if (on && defaultValue) {
            inputElement().setValue(true);
        }
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    static class CheckBoxElement extends AbstractCheckBoxElement {

        @Override
        public boolean isEnabled() {
            return !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setDisabled(!b);
        }

        @Override
        public Boolean getValue() {
            return element.isChecked();
        }

        @Override
        public void setValue(final Boolean value) {
            element.setChecked(value);
        }

        @Override
        public void clearValue() {
            element.setChecked(false);
        }
    }
}
