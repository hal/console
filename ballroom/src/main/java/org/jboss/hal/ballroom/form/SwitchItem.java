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

import org.jboss.hal.ballroom.form.InputElement.Context;
import org.jboss.hal.ballroom.form.SwitchBridge.Bridge;

import static org.jboss.hal.ballroom.form.InputElement.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.bootstrapSwitch;
import static org.jboss.hal.resources.CSS.disabled;

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
        Bridge.element(switchElement.asElement()).onChange((event, state) -> {
            setModified(true);
            setUndefined(false);
            signalChange(state);
        });
        return switchElement;
    }

    @Override
    void markDefaultValue(final boolean on, final Boolean defaultValue) {
        super.markDefaultValue(on, defaultValue);
        if (on && defaultValue) {
            inputElement().setValue(true);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (enabled) {
            inputContainer.getClassList().remove(disabled);
        } else  {
            inputContainer.getClassList().add(disabled);
        }
        if (isAttached()) {
            switchElement.setEnabled(enabled);
        } else {
            // if the switch item is not yet attached we must modify the input element directly
            switchElement.element.setDisabled(!enabled);
        }
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    static class SwitchElement extends AbstractCheckBoxElement {

        @Override
        public boolean isEnabled() {
            return Bridge.element(asElement()).isEnable();
        }

        @Override
        public void setEnabled(final boolean b) {
            Bridge.element(asElement()).setEnable(b);
        }

        @Override
        public Boolean getValue() {
            return Bridge.element(asElement()).getValue();
        }

        @Override
        public void setValue(final Boolean value) {
            Bridge.element(asElement()).setValue(value);
        }

        @Override
        public void clearValue() {
            Bridge.element(asElement()).setValue(false);
        }
    }
}