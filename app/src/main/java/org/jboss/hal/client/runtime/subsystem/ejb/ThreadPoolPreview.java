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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import elemental2.dom.CSSProperties.MarginTopUnionType;
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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.EJB3_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class ThreadPoolPreview extends PreviewContent<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Donut tasks;
    private final Utilization threads;

    public ThreadPoolPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.EJB3);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

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
        threads.asElement().style.marginTop = MarginTopUnionType.of(Skeleton.MARGIN_BIG + "px"); //NON-NLS
        previewBuilder()
                .add(h(2, Names.THREAD_POOL))
                .add(tasks)
                .add(threads);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress address = EJB3_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            List<Property> properties = failSafePropertyList(result, THREAD_POOL);
            if (!properties.isEmpty()) {
                ModelNode threadPool = properties.get(0).getValue();

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
        });
    }
}
