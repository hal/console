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

import com.google.gwt.resources.client.ImageResource;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.dom.Element;
import elemental.html.ImageElement;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.core.mvp.Places;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage-module")
abstract class HomepageModule implements IsElement {

    // @formatter:off
    static HomepageModule create(final Places places,
            final String token, final String header, final String intro,
            final ImageResource image, final Iterable<HomepageSection> sections) {
        return new Templated_HomepageModule(places, token, header, intro, image, sections);
    }

    abstract Places places();
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
        return places().historyToken(placeRequest);
    }
}
