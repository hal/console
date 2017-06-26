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
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.LabelBuilder;

import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
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

        TextBoxEditingAppearance(HTMLInputElement inputElement) {
            super(EnumSet.allOf(Decoration.class), inputElement);
        }

        @Override
        protected String name() {
            return "TextBoxEditingAppearance";
        }

        @Override
        public void showValue(final String value) {
            inputElement.value = value;
        }

        @Override
        public void showExpression(final String expression) {
            inputElement.value = expression;
        }

        @Override
        public void clearValue() {
            inputElement.value = "";
        }
    }


    public TextBoxItem(final String name) {
        this(name, new LabelBuilder().label(name), null);
    }

    public TextBoxItem(final String name, final String label) {
        this(name, label, null);
    }

    public TextBoxItem(final String name, final String label, final String hint) {
        super(name, label, hint);

        // read-only appearance
        addAppearance(Form.State.READONLY, new TextBoxReadOnlyAppearance());

        // editing appearance
        HTMLInputElement inputElement = input(text)
                .css(formControl)
                .asElement();

        bind(inputElement, change, event -> {
            if (hasExpressionScheme(inputElement.value)) {
                modifyExpressionValue(inputElement.value);
            } else {
                modifyValue(inputElement.value);
            }
        });
        bind(inputElement, keyup, event -> {
            toggleExpressionSupport(inputElement.value);
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
