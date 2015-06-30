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

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Harald Pehl
 */
public class ComboBoxItem extends FormItem<String> {

    private ComboBoxElement comboBox;

    public ComboBoxItem(final String name, final String label, List<String> values) {
        this(name, label, values, null);
    }

    public ComboBoxItem(final String name, final String label, List<String> values, String defaultValue) {
        super(name, label);
        setValues(values, defaultValue);
    }

    @Override
    protected InputElement<String> newInputElement() {
        comboBox = new ComboBoxElement();
        comboBox.setClassName("form-control");
        comboBox.element.addChangeHandler(changeEvent -> {
            String newValue = inputElement().getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(newValue));
            signalChange(newValue);
        });
        comboBox.element.addAttachHandler(event -> {
            if (event.isAttached()) {
                selectPicker(comboBox.getId());
            }
        });
        return comboBox;
    }

    private native void selectPicker(String id) /*-{
        $wnd.$("#" + id).selectpicker();
    }-*/;

    public void setValues(List<String> values, String defaultValue) {
        comboBox.setValues(values, defaultValue);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    static class ComboBoxElement extends InputElement<String> {

        final ListBox element;
        int defaultIndex;

        ComboBoxElement() {
            element = new ListBox();
            element.setMultipleSelect(false);
            element.setVisibleItemCount(1);
        }

        void setValues(List<String> values, String defaultValue) {
            int index = 0;
            defaultIndex = 0;
            for (String value : values) {
                element.addItem(value);
                if (defaultValue != null && defaultValue.equals(value)) {
                    defaultIndex = index;
                }
                index++;
            }
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
        public String getValue() {
            return element.getSelectedValue();
        }

        @Override
        void setValue(final String value) {
            for (int i = 0; i < element.getItemCount(); i++) {
                if (element.getItemText(i).equals(value)) {
                    element.setSelectedIndex(i);
                    break;
                }
            }
        }

        @Override
        void clearValue() {
            element.setSelectedIndex(defaultIndex);
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
            return getValue();
        }

        @Override
        public void setText(final String s) {
            setValue(s);
        }

        @Override
        public Widget asWidget() {
            return element;
        }
    }
}
