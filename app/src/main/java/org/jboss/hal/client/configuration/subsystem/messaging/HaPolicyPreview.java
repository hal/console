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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class HaPolicyPreview extends PreviewContent<StaticItem> {

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final Map<HaPolicy, PreviewAttributes<ModelNode>> ATTRIBUTES =
            new ImmutableMap.Builder<HaPolicy, PreviewAttributes<ModelNode>>()
                    .put(HaPolicy.LIVE_ONLY, new PreviewAttributes<>(new ModelNode(),
                            asList("scale-down", "scale-down-cluster-name", "scale-down-connectors",
                                    "scale-down-discovery-group", "scale-down-group-name")))
                    .put(HaPolicy.REPLICATION_COLOCATED, new PreviewAttributes<>(new ModelNode(),
                            asList("backup-port-offset", "backup-request-retries", "backup-request-retry-interval",
                                    "max-backups")))
                    .put(HaPolicy.REPLICATION_MASTER, new PreviewAttributes<>(new ModelNode(),
                            asList("cluster-name", "group-name")))
                    .put(HaPolicy.REPLICATION_SLAVE, new PreviewAttributes<>(new ModelNode(),
                            asList("cluster-name", "group-name", "scale-down-cluster-name", "scale-down-connectors",
                                    "scale-down-discovery-group", "scale-down-group-name")))

                    .put(HaPolicy.SHARED_STORE_COLOCATED, new PreviewAttributes<>(new ModelNode(),
                            asList("backup-port-offset", "backup-request-retries", "backup-request-retry-interval",
                                    "max-backups")))
                    .put(HaPolicy.SHARED_STORE_MASTER, new PreviewAttributes<>(new ModelNode(),
                            singletonList("failover-on-server-shutdown")))
                    .put(HaPolicy.SHARED_STORE_SLAVE, new PreviewAttributes<>(new ModelNode(),
                            asList("scale-down-cluster-name", "scale-down-group-name", "scale-down-connectors",
                                    "scale-down-discovery-group")))
                    .build();


    HaPolicyPreview(final HaPolicy haPolicy, final ModelNode modelNode) {
        super(haPolicy.type);
        if (ATTRIBUTES.containsKey(haPolicy)) {
            PreviewAttributes<ModelNode> attributes = ATTRIBUTES.get(haPolicy);
            previewBuilder().addAll(attributes);
            attributes.refresh(modelNode);
        }
    }
}
