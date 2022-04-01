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
package org.jboss.hal.client.runtime.subsystem.undertow;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class UndertowPreview extends PreviewContent<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final EmptyState noStatistics;
    private final HTMLElement descriptionPreview;

    public UndertowPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.WEB, Names.UNDERTOW);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        noStatistics = new EmptyState.Builder(Ids.UNDERTOW_STATISTICS_DISABLED,
                resources.constants().statisticsDisabledHeader())
                        .description(resources.messages().statisticsDisabled(Names.UNDERTOW))
                        .icon(fontAwesome("line-chart"))
                        .primaryAction(resources.constants().enableStatistics(), this::enableStatistics,
                                Constraint.writable(WEB_SUBSYSTEM_TEMPLATE, STATISTICS_ENABLED))
                        .build();
        Elements.setVisible(noStatistics.element(), false);

        descriptionPreview = section().element();
        Elements.setVisible(descriptionPreview, false);
        Previews.innerHtml(descriptionPreview, resources.previews().runtimeWeb());

        previewBuilder()
                .add(noStatistics)
                .add(descriptionPreview);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress addressWeb = WEB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        Operation opWeb = new Operation.Builder(addressWeb, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(opWeb, result -> {
            boolean statsEnabled = result.get(STATISTICS_ENABLED).asBoolean(false);
            Elements.setVisible(noStatistics.element(), !statsEnabled);
            Elements.setVisible(descriptionPreview, statsEnabled);
        });
    }

    private void enableStatistics() {
        ResourceAddress address = AddressTemplate.of("{selected.profile}/subsystem=undertow").resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(null));
    }
}
