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

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_THREAD_POOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

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

        Metadata threadPoolMetadata = metadataRegistry.lookup(TRANSPORT_THREAD_POOL_TEMPLATE);

        threadPoolDefaultForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_DEFAULT_FORM,
                threadPoolMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), DEFAULT);
                    presenter.saveSingleton(template1, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Default"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), DEFAULT);
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Default", form, threadPoolMetadata);
                })
                .build();
        threadPoolTimerForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_TIMER_FORM,
                threadPoolMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "timer");
                    presenter.saveSingleton(template1, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Timer"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "timer");
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Timer", form, threadPoolMetadata);
                })
                .build();
        threadPoolInternalForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_INTERNAL_FORM,
                threadPoolMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "internal");
                    presenter.saveSingleton(template1, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " Internal"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "internal");
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " Internal", form, threadPoolMetadata);
                })
                .build();
        threadPoolOobForm = new ModelNodeForm.Builder<>(Ids.JGROUPS_TRANSPORT_THREADPOOL_OOB_FORM,
                threadPoolMetadata)
                .onSave((form, changedValues) -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "oob");
                    presenter.saveSingleton(template1, changedValues,
                            resources.messages().modifySingleResourceSuccess(Names.THREAD_POOL + " OOB"));
                })
                .prepareReset(form -> {
                    AddressTemplate template1 = SELECTED_TRANSPORT_THREAD_POOL_TEMPLATE
                            .replaceWildcards(table.selectedRow().getName(), "oob");
                    presenter.resetSingleton(template1, Names.THREAD_POOL + " OOB", form, threadPoolMetadata);
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
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_TIMER_TAB, "Thread Pool Timer",
                threadPoolTimerForm.asElement());
        threadPoolTabs.add(Ids.JGROUPS_TRANSPORT_THREADPOOL_OOB_TAB, "Thread Pool OOB",
                threadPoolOobForm.asElement());

        parentElement.appendChild(threadPoolTabs.asElement());
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    void update(List<NamedNode> models) {
        super.update(models);
        boolean resourcesIsEmpty = models.isEmpty();
        // disable the ADD button if there is already a transport in the list
        // only one transport is allowed per stack
        table.enableButton(0, resourcesIsEmpty);
        if (!resourcesIsEmpty) {
            NamedNode transport = models.get(0);
            // the thread-pool are singleton resources
            threadPoolTimerForm.view(transport.get(THREAD_POOL).get("timer"));
            threadPoolDefaultForm.view(transport.get(THREAD_POOL).get("default"));
            threadPoolOobForm.view(transport.get(THREAD_POOL).get("oob"));
            threadPoolInternalForm.view(transport.get(THREAD_POOL).get("internal"));
        } else {
            threadPoolTimerForm.clear();
            threadPoolDefaultForm.clear();
            threadPoolOobForm.clear();
            threadPoolInternalForm.clear();
        }
    }
}
