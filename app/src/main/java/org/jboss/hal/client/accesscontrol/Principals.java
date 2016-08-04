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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.client.accesscontrol.Principal.Type.GROUP;
import static org.jboss.hal.client.accesscontrol.Principal.Type.USER;

/**
 * Contains a list of principals stored in the management model.
 *
 * @author Harald Pehl
 */
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
            if (set != null) {
                set.add(principal);
            }
            // Principal.getId() already encodes the type
            lookup.put(principal.getResourceName(), principal);
        }
    }

    boolean contains(final Principal principal) {
        return lookup.containsKey(principal.getResourceName());
    }

    Principal get(final String id) {
        return lookup.get(id);
    }

    Set<Principal> get(Principal.Type type) {
        return principals.get(type);
    }

    void remove(final Principal principal) {
        principals.get(principal.getType()).remove(principal);
        lookup.remove(principal.getResourceName());
    }

    void clear() {
        if (principals.containsKey(GROUP)) {
            principals.get(GROUP).clear();
        }
        if (principals.containsKey(USER)) {
            principals.get(USER).clear();
        }
        lookup.clear();
    }

    @Override
    public Iterator<Principal> iterator() {
        return lookup.values().iterator();
    }
}
