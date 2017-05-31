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

import elemental2.core.Array;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.EsReturn;
import org.jetbrains.annotations.NonNls;

/**
 * A standard or scoped role used when RBAC is turned on.
 *
 * @author Harald Pehl
 */
@JsType
public class Role {

    /**
     * Has all permissions except cannot read or write resources related to the administrative audit logging system.
     */
    public static final Role ADMINISTRATOR = new Role("Administrator");
    /**
     * Can read anything. Can only modify the resources related to the administrative audit logging system.
     */
    public static final Role AUDITOR = new Role("Auditor");
    /**
     * Like a Maintainer, but with permission to modify persistent configuration constrained to resources that are
     * considered to be "application resources". A deployment is an application resource. The messaging server is not.
     * Items like datasources and JMS destinations are not considered to be application resources by default, but this
     * is configurable.
     */
    public static final Role DEPLOYER = new Role("Deployer");
    /**
     * Operator permissions, plus can modify the persistent configuration.
     */
    public static final Role MAINTAINER = new Role("Maintainer");
    /**
     * A read-only role. Cannot modify any resource.
     */
    public static final Role MONITOR = new Role("Monitor");
    /**
     * Monitor permissions, plus can modify runtime state, but cannot modify anything that ends up in the persistent
     * configuration. Could, for example, restart a server.
     */
    public static final Role OPERATOR = new Role("Operator");
    /**
     * Has all permissions. Equivalent to a JBoss AS 7 administrator.
     */
    public static final Role SUPER_USER = new Role("SuperUser");


    public enum Type {
        STANDARD, HOST, SERVER_GROUP
    }


    private final String name;
    private final Type type;
    private final Role baseRole;
    private final SortedSet<String> scope;
    private boolean includeAll;

    @JsIgnore
    public Role(@NonNls final String name) {
        this(name, null, Type.STANDARD, Collections.emptySet());
    }

    @JsIgnore
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
    @JsIgnore
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
    @JsIgnore
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @return a string representation of this role.
     */
    @Override
    public String toString() {
        if (isStandard()) {
            return name;
        }
        //noinspection DuplicateStringLiteralInspection
        return name + " extends " + baseRole.getName() + " scoped to " + type.name()
                .toLowerCase() + scope + ", includeAll: " + includeAll;
    }

    /**
     * @return the unique ID of this role.
     */
    @JsProperty
    public String getId() {
        return Ids.role(name);
    }

    /**
     * @return true if this is a standard role, false otherwise.
     */
    @JsProperty
    public boolean isStandard() {
        return type == Type.STANDARD;
    }

    /**
     * @return true if this is a scoped role, false otherwise.
     */
    @JsProperty
    public boolean isScoped() {
        return type != Type.STANDARD;
    }

    /**
     * @return the name of this role.
     */
    @JsProperty
    public String getName() {
        return name;
    }

    /**
     * @return the base role if this is a scoped role, null otherwise.
     */
    @JsProperty
    public Role getBaseRole() {
        return baseRole;
    }

    @JsIgnore
    public Type getType() {
        return type;
    }

    @JsIgnore
    public SortedSet<String> getScope() {
        return scope;
    }

    @JsIgnore
    public boolean isIncludeAll() {
        return includeAll;
    }

    @JsIgnore
    public void setIncludeAll(final boolean includeAll) {
        this.includeAll = includeAll;
    }


    // ------------------------------------------------------ JS methods

    /**
     * @return the scopes if this is a scoped role, an empty array otherwise.
     */
    @JsProperty(name = "scope")
    @EsReturn("string[]")
    public Array<String> jsScope() {
        Array<String> array = new Array<>();
        for (String scope : getScope()) {
            array.push(scope);
        }
        return array;
    }
}
