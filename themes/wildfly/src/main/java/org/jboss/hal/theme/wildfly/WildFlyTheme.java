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
package org.jboss.hal.theme.wildfly;

import org.jboss.hal.resources.Logos;
import org.jboss.hal.resources.Theme;

import com.google.gwt.core.client.GWT;

public class WildFlyTheme implements Theme {

    private static final Logos LOGOS = GWT.create(WildFlyLogos.class);

    @Override
    public String getName() {
        return "WildFly";
    }

    @Override
    public String getFullName() {
        return "WildFly Application Server";
    }

    @Override
    public String getFirstName() {
        return "Wild";
    }

    @Override
    public String getLastName() {
        return "Fly";
    }

    @Override
    public Logos logos() {
        return LOGOS;
    }
}
