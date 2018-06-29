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
package org.jboss.hal.client.configuration.subsystem.infinispan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.infinispan.CacheType.LOCAL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * Element to manage the cache resources of a specific {@linkplain Cache cache type}. The element contains a table and
 * all necessary forms to add, update and remove caches.
 */
class CacheElement implements IsElement<HTMLElement>, Attachable, HasPresenter<CacheContainerPresenter> {

    private final CacheType cacheType;
    private final org.jboss.hal.ballroom.table.Table<NamedNode> table;
    private final Form<NamedNode> form;
    private final Map<Component, Form<ModelNode>> components;
    private final MemoryElement memoryElement;
    private final StoreElement storeElement;
    private final Pages pages;
    private org.jboss.hal.ballroom.table.Table<NamedNode> backupTable;
    private Form<NamedNode> backupForm;
    private CacheContainerPresenter presenter;

    @SuppressWarnings("ConstantConditions")
    CacheElement(CacheType cacheType, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {
        this.cacheType = cacheType;

        Metadata metadata = metadataRegistry.lookup(cacheType.template);
        ModelNodeTable.Builder<NamedNode> builder = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(cacheType.baseId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(cacheType.template, table -> presenter.addCache(cacheType)))
                .button(tableButtonFactory.remove(cacheType.template,
                        table -> presenter.removeCache(cacheType, table.selectedRow().getName())))
                .column(Names.NAME, (cell, type, row, meta) -> row.getName());
        if (cacheType != LOCAL) {
            builder.column(MODE);
        }
        List<InlineAction<NamedNode>> inlineActions = new ArrayList<>();
        inlineActions.add(new InlineAction<>(Names.MEMORY, row -> {
            presenter.selectCache(cacheType, row.getName());
            presenter.showCacheMemory();
        }));
        inlineActions.add(new InlineAction<>(Names.STORE, row -> {
            presenter.selectCache(cacheType, row.getName());
            presenter.showCacheStore();
        }));
        if (cacheType.backups) {
            inlineActions.add(new InlineAction<>(Names.BACKUPS, row -> {
                presenter.selectCache(cacheType, row.getName());
                presenter.showCacheBackup();
            }));
        }
        builder.column(inlineActions);
        table = builder.build();

        Tabs tabs = new Tabs(Ids.build(cacheType.baseId, Ids.TAB_CONTAINER));
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(cacheType.baseId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveCache(cacheType, form.getModel().getName(),
                        changedValues))
                .prepareReset(form -> presenter.resetCache(cacheType, form.getModel().getName(), form))
                .build();
        tabs.add(Ids.build(cacheType.baseId, Ids.TAB), resources.constants().attributes(), form.asElement());

        components = new HashMap<>();
        for (Component component : cacheType.components) {
            String tabId = Ids.build(cacheType.baseId, component.baseId, Ids.TAB);
            String formId = Ids.build(cacheType.baseId, component.baseId, Ids.FORM);
            Metadata cm = metadataRegistry.lookup(cacheType.template.append(COMPONENT + "=" + component.resource));
            Form<ModelNode> cf = new ModelNodeForm.Builder<>(formId, cm)
                    .singleton(() -> presenter.readCacheComponent(component),
                            () -> presenter.addCacheComponent(component))
                    .onSave((form, changedValues) -> presenter.saveCacheComponent(component, changedValues))
                    .prepareReset(form -> presenter.resetCacheComponent(component, form))
                    .prepareRemove(form -> presenter.removeCacheComponent(component, form))
                    .build();
            tabs.add(tabId, component.type, cf.asElement());
            components.put(component, cf);
        }

        memoryElement = new MemoryElement(cacheType, metadataRegistry, resources);
        storeElement = new StoreElement(cacheType, metadataRegistry, resources);

        HTMLElement root = section()
                .add(h(1).textContent(cacheType.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(tabs)
                .asElement();

        String id = Ids.build(cacheType.baseId, Ids.PAGES);
        String mainId = Ids.build(cacheType.baseId, Ids.PAGE);
        pages = new Pages(id, mainId, root);
        pages.addPage(mainId, Ids.build(cacheType.baseId, MEMORY, Ids.PAGE),
                () -> presenter.cacheSegment(), () -> presenter.memorySegment(), memoryElement);
        pages.addPage(mainId, Ids.build(cacheType.baseId, STORE, Ids.PAGE),
                () -> presenter.cacheSegment(), () -> presenter.storeSegment(), storeElement);

        if (cacheType.backups) {
            AddressTemplate backupTemplate = cacheType.template.append(COMPONENT + "=" + BACKUPS).append(BACKUP + "=*");
            Metadata backupMeta = metadataRegistry.lookup(backupTemplate);

            backupTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(cacheType.baseId, BACKUPS, Ids.TABLE),
                    backupMeta)
                    .button(tableButtonFactory.add(backupTemplate, table -> presenter.addCacheBackup()))
                    .button(tableButtonFactory.remove(backupTemplate,
                            table -> presenter.removeCacheBackup(table.selectedRow().getName())))
                    .column(Names.NAME, (cell, type, row, meta) -> row.getName())
                    .build();

            backupForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(cacheType.baseId, BACKUPS, Ids.FORM),
                    backupMeta)
                    .onSave((form, changedValues) -> presenter.saveCacheBackup(form.getModel().getName(),
                            changedValues))
                    .prepareReset(form -> presenter.resetCacheBackup(form.getModel().getName(), form))
                    .build();

            HTMLElement backupSection = section()
                    .add(h(1).textContent(Names.BACKUPS))
                    .add(p().textContent(backupMeta.getDescription().getDescription()))
                    .add(backupTable)
                    .add(backupForm)
                    .asElement();

            pages.addPage(mainId, Ids.build(cacheType.baseId, BACKUPS, Ids.PAGE),
                    () -> presenter.cacheSegment(), () -> Names.BACKUPS, backupSection);
        }
    }

    @Override
    public HTMLElement asElement() {
        return pages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        components.values().forEach(Attachable::attach);
        memoryElement.attach();
        storeElement.attach();
        if (cacheType.backups) {
            backupTable.attach();
            backupForm.attach();
            backupTable.bindForm(backupForm);
        }

        table.onSelectionChange(t -> {
            if (t.hasSelection()) {
                NamedNode selectedCache = t.selectedRow();
                presenter.selectCache(cacheType, selectedCache.getName());
                form.view(selectedCache);
                components.forEach((component, form) -> {
                    ModelNode modelNode = failSafeGet(selectedCache, component.path());
                    form.view(modelNode);
                });
                List<Property> memories = failSafePropertyList(selectedCache, MEMORY);
                storeElement.update(memories);
                List<Property> stores = failSafePropertyList(selectedCache, STORE);
                storeElement.update(stores);
            } else {
                form.clear();
                //noinspection Convert2MethodRef
                components.values().forEach((fsf) -> fsf.clear());
            }
        });
    }

    @Override
    public void detach() {
        if (cacheType.backups) {
            backupTable.detach();
            backupForm.detach();
        }
        storeElement.detach();
        memoryElement.detach();
        components.values().forEach(Attachable::detach);
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(CacheContainerPresenter presenter) {
        this.presenter = presenter;
        memoryElement.setPresenter(presenter);
        storeElement.setPresenter(presenter);
    }

    void update(List<NamedNode> caches) {
        form.clear();
        //noinspection Convert2MethodRef
        components.values().forEach((fsf) -> fsf.clear());
        table.update(caches);
        pages.showPage(Ids.build(cacheType.baseId, Ids.PAGE));
    }

    void updateBackups(List<NamedNode> backups) {
        pages.showPage(Ids.build(cacheType.baseId, BACKUPS, Ids.PAGE));
        backupForm.clear();
        backupTable.update(backups);
    }

    void updateMemory(List<Property> memories) {
        pages.showPage(Ids.build(cacheType.baseId, MEMORY, Ids.PAGE));
        memoryElement.update(memories);
    }

    void updateStore(List<Property> stores) {
        pages.showPage(Ids.build(cacheType.baseId, STORE, Ids.PAGE));
        storeElement.update(stores);
    }
}
