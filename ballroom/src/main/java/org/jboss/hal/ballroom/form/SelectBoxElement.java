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

import com.google.common.base.Strings;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.OptionElement;
import elemental.html.SelectElement;

import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;

/**
 * @author Harald Pehl
 */
abstract class SelectBoxElement<T> extends InputElement<T> {

    final boolean allowEmpty;
    final SelectElement element;

    SelectBoxElement(final boolean allowEmpty, final boolean multi) {
        this.allowEmpty = allowEmpty;
        element = Browser.getDocument().createSelectElement();
        element.setMultiple(multi);
        element.setSize(1);
    }

    void setOptions(List<String> options) {
        for (String option : options) {
            OptionElement optionElement = Browser.getDocument().createOptionElement();
            optionElement.setText(option);
            if (emptyToNull(option) == null) {
                optionElement.setTitle(UNDEFINED);
            }
            element.appendChild(optionElement);
        }
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
    public void setName(final String s) {
        element.setName(s);
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public Element asElement() {
        return element;
    }

    @Override
    public void setPlaceholder(final String placeHolder) {
        element.setTitle(Strings.isNullOrEmpty(placeHolder) ? UNDEFINED : placeHolder);
    }
}
