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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_TRANSPORT_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

class TransportElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    private Form<ModelNode> transportForm;
    private Form<ModelNode> threadPoolDefaultForm;
    private JGroupsPresenter presenter;
    private final Tabs transportTabs;
    private final HTMLElement section;
    private final HTMLElement heading;
    private final HTMLElement description;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private String currentTransportType;

    TransportElement(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        final String threadPoolDefaultName = Names.THREAD_POOL + " Default";
        Metadata threadPoolDefaultMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE);
        threadPoolDefaultForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_FORM,
                threadPoolDefaultMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(currentTransportType, DEFAULT);
                    presenter.saveSingleton(template1, threadPoolDefaultMetadata, changedValues,
                            resources.messages().modifySingleResourceSuccess(threadPoolDefaultName));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(currentTransportType, DEFAULT);
                    presenter.resetSingleton(template1, threadPoolDefaultName, form,
                            threadPoolDefaultMetadata);
                })
                .build();

        transportTabs = new Tabs(Ids.JGROUPS_TRANSPORT_THREADPOOL_TAB_CONTAINER);
        transportTabs.add(Ids.build("jgroups-transport", Ids.FORM, Ids.TAB), resources.constants().attributes(),
                section().element());
        transportTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_TAB, threadPoolDefaultName,
                threadPoolDefaultForm.element());

        section = section()
                .add(heading = h(1).element())
                .add(description = p().element())
                .add(transportTabs).element();
    }

    @Override
    public void attach() {
        threadPoolDefaultForm.attach();
    }

    @Override
    public void detach() {
        if (transportForm != null) {
            transportForm.detach();
        }
        threadPoolDefaultForm.detach();
    }

    void update(List<NamedNode> models) {
        if (models.isEmpty()) {
            return;
        }
        NamedNode transport = models.get(0);
        String transportType = transport.getName();

        if (!transportType.equals(currentTransportType)) {
            currentTransportType = transportType;
            // metadata is at .../stack=*/transport=<type>, the first wildcard needs to be preserved
            AddressTemplate metadataTemplate = TRANSPORT_TEMPLATE.replaceWildcards("*", currentTransportType);
            Metadata transportMetadata = metadataRegistry.lookup(metadataTemplate);
            AddressTemplate transportTemplate = SELECTED_TRANSPORT_TEMPLATE.replaceWildcards(currentTransportType);
            String fullName = Names.TRANSPORT + ": " + currentTransportType;

            transportForm = new ModelNodeForm.Builder<>(Ids.build(Ids.JGROUPS_TRANSPORT, Ids.FORM), transportMetadata)
                    .onSave((form, changedValues) -> {
                        presenter.saveSingleton(transportTemplate, transportMetadata, changedValues,
                                resources.messages().modifySingleResourceSuccess(fullName));
                    })
                    .prepareReset(form -> {
                        presenter.resetSingleton(transportTemplate, fullName, form,
                                transportMetadata);
                    })
                    .build();
            transportForm.attach();

            heading.textContent = fullName;
            description.textContent = transportMetadata.getDescription().getDescription();
            transportTabs.setContent(0, transportForm.element());
        }

        transportForm.view(transport);
        threadPoolDefaultForm.view(transport.get(THREAD_POOL).get(DEFAULT));
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    public void setPresenter(JGroupsPresenter presenter) {
        this.presenter = presenter;
    }
}
