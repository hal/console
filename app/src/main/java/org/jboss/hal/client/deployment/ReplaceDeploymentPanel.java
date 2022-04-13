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
package org.jboss.hal.client.deployment;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.Constants;

import com.google.gwt.core.client.GWT;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.hal.resources.CSS.loading;
import static org.jboss.hal.resources.CSS.loadingContainer;
import static org.jboss.hal.resources.CSS.spinner;

public class ReplaceDeploymentPanel implements IsElement<HTMLElement> {
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final HTMLElement root;

    public ReplaceDeploymentPanel() {
        this.root = div().css(loadingContainer)
                .add(div().css(loading)
                        .add(h(3).textContent(CONSTANTS.replaceDeployment()))
                        .add(div().css(spinner)))
                .element();
        document.body.appendChild(this.element());
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    public void on() {
        Elements.setVisible(root, true);
    }

    public void off() {
        Elements.setVisible(root, false);
    }
}
