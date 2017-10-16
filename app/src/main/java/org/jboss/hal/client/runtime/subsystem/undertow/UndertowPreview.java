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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class UndertowPreview extends PreviewContent<SubsystemMetadata> {

    private EmptyState noStatistics;
    private HTMLElement descriptionPreview;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private String profile;


    public UndertowPreview(final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super(Names.WEB, Names.UNDERTOW);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        dispatcher.execute(operation, result -> {

            profile = result.get(PROFILE_NAME).asString();
            noStatistics = new EmptyState.Builder(resources.constants().statisticsDisabledHeader())
                    .description(resources.messages().statisticsDisabled(Names.UNDERTOW, profile))
                    .icon(fontAwesome("line-chart"))
                    .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                            Constraint.writable(WEB_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                    .build();

            previewBuilder()
                    .add(noStatistics);

            // to prevent flickering we initially hide everything
            Elements.setVisible(noStatistics.asElement(), false);
            update(null);

        });

        descriptionPreview = section().asElement();
        Previews.innerHtml(descriptionPreview, resources.previews().runtimeWeb());

        previewBuilder()
                .add(descriptionPreview);
        Elements.setVisible(descriptionPreview, false);
    }

    @Override
    public void update(final SubsystemMetadata item) {
        ResourceAddress addressWeb = WEB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation opWeb = new Operation.Builder(addressWeb, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(opWeb, result -> {
            boolean statsEnabled = result.get(STATISTICS_ENABLED).asBoolean();
            Elements.setVisible(noStatistics.asElement(), !statsEnabled);
            Elements.setVisible(descriptionPreview, statsEnabled);
        });
    }

    private void enableStatistics() {
        ResourceAddress address = new ResourceAddress()
                .add(PROFILE, profile)
                .add(SUBSYSTEM, UNDERTOW);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            Elements.setVisible(noStatistics.asElement(), false);
            Elements.setVisible(descriptionPreview, true);
        });
    }
}
