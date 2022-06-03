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

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

class TransportElement extends GenericElement {

    private Form<ModelNode> threadPoolDefaultForm;

    TransportElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Metadata formMetadata, Resources resources, AddressTemplate template,
            String name, String resourceId) {
        super(formMetadata, tableButtonFactory, resources, template, name, resourceId);

        Metadata threadPoolDefaultMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE);

        threadPoolDefaultForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_FORM,
                threadPoolDefaultMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), DEFAULT);
                    presenter.saveSingleton(template1, threadPoolDefaultMetadata, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Default"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), DEFAULT);
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Default", form,
                            threadPoolDefaultMetadata);
                })
                .build();

        HTMLElement parentElement = (HTMLElement) table.element().parentNode;
        // as we are reusing the GenericElement, the form is already added to the section element, then we need to
        // retrieve the form element and add it to the tab
        HTMLElement form1 = (HTMLElement) parentElement.lastElementChild;
        // remove the element, then adds to the tab element
        parentElement.removeChild(form1);

        Tabs threadPoolTabs = new Tabs(Ids.JGROUPS_TRANSPORT_THREADPOOL_TAB_CONTAINER);
        threadPoolTabs.add(Ids.build("jgroups-transport", Ids.FORM), resources.constants().attributes(), form1);
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_TAB, "Thread Pool Default",
                threadPoolDefaultForm.element());

        parentElement.appendChild(threadPoolTabs.element());
    }

    @Override
    public void attach() {
        super.attach();
        threadPoolDefaultForm.attach();

        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                NamedNode selectedTransport = table.selectedRow();
                threadPoolDefaultForm.view(selectedTransport.get(THREAD_POOL).get(DEFAULT));
            } else {
                threadPoolDefaultForm.clear();
            }
        });
    }

    @Override
    public void detach() {
        super.detach();
        threadPoolDefaultForm.detach();
    }

    @Override
    void update(List<NamedNode> models) {
        super.update(models);
        // disable the ADD and REMOVE buttons, as the transport is a required singleton resource, but the r-r-d
        // doesn't says so
        // super.update enables the "remove" button if the model is not empty
        table.enableButton(0, false);
        table.enableButton(1, false);
    }
}
