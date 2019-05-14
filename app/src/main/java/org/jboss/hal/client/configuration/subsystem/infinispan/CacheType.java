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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.*;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Component.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/** Represents the different cache types of a cache container. */
enum CacheType {

    DISTRIBUTED(Ids.DISTRIBUTED_CACHE, Names.DISTRIBUTED_CACHE, NameTokens.DISTRIBUTED_CACHE,
            DISTRIBUTED_CACHE_TEMPLATE, pfIcon("cluster"), true,
            new Component[]{EXPIRATION, LOCKING, PARTITION_HANDLING, STATE_TRANSFER, TRANSACTION}),

    INVALIDATION(Ids.INVALIDATION_CACHE, Names.INVALIDATION_CACHE, NameTokens.INVALIDATION_CACHE,
            INVALIDATION_CACHE_TEMPLATE, fontAwesome("ban"), false,
            new Component[]{EXPIRATION, LOCKING, TRANSACTION}),

    LOCAL(Ids.LOCAL_CACHE, Names.LOCAL_CACHE, NameTokens.LOCAL_CACHE,
            LOCAL_CACHE_TEMPLATE, pfIcon("home"), false,
            new Component[]{EXPIRATION, LOCKING, TRANSACTION}),

    REPLICATED(Ids.REPLICATED_CACHE, Names.REPLICATED_CACHE, NameTokens.REPLICATED_CACHE,
            REPLICATED_CACHE_TEMPLATE, pfIcon("replicator"), true,
            new Component[]{EXPIRATION, LOCKING, PARTITION_HANDLING, STATE_TRANSFER, TRANSACTION}),

    SCATTERED(Ids.SCATTERED_CACHE, Names.SCATTERED_CACHE, NameTokens.SCATTERED_CACHE,
            SCATTERED_CACHE_TEMPLATE, pfIcon("registry"), true,
            new Component[]{EXPIRATION, LOCKING, PARTITION_HANDLING, STATE_TRANSFER, TRANSACTION});

    final String baseId;
    final String type;
    final String nameToken;
    final AddressTemplate template;
    final String icon;
    final boolean backups; // whether this cache type supports backups
    final Component[] components;

    CacheType(String baseId, String type, String nameToken, AddressTemplate template, String icons, boolean backups,
            Component[] components) {
        this.baseId = baseId;
        this.type = type;
        this.nameToken = nameToken;
        this.template = template;
        this.icon = icons;
        this.backups = backups;
        this.components = components;
    }

    String resource() {
        return template.lastName();
    }
}
