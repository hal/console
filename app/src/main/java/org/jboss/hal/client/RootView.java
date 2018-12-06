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
package org.jboss.hal.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Widgets;
import org.jboss.hal.core.mvp.Slots;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.client.RootPresenter.SLOT_FOOTER_CONTENT;
import static org.jboss.hal.client.RootPresenter.SLOT_HEADER_CONTENT;
import static org.jboss.hal.resources.CSS.containerFluid;

public class RootView extends ViewImpl implements RootPresenter.MyView {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(RootView.class);

    private final Map<Object, HTMLElement> slots;
    private final HTMLElement rootContainer;
    private boolean initialized;

    public RootView() {
        slots = new HashMap<>();
        rootContainer = div().id(Ids.ROOT_CONTAINER).css(containerFluid).get();
        initWidget(Widgets.widget(rootContainer));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {

        if (slot == SLOT_HEADER_CONTENT || slot == SLOT_FOOTER_CONTENT) {
            // single elements only!
            HTMLElement element = content instanceof IsElement ?
                    ((IsElement) content).element() :
                    Widgets.element(content);
            slots.put(slot, element);
            if (!initialized && slots.containsKey(SLOT_HEADER_CONTENT) && slots.containsKey(SLOT_FOOTER_CONTENT)) {
                // append all three building blocks to the document body
                document.body.appendChild(slots.get(SLOT_HEADER_CONTENT));
                document.body.appendChild(rootContainer);
                document.body.appendChild(slots.get(SLOT_FOOTER_CONTENT));
                initialized = true;
            }

        } else if (slot == Slots.MAIN) {
            // single or multiple elements with precedence for multiple elements
            boolean finished = false;
            Elements.removeChildrenFrom(rootContainer);

            if (content instanceof Iterable) {
                //noinspection unchecked
                Iterable<HTMLElement> elements = (Iterable<HTMLElement>) content;
                if (elements != null) {
                    for (HTMLElement element : elements) {
                        rootContainer.appendChild(element);
                    }
                    finished = true;
                }
            }

            if (!finished) {
                HTMLElement element = content instanceof IsElement
                        ? ((IsElement) content).element()
                        : Widgets.element(content);
                rootContainer.appendChild(element);
            }

        } else {
            logger.warn("Unknown slot {}. Delegate to super.setInSlot()", slot);
            super.setInSlot(slot, content);
        }
    }
}