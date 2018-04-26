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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

/** Represents the different thread pool singletons of a cache container. */
enum ThreadPool {

    ASYNC_OPERATIONS(Ids.CACHE_CONTAINER_THREAD_POOL_ASYNC_OPERATIONS, Names.ASYNC_OPERATIONS,
            ModelDescriptionConstants.ASYNC_OPERATIONS),
    EXPIRATION(Ids.CACHE_CONTAINER_THREAD_POOL_EXPIRATION, Names.EXPIRATION, ModelDescriptionConstants.EXPIRATION),
    LISTENER(Ids.CACHE_CONTAINER_THREAD_POOL_LISTENER, Names.LISTENER, ModelDescriptionConstants.LISTENER),
    PERSISTENCE(Ids.CACHE_CONTAINER_THREAD_POOL_PERSISTENCE, Names.PERSISTENCE, ModelDescriptionConstants.PERSISTENCE),
    REMOTE_COMMAND(Ids.CACHE_CONTAINER_THREAD_POOL_REMOTE_COMMAND, Names.REMOTE_COMMAND,
            ModelDescriptionConstants.REMOTE_COMMAND),
    STATE_TRANSFER(Ids.CACHE_CONTAINER_THREAD_POOL_STATE_TRANSFER, Names.STATE_TRANSFER,
            ModelDescriptionConstants.STATE_TRANSFER),
    TRANSPORT(Ids.CACHE_CONTAINER_THREAD_POOL_TRANSPORT, Names.TRANSPORT, ModelDescriptionConstants.TRANSPORT),;

    final String baseId;
    final String type;
    final String resource;

    ThreadPool(String baseId, String type, String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String path() {
        return THREAD_POOL + "/" + resource;
    }

    AddressTemplate template() {
        return CACHE_CONTAINER_TEMPLATE.append(THREAD_POOL + "=" + resource);
    }
}
