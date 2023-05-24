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
import java.util.List;
import java.util.Map;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
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

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.*;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.bootstrapSelect;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.selectpicker;
import static org.jboss.hal.resources.CSS.width;

/**
 * Element to view and modify the {@code store=*} singletons of a cache. Kind of a fail safe form with the difference that we
 * need to take care of {@code store=none}.
 */
class StoreElement implements IsElement<HTMLElement>, Attachable, HasPresenter<CachePresenter<?, ?>> {

    private final EmptyState emptyState;
    private final HTMLElement currentStore;
    private final HTMLElement headerForm;
    private final String selectStoreId;
    private final HTMLSelectElement selectStore;
    private final Map<Store, Tabs> tabs;
    private final Map<Store, Form<ModelNode>> storeForms;
    private final Map<Store, WriteElement> writeElements;
    private final Map<StoreTable, Form<ModelNode>> tableForms;
    private final HTMLElement root;
    private CachePresenter<?, ?> presenter;

    StoreElement(CacheType cacheType, MetadataRegistry metadataRegistry, Resources resources) {
        this.tabs = new HashMap<>();
        this.storeForms = new HashMap<>();
        this.writeElements = new HashMap<>();
        this.tableForms = new HashMap<>();

        HTMLSelectElement emptyStoreSelect = storeSelect();
        emptyState = new EmptyState.Builder(Ids.build(cacheType.baseId, STORE, Ids.EMPTY),
                resources.constants().noStore())
                .description(resources.messages().noStore())
                .add(emptyStoreSelect)
                .primaryAction(resources.constants().add(), () -> {
                    String value = SelectBoxBridge.Single.element(emptyStoreSelect).getValue();
                    presenter.addStore(Store.fromResource(value));
                })
                .build();

        selectStoreId = Ids.build(cacheType.baseId, STORE, "select");
        selectStore = storeSelect();
        selectStore.id = selectStoreId;

        for (Store store : Store.values()) {
            Tabs storeTabs = new Tabs(Ids.build(cacheType.baseId, store.baseId, Ids.TAB_CONTAINER));
            tabs.put(store, storeTabs);

            Metadata metadata = metadataRegistry.lookup(cacheType.template.append(STORE + "=" + store.resource));
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(cacheType.baseId, store.baseId, Ids.FORM),
                    metadata)
                    .onSave((f, changedValues) -> presenter.saveStore(store, changedValues))
                    .prepareReset(f -> presenter.resetStore(store, f))
                    .build();
            storeForms.put(store, form);
            storeTabs.add(Ids.build(cacheType.baseId, store.baseId, ATTRIBUTES, Ids.TAB),
                    resources.constants().attributes(), form.element());

            WriteElement writeElement = new WriteElement(cacheType, store, metadataRegistry, resources);
            storeTabs.add(Ids.build(cacheType.baseId, store.baseId, WRITE, Ids.TAB), Names.WRITE_BEHAVIOUR,
                    writeElement.element());
            writeElements.put(store, writeElement);

            if (store.tables != null) {
                for (Table table : store.tables) {
                    Form<ModelNode> tableForm = tableForm(cacheType, store, table, metadataRegistry);
                    storeTabs.add(Ids.build(cacheType.baseId, store.baseId, table.baseId, Ids.TAB), table.type,
                            tableForm.element());
                    tableForms.put(new StoreTable(store, table), tableForm);
                }
            }
        }

        root = section()
                .add(headerForm = div().css(CSS.headerForm)
                        .add(label()
                                .apply(l -> l.htmlFor = selectStoreId)
                                .textContent(resources.constants().switchStore()))
                        .add(selectStore).element())
                .add(h(1).textContent(Names.STORE_RESOURCE)
                        .add(currentStore = span().element()))
                .add(p().textContent(resources.constants().cacheStore()))
                .add(emptyState)
                .addAll(tabs.values().stream().map(Tabs::element).collect(toList())).element();

        Elements.setVisible(emptyState.element(), false);
        Elements.setVisible(headerForm, false);
        tabs.values().forEach(t -> Elements.setVisible(t.element(), false));
    }

    private HTMLSelectElement storeSelect() {
        HTMLSelectElement select = select().css(selectpicker)
                .apply(s -> {
                    s.multiple = false;
                    s.size = 1;
                }).element();

        for (Store store : Store.values()) {
            select.appendChild(option()
                    .apply(o -> {
                        o.value = store.resource;
                        o.text = store.type;
                    }).element());
        }
        return select;
    }

    private Form<ModelNode> tableForm(CacheType cacheType, Store store, Table table,
            MetadataRegistry metadataRegistry) {
        AddressTemplate template = cacheType.template
                .append(STORE + "=" + store.resource)
                .append(TABLE + "=" + table.resource);
        Metadata metadata = metadataRegistry.lookup(template);

        String id = Ids.build(cacheType.baseId, store.baseId, table.baseId, Ids.FORM);
        return new ModelNodeForm.Builder<>(id, metadata)
                .include(PREFIX)
                .customFormItem(ID_COLUMN, ad -> new ColumnFormItem(ID_COLUMN))
                .customFormItem(DATA_COLUMN, ad -> new ColumnFormItem(DATA_COLUMN))
                .customFormItem(TIMESTAMP_COLUMN, ad -> new ColumnFormItem(TIMESTAMP_COLUMN))
                .include(BATCH_SIZE, FETCH_SIZE)
                .unsorted()
                .onSave((f, changedValues) -> presenter.saveTable(table, changedValues))
                .prepareReset(f -> presenter.resetTable(table, f))
                .build();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $(HASH + selectStoreId).selectpicker(options);
        SelectBoxBridge.Single.element(selectStore).onChange((event, index) -> {
            String value = SelectBoxBridge.Single.element(selectStore).getValue();
            Store store = Store.fromResource(value);
            presenter.switchStore(store);
        });
        // selectStore.previousElementSibling.classList.add(dropdownMenuRight);
        autoWidth(emptyState.element());
        autoWidth(headerForm);
        storeForms.values().forEach(Attachable::attach);
        writeElements.values().forEach(Attachable::attach);
        tableForms.values().forEach(Attachable::attach);
    }

    private void autoWidth(HTMLElement element) {
        HTMLElement select = (HTMLElement) element.querySelector("." + btnGroup + "." + bootstrapSelect);
        if (select != null) {
            select.style.width = width("auto"); // NON-NLS
        }
    }

    @Override
    public void detach() {
        tableForms.values().forEach(Attachable::detach);
        writeElements.values().forEach(Attachable::detach);
        storeForms.values().forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(CachePresenter<?, ?> presenter) {
        this.presenter = presenter;
        writeElements.values().forEach(we -> we.setPresenter(presenter));
    }

    void update(List<Property> stores) {
        if (stores.isEmpty() || NONE.equals(stores.get(0).getName())) {
            emptyStateMode();

        } else {
            Store store = Store.fromResource(stores.get(0).getName());
            if (store != null) {
                currentStore.textContent = ": " + store.type;
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
        Elements.setVisible(emptyState.element(), true);
        Elements.setVisible(headerForm, false);
        tabs.values().forEach(t -> Elements.setVisible(t.element(), false));
    }

    private void formMode(Store store) {
        Elements.setVisible(emptyState.element(), false);
        Elements.setVisible(headerForm, true);
        tabs.forEach((s, t) -> Elements.setVisible(t.element(), s == store));
    }

    private static final class StoreTable {

        final Store store;
        final Table table;

        private StoreTable(Store store, Table table) {
            this.store = store;
            this.table = table;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StoreTable)) {
                return false;
            }

            StoreTable that = (StoreTable) o;
            // noinspection SimplifiableIfStatement
            if (store != that.store) {
                return false;
            }
            return table == that.table;
        }

        @Override
        public int hashCode() {
            int result = store.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }
}
