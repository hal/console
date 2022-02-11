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
package org.jboss.hal.meta.security;

import org.jboss.hal.dmr.ModelNode;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Represents the RBAC related payload from the read-resource-description operation. */
@JsType(namespace = "hal.meta")
@SuppressWarnings("SimplifiableIfStatement")
public class SecurityContext extends ModelNode {

    /** A security context with hardcoded permissions to read resources, write and execute operations are not allowed. */
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
        public boolean isReadable(String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(String attribute) {
            return false;
        }

        @Override
        public boolean isExecutable(String operation) {
            return false;
        }
    };

    /** A security context with hardcoded permissions to read, write and execute any resource. */
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
        public boolean isReadable(String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(String attribute) {
            return true;
        }

        @Override
        public boolean isExecutable(String operation) {
            return true;
        }
    };

    @JsIgnore
    public SecurityContext(ModelNode payload) {
        set(payload);
    }

    /**
     * @return whether the security context is readable
     */
    @JsProperty
    public boolean isReadable() {
        return get(READ).asBoolean();
    }

    /**
     * @return whether the security context is writable
     */
    @JsProperty
    public boolean isWritable() {
        return get(WRITE).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     *
     * @return whether the attribute is readable
     */
    public boolean isReadable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(READ).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     *
     * @return whether the attribute is writable
     */
    public boolean isWritable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(WRITE).asBoolean();
    }

    /**
     * @param operation The operation to check.
     *
     * @return whether the operation is executable
     */
    public boolean isExecutable(String operation) {
        return hasDefined(OPERATIONS) &&
                get(OPERATIONS).hasDefined(operation) &&
                get(OPERATIONS).get(operation).get(EXECUTE).asBoolean();
    }
}
