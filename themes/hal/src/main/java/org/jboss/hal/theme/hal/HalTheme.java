/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.theme.hal;

import org.jboss.hal.resources.Logos;
import org.jboss.hal.resources.Theme;

import com.google.gwt.core.client.GWT;

import static org.jboss.hal.resources.Names.HAL;
import static org.jboss.hal.resources.Names.MANAGEMENT_CONSOLE;

public class HalTheme implements Theme {

    private static final Logos LOGOS = GWT.create(HalLogos.class);

    public HalTheme() {
    }

    @Override
    public String getName() {
        return HAL;
    }

    @Override
    public String getFullName() {
        return HAL + " " + MANAGEMENT_CONSOLE;
    }

    @Override
    public String getFirstName() {
        return HAL + " ";
    }

    @Override
    public String getLastName() {
        return MANAGEMENT_CONSOLE;
    }

    @Override
    public Logos logos() {
        return LOGOS;
    }
}
