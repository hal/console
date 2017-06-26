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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import elemental2.core.Array;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.spi.EsReturn;

import static java.util.Comparator.comparing;
import static org.jboss.hal.config.Role.*;

/**
 * Provides access to all standard and scoped roles.
 *
 * @author Harald Pehl
 */
@JsType
public class Roles implements Iterable<Role> {

    @JsIgnore public static final Comparator<Role> STANDARD_FIRST = comparing(Role::getType);
    @JsIgnore public static final Comparator<Role> BY_NAME = comparing(Role::getName);
    @JsIgnore public static final Set<Role> DEFAULT_ROLES = Sets.newHashSet(
            ADMINISTRATOR,
            AUDITOR,
            DEPLOYER,
            MAINTAINER,
            MONITOR,
            OPERATOR,
            SUPER_USER);

    private final Map<String, Role> lookup;
    private final Set<Role> standardRoles;
    private final Set<Role> scopedRoles;

    Roles() {
        this.lookup = new HashMap<>();
        this.standardRoles = new HashSet<>();
        this.scopedRoles = new HashSet<>();
    }

    @JsIgnore
    public void add(Role role) {
        if (role != null) {
            lookup.put(role.getId(), role);
            if (role.isStandard()) {
                standardRoles.add(role);
            } else if (role.isScoped()) {
                scopedRoles.add(role);
            }
        }
    }

    @JsIgnore
    public void addAll(Iterable<Role> roles) {
        roles.forEach(this::add);
    }

    @JsIgnore
    public void clear() {
        lookup.clear();
        standardRoles.clear();
        scopedRoles.clear();
    }

    /**
     * @param id The unique ID of the role.
     *
     * @return the role for that ID or null if no such role was found.
     */
    public Role get(String id) {
        if (id != null) {
            return lookup.get(id);
        }
        return null;
    }

    @JsIgnore
    public Set<Role> standardRoles() {
        return standardRoles;
    }

    @JsIgnore
    public Set<Role> scopedRoles() {
        return scopedRoles;
    }

    @Override
    @JsIgnore
    public Iterator<Role> iterator() {
        return lookup.values().iterator();
    }


    // ------------------------------------------------------ JS methods

    /**
     * @return all roles (standard and scoped).
     */
    @JsProperty(name = "all")
    @EsReturn("Role[]")
    public Array<Role> jsAll() {
        Array<Role> array = new Array<>();
        for (Role role : lookup.values()) {
            array.push(role);
        }
        return array;
    }

    /**
     * @return standard roles.
     */
    @JsProperty(name = "standardRoles")
    @EsReturn("Role[]")
    public Array<Role> jsStandardRoles() {
        Array<Role> array = new Array<>();
        for (Role role : standardRoles) {
            array.push(role);
        }
        return array;
    }

    /**
     * @return scoped roles or an empty array if no scoped roles are defined.
     */
    @JsProperty(name = "scopedRoles")
    @EsReturn("Role[]")
    public Array<Role> jsScopedRoles() {
        Array<Role> array = new Array<>();
        for (Role role : scopedRoles) {
            array.push(role);
        }
        return array;
    }
}
