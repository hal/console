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

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import org.jboss.hal.ballroom.LabelBuilder;

public class PreListItem extends ListItem {

    public PreListItem(String name) {
        this(name, new LabelBuilder().label(name), null);
    }

    public PreListItem(String name, String label, SafeHtml helpText) {
        super(name, label, helpText);

        // replace read-only appearance
        addAppearance(Form.State.READONLY, new PreReadOnlyAppearance<List<String>>() {
            @Override
            public String asString(final List<String> value) {
                return String.join("\n", value);
            }
        });
    }
}
