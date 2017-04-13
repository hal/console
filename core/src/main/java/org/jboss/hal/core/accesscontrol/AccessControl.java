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

import java.util.Set;
import javax.inject.Inject;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
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
public class AccessControl {

    private final Environment environment;
    private final Settings settings;
    private final User user;

    @Inject
    public AccessControl(final Environment environment, final Settings settings, final User user) {
        this.environment = environment;
        this.settings = settings;
        this.user = user;
    }

    public boolean isSuperUserOrAdministrator() {
        if (environment.getAccessControlProvider() == AccessControlProvider.RBAC) {
            Set<String> runAs = settings.get(RUN_AS).asSet();
            if (runAs.isEmpty()) {
                return user.isSuperuser() || user.isAdministrator();
            } else {
                return runAs.contains(SUPER_USER.getName()) || runAs.contains(ADMINISTRATOR.getName());
            }
        }
        return true;
    }
}
