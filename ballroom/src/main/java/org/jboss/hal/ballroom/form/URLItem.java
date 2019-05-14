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

import java.util.EnumSet;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.hal.ballroom.form.Decoration.DEFAULT;
import static org.jboss.hal.ballroom.form.Decoration.DEPRECATED;
import static org.jboss.hal.ballroom.form.Decoration.RESTRICTED;

public class URLItem extends TextBoxItem {

    class URLReadOnlyAppearance extends ReadOnlyAppearance<String> {

        private HTMLAnchorElement anchorElement;

        URLReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, RESTRICTED));

            HTMLElement parent = (HTMLElement) valueContainer.parentNode;
            Elements.removeChildrenFrom(parent);

            anchorElement = a().attr("target", "_blank").get();
            valueElement = anchorElement;
            parent.appendChild(valueElement);
        }

        @Override
        public String asString(String value) {
            anchorElement.href = value;
            return value;
        }

        @Override
        protected String name() {
            return "URLReadOnlyAppearance";
        }
    }

    public URLItem(String name) {
        super(name, new LabelBuilder().label(name));

        // replace read-only appearance
        addAppearance(Form.State.READONLY, new URLReadOnlyAppearance());
    }

}
