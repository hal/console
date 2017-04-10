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
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE;

/**
 * Represents the RBAC related payload from the {@code r-r-d} operation.
 *
 * @author Harald Pehl
 */
@SuppressWarnings("SimplifiableIfStatement")
public class SecurityContext extends ModelNode {

    /**
     * A security context with hardcoded permissions to read resources, write and execute operations are not allowed.
     */
    public static final SecurityContext READ_ONLY = new SecurityContext(ResourceAddress.root(), new ModelNode()) {
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
     */
    public static final SecurityContext RWX = new SecurityContext(ResourceAddress.root(), new ModelNode()) {
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


    private final ResourceAddress address;

    public SecurityContext(final ResourceAddress address, final ModelNode payload) {
        this.address = address;
        set(payload);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof SecurityContext)) { return false; }
        if (!super.equals(o)) { return false; }

        SecurityContext that = (SecurityContext) o;

        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + address.hashCode();
        return result;
    }

    public ResourceAddress getAddress() {
        return address;
    }

    public boolean isReadable() {
        return get(READ).asBoolean();
    }

    public boolean isWritable() {
        return get(WRITE).asBoolean();
    }

    public boolean isReadable(final String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(READ).asBoolean();
    }

    public boolean isWritable(final String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(WRITE).asBoolean();
    }

    public boolean isExecutable(String operation) {
        return hasDefined(OPERATIONS) &&
                get(OPERATIONS).hasDefined(operation) &&
                get(OPERATIONS).get(operation).get(EXECUTE).asBoolean();
    }
}
