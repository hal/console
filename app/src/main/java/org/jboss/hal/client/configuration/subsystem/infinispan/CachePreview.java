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
import org.jboss.hal.core.finder.PreviewContent;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

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

/*
        PreviewAttributes<Cache> memoryAndStoreAttributes = new PreviewAttributes<>(cache,
                Names.MEMORY + " / " + Names.STORE);
        memoryAndStoreAttributes.append(c -> {
            ModelNode modelNode = failSafeGet(c, MEMORY);
            if (modelNode.isDefined()) {
                Property property = modelNode.asProperty();
                return new PreviewAttribute(Names.MEMORY, property.getName());
            } else {
                return new PreviewAttribute(Names.MEMORY, "");
            }
        });
        memoryAndStoreAttributes.append(c -> {
            ModelNode modelNode = failSafeGet(c, STORE);
            if (modelNode.isDefined()) {
                Property property = modelNode.asProperty();
                return new PreviewAttribute(Names.STORE, property.getName());
            } else {
                return new PreviewAttribute(Names.STORE, "");
            }
        });
        previewBuilder().addAll(memoryAndStoreAttributes);
*/
    }
}
