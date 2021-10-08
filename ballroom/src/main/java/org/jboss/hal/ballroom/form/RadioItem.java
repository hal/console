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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.LabelBuilder;

import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.InputType.radio;
import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;

public class RadioItem extends AbstractFormItem<String> {

    public RadioItem(String name, String label, List<String> options, boolean inline) {
        super(name, label, null, null);

        LabelBuilder labelBuilder = new LabelBuilder();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String option : options) {
            map.put(option, labelBuilder.label(option));
        }
        addAppearances(map, inline);
    }

    private void addAppearances(LinkedHashMap<String, String> options, boolean inline) {
        addAppearance(Form.State.READONLY, new RadioReadOnlyAppearance());

        List<HTMLInputElement> elements = new ArrayList<>();
        for (String ignore : options.keySet()) {
            elements.add(input(radio)
                    .on(change, e -> modifyValue(((HTMLInputElement) e.target).value)).element());
        }
        addAppearance(Form.State.EDITING, new RadioEditingAppearance(elements, options, inline));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }


    private static class RadioReadOnlyAppearance extends ReadOnlyAppearance<String> {

        RadioReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));
        }

        @Override
        protected String name() {
            return "RadioReadOnlyAppearance";
        }
    }


}

