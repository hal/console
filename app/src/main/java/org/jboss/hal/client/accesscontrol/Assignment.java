/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.accesscontrol;

import org.jboss.hal.config.Role;

/**
 * An assignment between a principal and a role.
 */
class Assignment {

    private final Principal principal;
    private final Role role;
    private final boolean include;

    Assignment(final Principal principal, final Role role, final boolean include) {
        this.principal = principal;
        this.role = role;
        this.include = include;
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Assignment)) {
            return false;
        }

        Assignment that = (Assignment) o;
        if (include != that.include) {
            return false;
        }
        if (!principal.equals(that.principal)) {
            return false;
        }
        return role.equals(that.role);

    }

    @Override
    public int hashCode() {
        int result = principal.hashCode();
        result = 31 * result + role.hashCode();
        result = 31 * result + (include ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return (include ? "Include " : "Exclude ") + principal + " -> " + role;
    }

    Principal getPrincipal() {
        return principal;
    }

    Role getRole() {
        return role;
    }

    boolean isInclude() {
        return include;
    }
}
