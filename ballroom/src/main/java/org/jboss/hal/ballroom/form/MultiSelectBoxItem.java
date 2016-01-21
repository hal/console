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

import com.google.common.base.Joiner;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * @author Harald Pehl
 */
public class MultiSelectBoxItem extends AbstractFormItem<List<String>> {

    private static <T> List<T> asList(JsArrayOf<T> array) {
        List<T> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            list.add(array.get(i));
        }
        return list;
    }

    private static <T> JsArrayOf<T> asArray(List<T> list) {
        JsArrayOf<T> array = JsArrayOf.create();
        for (T t : list) {
            array.push(t);
        }
        return array;
    }


    private MultiSelectBoxElement selectBox;

    public MultiSelectBoxItem(final String name, final String label, List<String> options) {
        super(name, label);
        setOptions(options);
    }

    @Override
    protected InputElement<List<String>> newInputElement() {
        selectBox = new MultiSelectBoxElement();
        selectBox.setClassName(formControl + " " + selectpicker);
        Selectpicker.Multi.element(selectBox.asElement()).onChange((event, index) -> {
            List<String> value = getValue();
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
        return selectBox;
    }

    public void setOptions(List<String> options) {
        selectBox.setOptions(options);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public boolean isUndefined() {
        return getValue().isEmpty();
    }


    static class MultiSelectBoxElement extends SelectBoxElement<List<String>> {

        MultiSelectBoxElement() {
            super(true, true);
        }

        @Override
        public List<String> getValue() {
            return asList(Selectpicker.Multi.element(asElement()).getValue());
        }

        @Override
        public void setValue(final List<String> value) {
            Selectpicker.Multi.element(asElement()).setValue(asArray(value));
        }

        @Override
        public void clearValue() {
            Selectpicker.Multi.element(asElement()).clear();
        }

        @Override
        public String getText() {
            return Joiner.on(',').join(getValue());
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
