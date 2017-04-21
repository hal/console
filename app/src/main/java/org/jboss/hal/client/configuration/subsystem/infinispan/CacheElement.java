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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.infinispan.Cache.LOCAL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.columnAction;

/**
 * Element to manage the cache resources of a specific {@linkplain Cache cache type}. The element contains a table and
 * all necessary forms to add, update and remove caches.
 *
 * @author Harald Pehl
 */
class CacheElement implements IsElement, Attachable, HasPresenter<CacheContainerPresenter> {

    private final Cache cache;
    private final NamedNodeTable<NamedNode> table;
    private final Form<NamedNode> form;
    private final Map<Component, Form<ModelNode>> components;
    private final StoreElement storeElement;
    private final Pages pages;
    private NamedNodeTable<NamedNode> backupTable;
    private Form<NamedNode> backupForm;
    private CacheContainerPresenter presenter;

    @SuppressWarnings("ConstantConditions")
    CacheElement(Cache cache, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {
        this.cache = cache;

        Metadata metadata = metadataRegistry.lookup(cache.template);
        ModelNodeTable.Builder<NamedNode> builder = new NamedNodeTable.Builder<>(metadata)
                .button(tableButtonFactory.add(cache.template, (event, api) -> presenter.addCache(cache)))
                .button(tableButtonFactory.remove(cache.template,
                        (event, api) -> presenter.removeCache(cache, api.selectedRow().getName())))
                .column(Names.NAME, (cell, type, row, meta) -> row.getName());
        if (cache != LOCAL) {
            builder.column(MODE);
        }
        if (cache.backups) {
            // two action links: 1) store, 2) backups
            builder.column(
                    columnActions -> {
                        String columnId = Ids.build(cache.baseId, STORE, "column");
                        return new ColumnBuilder<NamedNode>(columnId, resources.constants().action(),
                                (cell, t, row, meta) -> {
                                    String id1 = Ids.uniqueId();
                                    String id2 = Ids.uniqueId();
                                    columnActions.add(id1, row1 -> {
                                        presenter.selectCache(cache, row.getName());
                                        presenter.showCacheStore();
                                    });
                                    columnActions.add(id2, row2 -> {
                                        presenter.selectCache(cache, row.getName());
                                        presenter.showCacheBackup();
                                    });
                                    //noinspection HardCodedStringLiteral
                                    return "<a id=\"" + id1 + "\" class=\"" + columnAction + "\">" + Names.STORE + "</a> / " +
                                            "<a id=\"" + id2 + "\" class=\"" + columnAction + "\">" + Names.BACKUPS + "</a>";
                                })
                                .orderable(false)
                                .searchable(false)
                                .width("12em") //NON-NLS
                                .build();
                    });
        } else {
            // one action link: store
            builder.column(Names.STORE, row -> {
                presenter.selectCache(cache, row.getName());
                presenter.showCacheStore();
            });
        }
        table = new NamedNodeTable<>(Ids.build(cache.baseId, Ids.TABLE_SUFFIX), metadata, builder.build());

        Tabs tabs = new Tabs();
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(cache.baseId, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter.saveCache(cache, form.getModel().getName(), changedValues))
                .prepareReset(form -> presenter.resetCache(cache, form.getModel().getName(), form))
                .build();
        tabs.add(Ids.build(cache.baseId, Ids.TAB_SUFFIX), resources.constants().attributes(), form.asElement());

        components = new HashMap<>();
        for (Component component : cache.components) {
            String tabId = Ids.build(cache.baseId, component.baseId, Ids.TAB_SUFFIX);
            String formId = Ids.build(cache.baseId, component.baseId, Ids.FORM_SUFFIX);
            Metadata cm = metadataRegistry.lookup(cache.template.append(COMPONENT + "=" + component.resource));
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

        storeElement = new StoreElement(cache, metadataRegistry, resources);

        // @formatter:off
        Element root = new Elements.Builder()
            .section()
                .h(1).textContent(cache.type).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(tabs)
            .end()
        .build();
        // @formatter:on

        String mainId = Ids.build(cache.baseId, Ids.PAGE_SUFFIX);
        pages = new Pages(mainId, root);
        pages.addPage(mainId, Ids.build(cache.baseId, STORE, Ids.PAGE_SUFFIX),
                () -> presenter.cacheSegment(), () -> presenter.storeSegment(), storeElement);

        if (cache.backups) {
            AddressTemplate backupTemplate = cache.template.append(COMPONENT + "=" + BACKUPS).append(BACKUP + "=*");
            Metadata backupMeta = metadataRegistry.lookup(backupTemplate);

            Options<NamedNode> backupOptions = new NamedNodeTable.Builder<>(backupMeta)
                    .button(tableButtonFactory.add(backupTemplate, (event, api) -> presenter.addCacheBackup()))
                    .button(tableButtonFactory.remove(backupTemplate,
                            (event, api) -> presenter.removeCacheBackup(api.selectedRow().getName())))
                    .column(Names.NAME, (cell, type, row, meta) -> row.getName())
                    .build();
            backupTable = new NamedNodeTable<>(Ids.build(cache.baseId, BACKUPS, Ids.TABLE_SUFFIX),
                    backupMeta, backupOptions);

            backupForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(cache.baseId, BACKUPS, Ids.FORM_SUFFIX),
                    backupMeta)
                    .onSave((form, changedValues) -> presenter.saveCacheBackup(form.getModel().getName(),
                            changedValues))
                    .prepareReset(form -> presenter.resetCacheBackup(form.getModel().getName(), form))
                    .build();

            // @formatter:off
            Element backupSection = new Elements.Builder()
                .section()
                    .h(1).textContent(Names.BACKUPS).end()
                    .p().textContent(backupMeta.getDescription().getDescription()).end()
                    .add(backupTable)
                    .add(backupForm)
                .end()
            .build();
            // @formatter:on

            pages.addPage(mainId, Ids.build(cache.baseId, BACKUPS, Ids.PAGE_SUFFIX),
                    () -> presenter.cacheSegment(), () -> Names.BACKUPS, backupSection);
        }
    }

    @Override
    public Element asElement() {
        return pages.asElement();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        components.values().forEach(Attachable::attach);
        storeElement.attach();
        if (cache.backups) {
            backupTable.attach();
            backupForm.attach();
            backupTable.bindForm(backupForm);
        }

        table.api().onSelectionChange((Api<NamedNode> api) -> {
            if (api.hasSelection()) {
                NamedNode selectedCache = api.selectedRow();
                presenter.selectCache(cache, selectedCache.getName());
                form.view(selectedCache);
                components.forEach((component, form) -> {
                    ModelNode modelNode = failSafeGet(selectedCache, component.path());
                    form.view(modelNode);
                });
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
        if (cache.backups) {
            backupTable.detach();
            backupForm.detach();
        }
        storeElement.detach();
        components.values().forEach(Attachable::detach);
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
        storeElement.setPresenter(presenter);
    }

    void update(List<NamedNode> caches) {
        form.clear();
        //noinspection Convert2MethodRef
        components.values().forEach((fsf) -> fsf.clear());
        table.update(caches);
        pages.showPage(Ids.build(cache.baseId, Ids.PAGE_SUFFIX));
    }

    void updateBackups(final List<NamedNode> backups) {
        pages.showPage(Ids.build(cache.baseId, BACKUPS, Ids.PAGE_SUFFIX));
        backupForm.clear();
        backupTable.update(backups);
    }

    void updateStore(final List<Property> stores) {
        pages.showPage(Ids.build(cache.baseId, STORE, Ids.PAGE_SUFFIX));
        storeElement.update(stores);
    }
}
