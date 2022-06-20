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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.Skeleton;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.ImmutableMap;

import elemental2.dom.CSSProperties.MarginTopUnionType;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class ThreadPoolPreview extends PreviewContent<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final EmptyState noStatistics;
    private final HTMLElement statSection;
    private final Donut tasks;
    private final Utilization threads;

    public ThreadPoolPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.EJB3);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        noStatistics = new EmptyState.Builder(Ids.EJB3_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.UNDERTOW))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                        Constraint.writable(WEB_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                .build();

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));

        tasks = new Donut.Builder(Names.TASKS)
                .add(Ids.TASKS_ACTIVE, resources.constants().active(), PatternFly.colors.blue)
                .add(Ids.TASKS_COMPLETED, resources.constants().completed(), PatternFly.colors.green)
                .add(Ids.TASKS_QUEUE, resources.constants().queue(), PatternFly.colors.orange)
                .add(Ids.TASKS_REJECTED, resources.constants().rejected(), PatternFly.colors.red)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(tasks);
        threads = new Utilization(new LabelBuilder().label(CURRENT_THREAD_COUNT), Names.THREADS, false, false);
        threads.element().style.marginTop = MarginTopUnionType.of(Skeleton.MARGIN_BIG + "px"); // NON-NLS
        previewBuilder()
                .add(noStatistics)
                .add(statSection = section()
                        .add(h(2, Names.THREAD_POOL))
                        .add(tasks)
                        .add(threads)
                        .element());

        Elements.setVisible(noStatistics, false);
        Elements.setVisible(statSection, false);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress address = EJB3_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();
        dispatcher.execute(operation, result -> {
            List<Property> properties = failSafePropertyList(result, THREAD_POOL);
            if (!properties.isEmpty()) {
                ModelNode threadPool = properties.get(0).getValue();

                boolean statsAvailable = threadPool.get("task-count").asLong() > 0;
                boolean statsEnabled = result.get(STATISTICS_ENABLED).asBoolean(statsAvailable);

                if (statsEnabled) {
                    long active = threadPool.get(ACTIVE_COUNT).asLong();
                    long completed = threadPool.get(COMPLETED_TASK_COUNT).asLong();
                    long queue = threadPool.get(QUEUE_SIZE).asLong();
                    long rejected = threadPool.get(REJECTED_COUNT).asLong();
                    tasks.update(ImmutableMap.of(
                            Ids.TASKS_ACTIVE, active,
                            Ids.TASKS_COMPLETED, completed,
                            Ids.TASKS_QUEUE, queue,
                            Ids.TASKS_REJECTED, rejected));

                    int currentThreads = threadPool.get(CURRENT_THREAD_COUNT).asInt();
                    int maxThreads = threadPool.get(MAX_THREADS).asInt();
                    threads.update(currentThreads, maxThreads);
                }
                Elements.setVisible(noStatistics, !statsEnabled);
                Elements.setVisible(statSection, statsEnabled);
            }
        });
    }

    private void enableStatistics() {
        ResourceAddress address = EJB3_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(null));
    }
}
