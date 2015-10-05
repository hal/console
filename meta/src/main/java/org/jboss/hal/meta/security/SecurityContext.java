/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.meta.security;

import org.jboss.hal.dmr.ModelNode;

/**
 * Represents the RBAC related payload for the {@code r-r-d} operation.
 *
 * @author Harald Pehl
 */
public class SecurityContext extends ModelNode {

    public static final SecurityContext READ_ONLY = new SecurityContext(new ModelNode()) {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean isReadable(final String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(final String attribute) {
            return false;
        }

        @Override
        public boolean isExecutable(final String operation) {
            return false;
        }
    };

    public static final SecurityContext RWX = new SecurityContext(new ModelNode()) {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isReadable(final String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(final String attribute) {
            return true;
        }

        @Override
        public boolean isExecutable(final String operation) {
            return true;
        }
    };

    private static final String OPERATIONS = "operations";
    private static final String ATTRIBUTES = "attributes";

    public SecurityContext(ModelNode payload) {
        set(payload);
    }

    public boolean isReadable() {
        return get("read").asBoolean();
    }

    public boolean isWritable() {
        return get("write").asBoolean();
    }

    public boolean isReadable(final String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get("read").asBoolean();
    }

    public boolean isWritable(final String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get("write").asBoolean();
    }

    public boolean isExecutable(String operation) {
        return hasDefined(OPERATIONS) &&
                get(OPERATIONS).hasDefined(operation) &&
                get(OPERATIONS).get(operation).get("execute").asBoolean();
    }
}
