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

import java.util.Collections;
import javax.annotation.PostConstruct;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.in;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
@Templated("Homepage.html#homepage-section")
abstract class HomepageSection implements IsElement {

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


    @DataElement Element toggleIcon;
    @DataElement Element sectionHeader;
    @DataElement Element sectionBody;
    @DataElement Element sectionIntro;
    @DataElement Element sectionSteps;

    @PostConstruct
    void init() {
        if (open()) {
            toggleIcon.getClassList().remove("fa-angle-right");
            toggleIcon.getClassList().add("fa-angle-down");
            sectionBody.getClassList().add(in);
        } else {
            toggleIcon.getClassList().remove("fa-angle-down");
            toggleIcon.getClassList().add("fa-angle-right");
            sectionBody.getClassList().remove(in);
        }
        sectionHeader.setInnerHTML(header());
        sectionIntro.setInnerHTML(intro());
        sectionBody.setAttribute("aria-expanded", String.valueOf(open())); //NON-NLS

        Elements.removeChildrenFrom(sectionSteps);
        Document document = Browser.getDocument();
        for (String step : steps()) {
            Element li = document.createLIElement();
            li.setInnerHTML(step);
            sectionSteps.appendChild(li);
        }
    }

    String historyToken() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token()).build();
        return places().historyToken(placeRequest);
    }

    @EventHandler(element = "toggleSection", on = click)
    void toggle() {
        boolean open = toggleIcon.getClassList().contains("fa-angle-down");
        if (open) {
            toggleIcon.getClassList().remove("fa-angle-down");
            toggleIcon.getClassList().add("fa-angle-right");
        } else {
            toggleIcon.getClassList().remove("fa-angle-right");
            toggleIcon.getClassList().add("fa-angle-down");
        }
    }
}
