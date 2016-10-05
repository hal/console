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
package org.jboss.hal.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Sizes according to https://github.com/audreyr/favicon-cheat-sheet#the-images
 *
 * @author Harald Pehl
 */
public interface Favicons extends ClientBundle {

    String FILE_NAME_16 = "favicon.png";
    String FILE_NAME_32 = "icon-32.png";
    String FILE_NAME_128 = "icon-128.png";
    String FILE_NAME_152 = "icon-152.png";

    ImageResource x16();

    ImageResource x32();

    ImageResource x128();

    ImageResource x152();
}
