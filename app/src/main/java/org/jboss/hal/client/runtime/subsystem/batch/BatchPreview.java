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
package org.jboss.hal.client.runtime.subsystem.batch;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.BATCH_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACTIVE_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CURRENT_THREAD_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_JOB_REPOSITORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_THREAD_POOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LARGEST_THREAD_COUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX_THREADS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_JOBS_ON_RESUME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

public class BatchPreview extends PreviewContent<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final PreviewAttributes<ModelNode> attributes;
    private final Utilization activeCount;
    private final Utilization currentThreadCount;
    private final Utilization largestThreadCount;
    private final HTMLElement details;

    public BatchPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.BATCH);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.attributes = new PreviewAttributes<>(new ModelNode(), resources.constants().attributes(), asList(
                DEFAULT_JOB_REPOSITORY, DEFAULT_THREAD_POOL, RESTART_JOBS_ON_RESUME));

        details = section()
                .add(h(2, Names.THREADS))
                .add(activeCount = new Utilization(
                        resources.constants().active(), Names.THREADS, false, true))
                .add(currentThreadCount = new Utilization(
                        resources.constants().current(), Names.THREADS, false, true))
                .add(largestThreadCount = new Utilization(
                        resources.constants().largest(), Names.THREADS, false, true))
                .element();

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));
        previewBuilder()
                .addAll(attributes)
                .add(details);
        Elements.setVisible(details, false);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress address = BATCH_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION, true)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            attributes.refresh(result);
            if (result.hasDefined(DEFAULT_THREAD_POOL)) {
                String name = result.get(DEFAULT_THREAD_POOL).asString();
                ModelNode threadPool = failSafeGet(result, THREAD_POOL + "/" + name);
                if (threadPool.isDefined()) {
                    Elements.setVisible(details, true);
                    int max = threadPool.get(MAX_THREADS).asInt();
                    int active = threadPool.get(ACTIVE_COUNT).asInt();
                    int current = threadPool.get(CURRENT_THREAD_COUNT).asInt();
                    int largest = threadPool.get(LARGEST_THREAD_COUNT).asInt();
                    activeCount.update(active, max);
                    currentThreadCount.update(current, max);
                    largestThreadCount.update(largest, max);
                }
            }
        });
    }
}
