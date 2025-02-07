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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTION_CONFIGURATION_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.resources.CSS.fontAwesome;

class ResourceAdapterPreview extends PreviewContent<StatisticsResource> {

    private EmptyState noStatistics;

    ResourceAdapterPreview(ResourceAdapterColumn column, StatisticsResource resourceAdapter, Resources resources) {
        super(resourceAdapter.getName());

        noStatistics = new EmptyState.Builder(Ids.build(Ids.RESOURCE_ADAPTER, STATISTICS, DISABLED),
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.RESOURCE_ADAPTER))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), () -> column.enableStatistics(resourceAdapter),
                        Constraint.writable(TRANSACTION_CONFIGURATION_TEMPLATE, STATISTICS_ENABLED))
                .build();
        Elements.setVisible(noStatistics.element(), false);

        resourceAdapter.get(StatisticsResource.EXT_STATS_AVAILABLE).set(resourceAdapter.hasExtendedStats());

        previewBuilder()
                .add(noStatistics)
                .addAll(new PreviewAttributes<>(resourceAdapter,
                        List.of(StatisticsResource.EXT_STATS_AVAILABLE)));
    }

    @Override
    public void update(StatisticsResource item) {
        super.update(item);

        boolean statsEnabled = item.get(STATISTICS_ENABLED).asBoolean();
        Elements.setVisible(noStatistics.element(), !statsEnabled);
    }
}
