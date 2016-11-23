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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import elemental.dom.Element;
import org.jboss.hal.ballroom.form.SelectBoxBridge.Multi;

import static org.jboss.hal.ballroom.form.CreationContext.EMPTY_CONTEXT;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * @author Harald Pehl
 */
public class MultiSelectBoxItem extends AbstractFormItem<List<String>> {

    private MultiSelectBoxElement selectBox;

    public MultiSelectBoxItem(final String name, final String label, List<String> options) {
        super(name, label, null, EMPTY_CONTEXT);
        setOptions(options);
    }

    @Override
    protected InputElement<List<String>> newInputElement(CreationContext<?> context) {
        selectBox = new MultiSelectBoxElement();
        selectBox.setClassName(formControl + " " + selectpicker);
        return selectBox;
    }

    @Override
    public void attach() {
        super.attach();
        Multi.element(selectBox.asElement()).onChange((event, index) -> {
            List<String> value = getValue();
            setModified(true);
            setUndefined(value.isEmpty());
            signalChange(value);
        });
    }

    @Override
    void markDefaultValue(final boolean on, final List<String> defaultValue) {
        super.markDefaultValue(on, defaultValue);
        Multi.element(selectBox.asElement()).refresh();
    }

    public void setOptions(List<String> options) {
        selectBox.setOptions(options);
    }

    @Override
    String asString(final List<String> value) {
        return Joiner.on(", ").join(value);
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty() || isUndefined();
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class MultiSelectBoxElement extends SelectBoxElement<List<String>> {

        MultiSelectBoxElement() {
            super(true, true);
        }

        @Override
        public List<String> getValue() {
            return isAttached() ? Multi.element(asElement()).getValue() : Collections.emptyList();
        }

        @Override
        public void setValue(final List<String> value) {
            if (isAttached()) {
                Multi.element(asElement()).setValue(value);
            }
        }

        @Override
        public void clearValue() {
            if (isAttached()) {
                Multi.element(asElement()).clear();
            }
        }

        @Override
        public String getText() {
            return Joiner.on(", ").join(getValue());
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
}
