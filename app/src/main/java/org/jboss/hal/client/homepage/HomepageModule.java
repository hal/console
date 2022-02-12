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
package org.jboss.hal.client.homepage;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.mvp.Places;

import com.google.gwt.resources.client.ImageResource;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.resources.CSS.*;

class HomepageModule implements IsElement<HTMLDivElement> {

    private final HTMLDivElement root;

    public HomepageModule(Places places, String id, String token, String header, String intro,
            ImageResource image, Iterable<HomepageSection> sections) {
        HTMLElement moduleBody;

        root = div().css(eapHomeCol)
                .add(div().css(eapHomeModule)
                        .add(div().css(eapHomeModuleIcon)
                                .add(img(image.getSafeUri().asString())))
                        .add(moduleBody = div().css(eapHomeModuleContainer)
                                .add(div().css(eapHomeModuleHeader)
                                        .add(h(2).add(a(historyToken(places, token))
                                                .css(eapHomeModuleLink)
                                                .id(id)
                                                .textContent(header)))
                                        .add(p().textContent(intro)))
                                .element()))
                .element();

        int i = 0;
        for (HomepageSection section : sections) {
            moduleBody.appendChild(section.element());
            if (i > 0) {
                section.toggle();
            }
            i++;
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
}
