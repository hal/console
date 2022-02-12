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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

abstract class CachePresenter<V extends CacheView<?>, Proxy_ extends ProxyPlace<?>>
        extends ApplicationFinderPresenter<V, Proxy_>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";
    private static final String EQ_WILDCARD = "=*";

    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private final CacheType cacheType;
    private String cacheContainer;
    private String cache;
    private Memory memory;
    private Store store;

    CachePresenter(EventBus eventBus,
            V view,
            Proxy_ proxy_,
            Finder finder,
            FinderPathFactory finderPathFactory,
            CrudOperations crud,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources,
            CacheType cacheType) {
        super(eventBus, view, proxy_, finder);
        this.finderPathFactory = finderPathFactory;
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
        this.cacheType = cacheType;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        cacheContainer = request.getParameter(CACHE_CONTAINER, null);
        cache = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return cacheType.template.resolve(statementContext, cacheContainer, cache);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(INFINISPAN)
                .append(Ids.CACHE_CONTAINER, Ids.cacheContainer(cacheContainer), Names.CACHE_CONTAINER, cacheContainer)
                .append(Ids.CACHE, Ids.build(cacheType.baseId, cache), Names.CACHE, cache);
    }

    @Override
    protected void reload() {
        crud.readRecursive(resourceAddress(),
                result -> {
                    List<Property> properties = failSafePropertyList(result, MEMORY);
                    for (Property property : properties) {
                        if (property.getValue().isDefined()) {
                            memory = Memory.fromResource(property.getName());
                            break;
                        }
                    }
                    properties = failSafePropertyList(result, STORE);
                    for (Property property : properties) {
                        if (property.getValue().isDefined()) {
                            store = Store.fromResource(property.getName());
                            break;
                        }
                    }
                    getView().update(new Cache(cache, cacheType, result));
                });
    }

    // ------------------------------------------------------ cache

    void saveCache(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template);
        crud.save(cacheType.type, cache, resourceAddress(), changedValues, metadata, this::reload);
    }

    void resetCache(Form<Cache> form) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template);
        crud.reset(cacheType.type, cache, resourceAddress(), form, metadata,
                new FinishReset<Cache>(form) {
                    @Override
                    public void afterReset(Form<Cache> f) {
                        reload();
                    }
                });
    }

    // ------------------------------------------------------ component

    void addComponent(Component component) {
        crud.addSingleton(component.type, componentAddress(component), null, address -> reload());
    }

    Operation readComponent(Component component) {
        return new Operation.Builder(componentAddress(component), READ_RESOURCE_OPERATION).build();
    }

    void saveComponent(Component component, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(componentTemplate(component));
        crud.saveSingleton(component.type, componentAddress(component), changedValues, metadata, this::reload);
    }

    void resetComponent(Component component, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(componentTemplate(component));
        crud.resetSingleton(component.type, componentAddress(component), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeComponent(Component component, Form<ModelNode> form) {
        crud.removeSingleton(component.type, componentAddress(component), new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }

    private AddressTemplate componentTemplate(Component component) {
        return cacheType.template.append(COMPONENT + EQUALS + component.resource);
    }

    private ResourceAddress componentAddress(Component component) {
        return resourceAddress().add(COMPONENT, component.resource);
    }

    // ------------------------------------------------------ memory

    void saveMemory(Memory memory, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(memoryTemplate(memory));
        crud.saveSingleton(memory.type, memoryAddress(memory), changedValues, metadata, this::reload);
    }

    void resetMemory(Memory memory, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(memoryTemplate(memory));
        crud.resetSingleton(memory.type, memoryAddress(memory), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> f) {
                        reload();
                    }
                });
    }

    void switchMemory(Memory newMemory) {
        if (newMemory != null && newMemory != this.memory) {
            List<Operation> operations = new ArrayList<>();
            if (this.memory != null) {
                operations.add(new Operation.Builder(memoryAddress(this.memory), REMOVE).build());
            }
            operations.add(new Operation.Builder(memoryAddress(newMemory), ADD).build());
            Composite composite = new Composite(operations)
                    .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
            dispatcher.execute(composite, (CompositeResult result) -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().addSingleResourceSuccess(newMemory.type)));
                this.memory = newMemory;
                reload();
            });
        }
    }

    private AddressTemplate memoryTemplate(Memory memory) {
        return cacheType.template.append(MEMORY + EQUALS + memory.resource);
    }

    private ResourceAddress memoryAddress(Memory memory) {
        return resourceAddress().add(MEMORY, memory.resource);
    }

    // ------------------------------------------------------ store

    void addStore(Store store) {
        if (store.addWithDialog) {
            Metadata metadata = metadataRegistry.lookup(storeTemplate(store));
            String id = Ids.build(cacheType.baseId, store.baseId, Ids.ADD);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata) // custom form w/o unbound name item
                    .fromRequestProperties()
                    .requiredOnly()
                    .build();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(store.type), form,
                    (name, model) -> {
                        Operation operation = new Operation.Builder(storeAddress(store), ADD)
                                .header(ALLOW_RESOURCE_SERVICE_RESTART, true)
                                .payload(model)
                                .build();
                        crud.addSingleton(store.type, operation, address -> reload());
                    });
            dialog.show();

        } else {
            Operation operation = new Operation.Builder(storeAddress(store), ADD)
                    .header(ALLOW_RESOURCE_SERVICE_RESTART, true)
                    .build();
            crud.addSingleton(store.type, operation, address -> reload());
        }
    }

    void saveStore(Store store, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(storeTemplate(store));
        crud.saveSingleton(store.type, storeAddress(store), changedValues, metadata, this::reload);
    }

    void resetStore(Store store, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(storeTemplate(store));
        crud.resetSingleton(store.type, storeAddress(store), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> f) {
                reload();
            }
        });
    }

    void switchStore(Store newStore) {
        if (newStore != null && newStore != this.store) {
            List<Operation> operations = new ArrayList<>();
            if (this.store != null) {
                operations.add(new Operation.Builder(storeAddress(this.store), REMOVE).build());
            }

            if (newStore.addWithDialog) {
                Metadata metadata = metadataRegistry.lookup(
                        cacheType.template.append(STORE + EQUALS + newStore.resource));
                String id = Ids.build(cacheType.baseId, newStore.baseId, Ids.ADD);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata) // custom form w/o unbound name item
                        .fromRequestProperties()
                        .requiredOnly()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(newStore.type),
                        form, (name, model) -> {
                            operations.add(new Operation.Builder(storeAddress(newStore), ADD)
                                    .payload(model)
                                    .build());
                            Composite composite = new Composite(operations)
                                    .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
                            dispatcher.execute(composite, (CompositeResult result) -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages().addSingleResourceSuccess(newStore.type)));
                                this.store = newStore;
                                reload();
                            });
                        });
                dialog.show();

            } else {
                operations.add(new Operation.Builder(storeAddress(newStore), ADD).build());
                Composite composite = new Composite(operations)
                        .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
                dispatcher.execute(composite, (CompositeResult result) -> {
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().addSingleResourceSuccess(newStore.type)));
                    this.store = newStore;
                    reload();
                });
            }
        }
    }

    private AddressTemplate storeTemplate(Store store) {
        return cacheType.template.append(STORE + EQUALS + store.resource);
    }

    private ResourceAddress storeAddress(Store store) {
        return resourceAddress().add(STORE, store.resource);
    }

    // ------------------------------------------------------ tables of jdbc stores

    void saveTable(Table table, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(tableTemplate(table));
        crud.saveSingleton(table.type, tableAddress(table), changedValues, metadata, this::reload);
    }

    void resetTable(Table table, Form<ModelNode> f) {
        Metadata metadata = metadataRegistry.lookup(tableTemplate(table));
        crud.resetSingleton(table.type, tableAddress(table), f, metadata,
                new FinishReset<ModelNode>(f) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    private AddressTemplate tableTemplate(Table table) {
        return storeTemplate(store).append(TABLE + EQUALS + table.resource);
    }

    private ResourceAddress tableAddress(Table table) {
        return storeAddress(store).add(TABLE, table.resource);
    }

    // ------------------------------------------------------ write through / behind

    void addWrite(Write write) {
        crud.addSingleton(write.type, writeAddress(write), null, address -> reload());
    }

    void saveWrite(Write write, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(writeTemplate(write));
        crud.saveSingleton(Names.WRITE_BEHIND, writeAddress(write), changedValues, metadata, this::reload);
    }

    void resetWrite(Write write, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(writeTemplate(write));
        crud.resetSingleton(Names.WRITE_BEHIND, writeAddress(write), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> f) {
                reload();
            }
        });
    }

    void switchWrite(Write currentWrite, Write newWrite) {
        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(writeAddress(currentWrite), REMOVE).build());
        operations.add(new Operation.Builder(writeAddress(newWrite), ADD).build());
        dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().addSingleResourceSuccess(newWrite.type)));
            reload();
        });
    }

    private AddressTemplate writeTemplate(Write write) {
        return storeTemplate(store).append(WRITE + EQUALS + write.resource);
    }

    private ResourceAddress writeAddress(Write write) {
        return storeAddress(store).add(WRITE, write.resource);
    }

    // ------------------------------------------------------ backup

    void addBackup() {
        Metadata metadata = metadataRegistry.lookup(backupTemplate());
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cacheType.baseId, BACKUPS, Ids.ADD),
                resources.messages().addResourceTitle(Names.BACKUP), metadata,
                (name, model) -> {
                    ResourceAddress address = backupAddress(name);
                    crud.add(Names.BACKUP, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveCacheBackup(String name, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(backupTemplate());
        crud.save(Names.BACKUP, name, backupAddress(name), changedValues, metadata, this::reload);
    }

    void resetBackup(String name, Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(backupTemplate());
        crud.reset(Names.BACKUP, name, backupAddress(name), form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> f) {
                reload();
            }
        });
    }

    void removeBackup(String name) {
        crud.remove(Names.BACKUP, name, backupAddress(name), this::reload);
    }

    private AddressTemplate backupTemplate() {
        return cacheType.template
                .append(COMPONENT + EQUALS + BACKUPS)
                .append(BACKUP + EQ_WILDCARD);
    }

    private ResourceAddress backupAddress(String name) {
        return resourceAddress()
                .add(COMPONENT, BACKUPS)
                .add(BACKUP, name);
    }
}
