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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Strings;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

class CachePreview extends PreviewContent<Cache> {

    CachePreview(Cache cache) {
        super(cache.getName(), cache.type().type);

        List<String> attributes = new ArrayList<>(asList(INDEXING, JNDI_NAME, START, STATISTICS_ENABLED));
        switch (cache.type()) {
            case LOCAL:
                break;
            case DISTRIBUTED:
            case INVALIDATION:
            case REPLICATED:
                attributes.add(MODE);
                break;
            case SCATTERED:
                attributes.addAll(asList(MODE, SEGMENTS));
                break;
            default:
                break;
        }
        sort(attributes);
        PreviewAttributes<Cache> basicAttributes = new PreviewAttributes<>(cache, attributes);
        previewBuilder().addAll(basicAttributes);

        PreviewAttributes<Cache> memoryAndStoreAttributes = new PreviewAttributes<>(cache,
                Names.MEMORY + " / " + Names.STORE);
        memoryAndStoreAttributes.append(c -> {
            String resource = definedSingleton(c, MEMORY);
            Memory memory = Memory.fromResource(resource);
            if (memory != null) {
                return new PreviewAttribute(Names.MEMORY, memory.type);
            }
            return new PreviewAttribute(Names.MEMORY, Strings.capitalize(resource));
        });
        memoryAndStoreAttributes.append(c -> {
            String resource = definedSingleton(c, STORE);
            Store store = Store.fromResource(resource);
            if (store != null) {
                return new PreviewAttribute(Names.STORE, store.type);
            }
            return new PreviewAttribute(Names.STORE, Strings.capitalize(resource));
        });
        previewBuilder().addAll(memoryAndStoreAttributes);
    }

    private String definedSingleton(Cache cache, String name) {
        ModelNode modelNode = failSafeGet(cache, name);
        if (modelNode.isDefined()) {
            List<Property> properties = modelNode.asPropertyList();
            for (Property property : properties) {
                if (property.getValue().isDefined()) {
                    return property.getName();
                }
            }
        }
        return "";
    }
}
