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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.OptionElement;
import elemental.html.SelectElement;
import org.jboss.gwt.elemento.core.Elements;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * TODO isModified() does not work!
 * @author Harald Pehl
 */
public class SelectBoxItem extends AbstractFormItem<String> {

    private SelectBoxElement comboBox;

    public SelectBoxItem(final String name, final String label, List<String> values) {
        super(name, label);
        setValues(values);
    }

    @Override
    protected InputElement<String> newInputElement() {
        comboBox = new SelectBoxElement();
        comboBox.setClassName(formControl + " " + selectpicker);
        //noinspection Duplicates
        comboBox.element.setOnchange(event -> {
            String newValue = inputElement().getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(newValue));
            signalChange(newValue);
        });
        return comboBox;
    }

    public void setValues(List<String> values) {
        comboBox.setValues(values);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public boolean isUndefined() {
        // As for now a select box has always a value and is as such never undefined
        // TODO Check if there's a use case when the user wants to clear / undefine the select box
        return false;
    }


    static class SelectBoxElement extends InputElement<String> {

        final SelectElement element;
        final BiMap<Integer, String> indexedValues;
        int defaultIndex;

        SelectBoxElement() {
            element = Browser.getDocument().createSelectElement();
            element.setMultiple(false);
            element.setSize(1);
            indexedValues = HashBiMap.create();
        }

        void setValues(List<String> values) {
            String currentValue = getValue();

            indexedValues.clear();
            Elements.removeChildrenFrom(element);

            int i = 0;
            defaultIndex = 0;
            for (String value : values) {
                OptionElement option = Browser.getDocument().createOptionElement();
                option.setText(value);
                element.appendChild(option);
                indexedValues.put(i, value);
                if (value.equals(currentValue)) {
                    defaultIndex = i;
                }
                i++;
            }
            element.setSelectedIndex(defaultIndex);
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
        public String getValue() {
            int selectedIndex = element.getSelectedIndex();
            if (indexedValues.containsKey(selectedIndex)) {
                return indexedValues.get(selectedIndex);
            }
            return null;
        }

        @Override
        public void setValue(final String value) {
            if (indexedValues.containsValue(value)) {
                element.setSelectedIndex(indexedValues.inverse().get(value));
            }
        }

        @Override
        public void clearValue() {
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
        public Element asElement() {
            return element;
        }
    }
}
