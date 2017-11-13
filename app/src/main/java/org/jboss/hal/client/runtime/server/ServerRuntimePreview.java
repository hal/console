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
package org.jboss.hal.client.runtime.server;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.chart.Utilization;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.br;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.lead;

public class ServerRuntimePreview extends PreviewContent<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final HTMLElement osName;
    private final HTMLElement osVersion;
    private final HTMLElement processors;
    private final HTMLElement jvm;
    private final HTMLElement jvmVersion;
    private final HTMLElement uptime;
    private final Utilization usedHeap;
    private final Utilization committedHeap;
    private final Utilization threads;

    public ServerRuntimePreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(resources.constants().status());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.usedHeap = new Utilization(resources.constants().used(), Names.MB, false, true);
        this.committedHeap = new Utilization(resources.constants().committed(), Names.MB, false, true);
        this.threads = new Utilization("Daemon", Names.THREADS, false, false); //NON-NLS

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));
        previewBuilder()
                .add(p().css(lead)
                        .add(osName = span().asElement())
                        .add(osVersion = span().asElement())
                        .add(processors = span().asElement())
                        .add(br())
                        .add(jvm = span().asElement())
                        .add(jvmVersion = span().asElement())
                        .add(br())
                        .add(uptime = span().asElement()))
                .add(h(2).textContent(Names.HEAP))
                .add(usedHeap)
                .add(committedHeap)
                .add(h(2).textContent(Names.THREADS))
                .add(threads);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final SubsystemMetadata item) {
        AddressTemplate mbean = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER, "core-service=platform-mbean");
        AddressTemplate osTmpl = mbean.append("type=operating-system");
        AddressTemplate runtimeTmpl = mbean.append("type=runtime");
        AddressTemplate memoryTmpl = mbean.append("type=memory");
        AddressTemplate threadingTmpl = mbean.append("type=threading");

        Operation osOp = new Operation.Builder(osTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation runtimeOp = new Operation.Builder(runtimeTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation memoryOp = new Operation.Builder(memoryTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation threadingOp = new Operation.Builder(threadingTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(new Composite(osOp, runtimeOp, memoryOp, threadingOp), (CompositeResult result) -> {
            // os
            ModelNode osNode = result.step(0).get(RESULT);
            osName.textContent = osNode.get(NAME).asString();
            osVersion.textContent = " " + osNode.get("version").asString();
            processors.textContent = ", " + osNode.get("available-processors").asInt() + " " + resources.constants()
                    .processors();

            // runtime
            ModelNode runtimeNode = result.step(1).get(RESULT);
            jvm.textContent = runtimeNode.get("vm-name").asString();
            jvmVersion.textContent = " " + runtimeNode.get("spec-version").asString();
            uptime.textContent = resources.messages().uptime(
                    Format.humanReadableDuration(runtimeNode.get("uptime").asLong()));

            // memory
            ModelNode heapMemoryNode = result.step(2).get(RESULT).get("heap-memory-usage");
            long used = heapMemoryNode.get("used").asLong() / 1024 / 1024;
            long committed = heapMemoryNode.get("committed").asLong() / 1024 / 1024;
            long max = heapMemoryNode.get("max").asLong() / 1024 / 1024;
            usedHeap.update(used, max);
            committedHeap.update(committed, max);

            // threads
            ModelNode threadsNode = result.step(3).get(RESULT);
            long threadCount = threadsNode.get("thread-count").asLong();
            long daemonCount = threadsNode.get("daemon-thread-count").asLong();
            threads.update(daemonCount, threadCount);
        });
    }
}
