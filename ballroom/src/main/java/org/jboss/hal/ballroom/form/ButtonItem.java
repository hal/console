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

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.dmr.Deprecation;

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

        private final Element root;
        private final ButtonElement button;

        ButtonEditingAppearance(ButtonElement button) {
            super(EnumSet.of(DEPRECATED, ENABLED));
            this.button = button;

            // @formatter:off
            Elements.Builder builder = new Elements.Builder()
                .div().css(formGroup)
                    .label().css(controlLabel, halFormLabel).rememberAs(LABEL_ELEMENT).end()
                    .div().css(halFormInput)
                        .add(button)
                    .end()
                .end();
            // @formatter:on

            labelElement = builder.referenceFor(LABEL_ELEMENT);
            root = builder.build();
        }

        @Override
        public Element asElement() {
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
            button.setId(id);
        }

        @Override
        public void setName(final String name) {
            button.setName(name);
        }

        @Override
        public void setLabel(final String label) {
            labelElement.setTextContent("");
        }

        @Override
        <C> void safeApply(final Decoration decoration, final C context) {
            switch (decoration) {

                case DEPRECATED:
                    markAsDeprecated((Deprecation) context);
                    break;

                case ENABLED:
                    button.setDisabled(true);
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
                    button.setDisabled(false);
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
            return button.getTabIndex();
        }

        @Override
        public void setAccessKey(final char key) {
            button.setAccessKey(String.valueOf(key));
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
            button.setTabIndex(index);
        }
    }


    private final ButtonElement button;

    public ButtonItem(final String name, final String label) {
        super(name, label, null);

        addAppearance(Form.State.READONLY, new ButtonReadOnlyAppearance(label));

        button = Browser.getDocument().createButtonElement();
        button.setTextContent(label);
        button.setClassName(Button.DEFAULT_CSS);
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

    public void onClick(EventListener listener) {
        button.setOnclick(listener);
    }
}
