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

import static org.jboss.hal.ballroom.form.CreationContext.EMPTY_CONTEXT;

/**
 * @author Harald Pehl
 */
public class CheckBoxItem extends AbstractFormItem<Boolean> {

    public CheckBoxItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<Boolean> newInputElement(CreationContext<?> context) {
        CheckBoxElement checkBox = new CheckBoxElement();
        checkBox.element.setOnchange(event -> {
            Boolean newValue = inputElement().getValue();
            setModified(true);
            setUndefined(false);
            signalChange(newValue);
        });
        return checkBox;
    }

    @Override
    void markDefaultValue(final boolean on, final Boolean defaultValue) {
        super.markDefaultValue(on, defaultValue);
        if (on && defaultValue) {
            inputElement().setValue(true);
        }
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class CheckBoxElement extends AbstractCheckBoxElement {

        @Override
        public boolean isEnabled() {
            return !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            element.setDisabled(!b);
        }

        @Override
        public Boolean getValue() {
            return element.isChecked();
        }

        @Override
        public void setValue(final Boolean value) {
            element.setChecked(value);
        }

        @Override
        public void clearValue() {
            element.setChecked(false);
        }
    }
}
