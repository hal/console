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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
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
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

abstract class CacheViewImpl<P extends CachePresenter<?, ?>> extends HalViewImpl implements CacheView<P> {

    private VerticalNavigation navigation;
    private Form<Cache> form;
    private Map<Component, Form<ModelNode>> components;
    private MemoryElement memoryElement;
    private StoreElement storeElement;
    private org.jboss.hal.ballroom.table.Table<NamedNode> backupTable;
    private Form<NamedNode> backupForm;
    private P presenter;

    void init(CacheType cacheType, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {
        initNavigation();
        initConfiguration(cacheType, metadataRegistry, resources);
        initMemory(cacheType, metadataRegistry, resources);
        initStore(cacheType, metadataRegistry, resources);
        initBackups(cacheType, metadataRegistry, tableButtonFactory);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private void initNavigation() {
        navigation = new VerticalNavigation();
        registerAttachable(navigation);
    }

    private void initConfiguration(CacheType cacheType, MetadataRegistry metadataRegistry, Resources resources) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template);

        Tabs tabs = new Tabs(Ids.build(cacheType.baseId, Ids.TAB_CONTAINER));
        form = new ModelNodeForm.Builder<Cache>(Ids.build(cacheType.baseId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveCache(changedValues))
                .prepareReset(f -> presenter.resetCache(f))
                .build();
        tabs.add(Ids.build(cacheType.baseId, Ids.TAB), resources.constants().attributes(), form.element());
        registerAttachable(form);

        components = new HashMap<>();
        for (Component component : cacheType.components) {
            String tabId = Ids.build(cacheType.baseId, component.baseId, Ids.TAB);
            String formId = Ids.build(cacheType.baseId, component.baseId, Ids.FORM);
            Metadata cm = metadataRegistry.lookup(cacheType.template.append(COMPONENT + "=" + component.resource));
            Form<ModelNode> cf = new ModelNodeForm.Builder<>(formId, cm)
                    .singleton(() -> presenter.readComponent(component),
                            () -> presenter.addComponent(component))
                    .onSave((f, changedValues) -> presenter.saveComponent(component, changedValues))
                    .prepareReset(f -> presenter.resetComponent(component, f))
                    .prepareRemove(f -> presenter.removeComponent(component, f))
                    .build();
            tabs.add(tabId, component.type, cf.element());
            components.put(component, cf);
        }
        registerAttachables(components.values());

        HTMLElement section = section()
                .add(h(1).textContent(cacheType.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(tabs).element();
        navigation.addPrimary(Ids.build(cacheType.baseId, Ids.ITEM), Names.CONFIGURATION, pfIcon("settings"),
                section);
    }

    private void initMemory(CacheType cacheType, MetadataRegistry metadataRegistry, Resources resources) {
        memoryElement = new MemoryElement(cacheType, metadataRegistry, resources);
        navigation.addPrimary(Ids.build(cacheType.baseId, MEMORY, Ids.ITEM), Names.MEMORY, pfIcon("memory"),
                memoryElement);
        registerAttachable(memoryElement);
    }

    private void initStore(CacheType cacheType, MetadataRegistry metadataRegistry, Resources resources) {
        storeElement = new StoreElement(cacheType, metadataRegistry, resources);
        navigation.addPrimary(Ids.build(cacheType.baseId, STORE, Ids.ITEM), Names.STORE_RESOURCE,
                fontAwesome("shopping-basket"),
                storeElement);
        registerAttachable(storeElement);
    }

    private void initBackups(CacheType cacheType, MetadataRegistry metadataRegistry,
            TableButtonFactory tableButtonFactory) {
        if (cacheType.backups) {
            AddressTemplate backupTemplate = cacheType.template
                    .append(COMPONENT + "=" + BACKUPS)
                    .append(BACKUP + "=*");
            Metadata backupMeta = metadataRegistry.lookup(backupTemplate);

            backupTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(cacheType.baseId, BACKUPS, Ids.TABLE),
                    backupMeta)
                    .button(tableButtonFactory.add(backupTemplate, table -> presenter.addBackup()))
                    .button(tableButtonFactory.remove(backupTemplate,
                            table -> presenter.removeBackup(table.selectedRow().getName())))
                    .nameColumn()
                    .build();

            backupForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(cacheType.baseId, BACKUPS, Ids.FORM),
                    backupMeta)
                    .onSave((form, changedValues) -> presenter.saveCacheBackup(form.getModel().getName(),
                            changedValues))
                    .prepareReset(form -> presenter.resetBackup(form.getModel().getName(), form))
                    .build();

            HTMLElement backupSection = section()
                    .add(h(1).textContent(Names.BACKUPS))
                    .add(p().textContent(backupMeta.getDescription().getDescription()))
                    .add(backupTable)
                    .add(backupForm).element();
            navigation.addPrimary(Ids.build(cacheType.baseId, BACKUP, Ids.ITEM), Names.BACKUP, fontAwesome("life-ring"),
                    backupSection);
            registerAttachable(backupTable, backupForm);
        }
    }

    @Override
    public void attach() {
        super.attach();
        if (backupTable != null && backupForm != null) {
            backupTable.bindForm(backupForm);
        }
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
        this.memoryElement.setPresenter(presenter);
        this.storeElement.setPresenter(presenter);
    }

    @Override
    public void update(Cache cache) {
        form.view(cache);
        components.forEach((component, f) -> {
            ModelNode modelNode = failSafeGet(cache, component.path());
            f.view(modelNode);
        });
        memoryElement.update(failSafePropertyList(cache, MEMORY));
        storeElement.update(failSafePropertyList(cache, STORE));
        if (backupTable != null && backupForm != null) {
            String path = String.join("/", COMPONENT, BACKUPS, BACKUP);
            backupForm.clear();
            backupTable.update(asNamedNodes(failSafePropertyList(cache, path)));
        }
    }
}
