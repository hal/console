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
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Represents the different cache types of a cache container.
 *
 * @author Harald Pehl
 */
enum Cache {

    DISTRIBUTED(Ids.DISTRIBUTED_CACHE, Names.DISTRIBUTED_CACHE, AddressTemplates.DISTRIBUTED_CACHE_TEMPLATE,
            pfIcon("cluster")),
    INVALIDATION(Ids.INVALIDATION_CACHE, Names.INVALIDATION_CACHE, AddressTemplates.INVALIDATION_CACHE_TEMPLATE,
            fontAwesome("ban")),
    LOCAL(Ids.LOCAL_CACHE, Names.LOCAL_CACHE, AddressTemplates.LOCAL_CACHE_TEMPLATE, CSS.pfIcon("home")),
    REPLICATED(Ids.REPLICATED_CACHE, Names.REPLICATED_CACHE, AddressTemplates.REPLICATED_CACHE_TEMPLATE,
            CSS.pfIcon("replicator"));

    final String baseId;
    final String type;
    final AddressTemplate template;
    final String icon;

    Cache(final String baseId, final String type, final AddressTemplate template, final String icons) {
        this.baseId = baseId;
        this.type = type;
        this.template = template;
        this.icon = icons;
    }

    String resource() {
        return template.lastKey();
    }
}
