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

import org.jboss.hal.resources.Ids;

/** A user or a group with an optional realm. */
public class Principal {

    public enum Type {
        USER, GROUP
    }

    public static String buildResourceName(final Type type, final String name, final String realm) {
        StringBuilder builder = new StringBuilder();
        builder.append(type.name().toLowerCase()).append("-").append(name);
        if (realm != null) {
            builder.append("@").append(realm);
        }
        return builder.toString();
    }

    private final Type type;
    private final String resourceName;
    private final String name;
    private final String realm;

    public Principal(final Type type, final String resourceName, final String name, final String realm) {
        this.type = type;
        this.resourceName = resourceName;
        this.name = name;
        this.realm = realm;
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Principal)) {
            return false;
        }

        Principal principal = (Principal) o;
        if (type != principal.type) {
            return false;
        }
        if (!resourceName.equals(principal.resourceName)) {
            return false;
        }
        if (!name.equals(principal.name)) {
            return false;
        }
        return !(realm != null ? !realm.equals(principal.realm) : principal.realm != null);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + resourceName.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (realm != null ? realm.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return type + "(" + getNameAndRealm() + ")";
    }

    private String getNameAndRealm() {
        return realm == null ? name : name + "@" + realm;
    }

    public String getId() {
        return Ids.principal(getType().name().toLowerCase(), getName());
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getName() {
        return name;
    }

    public String getRealm() {
        return realm;
    }

    public Type getType() {
        return type;
    }
}
