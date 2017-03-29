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
package org.jboss.hal.config;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.hal.config.Role.ADMINISTRATOR;
import static org.jboss.hal.config.Role.SUPER_USER;

/**
 * @author Harald Pehl
 */
public class User {

    private static final User current = new User("Unknown", new HashSet<>()); //NON-NLS

    public static User current() {
        return current;
    }

    private final Set<Role> roles;
    private String name;

    private User(final String name, final Set<Role> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public boolean isSuperuser() {
        for (Role role : roles) {
            if (SUPER_USER.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdministrator() {
        for (Role role : roles) {
            if (ADMINISTRATOR.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
