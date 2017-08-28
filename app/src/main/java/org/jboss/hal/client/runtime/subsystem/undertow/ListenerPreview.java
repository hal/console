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

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SERVER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_LISTENER_TYPE;
import static org.jboss.hal.client.runtime.subsystem.undertow.ListenerColumn.HAL_WEB_SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

class ListenerPreview extends PreviewContent<NamedNode> {

    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private PreviewAttributes<NamedNode> previewAttributes;

    ListenerPreview(final Dispatcher dispatcher, final StatementContext statementContext,
            NamedNode server) {
        super(server.getName());
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        previewAttributes = new PreviewAttributes<>(server, asList("bytes-received", "bytes-sent", "error-count",
                "processing-time", "max-processing-time", "request-count"));
        getHeaderContainer().appendChild(refreshLink(() -> update(server)));
        previewBuilder().addAll(previewAttributes);
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
            NamedNode n = new NamedNode(result);
            previewAttributes.refresh(n);
        });

    }
}
