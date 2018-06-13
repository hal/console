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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.DISTRIBUTED_CACHE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.INVALIDATION_CACHE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.LOCAL_CACHE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REPLICATED_CACHE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Component.*;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Memory.BINARY;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Memory.OBJECT;
import static org.jboss.hal.client.configuration.subsystem.infinispan.Memory.OFF_HEAP;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/** Represents the different cache types of a cache container. */
enum Cache {

    DISTRIBUTED(Ids.DISTRIBUTED_CACHE, Names.DISTRIBUTED_CACHE, DISTRIBUTED_CACHE_TEMPLATE, pfIcon("cluster"),
            true, new Component[]{EXPIRATION, LOCKING, PARTITION_HANDLING, STATE_TRANSFER, TRANSACTION},
            new Memory[]{BINARY, OBJECT, OFF_HEAP}),

    INVALIDATION(Ids.INVALIDATION_CACHE, Names.INVALIDATION_CACHE, INVALIDATION_CACHE_TEMPLATE, fontAwesome("ban"),
            false, new Component[]{EXPIRATION, LOCKING, TRANSACTION}, new Memory[]{BINARY, OBJECT, OFF_HEAP}),

    LOCAL(Ids.LOCAL_CACHE, Names.LOCAL_CACHE, LOCAL_CACHE_TEMPLATE, pfIcon("home"),
            false, new Component[]{EXPIRATION, LOCKING, TRANSACTION}, new Memory[]{BINARY, OBJECT, OFF_HEAP}),

    REPLICATED(Ids.REPLICATED_CACHE, Names.REPLICATED_CACHE, REPLICATED_CACHE_TEMPLATE, pfIcon("replicator"),
            true, new Component[]{EXPIRATION, LOCKING, PARTITION_HANDLING, STATE_TRANSFER, TRANSACTION},
            new Memory[]{BINARY, OBJECT, OFF_HEAP});

    final String baseId;
    final String type;
    final AddressTemplate template;
    final String icon;
    final boolean backups; // whether there's a nested page for the backups
    final Component[] components;
    final Memory[] memories;

    Cache(String baseId, String type, AddressTemplate template, String icons, boolean backups,
            Component[] components, Memory[] memories) {
        this.baseId = baseId;
        this.type = type;
        this.template = template;
        this.icon = icons;
        this.backups = backups;
        this.components = components;
        this.memories = memories;
    }

    String resource() {
        return template.lastName();
    }
}
