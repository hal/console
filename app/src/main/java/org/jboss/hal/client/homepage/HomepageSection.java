/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.homepage;

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
import org.jboss.hal.resources.Resources;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage-section")
abstract class HomepageSection implements IsElement {

    // @formatter:off
    static HomepageSection create(final TokenFormatter tokenFormatter, final Resources resources,
            final String id, final String token, final String header, final String intro,
            final Iterable<String> steps, final boolean open) {
        return new Templated_HomepageSection(tokenFormatter, resources, id, token, header, intro, steps, open);
    }

    abstract TokenFormatter tokenFormatter();
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
            sectionBody.getClassList().add("in");
        } else {
            toggleIcon.getClassList().remove("fa-angle-down");
            toggleIcon.getClassList().add("fa-angle-right");
            sectionBody.getClassList().remove("in");
        }
        sectionHeader.setInnerHTML(header());
        sectionIntro.setInnerHTML(intro());
        sectionBody.setAttribute("aria-expanded", String.valueOf(open()));

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
        return "#" + tokenFormatter().toHistoryToken(Collections.singletonList(placeRequest));
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
