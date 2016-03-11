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
import org.jboss.hal.ballroom.form.SelectBoxBridge.Single;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * @author Harald Pehl
 */
public class SingleSelectBoxItem extends AbstractFormItem<String> {

    private final boolean allowEmpty;
    private SingleSelectBoxElement selectBox;

    public SingleSelectBoxItem(final String name, final String label, List<String> options) {
        this(name, label, options, false);
    }

    public SingleSelectBoxItem(final String name, final String label, List<String> options, boolean allowEmpty) {
        super(name, label, null, new Context<>(allowEmpty));
        this.allowEmpty = allowEmpty;
        List<String> localOptions = options;
        if (allowEmpty && !options.isEmpty() && emptyToNull(options.get(0)) != null) {
            localOptions = new ArrayList<>(options);
            localOptions.add(0, "");
        }
        setOptions(localOptions);
    }

    @Override
    protected InputElement<String> newInputElement(Context<?> context) {
        Boolean allowEmpty = (Boolean) context.data();
        selectBox = new SingleSelectBoxElement(allowEmpty);
        selectBox.setClassName(formControl + " " + selectpicker);
        Single.element(selectBox.asElement()).onChange((event, index) -> {
            String value = getValue();
            setModified(true);
            setUndefined(isNullOrEmpty(value));
            signalChange(value);
        });
        return selectBox;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    public void setOptions(List<String> options) {
        selectBox.setOptions(options);
        setUndefined(allowEmpty);
        if (!allowEmpty) {
            setModified(true);
        }
    }

    @Override
    public void setUndefined(final boolean undefined) {
        if (allowEmpty || !undefined) {
            // ok
            super.setUndefined(undefined);
        } else {
            // there's always a value and this form item can never get undefined!
            setUndefined(false);
        }
    }

    @Override
    void markDefaultValue(final boolean on, final String defaultValue) {
        super.markDefaultValue(on, defaultValue);
        Single.element(selectBox.asElement()).refresh();
    }


    private static class SingleSelectBoxElement extends SelectBoxElement<String> {

        SingleSelectBoxElement(final boolean allowEmpty) {
            super(allowEmpty, false);
        }

        @Override
        public String getValue() {
            return isAttached() ? Single.element(asElement()).getValue() : element.getValue();
        }

        @Override
        public void setValue(final String value) {
            if (isAttached()) {
                Single.element(asElement()).setValue(value);
            } else {
                element.setValue(value);
            }
        }

        @Override
        public void clearValue() {
            if (allowEmpty) {
                if (isAttached()) {
                    Single.element(asElement()).setValue("");
                } else {
                    element.setValue("");
                }
            }
        }

        @Override
        public String getText() {
            return getValue();
        }

        @Override
        public void setText(final String s) {
            setValue(s);
        }
    }
}
