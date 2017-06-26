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

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.gwt.elemento.core.EventCallbackFn;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.dmr.Deprecation;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.ENABLED;
import static org.jboss.hal.resources.CSS.controlLabel;
import static org.jboss.hal.resources.CSS.formGroup;
import static org.jboss.hal.resources.CSS.halFormInput;
import static org.jboss.hal.resources.CSS.halFormLabel;

/**
 * @author Harald Pehl
 */
public class ButtonItem extends AbstractFormItem<Void> {

    private static class ButtonReadOnlyAppearance extends ReadOnlyAppearance<Void> {

        private final String label;

        ButtonReadOnlyAppearance(String label) {
            super(EnumSet.noneOf(Decoration.class));
            this.label = label;
        }

        @Override
        public String asString(final Void value) {
            return label;
        }

        @Override
        protected String name() {
            return "ButtonReadOnlyAppearance";
        }
    }


    private static class ButtonEditingAppearance extends AbstractAppearance<Void> {

        private final HTMLElement root;
        private final HTMLButtonElement button;

        ButtonEditingAppearance(HTMLButtonElement button) {
            super(EnumSet.of(DEPRECATED, ENABLED));
            this.button = button;
            this.root = div().css(formGroup)
                    .add(labelElement = label().css(controlLabel, halFormLabel).asElement())
                    .add(div().css(halFormInput)
                            .add(button))
                    .asElement();
        }

        @Override
        public HTMLElement asElement() {
            return root;
        }

        @Override
        public void attach() {
            // noop
        }

        @Override
        protected String name() {
            return "ButtonEditingAppearance";
        }


        @Override
        public void showValue(final Void value) {
            // noop
        }

        @Override
        public void clearValue() {
            // noop
        }

        @Override
        public void setId(final String id) {
            button.id = id;
        }

        @Override
        public void setName(final String name) {
            button.name = name;
        }

        @Override
        public void setLabel(final String label) {
            labelElement.textContent = "";
        }

        @Override
        <C> void safeApply(final Decoration decoration, final C context) {
            switch (decoration) {

                case DEPRECATED:
                    markAsDeprecated((Deprecation) context);
                    break;

                case ENABLED:
                    button.disabled = true;
                    break;

                // not supported
                case RESTRICTED:
                case DEFAULT:
                case EXPRESSION:
                case HINT:
                case INVALID:
                case REQUIRED:
                case SUGGESTIONS:
                    break;
            }
        }

        @Override
        void safeUnapply(final Decoration decoration) {
            switch (decoration) {

                case DEPRECATED:
                    clearDeprecation();
                    break;

                case ENABLED:
                    button.disabled = false;
                    break;

                // not supported
                case DEFAULT:
                case EXPRESSION:
                case HINT:
                case INVALID:
                case REQUIRED:
                case RESTRICTED:
                case SUGGESTIONS:
            }
        }

        @Override
        public int getTabIndex() {
            return (int) button.tabIndex;
        }

        @Override
        public void setAccessKey(final char key) {
            button.accessKey = String.valueOf(key);
        }

        @Override
        public void setFocus(final boolean focused) {
            if (focused) {
                button.focus();
            } else {
                button.blur();
            }
        }

        @Override
        public void setTabIndex(final int index) {
            button.tabIndex = index;
        }
    }


    private final HTMLButtonElement button;

    public ButtonItem(final String name, final String label) {
        super(name, label, null);

        addAppearance(Form.State.READONLY, new ButtonReadOnlyAppearance(label));

        button = button().textContent(label).css(Button.DEFAULT_CSS).asElement();
        addAppearance(Form.State.EDITING, new ButtonEditingAppearance(button));
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    public void onClick(EventCallbackFn<MouseEvent> listener) {
        bind(button, click, listener);
    }
}
