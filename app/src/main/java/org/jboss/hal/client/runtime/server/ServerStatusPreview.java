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

import elemental.dom.Element;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.metric.Utilization;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class ServerStatusPreview extends PreviewContent<StaticItem> {

    private static final String OS_NAME = "os";
    private static final String OS_VERSION = "os-version";
    private static final String PROCESSORS = "processors";
    private static final String JVM = "jvm";
    private static final String JVM_VERSION = "jvm-version";
    private static final String UPTIME = "uptime";

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Element osName;
    private final Element osVersion;
    private final Element processors;
    private final Element jvm;
    private final Element jvmVersion;
    private final Element uptime;
    private final Utilization usedHeap;
    private final Utilization committedHeap;
    private final Utilization threads;

    ServerStatusPreview(final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super(resources.constants().status());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.usedHeap = new Utilization(resources.constants().used(), Names.MB, true, true);
        this.committedHeap = new Utilization(resources.constants().commited(), Names.MB, true, true);
        this.threads = new Utilization("Daemon", Names.THREADS, true, false); //NON-NLS

        // @formatter:off
        previewBuilder()
            .p().css(lead)
                .span().rememberAs(OS_NAME).end()
                .span().rememberAs(OS_VERSION).end()
                .span().rememberAs(PROCESSORS).end()
                .add("br")
                .span().rememberAs(JVM).end()
                .span().rememberAs(JVM_VERSION).end()
                .add("br")
                .span().rememberAs(UPTIME).end()
            .end()
            .p()
                .a().css(clickable, pullRight).on(click, event -> update(null))
                    .span().css(fontAwesome("refresh"), marginRight4).end()
                    .span().textContent(resources.constants().refresh()).end()
                .end()
            .end()
            .h(2).css(underline).textContent(Names.HEAP).end()
            .add(usedHeap)
            .add(committedHeap)
            .h(2).css(underline).textContent(Names.THREADS).end()
            .add(threads);
        // @formatter:on

        this.osName = previewBuilder().referenceFor(OS_NAME);
        this.osVersion = previewBuilder().referenceFor(OS_VERSION);
        this.processors = previewBuilder().referenceFor(PROCESSORS);
        this.jvm = previewBuilder().referenceFor(JVM);
        this.jvmVersion = previewBuilder().referenceFor(JVM_VERSION);
        this.uptime = previewBuilder().referenceFor(UPTIME);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final StaticItem item) {
        AddressTemplate mbean = AddressTemplate.of("/{selected.host}/{selected.server}/core-service=platform-mbean");
        AddressTemplate osTmpl = mbean.append("type=operating-system");
        AddressTemplate runtimeTmpl = mbean.append("type=runtime");
        AddressTemplate memoryTmpl = mbean.append("type=memory");
        AddressTemplate threadingTmpl = mbean.append("type=threading");

        Operation osOp = new Operation.Builder(READ_RESOURCE_OPERATION, osTmpl.resolve(statementContext))
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation runtimeOp = new Operation.Builder(READ_RESOURCE_OPERATION, runtimeTmpl.resolve(statementContext))
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation memoryOp = new Operation.Builder(READ_RESOURCE_OPERATION, memoryTmpl.resolve(statementContext))
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation threadingOp = new Operation.Builder(READ_RESOURCE_OPERATION, threadingTmpl.resolve(statementContext))
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(new Composite(osOp, runtimeOp, memoryOp, threadingOp), (CompositeResult result) -> {
            // os
            ModelNode osNode = result.step(0).get(RESULT);
            osName.setTextContent(osNode.get(NAME).asString());
            osVersion.setTextContent(" " + osNode.get("version").asString());
            processors.setTextContent(
                    ", " + osNode.get("available-processors").asInt() + " " + resources.constants().processors());

            // runtime
            ModelNode runtimeNode = result.step(1).get(RESULT);
            jvm.setTextContent(runtimeNode.get("vm-name").asString());
            jvmVersion.setTextContent(" " + runtimeNode.get("spec-version").asString());
            uptime.setTextContent(resources.messages().uptime(
                    Format.humanReadableDuration(runtimeNode.get("uptime").asLong())));

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
