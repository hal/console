/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.accesscontrol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.client.accesscontrol.Principal.Type.GROUP;
import static org.jboss.hal.client.accesscontrol.Principal.Type.USER;

/** Contains a list of principals stored in the management model. */
class Principals implements Iterable<Principal> {

    private final Map<Principal.Type, Set<Principal>> principals;
    private final Map<String, Principal> lookup;

    Principals() {
        principals = new HashMap<>();
        principals.put(GROUP, new HashSet<>());
        principals.put(USER, new HashSet<>());
        lookup = new HashMap<>();
    }

    void add(Principal principal) {
        if (principal != null) {
            Set<Principal> set = principals.get(principal.getType());
            set.add(principal);
            lookup.put(principal.getId(), principal);
        }
    }

    void clear() {
        principals.get(GROUP).clear();
        principals.get(USER).clear();
        lookup.clear();
    }

    Principal get(final String id) {
        return lookup.get(id);
    }

    Set<Principal> users() {
        return principals.get(USER);
    }

    Set<Principal> groups() {
        return principals.get(GROUP);
    }

    @Override
    public Iterator<Principal> iterator() {
        return lookup.values().iterator();
    }
}
