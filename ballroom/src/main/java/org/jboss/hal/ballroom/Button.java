/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.IsElement;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnHal;

public class Button implements IsElement {

    public static final String DEFAULT_CSS = btn + " " + btnHal + " " + btnDefault;

    protected final HTMLButtonElement element;

    public Button(final String label, final EventCallbackFn<MouseEvent> listener) {
        this(label, DEFAULT_CSS, listener);
    }

    public Button(final String label, final String css, final EventCallbackFn<MouseEvent> listener) {
        element = button().textContent(label).on(click, listener).element();
        if (css != null) {
            element.className = css;
        }
    }

    @Override
    public HTMLElement element() {
        return element;
    }
}
