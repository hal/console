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

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;

/**
 * @author Harald Pehl
 */
public class Role {

    public static final Role ADMINISTRATOR = new Role("Administrator");
    public static final Role AUDITOR = new Role("Auditor");
    public static final Role DEPLOYER = new Role("Deployer");
    public static final Role MAINTAINER = new Role("Maintainer");
    public static final Role MONITOR = new Role("Monitor");
    public static final Role OPERATOR = new Role("Operator");
    public static final Role SUPER_USER = new Role("SuperUser");


    public enum Type {
        STANDARD, HOST, SERVER_GROUP
    }


    private final String name;
    private final Type type;
    private final Role baseRole;
    private final SortedSet<String> scope;
    private boolean includeAll;

    public Role(@NonNls final String name) {
        this(name, null, Type.STANDARD, Collections.emptySet());
    }

    public Role(@NonNls final String name, final Role baseRole, final Type type,
            final Iterable<String> scope) {
        this.name = name;
        this.baseRole = baseRole;
        this.type = type;
        this.scope = new TreeSet<>();
        if (scope != null) {
            scope.forEach(this.scope::add);
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

    public String getId() {
        return Ids.role(name);
    }

    public boolean isStandard() {
        return type == Type.STANDARD;
    }

    public boolean isScoped() {
        return type != Type.STANDARD;
    }

    public String getName() {
        return name;
    }

    public Role getBaseRole() {
        return baseRole;
    }

    public Type getType() {
        return type;
    }

    public SortedSet<String> getScope() {
        return scope;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(final boolean includeAll) {
        this.includeAll = includeAll;
    }
}
