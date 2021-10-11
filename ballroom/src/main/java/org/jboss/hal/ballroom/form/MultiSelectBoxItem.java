/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLSelectElement;
import org.jboss.hal.ballroom.form.SelectBoxBridge.Multi;

import static org.jboss.gwt.elemento.core.Elements.select;
import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.HELP;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;

public class MultiSelectBoxItem extends AbstractFormItem<List<String>> {

    public MultiSelectBoxItem(String name, String label, List<String> options, SafeHtml helpText) {
        super(name, label, null, helpText);

        // read-only appearance
        addAppearance(Form.State.READONLY, new MultiSelectBoxReadOnlyAppearance());

        // editing appearance
        HTMLSelectElement selectElement = select()
                .apply(select -> {
                    select.size = 1;
                    select.multiple = true;
                }).element();
        addAppearance(Form.State.EDITING, new MultiSelectBoxEditingAppearance(selectElement, options));
    }

    @Override
    public boolean isEmpty() {
        return getValue() == null || getValue().isEmpty();
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class MultiSelectBoxReadOnlyAppearance extends ReadOnlyAppearance<List<String>> {

        MultiSelectBoxReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, HELP, RESTRICTED));
        }

        @Override
        protected String name() {
            return "MultiSelectBoxReadOnlyAppearance";
        }

        @Override
        public String asString(List<String> value) {
            return String.join(", ", value);
        }
    }


    private class MultiSelectBoxEditingAppearance extends SelectBoxEditingAppearance<List<String>> {

        MultiSelectBoxEditingAppearance(HTMLSelectElement selectElement, List<String> options) {
            super(selectElement, options, true);
        }

        @Override
        public void attach() {
            super.attach();
            Multi.element(selectElement).onChange((event, index) ->
                    modifyValue(Multi.element(selectElement).getValue()));
        }

        @Override
        void refresh() {
            Multi.element(selectElement).refresh();
        }

        @Override
        public void showValue(List<String> value) {
            if (attached) {
                Multi.element(selectElement).setValue(value);
            }
        }

        @Override
        public String asString(List<String> value) {
            return String.join(", ", value);
        }

        @Override
        public void clearValue() {
            if (allowEmpty) {
                if (attached) {
                    Multi.element(selectElement).setValue(Collections.emptyList());
                } else {
                    selectElement.value = "";
                }
            }
        }
    }
}
