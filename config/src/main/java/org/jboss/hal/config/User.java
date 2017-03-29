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

/**
 * @author Harald Pehl
 */
public class User {

    public static final String SUPER_USER = "SuperUser";
    public static final String ADMINISTRATOR = "Administrator";
    public static final User UNKNOWN = new User("Unknown", new HashSet<>()); //NON-NLS
    private static final User current = UNKNOWN;

    public static User current() {
        return current;
    }

    private final Set<String> roles;
    private String name;
    private String runAs;

    public User(final String name, final Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public boolean isSuperuser() {
        for (String role : roles) {
            if (SUPER_USER.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdministrator() {
        for (String role : roles) {
            if (ADMINISTRATOR.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(final String runAs) {
        this.runAs = runAs;
    }
}
