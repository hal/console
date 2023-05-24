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
package org.jboss.hal.client.runtime.subsystem.webservice;

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

import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class WebservicesPreview extends PreviewContent<SubsystemMetadata> {

    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private HTMLElement attributesElement;
    private PreviewAttributes<ModelNode> attributes;

    public WebservicesPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.WEBSERVICES);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        attributes = new PreviewAttributes<>(new ModelNode(), resources.constants().attributes(),
                asList("modify-wsdl-address", "wsdl-host", "wsdl-path-rewrite-rule", "wsdl-port", "wsdl-secure-port",
                        "wsdl-uri-scheme"));
        attributesElement = section()
                .addAll(attributes).element();

        previewBuilder()
                .add(attributesElement);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress runtimeAddress = WEBSERVICES_RUNTIME_TEMPLATE.resolve(statementContext);
        Operation opSubsystem = new Operation.Builder(runtimeAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RESOLVE_EXPRESSIONS, true)
                .build();
        dispatcher.execute(opSubsystem, result -> {
            attributes.refresh(result);
        });
    }
}
