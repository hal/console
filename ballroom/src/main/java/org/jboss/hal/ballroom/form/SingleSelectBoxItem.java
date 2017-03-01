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
import java.util.List;

import com.google.common.base.Strings;
import elemental.client.Browser;
import elemental.html.SelectElement;
import org.jboss.hal.ballroom.form.SelectBoxBridge.Single;

import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;

/**
 * @author Harald Pehl
 */
public class SingleSelectBoxItem extends AbstractFormItem<String> {

    private static class SingleSelectBoxReadOnlyAppearance extends ReadOnlyAppearance<String> {

        SingleSelectBoxReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "SingleSelectBoxReadOnlyAppearance";
        }
    }


    private class SingleSelectBoxEditingAppearance extends SelectBoxEditingAppearance<String> {

        SingleSelectBoxEditingAppearance(final SelectElement selectElement, final List<String> options,
                final boolean allowEmpty) {
            super(selectElement, options, allowEmpty);
        }

        @Override
        public void attach() {
            super.attach();
            Single.element(selectElement).onChange((event, index) ->
                    modifyValue(Single.element(selectElement).getValue()));
        }

        @Override
        void refresh() {
            Single.element(selectElement).refresh();
        }

        @Override
        public void showValue(final String value) {
            if (attached) {
                Single.element(selectElement).setValue(value);
            } else {
                selectElement.setValue(value);
            }
        }

        @Override
        public void clearValue() {
            if (allowEmpty) {
                if (attached) {
                    Single.element(selectElement).setValue("");
                } else {
                    selectElement.setValue("");
                }
            }
        }
    }


    private final boolean allowEmpty;

    public SingleSelectBoxItem(final String name, final String label, List<String> options, boolean allowEmpty) {
        super(name, label, null);
        this.allowEmpty = allowEmpty;

        setUndefined(allowEmpty);
        if (!allowEmpty) {
            setModified(true);
        }

        // read-only appearance
        addAppearance(Form.State.READONLY, new SingleSelectBoxReadOnlyAppearance());

        // editing appearance
        elemental.html.SelectElement selectElement = Browser.getDocument().createSelectElement();
        selectElement.setSize(1);
        selectElement.setMultiple(false);

        addAppearance(Form.State.EDITING, new SingleSelectBoxEditingAppearance(selectElement, options, allowEmpty));
    }

    @Override
    public boolean isEmpty() {
        return allowEmpty && Strings.isNullOrEmpty(getValue());
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public void setUndefined(final boolean undefined) {
        if (allowEmpty || !undefined) {
            // ok
            super.setUndefined(undefined);
        } else {
            // there's always a value and this form item can never get undefined!
            super.setUndefined(false);
        }
    }
}
