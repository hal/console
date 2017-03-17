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

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.OptionElement;
import elemental.html.SelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.PatternFly.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.bootstrapSelect;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.selectpicker;

/**
 * Element to view and modify the {@code store=*} singletons of a cache. Kind of a fail safe form with the difference
 * that we need to take care of {@code store=none}.
 *
 * @author Harald Pehl
 */
class StoreElement implements IsElement, Attachable, HasPresenter<CacheContainerPresenter> {

    private static final class StoreTable {

        final Store store;
        final Table table;

        private StoreTable(final Store store, final Table table) {
            this.store = store;
            this.table = table;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) { return true; }
            if (!(o instanceof StoreTable)) { return false; }

            StoreTable that = (StoreTable) o;

            if (store != that.store) { return false; }
            return table == that.table;
        }

        @Override
        public int hashCode() {
            int result = store.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }


    private static final String HEADER_FORM = "headerForm";

    private final EmptyState emptyState;
    private final Element headerForm;
    private final String selectStoreId;
    private final SelectElement selectStore;
    private final Map<Store, Tabs> tabs;
    private final Map<Store, Form<ModelNode>> storeForms;
    private final Map<Store, WriteElement> writeElements;
    private final Map<StoreTable, Form<ModelNode>> tableForms;
    private final Element root;
    private CacheContainerPresenter presenter;

    StoreElement(final Cache cache, final MetadataRegistry metadataRegistry, final Resources resources) {
        this.tabs = new HashMap<>();
        this.storeForms = new HashMap<>();
        this.writeElements = new HashMap<>();
        this.tableForms = new HashMap<>();

        SelectElement emptyStoreSelect = storeSelect();
        emptyState = new EmptyState.Builder(resources.constants().noStore())
                .description(resources.messages().noStore())
                .add(emptyStoreSelect)
                .primaryAction(resources.constants().add(), () -> {
                    String value = SelectBoxBridge.Single.element(emptyStoreSelect).getValue();
                    presenter.addCacheStore(Store.fromResource(value));
                })
                .build();

        selectStoreId = Ids.build(cache.baseId, STORE, "select");
        selectStore = storeSelect();
        selectStore.setId(selectStoreId);

        for (Store store : Store.values()) {
            Tabs storeTabs = new Tabs();
            tabs.put(store, storeTabs);

            Metadata metadata = metadataRegistry.lookup(cache.template.append(STORE + "=" + store.resource));
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(cache.baseId, store.baseId, Ids.FORM_SUFFIX),
                    metadata)
                    .onSave((f, changedValues) -> presenter.saveCacheStore(store, changedValues))
                    .onReset(f -> presenter.resetCacheStore(store, f))
                    .build();
            storeForms.put(store, form);
            storeTabs.add(Ids.build(cache.baseId, store.baseId, ATTRIBUTES, Ids.TAB_SUFFIX),
                    resources.constants().attributes(), form.asElement());

            WriteElement writeElement = new WriteElement(cache, store, metadataRegistry, resources);
            storeTabs.add(Ids.build(cache.baseId, store.baseId, WRITE, Ids.TAB_SUFFIX), Names.WRITE_BEHAVIOUR,
                    writeElement.asElement());
            writeElements.put(store, writeElement);

            if (store.tables != null) {
                for (Table table : store.tables) {
                    Form<ModelNode> tableForm = tableForm(cache, store, table, metadataRegistry);
                    storeTabs.add(Ids.build(cache.baseId, store.baseId, table.baseId, Ids.TAB_SUFFIX), table.type,
                            tableForm.asElement());
                    tableForms.put(new StoreTable(store, table), tableForm);
                }
            }
        }

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .section()
                .div().css(CSS.headerForm).rememberAs(HEADER_FORM)
                    .label().attr("for", selectStoreId)
                        .textContent(resources.constants().switchStore())
                    .end()
                    .add(selectStore)
                .end()
                .h(1).textContent(Names.STORE).end()
                .p().textContent(resources.messages().cacheStore()).end()
                .add(emptyState);
                tabs.values().forEach(builder::add);
            builder.end();
        // @formatter:on

        headerForm = builder.referenceFor(HEADER_FORM);
        root = builder.build();

        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(headerForm, false);
        tabs.values().forEach(t -> Elements.setVisible(t.asElement(), false));
    }

    private SelectElement storeSelect() {
        SelectElement select = Browser.getDocument().createSelectElement();
        select.setMultiple(false);
        select.setSize(1);
        select.getClassList().add(selectpicker);

        for (Store store : Store.values()) {
            OptionElement option = Browser.getDocument().createOptionElement();
            option.setValue(store.resource);
            option.setText(store.type);
            select.appendChild(option);
        }

        return select;
    }

    private Form<ModelNode> tableForm(Cache cache, Store store, Table table, MetadataRegistry metadataRegistry) {
        AddressTemplate template = cache.template
                .append(STORE + "=" + store.resource)
                .append(TABLE + "=" + table.resource);
        Metadata metadata = metadataRegistry.lookup(template);

        String id = Ids.build(cache.baseId, store.baseId, table.baseId, Ids.FORM_SUFFIX);
        return new ModelNodeForm.Builder<>(id, metadata)
                .include(PREFIX)
                .customFormItem(ID_COLUMN, ad -> new ColumnFormItem(ID_COLUMN))
                .customFormItem(DATA_COLUMN, ad -> new ColumnFormItem(DATA_COLUMN))
                .customFormItem(TIMESTAMP_COLUMN, ad -> new ColumnFormItem(TIMESTAMP_COLUMN))
                .include(BATCH_SIZE, FETCH_SIZE)
                .unsorted()
                .onSave((f, changedValues) -> presenter.saveStoreTable(table, changedValues))
                .onReset(f -> presenter.resetStoreTable(table, f))
                .build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $("#" + selectStoreId).selectpicker(options);
        SelectBoxBridge.Single.element(selectStore).onChange((event, index) -> {
            String value = SelectBoxBridge.Single.element(selectStore).getValue();
            Store store = Store.fromResource(value);
            presenter.switchStore(store);
        });
        autoWidth(emptyState.asElement());
        autoWidth(headerForm);
        storeForms.values().forEach(Attachable::attach);
        writeElements.values().forEach(Attachable::attach);
        tableForms.values().forEach(Attachable::attach);
    }

    private void autoWidth(final Element element) {
        Element select = element.querySelector("." + btnGroup + "." + bootstrapSelect);
        if (select != null) {
            select.getStyle().setWidth("auto"); //NON-NLS
        }
    }

    @Override
    public void detach() {
        tableForms.values().forEach(Attachable::detach);
        writeElements.values().forEach(Attachable::detach);
        storeForms.values().forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(final CacheContainerPresenter presenter) {
        this.presenter = presenter;
        writeElements.values().forEach(we -> we.setPresenter(presenter));
    }

    void update(final List<Property> stores) {
        if (stores.isEmpty() || NONE.equals(stores.get(0).getName())) {
            emptyStateMode();

        } else {
            Store store = Store.fromResource(stores.get(0).getName());
            if (store != null) {
                SelectBoxBridge.Single.element(selectStore).setValue(store.resource);

                ModelNode storeNode = stores.get(0).getValue();
                storeForms.get(store).view(storeNode);
                writeElements.get(store).update(storeNode);

                if (store.tables != null) {
                    for (Table table : store.tables) {
                        StoreTable storeTable = new StoreTable(store, table);
                        Form<ModelNode> form = tableForms.get(storeTable);
                        if (form != null) {
                            form.view(failSafeGet(storeNode, table.path()));
                        }
                    }
                }
                formMode(store);

            } else {
                emptyStateMode();
            }
        }
    }

    private void emptyStateMode() {
        Elements.setVisible(emptyState.asElement(), true);
        Elements.setVisible(headerForm, false);
        tabs.values().forEach(t -> Elements.setVisible(t.asElement(), false));
    }

    private void formMode(Store store) {
        Elements.setVisible(emptyState.asElement(), false);
        Elements.setVisible(headerForm, true);
        tabs.forEach((s, t) -> Elements.setVisible(t.asElement(), s == store));
    }
}
