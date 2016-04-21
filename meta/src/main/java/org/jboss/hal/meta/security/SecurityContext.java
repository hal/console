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
package org.jboss.hal.meta.security;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;

/**
 * Represents the RBAC related payload for the {@code r-r-d} operation.
 *
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class SecurityContext extends ModelNode {

    public static final String RBAC_DATA_KEY = "rbac";

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

    /**
     * A security context with hardcoded permissions to read, write and execute any resource.
     * 
     */
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
