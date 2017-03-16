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

import java.util.EnumSet;

import com.google.common.base.Strings;
import elemental.client.Browser;

import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.formControl;

/**
 * @author Harald Pehl
 */
public class TextBoxItem extends AbstractFormItem<String> {

    private static class TextBoxReadOnlyAppearance extends ReadOnlyAppearance<String> {

        TextBoxReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HINT, RESTRICTED, SENSITIVE));
        }

        @Override
        protected String name() {
            return "TextBoxReadOnlyAppearance";
        }
    }


    private static class TextBoxEditingAppearance extends EditingAppearance<String> {

        TextBoxEditingAppearance(elemental.html.InputElement inputElement) {
            super(EnumSet.allOf(Decoration.class), inputElement);
        }

        @Override
        protected String name() {
            return "TextBoxEditingAppearance";
        }

        @Override
        public void showValue(final String value) {
            inputElement.setValue(value);
        }

        @Override
        public void showExpression(final String expression) {
            inputElement.setValue(expression);
        }

        @Override
        public void clearValue() {
            inputElement.setValue("");
        }
    }


    public TextBoxItem(final String name, final String label) {
        this(name, label, null);
    }

    public TextBoxItem(final String name, final String label, final String hint) {
        super(name, label, hint);

        // read-only appearance
        addAppearance(Form.State.READONLY, new TextBoxReadOnlyAppearance());

        // editing appearance
        elemental.html.InputElement inputElement = Browser.getDocument().createInputElement();
        inputElement.setType("text"); //NON-NLS
        inputElement.getClassList().add(formControl);

        inputElement.setOnchange(event -> {
            String value = inputElement.getValue();
            if (hasExpressionScheme(value)) {
                modifyExpressionValue(value);
            } else {
                modifyValue(value);
            }
        });
        inputElement.setOnkeyup(event -> {
            toggleExpressionSupport(inputElement.getValue());
            inputElement.focus();
        });

        addAppearance(EDITING, new TextBoxEditingAppearance(inputElement));
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(isExpressionValue() ? getExpressionValue() : getValue());
    }

    @Override
    public boolean supportsExpressions() {
        return isExpressionAllowed();
    }

    @Override
    public void onSuggest(final String suggestion) {
        modifyValue(suggestion);
    }
}
