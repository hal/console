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
import java.util.HashMap;
import java.util.Map;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.*;

/** A form item to select from a small set of distinct numbers using a button group. */
public class NumberSelectItem extends AbstractFormItem<Long> {

    public NumberSelectItem(String name, long[] numbers) {
        this(name, new LabelBuilder().label(name), numbers);
    }

    private NumberSelectItem(String name, String label, long[] numbers) {
        super(name, label, null);

        // read-only appearance
        addAppearance(READONLY, new NumberSelectReadOnlyAppearance());

        // editing appearance
        addAppearance(EDITING, new NumberSelectEditingAppearance(numbers));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    private static class NumberSelectReadOnlyAppearance extends ReadOnlyAppearance<Long> {

        NumberSelectReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, HINT, RESTRICTED));
        }

        @Override
        protected String name() {
            return "NumberSelectReadOnlyAppearance";
        }
    }

    private class NumberSelectEditingAppearance extends AbstractAppearance<Long> {

        private static final String NUMBER = "number";

        private final Map<Long, HTMLButtonElement> buttons;
        private final HTMLElement buttonGroup;
        private final HTMLElement helpBlock;
        private final HTMLElement inputContainer;
        private final HTMLElement root;

        NumberSelectEditingAppearance(long[] numbers) {
            super(EnumSet.of(DEPRECATED, ENABLED, INVALID, REQUIRED));
            this.buttons = new HashMap<>();

            root = div().css(formGroup)
                    .add(labelElement = label().css(controlLabel, halFormLabel).element())
                    .add(inputContainer = div().css(halFormInput)
                            .add(buttonGroup = div().css(btnGroup)
                                    .attr(UIConstants.ROLE, UIConstants.GROUP).element())
                            .element())
                    .element();
            for (long number : numbers) {
                String value = String.valueOf(number);
                HTMLButtonElement button = button().css(btn, btnDefault)
                        .textContent(value)
                        .data(NUMBER, value)
                        .on(click, event -> {
                            showValue(number);
                            modifyValue(number);
                        }).element();
                buttons.put(number, button);
                buttonGroup.appendChild(button);
            }
            helpBlock = Appearance.helpBlock();
        }

        @Override
        protected String name() {
            return "NumberSelectEditingAppearance";
        }

        @Override
        public void attach() {
            // noop
        }

        @Override
        public HTMLElement element() {
            return root;
        }

        @Override
        public void setId(String id) {
            this.id = Ids.build(id, EDITING.name().toLowerCase());
            root.dataset.set(FORM_ITEM_GROUP, this.id);
            buttonGroup.id = this.id;
            labelElement.htmlFor = this.id;
        }

        @Override
        public void setName(String name) {
            // noop
        }

        @Override
        public void showValue(Long value) {
            buttons.forEach((number, button) -> {
                if (number.equals(value)) {
                    button.classList.remove(btnDefault);
                    button.classList.add(btnPrimary);
                } else {
                    button.classList.remove(btnPrimary);
                    button.classList.add(btnDefault);
                }
            });
        }

        @Override
        public void clearValue() {
            buttons.values().forEach(button -> button.classList.remove(btnPrimary));
        }

        @Override
        <C> void safeApply(Decoration decoration, C context) {
            switch (decoration) {

                case DEPRECATED:
                    markAsDeprecated((Deprecation) context);
                    break;

                case ENABLED:
                    buttons.values().forEach(button -> button.disabled = false);
                    break;

                case INVALID:
                    helpBlock.textContent = String.valueOf(context);
                    root.classList.add(hasError);
                    inputContainer.appendChild(helpBlock);
                    break;

                case REQUIRED:
                    markAsRequired();
                    break;

                // unsupported
                case DEFAULT:
                case EXPRESSION:
                case HINT:
                case SENSITIVE:
                case SUGGESTIONS:
                    break;
                default:
                    break;
            }
        }

        @Override
        void safeUnapply(Decoration decoration) {
            switch (decoration) {

                case DEPRECATED:
                    clearDeprecation();
                    break;

                case ENABLED:
                    buttons.values().forEach(button -> button.disabled = true);
                    break;

                case INVALID:
                    root.classList.remove(hasError);
                    Elements.failSafeRemove(inputContainer, helpBlock);
                    break;

                case REQUIRED:
                    clearRequired();
                    break;

                // unsupported
                case DEFAULT:
                case EXPRESSION:
                case HINT:
                case SENSITIVE:
                case SUGGESTIONS:
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getTabIndex() {
            return (int) buttonGroup.tabIndex;
        }

        @Override
        public void setAccessKey(char key) {
            // noop
        }

        @Override
        public void setFocus(boolean focused) {
            if (focused) {
                buttonGroup.focus();
            } else {
                buttonGroup.blur();
            }
        }

        @Override
        public void setTabIndex(int index) {
            buttonGroup.tabIndex = index;
        }
    }
}
