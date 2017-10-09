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
package org.jboss.hal.client.accesscontrol;

import org.jboss.hal.config.Role;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.config.Role.Type.HOST;
import static org.jboss.hal.config.Role.Type.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXCLUDE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_SCOPED_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLE_MAPPING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP_SCOPED_ROLE;
import static org.jboss.hal.meta.AddressTemplate.OPTIONAL;

interface AddressTemplates {

    String ROOT_ADDRESS = "/core-service=management/access=authorization";
    String ROLE_MAPPING_ADDRESS = ROOT_ADDRESS + "/role-mapping=*";
    String INCLUDE_ADDRESS = ROLE_MAPPING_ADDRESS + "/include=*";
    String EXCLUDE_ADDRESS = ROLE_MAPPING_ADDRESS + "/exclude=*";
    String HOST_SCOPED_ROLE_ADDRESS = OPTIONAL + ROOT_ADDRESS + "/host-scoped-role=*";
    String SERVER_GROUP_SCOPED_ROLE_ADDRESS = OPTIONAL + ROOT_ADDRESS + "/server-group-scoped-role=*";

    AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);
    AddressTemplate ROLE_MAPPING_TEMPLATE = AddressTemplate.of(ROLE_MAPPING_ADDRESS);
    AddressTemplate INCLUDE_TEMPLATE = AddressTemplate.of(INCLUDE_ADDRESS);
    AddressTemplate EXCLUDE_TEMPLATE = AddressTemplate.of(EXCLUDE_ADDRESS);
    AddressTemplate SERVER_GROUP_SCOPED_ROLE_TEMPLATE = AddressTemplate.of(SERVER_GROUP_SCOPED_ROLE_ADDRESS);
    AddressTemplate HOST_SCOPED_ROLE_TEMPLATE = AddressTemplate.of(HOST_SCOPED_ROLE_ADDRESS);

    static ResourceAddress root() {
        return ROOT_TEMPLATE.resolve(StatementContext.NOOP);
    }

    static ResourceAddress roleMapping(Role role) {
        return root().add(ROLE_MAPPING, role.getName());
    }

    static ResourceAddress assignment(Assignment assignment) {
        return roleMapping(assignment.getRole())
                .add(assignment.isInclude() ? INCLUDE : EXCLUDE, assignment.getPrincipal().getResourceName());
    }

    static ResourceAddress scopedRole(Role scopedRole) {
        ResourceAddress address = root();
        if (scopedRole.getType() == HOST) {
            address.add(HOST_SCOPED_ROLE, scopedRole.getName());
        } else if (scopedRole.getType() == SERVER_GROUP) {
            address.add(SERVER_GROUP_SCOPED_ROLE, scopedRole.getName());
        }
        return address;
    }
}
