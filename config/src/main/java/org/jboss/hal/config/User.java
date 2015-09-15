/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Harald Pehl
 */
public class User {

    public static final String SUPER_USER = "SuperUser";
    public static final String ADMINISTRTATOR = "Administrator";
    public static final User UNKNOWN = new User("Unknown", new HashSet<>());
    private static User current = UNKNOWN;

    public static User current() {
        return current;
    }

    private String name;
    private Set<String> roles;

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
            if (ADMINISTRTATOR.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
