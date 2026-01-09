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
package org.jboss.hal.client.configuration.subsystem.jca;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.WORKMANAGER_LRT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX_THREADS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_FACTORY;
import static org.jboss.hal.resources.Names.THREAD_POOLS;

/**
 * Element to view and manage short and long running thread pools of a (distributed) workmanager. This class assumes that the
 * {@code short-running-threads} and {@code long-running-threads} resources have the same attributes.
 * <p>
 * TODO Implement save and reset callbacks
 */
class ThreadPoolsEditor implements IsElement<HTMLElement>, Attachable, HasPresenter<JcaPresenter> {

    private final HTMLElement root;
    private final List<Attachable> attachables;
    private final Table<ThreadPool> table;
    private final ModelNodeForm<ThreadPool> attributesForm;
    private final ModelNodeForm<ThreadPool> sizingForm;

    private JcaPresenter presenter;
    private AddressTemplate workmanagerTemplate;
    private String workmanager;

    @SuppressWarnings("ConstantConditions")
    ThreadPoolsEditor(String prefixId, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory, Resources resources) {
        attachables = new ArrayList<>();

        Metadata metadata = metadataRegistry.lookup(WORKMANAGER_LRT_TEMPLATE);
        table = new ModelNodeTable.Builder<ThreadPool>(Ids.build(prefixId, Ids.JCA_THREAD_POOL_TABLE), metadata)
                .button(tableButtonFactory.add(WORKMANAGER_LRT_TEMPLATE,
                        table -> presenter.launchAddThreadPool(workmanagerTemplate, workmanager)))
                .button(tableButtonFactory.remove(WORKMANAGER_LRT_TEMPLATE,
                        table -> presenter.removeThreadPool(workmanagerTemplate, workmanager,
                                table.selectedRow())))
                .nameColumn()
                .column(resources.constants().type(), (cell, type, row, meta) -> row.getRunningMode())
                .column(MAX_THREADS)
                .build();
        attachables.add(table);

        attributesForm = new ModelNodeForm.Builder<ThreadPool>(
                Ids.build(prefixId, Ids.JCA_THREAD_POOL_ATTRIBUTES_FORM), metadata)
                .include(NAME, "allow-core-timeout", THREAD_FACTORY)
                .unsorted()
                .onSave((form, changedValues) -> presenter.saveThreadPool(workmanagerTemplate, workmanager,
                        form.getModel(), changedValues))
                .prepareReset(form -> presenter.resetThreadPool(workmanagerTemplate, workmanager,
                        form.getModel(), form))
                .build();
        attachables.add(attributesForm);
        sizingForm = new ModelNodeForm.Builder<ThreadPool>(
                Ids.build(prefixId, Ids.JCA_THREAD_POOL_SIZING_FORM), metadata)
                .include(MAX_THREADS, "core-threads", "queue-length")
                .onSave((form, changedValues) -> presenter.saveThreadPool(workmanagerTemplate, workmanager,
                        form.getModel(), changedValues))
                .prepareReset(form -> presenter.resetThreadPool(workmanagerTemplate, workmanager,
                        form.getModel(), form))
                .build();
        attachables.add(sizingForm);

        Tabs tabs = new Tabs(Ids.build(prefixId, Ids.JCA_THREAD_POOL_TAB_CONTAINER))
                .add(Ids.build(prefixId, Ids.JCA_THREAD_POOL_ATTRIBUTES_TAB), resources.constants().attributes(),
                        attributesForm.element())
                .add(Ids.build(prefixId, Ids.JCA_THREAD_POOL_SIZING_TAB), resources.constants().sizing(),
                        sizingForm.element());

        root = section()
                .add(h(1).textContent(THREAD_POOLS))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(tabs).element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        attachables.forEach(Attachable::attach);
        table.bindForms(asList(attributesForm, sizingForm));
    }

    @Override
    public void detach() {
        attachables.forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(JcaPresenter presenter) {
        this.presenter = presenter;
    }

    void update(AddressTemplate workmanagerTemplate, String workmanager,
            List<Property> longRunningThreads, List<Property> shortRunningThreads) {
        this.workmanagerTemplate = workmanagerTemplate;
        this.workmanager = workmanager;

        List<ThreadPool> lrt = longRunningThreads.stream()
                .map(property -> new ThreadPool(property, true))
                .collect(toList());
        List<ThreadPool> srt = shortRunningThreads.stream()
                .map(property -> new ThreadPool(property, false))
                .collect(toList());

        List<ThreadPool> threadPools = new ArrayList<>(lrt);
        threadPools.addAll(srt);
        threadPools.sort(comparing(NamedNode::getName));

        attributesForm.clear();
        sizingForm.clear();
        table.update(threadPools, ThreadPool::id);
        table.enableButton(0, threadPools.size() < 2);
    }
}
