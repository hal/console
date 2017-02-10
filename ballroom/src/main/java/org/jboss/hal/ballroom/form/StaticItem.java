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
import org.jboss.hal.resources.Names;

import static org.jboss.hal.ballroom.form.CreationContext.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.formControlStatic;

/**
 * @author Harald Pehl
 */
public class StaticItem extends AbstractFormItem<String> {

    public StaticItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    protected InputElement<String> newInputElement(final CreationContext<?> context) {
        StaticElement staticElement = new StaticElement();
        staticElement.asElement().getClassList().add(formControlStatic);
        return staticElement;
    }

    static class StaticElement extends InputElement<String> {

        final Element element;

        StaticElement() {
            element = Browser.getDocument().createElement("p"); //NON-NLS
        }
        @Override
        public String getValue() {
            return element.getTextContent();
        }

        @Override
        public void setValue(final String value) {
            element.setTextContent(value);
        }

        @Override
        public void clearValue() {
            element.setTextContent("");
        }

        @Override
        public int getTabIndex() {
            return -1;
        }

        @Override
        public void setAccessKey(final char key) {
            // noop
        }

        @Override
        public void setFocus(final boolean focused) {
            // noop
        }

        @Override
        public void setTabIndex(final int index) {
            // noop
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(final boolean enabled) {
            // noop
        }

        @Override
        public void setName(final String name) {
            // noop
        }

        @Override
        public String getName() {
            return Names.NOT_AVAILABLE;
        }

        @Override
        public String getText() {
            return element.getTextContent();
        }

        @Override
        public void setText(final String text) {
            element.setTextContent(text);
        }

        @Override
        public Element asElement() {
            return element;
        }
    }
}
