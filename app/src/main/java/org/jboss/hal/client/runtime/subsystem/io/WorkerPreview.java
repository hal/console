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
package org.jboss.hal.client.runtime.subsystem.io;

import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.client.runtime.subsystem.io.AddressTemplates.WORKER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

class WorkerPreview extends PreviewContent<NamedNode> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final LabelBuilder labelBuilder;
    private final Utilization corePoolSize;
    private final Utilization maxPoolSize;
    private final Utilization ioThreadCount;
    private final Utilization busyWorkerThreadCount;
    private final HTMLElement connectionsContainer;

    WorkerPreview(NamedNode worker, Dispatcher dispatcher, StatementContext statementContext) {
        super(worker.getName());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.labelBuilder = new LabelBuilder();
        this.corePoolSize = new Utilization(labelBuilder.label(CORE_POOL_SIZE), Names.THREAD_POOLS, false, false);
        this.maxPoolSize = new Utilization(labelBuilder.label(MAX_POOL_SIZE), Names.THREAD_POOLS, false, false);
        this.busyWorkerThreadCount = new Utilization(labelBuilder.label(BUSY_WORKER_THREAD_COUNT), Names.THREADS, false, false);
        this.ioThreadCount = new Utilization(labelBuilder.label(IO_THREAD_COUNT), Names.THREADS, false, false);

        getHeaderContainer().appendChild(refreshLink(() -> update(worker)));
        previewBuilder()
                .add(h(2).textContent(Names.THREADS))
                .addAll(corePoolSize, maxPoolSize, busyWorkerThreadCount, ioThreadCount)
                .add(connectionsContainer = div().get());
    }

    @Override
    public void update(NamedNode worker) {
        ResourceAddress address = WORKER_TEMPLATE.resolve(statementContext, worker.getName());
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            corePoolSize.update(result.get(CORE_POOL_SIZE).asLong(), result.get(TASK_MAX_THREADS).asLong());
            maxPoolSize.update(result.get(MAX_POOL_SIZE).asLong(), result.get(TASK_MAX_THREADS).asLong());
            busyWorkerThreadCount.update(result.get(BUSY_WORKER_THREAD_COUNT).asLong(), result.get(TASK_MAX_THREADS).asLong());
            ioThreadCount.update(result.get(IO_THREAD_COUNT).asLong(), result.get(IO_THREADS).asLong());

            List<Property> serverConnections = failSafePropertyList(result, SERVER);
            Elements.removeChildrenFrom(connectionsContainer);
            if (!serverConnections.isEmpty()) {
                NamedNode dummy = new NamedNode(new ModelNode());
                PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(dummy, Names.CONNECTIONS);
                for (Property property : serverConnections) {
                    ModelNode serverConnection = property.getValue();
                    attributes.append(unused -> {
                        int connectionCount = serverConnection.get(CONNECTION_COUNT).asInt();
                        int lowWaterMark = serverConnection.get(CONNECTION_LIMIT_LOW_WATER_MARK).asInt();
                        int highWaterMark = serverConnection.get(CONNECTION_LIMIT_HIGH_WATER_MARK).asInt();
                        HTMLElement element = span()
                                .add(span()
                                        .title(labelBuilder.label(CONNECTION_COUNT))
                                        .textContent(String.valueOf(connectionCount)))
                                .add(span().style("color: " + PatternFly.colors.black500) //NON-NLS
                                        .add(" (")
                                        .add(span()
                                                .title(labelBuilder.label(CONNECTION_LIMIT_LOW_WATER_MARK))
                                                .textContent(String.valueOf(lowWaterMark)))
                                        .add(" / ")
                                        .add(span()
                                                .title(labelBuilder.label(CONNECTION_LIMIT_HIGH_WATER_MARK))
                                                .textContent(String.valueOf(highWaterMark)))
                                        .add(")"))
                                .get();
                        return new PreviewAttribute(property.getName(), element);
                    });
                }
                attributes.forEach(connectionsContainer::appendChild);
            }
        });
    }
}
