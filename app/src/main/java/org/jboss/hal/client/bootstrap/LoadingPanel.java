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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.core.client.GWT;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.resources.CSS.loading;
import static org.jboss.hal.resources.CSS.loadingContainer;
import static org.jboss.hal.resources.CSS.spinner;

public class LoadingPanel implements IsElement {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    public static LoadingPanel get() {
        if (instance == null) {
            instance = new LoadingPanel();
            instance.off();
            document.body.appendChild(instance.asElement());
        }
        return instance;
    }

    private static LoadingPanel instance;

    private final HTMLElement root;

    private LoadingPanel() {
        root = div().css(loadingContainer)
                .add(div().css(loading)
                        .add(h(3).textContent(CONSTANTS.loading()))
                        .add(div().css(spinner)))
                .asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public void on() {
        Elements.setVisible(root, true);
    }

    public void off() {
        Elements.setVisible(root, false);
    }
}
