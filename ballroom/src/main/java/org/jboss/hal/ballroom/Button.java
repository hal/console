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
package org.jboss.hal.ballroom;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Button implements IsElement {

    public static final String DEFAULT_CSS = btn + " " + btnHal + " " + btnDefault;

    protected final ButtonElement element;

    public Button(final String label, final EventListener listener) {
        this(label, DEFAULT_CSS, listener);
    }

    public Button(final String label, final String css, final EventListener listener) {
        element = Browser.getDocument().createButtonElement();
        element.setInnerText(label);
        if (css != null) {
            element.setClassName(css);
        }
        element.setOnclick(listener);
    }

    @Override
    public Element asElement() {
        return element;
    }
}
