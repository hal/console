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

/**
 * @author Harald Pehl
 */
abstract class AbstractCheckBoxElement extends InputElement<Boolean> {

    final elemental.html.InputElement element;

    AbstractCheckBoxElement() {
        element = Browser.getDocument().createInputElement();
        element.setType("checkbox"); //NON-NLS
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
    public void setName(final String s) {
        element.setName(s);
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getText() {
        return element.getValue();
    }

    @Override
    public void setText(final String s) {
        // not supported
    }

    @Override
    public Element asElement() {
        return element;
    }
}
