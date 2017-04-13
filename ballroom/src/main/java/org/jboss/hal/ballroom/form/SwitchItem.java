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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class SwitchItem extends AbstractFormItem<Boolean> {

    private static class SwitchReadOnlyAppearance extends ReadOnlyAppearance<Boolean> {

        SwitchReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, RESTRICTED));
        }

        @Override
        protected String name() {
            return "SwitchReadOnlyAppearance";
        }
    }


    private class SwitchEditingAppearance extends EditingAppearance<Boolean> {

        private static final String EXPRESSION_MODE_INPUT = "expressionModeInput";
        private static final String EXPRESSION_MODE_BUTTON = "expressionModeButton";

        private final Element normalModeContainer;
        private final Element switchToExpressionButton;
        private final elemental.html.InputElement expressionModeInput;
        private final Element resolveExpressionButton;
        private final Element expressionModeContainer;
        private final FormItemValidation<Boolean> expressionValidation;
        private Boolean backup;


        // ------------------------------------------------------ ui code

        SwitchEditingAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, EXPRESSION, INVALID, REQUIRED, RESTRICTED),
                    Browser.getDocument().createInputElement());
            inputElement.setType("checkbox"); //NON-NLS

            // put the <input type="checkbox"/> into an extra div
            // this makes switching between normal and expression mode easier
            inputContainer.removeChild(inputElement);
            normalModeContainer = Browser.getDocument().createDivElement();
            normalModeContainer.appendChild(inputElement);
            inputContainer.appendChild(normalModeContainer);

            // @formatter:off alternative UI to enter expressions
            Elements.Builder expressionModeBuilder = new Elements.Builder()
                .div().css(CSS.inputGroup)
                    .span().css(inputGroupBtn)
                        .button().css(btn, btnDefault, expressionModeSwitcher)
                                 .style("margin-right: -2px")
                                 .title(CONSTANTS.switchToNormalMode())
                                 .on(click, event-> switchToNormalMode())
                            .start("i").css(CSS.fontAwesome("toggle-on")).end()
                        .end()
                    .end()
                    .input(text)
                        .css(formControl)
                        .rememberAs(EXPRESSION_MODE_INPUT)
                        .attr(UIConstants.PLACEHOLDER, CONSTANTS.expression())
                    .span().css(inputGroupBtn)
                        .button().css(btn, btnDefault).rememberAs(EXPRESSION_MODE_BUTTON)
                                 .title(CONSTANTS.resolveExpression())
                            .start("i").css(fontAwesome("link")).end()
                        .end()
                    .end()
                .end();
            // @formatter:on

            this.expressionModeInput = expressionModeBuilder.referenceFor(EXPRESSION_MODE_INPUT);
            this.expressionModeInput.setOnchange(event -> modifyExpressionValue(expressionModeInput.getValue()));
            this.resolveExpressionButton = expressionModeBuilder.referenceFor(EXPRESSION_MODE_BUTTON);
            this.expressionModeContainer = expressionModeBuilder.build();

            // @formatter:off
            switchToExpressionButton = new Elements.Builder()
                .button().css(btn, btnDefault, expressionModeSwitcher)
                         .title(CONSTANTS.switchToExpressionMode())
                         .on(click, event-> switchToExpressionMode())
                    .start("i").css(fontAwesome("link")).end()
                .end()
            .build();
            // @formatter:on

            // it's types to boolean, but used to validate the expression
            this.expressionValidation = value -> {
                if (!hasExpressionScheme(expressionModeInput.getValue())) {
                    return ValidationResult.invalid(FormItemValidation.CONSTANTS.invalidExpression());
                }
                return ValidationResult.OK;
            };
        }

        private void switchToNormalMode() {
            unapply(INVALID);
            unapply(EXPRESSION);
            if (backup != null) {
                modifyValue(backup);
            }
        }

        private void switchToExpressionMode() {
            backup = getValue();
            applyExpressionValue(expressionModeInput.getValue());
            modifyExpressionValue(expressionModeInput.getValue());
        }

        @Override
        public void attach() {
            super.attach();
            inputElement.getClassList().add(bootstrapSwitch);
            SwitchBridge.Bridge.element(inputElement).onChange((event, state) -> modifyValue(state));
        }

        @Override
        public void detach() {
            super.detach();
            if (attached) {
                inputElement.getClassList().remove(bootstrapSwitch);
                SwitchBridge.Bridge.element(inputElement).destroy();
            }
        }

        @Override
        protected String name() {
            return "SwitchEditingAppearance";
        }


        // ------------------------------------------------------ value

        @Override
        public void showValue(final Boolean value) {
            if (attached) {
                SwitchBridge.Bridge.element(inputElement).setValue(value);
            } else {
                inputElement.setChecked(value);
            }
        }

        @Override
        public void showExpression(final String expression) {
            expressionModeInput.setValue(expression);
        }

        @Override
        public void clearValue() {
            if (attached) {
                SwitchBridge.Bridge.element(inputElement).setValue(false);
            } else {
                inputElement.setChecked(false);
            }
        }


        // ------------------------------------------------------ decorations


        @Override
        void applyDefault(final String defaultValue) {
            if (attached) {
                SwitchBridge.Bridge.element(inputElement).setValue(Boolean.parseBoolean(defaultValue));
            } else {
                inputElement.setChecked(Boolean.parseBoolean(defaultValue));
            }
        }

        @Override
        protected void applyExpression(final ExpressionContext expressionContext) {
            Elements.failSafeRemove(inputContainer, normalModeContainer);
            Elements.lazyAppend(inputContainer, expressionModeContainer);
            resolveExpressionButton.setOnclick(
                    event -> expressionContext.callback.resolveExpression(expressionModeInput.getValue()));
            addValidationHandler(expressionValidation);
        }

        @Override
        void unapplyExpression() {
            Elements.failSafeRemove(inputContainer, expressionModeContainer);
            Elements.lazyAppend(inputContainer, normalModeContainer);
            removeValidationHandler(expressionValidation);
        }

        void setExpressionAllowed(final boolean expressionAllowed) {
            Elements.setVisible(switchToExpressionButton, expressionAllowed);
            if (expressionAllowed) {
                Elements.lazyInsertBefore(normalModeContainer, switchToExpressionButton,
                        normalModeContainer.getFirstElementChild());
            } else {
                Elements.failSafeRemove(normalModeContainer, switchToExpressionButton);
            }
        }


        // ------------------------------------------------------ properties & delegates

        @Override
        public void setId(final String id) {
            super.setId(id);
            // the checkbox item and the expression input have the same id
            // make sure only one is part of the DOM!
            expressionModeInput.setId(Ids.build(id, EDITING.name().toLowerCase()));
        }

        @Override
        public void setName(final String name) {
            inputElement.setName(name);
            expressionModeInput.setName(name);
        }

        @Override
        public int getTabIndex() {
            if (isApplied(EXPRESSION)) {
                return expressionModeInput.getTabIndex();
            } else {
                return inputElement.getTabIndex();
            }
        }

        @Override
        public void setAccessKey(final char key) {
            super.setAccessKey(key);
            expressionModeInput.setAccessKey(String.valueOf(key));
        }

        @Override
        public void setFocus(final boolean focused) {
            if (isApplied(EXPRESSION)) {
                if (focused) {
                    expressionModeInput.focus();
                } else {
                    expressionModeInput.blur();
                }
            } else {
                if (focused) {
                    inputElement.focus();
                } else {
                    inputElement.blur();
                }
            }
        }

        @Override
        public void setTabIndex(final int index) {
            super.setTabIndex(index);
            expressionModeInput.setTabIndex(index);
        }
    }


    private final SwitchEditingAppearance editingAppearance;

    public SwitchItem(final String name, final String label) {
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new SwitchReadOnlyAppearance());

        // editing appearance
        editingAppearance = new SwitchEditingAppearance();
        addAppearance(Form.State.EDITING, editingAppearance);
    }

    @Override
    public boolean isEmpty() {
        return isExpressionValue() && Strings.isNullOrEmpty(getExpressionValue());
    }

    @Override
    public boolean supportsExpressions() {
        return true;
    }

    @Override
    public void setExpressionAllowed(final boolean expressionAllowed) {
        super.setExpressionAllowed(expressionAllowed);
        editingAppearance.setExpressionAllowed(expressionAllowed);
    }
}
