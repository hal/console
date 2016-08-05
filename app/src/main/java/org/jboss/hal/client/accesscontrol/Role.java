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

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.hal.resources.Ids;

/**
 * @author Harald Pehl
 */
class Role {

    enum Type {
        STANDARD, HOST, SERVER_GROUP
    }


    private String name;
    private Type type;
    private Role baseRole;
    private SortedSet<String> scope;
    private boolean includeAll;

    Role(String name) {
        this(name, null, Type.STANDARD, Collections.emptySet());
    }

    Role(final String name, final Role baseRole, final Type type,
            final Iterable<String> scope) {
        this.name = name;
        this.baseRole = baseRole;
        this.type = type;
        this.scope = new TreeSet<>();
        if (scope != null) {
            scope.forEach(s -> this.scope.add(s));
        }
        this.includeAll = false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }

        Role role = (Role) o;
        //noinspection RedundantIfStatement
        if (!name.equals(role.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        if (isStandard()) {
            return name;
        }
        return name + " extends " + baseRole.getName() + " scoped to " + type.name()
                .toLowerCase() + scope + ", includeAll: " + includeAll;
    }

    boolean isStandard() {
        return type == Type.STANDARD;
    }

    boolean isScoped() {
        return type != Type.STANDARD;
    }

    String getId() {
        return Ids.role(name);
    }

    String getName() {
        return name;
    }

    void setName(final String name) {
        this.name = name;
    }

    Role getBaseRole() {
        return baseRole;
    }

    void setBaseRole(final Role baseRole) {
        this.baseRole = baseRole;
    }

    Type getType() {
        return type;
    }

    void setType(final Type type) {
        this.type = type;
    }

    SortedSet<String> getScope() {
        return scope;
    }

    void setScope(final Collection<String> scope) {
        this.scope.clear();
        this.scope.addAll(scope);
    }

    boolean isIncludeAll() {
        return includeAll;
    }

    void setIncludeAll(final boolean includeAll) {
        this.includeAll = includeAll;
    }
}
