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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

enum Write {

    BEHIND(Ids.CACHE_STORE_WRITE_BEHIND, Names.WRITE_BEHIND, ModelDescriptionConstants.BEHIND),
    THROUGH(Ids.CACHE_STORE_WRITE_THROUGH, Names.WRITE_THROUGH, ModelDescriptionConstants.THROUGH);

    final String baseId;
    final String type;
    final String resource;

    Write(final String baseId, final String type, final String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String path() {
        return ModelDescriptionConstants.WRITE + "/" + resource;
    }
}
