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
package org.jboss.hal.client.homepage;

import javax.annotation.PostConstruct;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.template.DataElement;
import org.jboss.gwt.elemento.template.Templated;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.in;

@SuppressWarnings("HardCodedStringLiteral")
@Templated("Homepage.html#homepage-section")
abstract class HomepageSection implements IsElement {

    private static final String FA_ANGLE_RIGHT = "fa-angle-right";
    private static final String FA_ANGLE_DOWN = "fa-angle-down";

    // @formatter:off
    static HomepageSection create(final Places places, final Resources resources,
            final String id, final String token, final String header, final String intro,
            final Iterable<String> steps, final boolean open) {
        return new Templated_HomepageSection(places, resources, id, token, header, intro, steps, open);
    }

    abstract Places places();
    abstract Resources resources();
    abstract String id();
    abstract String token();
    abstract String header();
    abstract String intro();
    abstract Iterable<String> steps();
    abstract boolean open();
    // @formatter:on


    @DataElement HTMLElement toggleIcon;
    @DataElement HTMLElement toggleSection;
    @DataElement HTMLElement sectionHeader;
    @DataElement HTMLElement sectionBody;
    @DataElement HTMLElement sectionIntro;
    @DataElement HTMLElement sectionSteps;

    @PostConstruct
    void init() {
        if (open()) {
            toggleIcon.classList.remove(FA_ANGLE_RIGHT);
            toggleIcon.classList.add(FA_ANGLE_DOWN);
            sectionBody.classList.add(in);
        } else {
            toggleIcon.classList.remove(FA_ANGLE_DOWN);
            toggleIcon.classList.add(FA_ANGLE_RIGHT);
            sectionBody.classList.remove(in);
        }
        sectionHeader.innerHTML = header();
        sectionIntro.innerHTML = intro();
        sectionBody.setAttribute("aria-expanded", String.valueOf(open())); //NON-NLS

        Elements.removeChildrenFrom(sectionSteps);
        for (String step : steps()) {
            HTMLLIElement li = li().innerHtml(SafeHtmlUtils.fromString(step)).asElement();
            sectionSteps.appendChild(li);
        }

        bind(toggleSection, click, event -> toggle());
    }

    String historyToken() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token()).build();
        return places().historyToken(placeRequest);
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
