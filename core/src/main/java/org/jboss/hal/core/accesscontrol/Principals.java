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
package org.jboss.hal.core.accesscontrol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.core.accesscontrol.Principal.Type;

/** Contains a list of principals stored in the management model. */
public class Principals implements Iterable<Principal> {

    private final Map<Type, Set<Principal>> principals;
    private final Map<String, Principal> lookup;

    Principals() {
        principals = new HashMap<>();
        principals.put(Type.GROUP, new HashSet<>());
        principals.put(Type.USER, new HashSet<>());
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
        principals.get(Type.GROUP).clear();
        principals.get(Type.USER).clear();
        lookup.clear();
    }

    public Principal get(final String id) {
        return lookup.get(id);
    }

    public Set<Principal> users() {
        return principals.get(Type.USER);
    }

    public Set<Principal> groups() {
        return principals.get(Type.GROUP);
    }

    @Override
    public Iterator<Principal> iterator() {
        return lookup.values().iterator();
    }
}
