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

import com.google.gwt.resources.client.ImageResource;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import elemental.html.ImageElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage-module")
abstract class HomepageModule implements IsElement {

    // @formatter:off
    static HomepageModule create(final TokenFormatter tokenFormatter,
            final String token, final String header, final String intro,
            final ImageResource image, final Iterable<HomepageSection> sections) {
        return new Templated_HomepageModule(tokenFormatter, token, header, intro, image, sections);
    }

    abstract TokenFormatter tokenFormatter();
    abstract String token();
    abstract String header();
    abstract String intro();
    abstract ImageResource image();
    abstract Iterable<HomepageSection> sections();
    // @formatter:on


    @DataElement ImageElement moduleImage;
    @DataElement Element moduleBody;
    @DataElement Element moduleHeader;
    @DataElement Element moduleIntro;

    @PostConstruct
    void init() {
        moduleImage.setSrc(image().getSafeUri().asString());
        moduleHeader.setInnerHTML(header());
        moduleIntro.setInnerHTML(intro());

        int i = 0;
        for (HomepageSection section : sections()) {
            moduleBody.appendChild(section.asElement());
            if (i > 0) {
                section.toggle();
            }
            i++;
        }
    }

    String historyToken() {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token()).build();
        return "#" + tokenFormatter().toHistoryToken(Collections.singletonList(placeRequest));
    }
}
