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

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;

import static java.util.Arrays.asList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class CacheContainerPreview extends PreviewContent<CacheContainer> {

    CacheContainerPreview(CacheContainer cc) {
        super(cc.getName(), cc.isRemote() ? Names.REMOTE_CACHE_CONTAINER : Names.CACHE_CONTAINER);

        if (cc.isRemote()) {
            PreviewAttributes<CacheContainer> previewAttributes = new PreviewAttributes<>(cc,
                    asList(CONNECTION_TIMEOUT, DEFAULT_REMOTE_CLUSTER, KEY_SIZE_ESTIMATE, MAX_RETRIES,
                            SOCKET_TIMEOUT, TCP_KEEP_ALIVE, TCP_NO_DELAY));
            previewBuilder().addAll(previewAttributes);
        } else {
            PreviewAttributes<CacheContainer> previewAttributes = new PreviewAttributes<>(cc,
                    asList(DEFAULT_CACHE, STATISTICS_ENABLED));
            previewBuilder().addAll(previewAttributes);
        }
    }
}
