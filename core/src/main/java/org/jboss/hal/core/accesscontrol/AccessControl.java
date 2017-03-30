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
package org.jboss.hal.core.accesscontrol;

import org.jboss.hal.config.Settings;
import org.jboss.hal.config.User;

import static org.jboss.hal.config.Role.ADMINISTRATOR;
import static org.jboss.hal.config.Role.SUPER_USER;
import static org.jboss.hal.config.Settings.Key.RUN_AS;

/**
 * Small helper class to check if the current user has access to restricted features.
 *
 * @author Harald Pehl
 */
public final class AccessControl {

    public static boolean isSuperUserOrAdministrator() {
        String runAs = Settings.INSTANCE.get(RUN_AS).value();
        if (runAs != null) {
            return runAs.equals(SUPER_USER.getName()) || runAs.equals(ADMINISTRATOR.getName());
        } else {
            return User.current().isSuperuser() || User.current().isAdministrator();
        }
    }

    private AccessControl() {}
}
