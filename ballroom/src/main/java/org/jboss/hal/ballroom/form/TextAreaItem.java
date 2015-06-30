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
import com.google.common.base.Splitter;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;

/**
 * @author Harald Pehl
 */
public class TextAreaItem extends FormItem<List<String>> {

    public TextAreaItem(final String name, final String label) {
        super(name, label);
    }

    @Override
    protected InputElement<List<String>> newInputElement() {
        TextAreaElement textArea = new TextAreaElement();
        textArea.setClassName("form-control");
        textArea.element.addValueChangeHandler(valueChangeEvent -> {
            List<String> newValue = inputElement().getValue();
            setModified(true);
            setUndefined(newValue.isEmpty());
            signalChange(newValue);
        });
        return textArea;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    static class TextAreaElement extends InputElement<List<String>> {

        final TextArea element;

        TextAreaElement() {
            element = new TextArea();
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
        public List<String> getValue() {
            return asList(element.getValue());
        }

        @Override
        public void setValue(final List<String> values) {
            element.setValue(asString(values));
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
            return asString(getValue());
        }

        @Override
        public void setText(final String s) {
            setValue(asList(s));
        }

        @Override
        public Widget asWidget() {
            return element;
        }

        private List<String> asList(String value) {
            if (isNullOrEmpty(value)) {
                return emptyList();
            }
            return Splitter.on('\n')
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(value);
        }

        private String asString(final List<String> values) {
            return Joiner.on('\n')
                    .skipNulls()
                    .join(values);
        }
    }
}
