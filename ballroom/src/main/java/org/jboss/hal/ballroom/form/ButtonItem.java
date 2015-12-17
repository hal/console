/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.form;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import org.jboss.hal.ballroom.Button;

import static org.jboss.hal.resources.CSS.labelColumns;
import static org.jboss.hal.resources.CSS.offset;

/**
 * @author Harald Pehl
 */
public class ButtonItem extends AbstractFormItem<Void> {

    private ButtonElement button;

    public ButtonItem(final String name, final String label) {
        super(name, label);
        button.setText(label);
    }

    @Override
    protected InputElement<Void> newInputElement() {
        button = new ButtonElement();
        button.setClassName(Button.DEFAULT_CSS);
        return button;
    }

    @Override
    protected void assembleUI() {
        inputContainer.getClassList().add(offset(labelColumns));
        inputContainer.appendChild(inputElement.asElement());
        container.appendChild(inputContainer);
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
