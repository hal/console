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

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class PasswordItem extends AbstractFormItem<String> {

    @SuppressWarnings({"ReplaceAllDot", "HardCodedStringLiteral"})
    private class PasswordReadOnlyAppearance extends ReadOnlyAppearance<String> {

        private Element peekLink;
        private boolean hidden;

        @SuppressWarnings("DuplicateStringLiteralInspection")
        PasswordReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
            hidden = true;

            peekLink = new Elements.Builder().span()
                    .css(fontAwesome("eye"), clickable)
                    .title(CONSTANTS.showSensitive())
                    .on(click, event -> {
                        if (hidden) {
                            valueElement.setTextContent(getValue());
                            peekLink.setTitle(CONSTANTS.hideSensitive());
                            peekLink.getClassList().add("fa-eye-slash");
                            peekLink.getClassList().remove("fa-eye");
                        } else {
                            valueElement.setTextContent(getValue().replaceAll(".", "\u25CF"));
                            peekLink.setTitle(CONSTANTS.showSensitive());
                            peekLink.getClassList().add("fa-eye");
                            peekLink.getClassList().remove("fa-eye-slash");
                        }
                        hidden = !hidden;
                    })
                    .end().build();
        }

        @Override
        protected String name() {
            return "PasswordReadOnlyAppearance";
        }

        @Override
        public void showValue(final String value) {
            if (isEmpty()) {
                Elements.failSafeRemove(valueContainer, peekLink);
            } else {
                valueContainer.appendChild(peekLink);
            }
            super.showValue(value);
        }

        @Override
        public String asString(final String value) {
            if (hidden) {
                return value.replaceAll(".", "\u25CF");
            } else {
                return value;
            }
        }
    }


    private class PasswordEditingAppearance extends EditingAppearance<String> {

        private static final String PEEK_BUTTON = "peekButton";
        private static final String PEEK_ICON = "peekIcon";

        private Element peekButton;
        private Element peekIcon;

        @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
        PasswordEditingAppearance(elemental.html.InputElement inputElement) {
            super(EnumSet.of(DEFAULT, DEPRECATED, ENABLED, INVALID, REQUIRED, RESTRICTED), inputElement);
            // @formatter:off
            Elements.Builder builder = new Elements.Builder()
                .span().css(inputGroupBtn)
                    .button().css(btn, btnDefault)
                             .rememberAs(PEEK_BUTTON)
                             .title(CONSTANTS.showSensitive())
                             .on(click, event -> {
                                 if ("password".equals(inputElement.getType())) {
                                     inputElement.setType("text");
                                     inputElement.focus();
                                     peekButton.setTitle(CONSTANTS.hideSensitive());
                                     peekIcon.getClassList().add("fa-eye-slash");
                                     peekIcon.getClassList().remove("fa-eye");

                                 } else if ("text".equals(inputElement.getType())) {
                                     inputElement.setType("password");
                                     inputElement.focus();
                                     peekButton.setTitle(CONSTANTS.showSensitive());
                                     peekIcon.getClassList().add("fa-eye");
                                     peekIcon.getClassList().remove("fa-eye-slash");
                                 }
                             })
                        .start("i").css(fontAwesome("eye")).rememberAs(PEEK_ICON).end()
                    .end()
                .end();
            // @formatter:on

            peekButton = builder.referenceFor(PEEK_BUTTON);
            peekIcon = builder.referenceFor(PEEK_ICON);
            Element peekContainer = builder.build();

            wrapInputElement();
            inputGroup.appendChild(peekContainer);
        }

        @Override
        protected String name() {
            return "PasswordEditingAppearance";
        }

        @Override
        public void showValue(final String value) {
            inputElement.setValue(value);
        }

        @Override
        public void clearValue() {
            inputElement.setValue("");
        }
    }

    public PasswordItem(final String name, final String label) {
        super(name, label, null);

        // read-only appearance
        addAppearance(Form.State.READONLY, new PasswordReadOnlyAppearance());

        // editing appearance
        elemental.html.InputElement inputElement = Browser.getDocument().createInputElement();
        inputElement.setType("password"); //NON-NLS
        inputElement.getClassList().add(formControl);
        inputElement.setOnchange(event -> modifyValue(inputElement.getValue()));

        addAppearance(EDITING, new PasswordEditingAppearance(inputElement));
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(getValue());
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }
}
