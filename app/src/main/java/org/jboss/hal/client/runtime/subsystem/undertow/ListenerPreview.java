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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jboss.gwt.elemento.core.Elements;
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
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_CONFIGURATION_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_LISTENER_TYPE;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_WEB_SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.fontAwesome;

class ListenerPreview extends PreviewContent<NamedNode> {

    private GroupedBar processingTime;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private Resources resources;
    private PreviewAttributes<NamedNode> previewAttributes;
    private Donut requests;
    private EmptyState noProcessingTime;
    private String profile;

    ListenerPreview(final Dispatcher dispatcher, final StatementContext statementContext, final Resources resources,
            NamedNode server) {
        super(server.getName());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        previewAttributes = new PreviewAttributes<>(server, asList("bytes-received", "bytes-sent"));
        getHeaderContainer().appendChild(refreshLink(() -> update(server)));

        // the order of rows is determined at update time.
        processingTime = new GroupedBar.Builder(resources.constants().milliseconds())
                .add(MAX_PROCESSING_TIME, resources.constants().maxProcessingTime(), PatternFly.colors.orange)
                .add(PROCESSING_TIME, resources.constants().totalProcessingTime(), PatternFly.colors.green)
                .responsive(true)
                .horizontal()
                .build();
        registerAttachable(processingTime);

        ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        dispatcher.execute(operation, result -> {

            profile = result.get(PROFILE_NAME).asString();
            noProcessingTime = new EmptyState.Builder(resources.constants().undertowListenerProcessingDisabledHeader())
                    .icon(fontAwesome("line-chart"))
                    .primaryAction(resources.constants().enable(), () -> recordProcessingTime(server))
                    .build();

            requests = new Donut.Builder(Names.REQUESTS)
                    .add(REQUEST_COUNT, resources.constants().requests(), PatternFly.colors.green)
                    .add(ERROR_COUNT, resources.constants().error(), PatternFly.colors.red)
                    .legend(Donut.Legend.BOTTOM)
                    .responsive(true)
                    .build();
            registerAttachable(requests);

            previewBuilder().addAll(previewAttributes);
            previewBuilder()
                    .add(h(2, resources.constants().processingTime()))
                    .add(processingTime)
                    .add(noProcessingTime)
                    .add(h(2, resources.constants().requests()))
                    .add(requests);

            Elements.setVisible(noProcessingTime.asElement(), false);
        });
    }

    @Override
    public void update(final NamedNode item) {
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

            if (listenerResult.get(RECORD_REQUEST_START_TIME).asBoolean()) {
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

                Elements.setVisible(noProcessingTime.asElement(), false);
                Elements.setVisible(processingTime.asElement(), true);
            } else {
                Elements.setVisible(noProcessingTime.asElement(), true);
                Elements.setVisible(processingTime.asElement(), false);
            }

            SafeHtml desc = SafeHtmlUtils
                    .fromTrustedString(
                            resources.messages().undertowListenerProcessingDisabled(listenerType, webserver, profile));
            noProcessingTime.setDescription(desc);

            Map<String, Long> metricUpdates = new HashMap<>(7);
            metricUpdates.put(REQUEST_COUNT, result.get(REQUEST_COUNT).asLong());
            metricUpdates.put(ERROR_COUNT, result.get(ERROR_COUNT).asLong());
            requests.update(metricUpdates);
        });
    }

    private void recordProcessingTime(final NamedNode listener) {
        // the HAL_LISTENER_TYPE and HAL_WEB_SERVER is added to the model in ListenerColumn class.
        String listenerType = listener.asModelNode().get(HAL_LISTENER_TYPE).asString();
        String webserver = listener.asModelNode().get(HAL_WEB_SERVER).asString();

        ResourceAddress address = WEB_SERVER_CONFIGURATION_TEMPLATE.append(
                "/" + listenerType + "=" + listener.getName())
                .resolve(statementContext, profile, webserver);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, RECORD_REQUEST_START_TIME)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            Elements.setVisible(noProcessingTime.asElement(), false);
            Elements.setVisible(processingTime.asElement(), true);
        });
    }
}
