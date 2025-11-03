/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.EnumSet;

import org.jboss.elemento.EventCallbackFn;
import org.jboss.hal.ballroom.LabelBuilder;

import com.google.common.base.Strings;

import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.change;
import static org.jboss.elemento.EventType.keyup;
import static org.jboss.elemento.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.formControl;

public class TextBoxItem extends AbstractFormItem<String> {

    private HTMLInputElement inputElement;

    public TextBoxItem(String name) {
        this(name, new LabelBuilder().label(name), null);
    }

    public TextBoxItem(String name, String label) {
        this(name, label, null);
    }

    public TextBoxItem(String name, String label, String hint) {
        super(name, label, hint);

        // read-only appearance
        addAppearance(Form.State.READONLY, new TextBoxReadOnlyAppearance());

        // editing appearance
        inputElement = input(text)
                .css(formControl).element();

        addAppearance(EDITING, new TextBoxEditingAppearance(inputElement));
    }

    @Override
    public void attach() {
        super.attach();
        remember(bind(inputElement, change, event -> {
            if (hasExpressionScheme(inputElement.value)) {
                modifyExpressionValue(inputElement.value);
            } else {
                modifyValue(inputElement.value);
            }
        }));
        EventCallbackFn<Event> keyUpCallback = (__ -> {
            toggleExpressionSupport(inputElement.value);
            inputElement.focus();
        });
        remember(bind(inputElement, keyup.getName(), (e) -> keyUpCallback.onEvent(Js.cast(e))));
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
    public void onSuggest(String suggestion) {
        modifyValue(suggestion);
    }

    private static class TextBoxReadOnlyAppearance extends ReadOnlyAppearance<String> {

        TextBoxReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HINT, RESTRICTED, SENSITIVE, STABILITY));
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
        public void showValue(String value) {
            inputElement.value = value;
        }

        @Override
        public void showExpression(String expression) {
            inputElement.value = expression;
        }

        @Override
        public void clearValue() {
            inputElement.value = "";
        }
    }
}
