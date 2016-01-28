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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.form.InputElement.Context;

import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;

/**
 * @author Harald Pehl
 */
public class CheckBoxItem extends AbstractFormItem<Boolean> {

    public CheckBoxItem(final String name, final String label) {
        super(name, label, EMPTY_CONTEXT);
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
    public boolean supportsExpressions() {
        return false;
    }


    static class CheckBoxElement extends InputElement<Boolean> {

        final elemental.html.InputElement element;

        CheckBoxElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("checkbox"); //NON-NLS
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
            return null;
        }

        @Override
        public void setText(final String s) {
            // not supported
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
