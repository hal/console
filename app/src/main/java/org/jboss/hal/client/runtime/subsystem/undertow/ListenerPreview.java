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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.ballroom.chart.GroupedBar;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_LISTENER_TYPE;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_WEB_SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

class ListenerPreview extends PreviewContent<NamedNode> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;

    private final EmptyState noStatistics;
    private final PreviewAttributes<NamedNode> previewAttributes;
    private final HTMLElement processingElement;
    private final GroupedBar processingTime;
    private final HTMLElement requestsElement;
    private final Donut requests;

    ListenerPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources, NamedNode server) {
        super(server.getName());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        getHeaderContainer().appendChild(refreshLink(() -> update(server)));
        previewAttributes = new PreviewAttributes<>(server, asList("bytes-received", "bytes-sent"));

        noStatistics = new EmptyState.Builder(Ids.UNDERTOW_LISTENER_PROCESSING_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enable(), () -> recordProcessingTime(server))
                .build();

        // the order of rows is determined at update time.
        processingTime = new GroupedBar.Builder(resources.constants().milliseconds())
                .add(MAX_PROCESSING_TIME, resources.constants().maxProcessingTime(), PatternFly.colors.orange)
                .add(PROCESSING_TIME, resources.constants().totalProcessingTime(), PatternFly.colors.green)
                .responsive(true)
                .horizontal()
                .build();
        registerAttachable(processingTime);
        processingElement = section()
                .add(h(2, resources.constants().processingTime()))
                .add(processingTime).element();

        requests = new Donut.Builder(Names.REQUESTS)
                .add(REQUEST_COUNT, Names.REQUESTS, PatternFly.colors.green)
                .add(ERROR_COUNT, resources.constants().error(), PatternFly.colors.red)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(requests);
        requestsElement = section()
                .add(h(2, Names.REQUESTS))
                .add(requests).element();

        previewBuilder().addAll(previewAttributes);
        previewBuilder()
                .add(noStatistics)
                .add(processingElement)
                .add(requestsElement);

        setVisible(noStatistics.element(), false);
        setVisible(processingElement, false);
        setVisible(requestsElement, false);
    }

    @Override
    public void update(NamedNode item) {
        // the HAL_LISTENER_TYPE and HAL_WEB_SERVER is added to the model in ListenerColumn class.
        String listenerType = item.asModelNode().get(HAL_LISTENER_TYPE).asString();
        String webserver = item.asModelNode().get(HAL_WEB_SERVER).asString();
        ResourceAddress address = AddressTemplate.of(WEB_SERVER_ADDRESS + "/" + listenerType + "=" + item.getName())
                .resolve(statementContext, webserver);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            NamedNode listenerResult = new NamedNode(result);
            previewAttributes.refresh(listenerResult);
            boolean statisticsEnabled = listenerResult.get(RECORD_REQUEST_START_TIME).asBoolean(false);

            if (statisticsEnabled) {
                Map<String, Long> processingTimes = new HashMap<>();
                long procTime = result.get(PROCESSING_TIME).asLong();
                long maxProcTime = result.get(MAX_PROCESSING_TIME).asLong();
                // convert nanoseconds to milliseconds
                if (procTime > 0) {
                    procTime = procTime / 1000000;
                }
                if (maxProcTime > 0) {
                    maxProcTime = maxProcTime / 1000000;
                }

                // the order of rows is determined at update time.
                processingTimes.put(MAX_PROCESSING_TIME, maxProcTime);
                processingTimes.put(PROCESSING_TIME, procTime);
                processingTime.update(processingTimes);

                Map<String, Long> metricUpdates = new HashMap<>(7);
                metricUpdates.put(REQUEST_COUNT, result.get(REQUEST_COUNT).asLong());
                metricUpdates.put(ERROR_COUNT, result.get(ERROR_COUNT).asLong());
                requests.update(metricUpdates);
            } else {
                SafeHtml desc = SafeHtmlUtils.fromTrustedString(
                        resources.messages().undertowListenerProcessingDisabled(listenerType, webserver));
                noStatistics.setDescription(desc);
            }

            setVisible(noStatistics.element(), !statisticsEnabled);
            setVisible(processingElement, statisticsEnabled);
            setVisible(requestsElement, statisticsEnabled);
        });
    }

    private void recordProcessingTime(NamedNode listener) {
        // the HAL_LISTENER_TYPE and HAL_WEB_SERVER is added to the model in ListenerColumn class.
        String webserver = listener.asModelNode().get(HAL_WEB_SERVER).asString();
        String listenerType = listener.asModelNode().get(HAL_LISTENER_TYPE).asString();
        ResourceAddress address = AddressTemplate.of(
                "{selected.profile}/subsystem=undertow/server=" + webserver + "/" + listenerType + "=" + listener.getName())
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, RECORD_REQUEST_START_TIME)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(listener));
    }
}
