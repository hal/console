/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.EsReturn;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Represents a DMR operation.
 */
@JsType
public class Operation extends ModelNode {

    private final String name;
    private final ResourceAddress address;
    private final ModelNode parameter;
    private final ModelNode header;
    private final Set<String> roles;

    @JsIgnore
    public Operation(ModelNode modelNode) {
        this.name = modelNode.get(OP).asString();
        this.address = new ResourceAddress(modelNode.get(ADDRESS));
        this.parameter = modelNode.clone();
        this.parameter.remove(OP);
        this.parameter.remove(ADDRESS);
        this.parameter.remove(OPERATION_HEADERS);
        this.header = modelNode.hasDefined(OPERATION_HEADERS) ? modelNode.get(OPERATION_HEADERS) : new ModelNode();
        ModelNode roles = ModelNodeHelper.failSafeGet(modelNode, OPERATION_HEADERS + "/" + ROLES);
        if (roles.isDefined()) {
            if (roles.getType() == ModelType.LIST) {
                this.roles = roles.asList().stream().map(ModelNode::asString).collect(toSet());
            } else if (roles.getType() == ModelType.STRING) {
                this.roles = new HashSet<>();
                this.roles.add(roles.asString());
            } else {
                this.roles = new HashSet<>();
            }
        } else {
            this.roles = new HashSet<>();
        }
        addRolesAsHeaders();
        set(modelNode.clone());
    }

    Operation(String name, ResourceAddress address, ModelNode parameter, ModelNode header, Set<String> roles) {
        this.name = name;
        this.address = address;
        this.parameter = parameter == null ? new ModelNode() : parameter;
        this.header = header;
        this.roles = roles;

        set(this.parameter.clone());
        get(OP).set(name);
        get(ADDRESS).set(address);
        if (header.isDefined()) {
            get(OPERATION_HEADERS).set(header);
        }
        addRolesAsHeaders();
    }

    private void addRolesAsHeaders() {
        if (roles != null && !roles.isEmpty() && !name.equals(WHOAMI)) {
            // roles are headers!
            if (roles.size() == 1) {
                header.get(ROLES).set(roles.iterator().next());
            } else {
                roles.forEach(role -> header.get(ROLES).add(role));
            }
            get(OPERATION_HEADERS).set(header);
        }
    }

    /**
     * @return the name of the operation
     */
    @JsProperty
    public String getName() {
        return get(OP).asString();
    }

    /**
     * @return the address of the operation
     */
    @JsProperty
    public ResourceAddress getAddress() {
        return address;
    }

    /**
     * @return the parameters of the operation
     */
    @JsProperty
    public ModelNode getParameter() {
        return parameter;
    }

    /**
     * @return the header of the operation
     */
    @JsProperty
    public ModelNode getHeader() {
        return header;
    }

    @JsIgnore
    public boolean hasParameter() {
        return parameter.isDefined() && !parameter.asList().isEmpty();
    }

    @JsIgnore
    public Set<String> getRoles() {
        return roles;
    }

    @JsIgnore
    public Operation runAs(Set<String> runAs) {
        return new Operation(name, address, parameter, header, newHashSet(runAs));
    }

    /**
     * @return the string representation of the operation as used in the CLI
     */
    @Override
    public String toString() {
        return asCli();
    }

    /**
     * @return the string representation of the operation as used in the CLI
     */
    public String asCli() {
        StringBuilder builder = new StringBuilder();
        if (address.isDefined() && !address.asList().isEmpty()) {
            builder.append(address);
        }
        builder.append(":").append(name);
        if (hasParameter()) {
            builder.append("(");
            for (Iterator<Property> iterator = parameter.asPropertyList().iterator(); iterator.hasNext(); ) {
                Property p = iterator.next();
                builder.append(p.getName()).append("=").append(p.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append(")");
        }
        if (header.isDefined() && !header.asList().isEmpty()) {
            builder.append("{");
            for (Iterator<Property> iterator = header.asPropertyList().iterator(); iterator.hasNext(); ) {
                Property p = iterator.next();
                builder.append(p.getName()).append("=").append(p.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
        }
        return builder.toString();
    }


    /**
     * A builder for operations.
     */
    @JsType(name = "OperationBuilder")
    public static class Builder {

        private final String name;
        private final ResourceAddress address;
        private ModelNode parameter;
        private final ModelNode header;
        private final Set<String> roles;

        @JsIgnore
        public Builder(ResourceAddress address, String name) {
            this(address, name, false);
        }

        @JsIgnore
        public Builder(ResourceAddress address, String name, boolean resolveExpression) {
            this.address = address;
            this.name = name;
            this.parameter = new ModelNode();
            this.header = new ModelNode();
            this.roles = new HashSet<>();
            if (resolveExpression) {
                parameter.get(RESOLVE_EXPRESSION).set(resolveExpression);
            }
        }

        @JsIgnore
        public Builder param(String name, boolean value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder param(String name, int value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder param(String name, long value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder param(String name, double value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder param(String name, String value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder param(String name, String[] values) {
            for (String value : values) {
                parameter.get(name).add(value);
            }
            return this;
        }

        @JsIgnore
        public Builder param(String name, ModelNode value) {
            parameter.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder header(String name, String value) {
            header.get(name).set(value);
            return this;
        }

        @JsIgnore
        public Builder header(String name, boolean value) {
            header.get(name).set(value);
            return this;
        }

        /**
         * Uses the specified payload for the operation.
         *
         * @param payload The operation as model node.
         * @return this builder
         */
        @EsReturn("OperationBuilder")
        public Builder payload(ModelNode payload) {
            parameter = payload;
            return this;
        }

        /**
         * @return builds and returns the operation
         */
        public Operation build() {
            return new Operation(name, address, parameter, header, roles);
        }


        // ------------------------------------------------------ JS methods

        /**
         * Add a parameter to the operation
         *
         * @param name  The name of the parameter.
         * @param value The value of the parameter.
         * @return this builder
         */
        @JsMethod(name = "param")
        @EsReturn("OperationBuilder")
        public Builder jsParam(String name, @EsParam("boolean|int|string") Object value) {
            if (value instanceof Boolean) {
                param(name, ((Boolean) value));
            } else if (value instanceof Integer) {
                param(name, ((Integer) value));
            } else if (value instanceof String) {
                param(name, ((String) value));
            } else if (value instanceof ModelNode) {
                param(name, ((ModelNode) value));
            }
            return this;
        }

        /**
         * Add a header to the operation
         *
         * @param name  The name of the header.
         * @param value The value of the header.
         * @return this builder
         */
        @JsMethod(name = "header")
        @EsReturn("OperationBuilder")
        public Builder jsHeader(String name, @EsParam("boolean|int|string") Object value) {
            if (value instanceof Boolean) {
                header(name, ((Boolean) value));
            } else if (value instanceof String) {
                header(name, ((String) value));
            }
            return this;
        }
    }
}
