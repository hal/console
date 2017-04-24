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
import java.util.HashMap;
import java.util.Map;

import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.*;

/**
 * A form item to select from a small set of distinct numbers using a button group.
 *
 * @author Harald Pehl
 */
public class NumberSelectItem extends AbstractFormItem<Long> {

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

        private static final String BUTTON_GROUP = "buttonGroup";
        private static final String NUMBER = "number";

        private final Map<Long, ButtonElement> buttons;
        private final Element buttonGroup;
        private final Element helpBlock;
        private final Element inputContainer;
        private final Element root;

        NumberSelectEditingAppearance(long[] numbers) {
            super(EnumSet.of(DEPRECATED, ENABLED, INVALID, REQUIRED));
            this.buttons = new HashMap<>();

            // @formatter:off
            Elements.Builder builder = new Elements.Builder()
                .div().css(formGroup)
                    .label().css(controlLabel, halFormLabel).rememberAs(LABEL_ELEMENT).end()
                    .div().css(halFormInput).rememberAs(INPUT_CONTAINER)
                        .div().css(btnGroup).attr(UIConstants.ROLE, UIConstants.GROUP).rememberAs(BUTTON_GROUP);
                            for (long number : numbers) {
                                String value = String.valueOf(number);
                                builder.button()
                                        .data(NUMBER, value)
                                        .css(btn, btnDefault)
                                        .on(click, event -> {
                                            showValue(number);
                                            modifyValue(number);
                                        })
                                        .rememberAs(value)
                                        .textContent(value)
                                        .end();
                                buttons.put(number, builder.referenceFor(value));
                            }
                        builder.end()
                    .end()
                .end();
            // @formatter:on

            helpBlock = Appearance.helpBlock();
            buttonGroup = builder.referenceFor(BUTTON_GROUP);
            labelElement = builder.referenceFor(LABEL_ELEMENT);
            inputContainer = builder.referenceFor(INPUT_CONTAINER);
            root = builder.build();
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
        public Element asElement() {
            return root;
        }

        @Override
        public void setId(final String id) {
            this.id = Ids.build(id, EDITING.name().toLowerCase());
            root.getDataset().setAt(FORM_ITEM_GROUP, this.id);
            buttonGroup.setId(this.id);
            labelElement.setHtmlFor(this.id);
        }

        @Override
        public void setName(final String name) {
            // noop
        }

        @Override
        public void showValue(final Long value) {
            buttons.forEach((number, button) -> {
                if (number.equals(value)) {
                    button.getClassList().remove(btnDefault);
                    button.getClassList().add(btnPrimary);
                } else {
                    button.getClassList().remove(btnPrimary);
                    button.getClassList().add(btnDefault);
                }
            });
        }

        @Override
        public void clearValue() {
            buttons.values().forEach(button -> button.getClassList().remove(btnPrimary));
        }

        @Override
        <C> void safeApply(final Decoration decoration, final C context) {
            switch (decoration) {

                case DEPRECATED:
                    markAsDeprecated((Deprecation) context);
                    break;

                case ENABLED:
                    buttons.values().forEach(button -> button.setDisabled(false));
                    break;

                case INVALID:
                    helpBlock.setTextContent(String.valueOf(context));
                    root.getClassList().add(hasError);
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
            }
        }

        @Override
        void safeUnapply(final Decoration decoration) {
            switch (decoration) {

                case DEPRECATED:
                    clearDeprecation();
                    break;

                case ENABLED:
                    buttons.values().forEach(button -> button.setDisabled(true));
                    break;

                case INVALID:
                    root.getClassList().remove(hasError);
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
            }
        }

        @Override
        public int getTabIndex() {
            return buttonGroup.getTabIndex();
        }

        @Override
        public void setAccessKey(final char key) {
            // noop
        }

        @Override
        public void setFocus(final boolean focused) {
            if (focused) {
                buttonGroup.focus();
            } else {
                buttonGroup.blur();
            }
        }

        @Override
        public void setTabIndex(final int index) {
            buttonGroup.setTabIndex(index);
        }
    }


    public NumberSelectItem(final String name, final long[] numbers) {
        this(name, new LabelBuilder().label(name), numbers);
    }

    public NumberSelectItem(final String name, final String label, final long[] numbers) {
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
}
