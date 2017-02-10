/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import elemental.client.Browser;
import elemental.dom.Element;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.hal.ballroom.form.CreationContext.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.formControl;

/**
 * @author Harald Pehl
 */
public class TextBoxItem extends AbstractFormItem<String> {

    public TextBoxItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<String> newInputElement(CreationContext<?> context) {
        TextBoxElement textBox = new TextBoxElement();
        setupInputElement(textBox);
        return textBox;
    }

    final void setupInputElement(final TextBoxElement textBox) {
        textBox.setClassName(formControl);
        //noinspection Duplicates
        textBox.element.setOnchange(event -> {
            String newValue = inputElement().getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(newValue));
            signalChange(newValue);
        });
        // toggle expression support on the fly
        textBox.element.setOnkeyup(event -> {
            if (toggleExpressionSupport(isExpressionValue())) {
                setFocus(true);
            }
        });
    }

    @Override
    public void onSuggest(final String suggestion) {
        setValue(suggestion);
        setModified(true);
        setUndefined(false);
    }

    @Override
    public boolean supportsExpressions() {
        return isExpressionAllowed();
    }

    @Override
    public boolean isEmpty() {
        return isNullOrEmpty(getValue()) || isUndefined();
    }


    static class TextBoxElement extends InputElement<String> {

        final elemental.html.InputElement element;

        TextBoxElement() {
            element = Browser.getDocument().createInputElement();
            element.setType("text"); //NON-NLS
        }

        TextBoxElement(final elemental.html.InputElement element) {
            this.element = element;
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
            return element.getValue();
        }

        @Override
        public void setValue(final String value) {
            element.setValue(value);
        }

        @Override
        public void clearValue() {
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
            return getValue();
        }

        @Override
        public void setText(final String s) {
            setValue(s);
        }

        @Override
        public void setPlaceholder(final String placeholder) {
            element.setPlaceholder(placeholder);
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
