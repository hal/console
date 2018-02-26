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
package org.jboss.hal.client.runtime.subsystem.webservice;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_CONFIGURATION_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class WebservicesPreview extends PreviewContent<SubsystemMetadata> {

    private EmptyState noStatistics;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private HTMLElement attributesElement;
    private PreviewAttributes<ModelNode> attributes;

    public WebservicesPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.WEBSERVICES);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        noStatistics = new EmptyState.Builder(Ids.WEBSERVICES_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                .description(resources.messages().statisticsDisabled(Names.WEBSERVICES))
                .icon(fontAwesome("line-chart"))
                .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                        Constraint.writable(WEBSERVICES_CONFIGURATION_TEMPLATE, STATISTICS_ENABLED))
                .build();
        Elements.setVisible(noStatistics.asElement(), false);

        attributes = new PreviewAttributes<>(new ModelNode(), resources.constants().attributes(),
                asList("modify-wsdl-address", "wsdl-host", "wsdl-path-rewrite-rule", "wsdl-port", "wsdl-secure-port",
                        "wsdl-uri-scheme"));
        attributesElement = section()
                .addAll(attributes)
                .asElement();

        previewBuilder()
                .add(noStatistics)
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
            boolean statsEnabled = result.get(STATISTICS_ENABLED).asBoolean();
            attributes.refresh(result);
            Elements.setVisible(noStatistics.asElement(), !statsEnabled);
            Elements.setVisible(attributesElement, statsEnabled);
        });
    }

    private void enableStatistics() {
        ResourceAddress address = AddressTemplate.of("{selected.profile}/subsystem=webservices")
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(null));
    }
}
