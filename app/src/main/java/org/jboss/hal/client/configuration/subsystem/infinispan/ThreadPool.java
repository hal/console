/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

/**
 * Represents the different thread pool singletons of a cache container.
 */
enum ThreadPool {

    ASYNC(Ids.CACHE_CONTAINER_THREAD_POOL_ASYNC,
            Names.ASYNC, ModelDescriptionConstants.ASYNC, true),

    BLOCKING(Ids.CACHE_CONTAINER_THREAD_POOL_BLOCKING,
            Names.BLOCKING, ModelDescriptionConstants.BLOCKING, false),

    EXPIRATION(Ids.CACHE_CONTAINER_THREAD_POOL_EXPIRATION,
            Names.EXPIRATION, ModelDescriptionConstants.EXPIRATION, false),

    LISTENER(Ids.CACHE_CONTAINER_THREAD_POOL_LISTENER,
            Names.LISTENER, ModelDescriptionConstants.LISTENER, false),

    NON_BLOCKING(Ids.CACHE_CONTAINER_THREAD_POOL_NON_BLOCKING,
            Names.NON_BLOCKING, ModelDescriptionConstants.NON_BLOCKING, false),
            ;

    final String baseId;
    final String type;
    final String resource;
    final boolean remote;

    ThreadPool(String baseId, String type, String resource, boolean remote) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
        this.remote = remote;
    }

    String path() {
        return THREAD_POOL + "/" + resource;
    }

    AddressTemplate template() {
        return CACHE_CONTAINER_TEMPLATE.append(THREAD_POOL + "=" + resource);
    }
}
