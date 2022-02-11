/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.homepage;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.i;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

class HomepageSection implements IsElement<HTMLDivElement> {

    private static final String FA_ANGLE_RIGHT = "fa-angle-right";
    private static final String FA_ANGLE_DOWN = "fa-angle-down";

    private final HTMLDivElement root;
    private final HTMLElement toggleIcon;

    HomepageSection(Places places, Resources resources,
            String id, String token, String header, String intro,
            Iterable<String> steps, boolean open) {

        HTMLElement sectionBody;
        HTMLElement sectionSteps;

        root = div()
                .add(div().css(CSS.eapToggleControls)
                        .add(a("#" + id)
                                .aria(CONTROLS, id)
                                .aria(EXPANDED, TRUE)
                                .data(TOGGLE, COLLAPSE)
                                .on(click, e -> toggle())
                                .add(toggleIcon = i().css(eapHomeSectionIcon, fontAwesome("angle-down"))
                                        .element())
                                .add(span().textContent(header)))
                        .add(a(historyToken(places, token))
                                .add(span().textContent(resources.constants().start() + " "))
                                .add(i().css(fontAwesome("arrow-circle-right")))))
                .add(sectionBody = div().css(eapToggleContainer, collapse)
                        .id(id)
                        .add(p().textContent(intro))
                        .add(sectionSteps = ol().element())
                        .element())
                .element();

        if (open) {
            toggleIcon.classList.remove(FA_ANGLE_RIGHT);
            toggleIcon.classList.add(FA_ANGLE_DOWN);
            sectionBody.classList.add(in);
        } else {
            toggleIcon.classList.remove(FA_ANGLE_DOWN);
            toggleIcon.classList.add(FA_ANGLE_RIGHT);
            sectionBody.classList.remove(in);
        }
        sectionBody.setAttribute("aria-expanded", String.valueOf(open));

        removeChildrenFrom(sectionSteps);
        for (String step : steps) {
            HTMLLIElement li = li().innerHtml(SafeHtmlUtils.fromString(step)).element();
            sectionSteps.appendChild(li);
        }
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }

    String historyToken(Places places, String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        return places.historyToken(placeRequest);
    }

    void toggle() {
        boolean open = toggleIcon.classList.contains(FA_ANGLE_DOWN);
        if (open) {
            toggleIcon.classList.remove(FA_ANGLE_DOWN);
            toggleIcon.classList.add(FA_ANGLE_RIGHT);
        } else {
            toggleIcon.classList.remove(FA_ANGLE_RIGHT);
            toggleIcon.classList.add(FA_ANGLE_DOWN);
        }
    }
}
