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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class LoadingPanel implements IsElement {

    private final static Constants CONSTANTS = GWT.create(Constants.class);

    static public LoadingPanel get() {
        if (instance == null) {
            instance = new LoadingPanel();
            instance.off();
            Browser.getDocument().getBody().appendChild(instance.asElement());
        }
        return instance;
    }

    private static LoadingPanel instance;

    private final Element root;

    private LoadingPanel() {
        // @formatter:off
        root = new Elements.Builder()
            .div().css(loadingContainer)
                .div().css(loading)
                    .h(3).textContent(CONSTANTS.loading()).end()
                    .div().css(spinner).end()
                .end()
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void on() {
        Elements.setVisible(root, true);
    }

    public void off() {
        Elements.setVisible(root, false);
    }
}
