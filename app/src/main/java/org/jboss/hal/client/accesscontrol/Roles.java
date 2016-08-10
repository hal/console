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
package org.jboss.hal.client.accesscontrol;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparing;

/**
 * Contains the list of standard roles plus the custom defined scoped roles.
 *
 * @author Harald Pehl
 */
class Roles implements Iterable<Role> {

    static final Comparator<Role> STANDARD_FIRST = comparing(Role::getType);
    static final Comparator<Role> BY_NAME = comparing(Role::getName);

    private final Map<String, Role> lookup;
    private final Set<Role> standardRoles;
    private final Set<Role> scopedRoles;

    Roles() {
        this.lookup = new HashMap<>();
        this.standardRoles = new HashSet<>();
        this.scopedRoles = new HashSet<>();
    }

    void add(Role role) {
        if (role != null) {
            lookup.put(role.getId(), role);
            if (role.isStandard()) {
                standardRoles.add(role);
            } else if (role.isScoped()) {
                scopedRoles.add(role);
            }
        }
    }

    void clear() {
        lookup.clear();
        standardRoles.clear();
        scopedRoles.clear();
    }

    Role get(String id) {
        if (id != null) {
            return lookup.get(id);
        }
        return null;
    }

    Set<Role> standardRoles() {
        return standardRoles;
    }

    Set<Role> scopedRoles() {
        return scopedRoles;
    }

    @Override
    public Iterator<Role> iterator() {
        return lookup.values().iterator();
    }
}
