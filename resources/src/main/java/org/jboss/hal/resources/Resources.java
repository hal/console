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
package org.jboss.hal.resources;

import javax.inject.Inject;

import com.google.gwt.resources.client.ExternalTextResource;

/** Umbrella over all kind of resources in HAL. */
public class Resources implements Ids, Names, UIConstants, CSS, Icons {

    private final Constants constants;
    private final Messages messages;
    private final Previews previews;
    private final Images images;
    private final Theme theme;

    @Inject
    public Resources(Constants constants, Messages messages, Previews previews, Images images, Theme theme) {
        this.constants = constants;
        this.messages = messages;
        this.previews = previews;
        this.images = images;
        this.theme = theme;
    }

    public Constants constants() {
        return constants;
    }

    public Messages messages() {
        return messages;
    }

    public Previews previews() {
        return previews;
    }

    public ExternalTextResource preview(String name) {
        return (ExternalTextResource) previews.getResource(name);
    }

    public Images images() {
        return images;
    }

    public Theme theme() {
        return theme;
    }
}
