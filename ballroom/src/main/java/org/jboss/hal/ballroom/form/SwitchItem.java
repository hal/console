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

import org.jboss.hal.ballroom.form.InputElement.Context;
import org.jboss.hal.ballroom.form.SwitchBridge.Bridge;

import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.bootstrapSwitch;

/**
 * @author Harald Pehl
 */
public class SwitchItem extends AbstractFormItem<Boolean> {

    private SwitchElement switchElement;

    public SwitchItem(final String name, final String label) {
        super(name, label, null, EMPTY_CONTEXT);
    }

    @Override
    protected InputElement<Boolean> newInputElement(Context<?> context) {
        switchElement = new SwitchElement();
        switchElement.setClassName(bootstrapSwitch);
        return switchElement;
    }

    @Override
    public void attach() {
        super.attach();
        Bridge.element(switchElement.asElement()).onChange((event, state) -> {
            setModified(true);
            setUndefined(false);
            signalChange(state);
        });
    }

    @Override
    public void detach() {
        super.detach();
        if (switchElement != null) {
            switchElement.asElement().getClassList().remove(bootstrapSwitch);
            Bridge.element(switchElement.asElement()).destroy();
        }
    }

    @Override
    void markDefaultValue(final boolean on, final Boolean defaultValue) {
        super.markDefaultValue(on, defaultValue);
        if (on && defaultValue) {
            inputElement().setValue(true);
        }
    }

    @Override
    public void setValue(final Boolean value) {
        super.setValue(value);
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class SwitchElement extends AbstractCheckBoxElement {

        @Override
        public boolean isEnabled() {
            return isAttached() ? Bridge.element(asElement()).isEnable() : !element.isDisabled();
        }

        @Override
        public void setEnabled(final boolean b) {
            if (isAttached()) {
                Bridge.element(asElement()).setEnable(b);
            } else {
                element.setDisabled(!b);
            }
        }

        @Override
        public Boolean getValue() {
            return isAttached() ? Bridge.element(asElement()).getValue() : element.isChecked();
        }

        @Override
        public void setValue(final Boolean value) {
            if (isAttached()) {
                Bridge.element(asElement()).setValue(value);
            } else {
                element.setChecked(value);
            }
        }

        @Override
        public void clearValue() {
            if (isAttached()) {
                Bridge.element(asElement()).setValue(false);
            } else {
                element.setChecked(false);
            }
        }
    }
}