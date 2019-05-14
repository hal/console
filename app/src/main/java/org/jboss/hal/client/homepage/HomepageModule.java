/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.homepage;

import javax.annotation.PostConstruct;

import com.google.gwt.resources.client.ImageResource;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.template.DataElement;
import org.jboss.gwt.elemento.template.Templated;
import org.jboss.hal.core.mvp.Places;

@Templated("Homepage.html#homepage-module")
abstract class HomepageModule implements IsElement {

    // @formatter:off
    static HomepageModule create(Places places, String id, String token, String header, String intro,
            ImageResource image, Iterable<HomepageSection> sections) {
        return new Templated_HomepageModule(places, id, token, header, intro, image, sections);
    }

    abstract Places places();
    abstract String id();
    abstract String token();
    abstract String header();
    abstract String intro();
    abstract ImageResource image();
    abstract Iterable<HomepageSection> sections();
    // @formatter:on


    @DataElement HTMLImageElement moduleImage;
    @DataElement HTMLElement moduleBody;
    @DataElement HTMLElement moduleHeader;
    @DataElement HTMLElement moduleIntro;

    @PostConstruct
    void init() {
        moduleImage.src = image().getSafeUri().asString();
        moduleHeader.id = id();
        moduleHeader.textContent = header();
        moduleIntro.textContent = intro();

        int i = 0;
        for (HomepageSection section : sections()) {
            moduleBody.appendChild(section.element());
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
