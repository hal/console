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
package org.jboss.hal.theme;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.resources.Favicons;
import org.jboss.hal.resources.Logos;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Theme;

import static org.jboss.hal.resources.Names.HAL;

/**
 * @author Harald Pehl
 */
public class HalTheme implements Theme {

    private static final Favicons FAVICONS = GWT.create(HalFavicons.class);
    private static final Logos LOGOS = GWT.create(HalLogos.class);

    @Override
    public String getId() {
        return "org.jboss.hal.theme.hal";
    }

    @Override
    public String getTitle() {
        return HAL;
    }

    @Override
    public String getSecondaryTitle() {
        return Names.MANAGEMENT_CONSOLE;
    }

    @Override
    public String getDescription() {
        return "HAL Management Console";
    }

    @Override
    public Favicons favicons() {
        return FAVICONS;
    }

    @Override
    public Logos logos() {
        return LOGOS;
    }
}
