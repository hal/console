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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

enum Table {

    BINARY(Ids.CACHE_STORE_BINARY_TABLE, Names.BINARY_TABLE, ModelDescriptionConstants.BINARY), STRING(
            Ids.CACHE_STORE_STRING_TABLE, Names.STRING_TABLE, ModelDescriptionConstants.STRING);

    final String baseId;
    final String type;
    final String resource;

    Table(String baseId, String type, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String path() {
        return ModelDescriptionConstants.TABLE + "/" + resource;
    }
}
