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
package org.jboss.hal.client.configuration.subsystem.securitymanager;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.resources.CSS.fontAwesome;

enum Permission {

    MINIMUM_PERMISSIONS(Ids.SECURITY_MANAGER_MINIMUM_PERMISSIONS, Names.MINIMUM_PERMISSIONS,
            fontAwesome("minus-square-o"), ModelDescriptionConstants.MINIMUM_PERMISSIONS), MAXIMUM_PERMISSIONS(
                    Ids.SECURITY_MANAGER_MAXIMUM_PERMISSIONS, Names.MAXIMUM_PERMISSIONS,
                    fontAwesome("plus-square-o"), ModelDescriptionConstants.MAXIMUM_PERMISSIONS);

    final String baseId;
    final String type;
    final String icon;
    final String resource;

    Permission(String baseId, String type, String icon, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.icon = icon;
        this.resource = resource;
    }
}
