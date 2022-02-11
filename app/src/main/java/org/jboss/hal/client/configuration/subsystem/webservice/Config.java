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
package org.jboss.hal.client.configuration.subsystem.webservice;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

/** Enum struct for the client and endpoint configuration. */
enum Config {

    CLIENT_CONFIG(Ids.WEBSERVICES_CLIENT_CONFIG,
            Names.CLIENT_CONFIGURATION,
            ModelDescriptionConstants.CLIENT_CONFIG,
            AddressTemplates.CLIENT_CONFIG_TEMPLATE),

    ENDPOINT_CONFIG(Ids.WEBSERVICES_ENDPOINT_CONFIG,
            Names.ENDPOINT_CONFIGURATION,
            ModelDescriptionConstants.ENDPOINT_CONFIG,
            AddressTemplates.ENDPOINT_CONFIG_TEMPLATE);

    final String baseId;
    final String type;
    final String resource;
    final AddressTemplate template;

    Config(final String baseId, final String type, final String resource, final AddressTemplate template) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.template = template;
    }
}
