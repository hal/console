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
package org.jboss.hal.dmr.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class Operation extends ModelNode {

    public static class Builder {

        private final String name;
        private final ResourceAddress address;
        private ModelNode parameter;
        private ModelNode header;
        private List<String> roles;

        public Builder(final String name, final ResourceAddress address) {
            this.address = address;
            this.name = name;
            this.parameter = new ModelNode();
            this.header = new ModelNode();
            this.roles = new ArrayList<>();
        }

        public Builder param(String name, boolean value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder param(String name, int value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder param(String name, long value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder param(String name, double value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder param(String name, @NonNls String value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder param(String name, @NonNls String[] values) {
            for (String value : values) {
                parameter.get(name).add(value);
            }
            return this;
        }

        public Builder param(String name, ModelNode value) {
            parameter.get(name).set(value);
            return this;
        }

        public Builder header(String name, String value) {
            header.get(name).set(value);
            return this;
        }

        public Builder header(String name, boolean value) {
            header.get(name).set(value);
            return this;
        }

        public Builder payload(ModelNode payload) {
            parameter = payload;
            return this;
        }

        public Builder runAs(String role, String... moreRoles) {
            this.roles.add(role);
            if (moreRoles != null) {
                roles.addAll(Arrays.asList(moreRoles));
            }
            return this;
        }

        public Operation build() {
            return new Operation(name, address, parameter, header, roles);
        }
    }


    private final String name;
    private final ResourceAddress address;
    private final ModelNode parameter;
    private final ModelNode header;
    private final List<String> roles;

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
                this.roles = roles.asList().stream().map(ModelNode::asString).collect(toList());
            } else if (roles.getType() == ModelType.STRING) {
                this.roles = new ArrayList<>();
                this.roles.add(roles.asString());
            } else {
                this.roles = new ArrayList<>();
            }
        } else {
            this.roles = new ArrayList<>();
        }
        addRolesAsHeaders();
        set(modelNode.clone());
    }

    public Operation(final String name, final ResourceAddress address, final ModelNode parameter,
            final ModelNode header, final List<String> roles) {
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
                this.header.get(ROLES).set(roles.get(0));
            } else {
                roles.forEach(role -> this.header.get(ROLES).add(role));
            }
            roles.forEach(role -> get(OPERATION_HEADERS).get(ROLES).add(role));
        }
    }

    public String getName() {
        return get(OP).asString();
    }

    public ResourceAddress getAddress() {
        return address;
    }

    public ModelNode getParameter() {
        return parameter;
    }

    public ModelNode getHeader() {
        return header;
    }

    public boolean hasParamter() {
        return parameter.isDefined() && !parameter.asList().isEmpty();
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public Operation clone() {
        return new Operation(name, address, parameter, header, roles);
    }

    @Override
    public String toString() {
        return asCli();
    }

    public String asCli() {
        StringBuilder builder = new StringBuilder();
        if (address.isDefined() && !address.asList().isEmpty()) {
            builder.append(address);
        }
        builder.append(":").append(name);
        if (hasParamter()) {
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
}
