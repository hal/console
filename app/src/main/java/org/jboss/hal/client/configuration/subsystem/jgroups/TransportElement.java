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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;

import elemental2.dom.HTMLElement;
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

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class TransportElement extends GenericElement {

    private Form<ModelNode> threadPoolDefaultForm;
    private Form<ModelNode> threadPoolTimerForm;
    private Form<ModelNode> threadPoolInternalForm;
    private Form<ModelNode> threadPoolOobForm;

    @SuppressWarnings({"HardCodedStringLiteral", "ConstantConditions", "DuplicateStringLiteralInspection"})
    TransportElement(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Metadata formMetadata, Resources resources, AddressTemplate template,
            String name, String resourceId) {
        super(formMetadata, tableButtonFactory, resources, template, name, resourceId);

        // thread-pool are singletons resources, but each one has different "default" values
        // then, we need specific metadata for each one, for the reset operation to work properly
        Metadata threadPoolDefaultMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_DEFAULT_TEMPLATE);
        Metadata threadPoolInternalMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_INTERNAL_TEMPLATE);
        Metadata threadPoolOobMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_OOB_TEMPLATE);
        Metadata threadPoolTimerMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_TIMER_TEMPLATE);

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
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Default", form, threadPoolDefaultMetadata);
                })
                .build();
        threadPoolTimerForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_TIMER_FORM,
                threadPoolTimerMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), TIMER);
                    presenter.saveSingleton(template1, threadPoolTimerMetadata, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Timer"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), TIMER);
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Timer", form, threadPoolTimerMetadata);
                })
                .build();
        threadPoolInternalForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_INTERNAL_FORM,
                threadPoolInternalMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), INTERNAL);
                    presenter.saveSingleton(template1, threadPoolInternalMetadata, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Internal"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), INTERNAL);
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Internal", form, threadPoolInternalMetadata);
                })
                .build();
        threadPoolOobForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_OOB_FORM,
                threadPoolOobMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), OOB);
                    presenter.saveSingleton(template1, threadPoolOobMetadata, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " OOB"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), OOB);
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " OOB", form, threadPoolOobMetadata);
                })
                .build();

        HTMLElement parentElement = (HTMLElement) table.asElement().parentNode;
        // retrieve the form element to add it to the tab
        HTMLElement form1 = (HTMLElement) parentElement.lastElementChild;
        // remove the element, then adds to the tab element
        parentElement.removeChild(form1);

        Tabs threadPoolTabs = new Tabs(Ids.JGROUPS_TRANSPORT_THREADPOOL_TAB_CONTAINER);
        threadPoolTabs.add(Ids.build("jgroups-transport", Ids.FORM), resources.constants().attributes(),
                form1);
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_TAB, "Thread Pool Default",
                threadPoolDefaultForm.asElement());
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_INTERNAL_TAB, "Thread Pool Internal",
                threadPoolInternalForm.asElement());
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_OOB_TAB, "Thread Pool OOB",
                threadPoolOobForm.asElement());
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_TIMER_TAB, "Thread Pool Timer",
                threadPoolTimerForm.asElement());

        parentElement.appendChild(threadPoolTabs.asElement());
    }

    @Override
    public void attach() {
        super.attach();
        threadPoolDefaultForm.attach();
        threadPoolInternalForm.attach();
        threadPoolOobForm.attach();
        threadPoolTimerForm.attach();

        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                NamedNode selectedTransport = table.selectedRow();
                threadPoolDefaultForm.view(selectedTransport.get(THREAD_POOL).get(DEFAULT));
                threadPoolInternalForm.view(selectedTransport.get(THREAD_POOL).get(INTERNAL));
                threadPoolOobForm.view(selectedTransport.get(THREAD_POOL).get(OOB));
                threadPoolTimerForm.view(selectedTransport.get(THREAD_POOL).get(TIMER));
            } else {
                threadPoolDefaultForm.clear();
                threadPoolInternalForm.clear();
                threadPoolOobForm.clear();
                threadPoolTimerForm.clear();
            }
        });
    }

    @Override
    public void detach() {
        super.detach();
        threadPoolDefaultForm.detach();
        threadPoolInternalForm.detach();
        threadPoolOobForm.detach();
        threadPoolTimerForm.detach();
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
