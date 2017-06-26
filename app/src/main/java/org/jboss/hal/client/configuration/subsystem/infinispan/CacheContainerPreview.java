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

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JNDI_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;

/**
 * @author Harald Pehl
 */
class CacheContainerPreview extends PreviewContent<CacheContainer> {

    CacheContainerPreview(final CacheContainer cc) {
        super(cc.getName());

        PreviewAttributes<CacheContainer> previewAttributes = new PreviewAttributes<>(cc,
                asList(DEFAULT_CACHE, JNDI_NAME, STATISTICS_ENABLED));
        previewBuilder().addAll(previewAttributes);
    }
}
