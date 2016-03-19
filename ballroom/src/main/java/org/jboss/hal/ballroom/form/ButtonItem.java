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
import elemental.events.EventListener;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.form.InputElement.Context;

import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.labelColumns;
import static org.jboss.hal.resources.CSS.offset;
import static org.jboss.hal.resources.Names.NOT_SUPPORTED;

/**
 * @author Harald Pehl
 */
public class ButtonItem extends AbstractFormItem<Void> {

    private ButtonElement button;
    private Element readonlyNotSupported;

    public ButtonItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
        button.setText(label);
    }

    @Override
    public Element asElement(final Form.State state) {
        if (state == READONLY) {
            if (readonlyNotSupported == null) {
                readonlyNotSupported = Browser.getDocument().createDivElement();
                readonlyNotSupported.setInnerText(NOT_SUPPORTED);
                Elements.setVisible(readonlyNotSupported, false);
            }
            return readonlyNotSupported;
        } else {
            return super.asElement(state);
        }
    }

    @Override
    protected InputElement<Void> newInputElement(Context<?> context) {
        button = new ButtonElement();
        button.setClassName(Button.DEFAULT_CSS);
        return button;
    }

    @Override
    protected void assembleUI() {
        inputContainer.getClassList().add(offset(labelColumns));
        inputContainer.appendChild(inputElement().asElement());
        editingRoot.appendChild(inputContainer);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    protected void toggleRestricted(final boolean on) {
        button.element.setDisabled(on);
    }

    public void onClick(EventListener listener) {
        button.element.setOnclick(listener);
    }


    static class ButtonElement extends InputElement<Void> {

        static final String DATA_NAME = "data-name";
        final elemental.html.ButtonElement element;

        ButtonElement() {
            element = Browser.getDocument().createButtonElement();
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
        public Void getValue() {
            return null;
        }

        @Override
        public void setValue(final Void value) {
            // noop
        }

        @Override
        public void clearValue() {
            // noop
        }

        @Override
        public void setName(final String s) {
            element.setAttribute(DATA_NAME, s);
        }

        @Override
        public String getName() {
            return element.getAttribute(DATA_NAME);
        }

        @Override
        public String getText() {
            return element.getInnerText();
        }

        @Override
        public void setText(final String s) {
            element.setInnerText(s);
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
