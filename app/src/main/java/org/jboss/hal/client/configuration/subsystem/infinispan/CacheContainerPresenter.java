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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
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
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class CacheContainerPresenter
        extends ApplicationFinderPresenter<CacheContainerPresenter.MyView, CacheContainerPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires({CACHE_CONTAINER_ADDRESS, DISTRIBUTED_CACHE_ADDRESS, INVALIDATION_CACHE_ADDRESS, LOCAL_CACHE_ADDRESS,
            REPLICATED_CACHE_ADDRESS, THREAD_POOL_ADDRESS, TRANSPORT_JGROUPS_ADDRESS})
    @NameToken(NameTokens.CACHE_CONTAINER)
    public interface MyProxy extends ProxyPlace<CacheContainerPresenter> {}

    public interface MyView extends HalView, HasPresenter<CacheContainerPresenter> {
        void update(CacheContainer cacheContainer, boolean jgroups);
        void updateCacheBackups(Cache cache, List<NamedNode> backups);
        void updateCacheStore(Cache cache, List<Property> stores);
    }
    // @formatter:on


    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String cacheContainer;
    private Cache cacheType;
    private String cacheName;
    private Store store;

    @Inject
    public CacheContainerPresenter(final EventBus eventBus,
            final CacheContainerPresenter.MyView view,
            final CacheContainerPresenter.MyProxy myProxy,
            final Finder finder,
            final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> cacheContainer);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        cacheContainer = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(INFINISPAN)
                .append(Ids.CACHE_CONTAINER, Ids.cacheContainer(cacheContainer),
                        Names.CACHE_CONTAINER, cacheContainer);
    }

    @Override
    protected void reload() {
        ResourceAddress profileAddress = new ResourceAddress();
        if (statementContext.selectedProfile() != null) {
            profileAddress.add(PROFILE, statementContext.selectedProfile());
        }
        Operation subsystems = new Operation.Builder(profileAddress, READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SUBSYSTEM)
                .build();

        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_ALIASES, true)
                .param(RECURSIVE, true)
                .build();

        dispatcher.execute(new Composite(subsystems, operation), (CompositeResult result) -> {
            boolean jgroups = result.step(0).get(RESULT).asList().stream()
                    .map(ModelNode::asString)
                    .anyMatch(JGROUPS::equals);

            CacheContainer cc = new CacheContainer(this.cacheContainer, result.step(1).get(RESULT));
            getView().update(cc, jgroups);
        });
    }


    // ------------------------------------------------------ cache container

    void saveCacheContainer(Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE);
        crud.save(Names.CACHE_CONTAINER, cacheContainer, address, changedValues, metadata, this::reload);
    }

    void resetCacheContainer(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE);
        crud.reset(Names.CACHE_CONTAINER, cacheContainer, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }


    // ------------------------------------------------------ cache

    void addCache(final Cache cache) {
        Metadata metadata = metadataRegistry.lookup(cache.template);
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cache.baseId, Ids.ADD),
                resources.messages().addResourceTitle(cache.type), metadata,
                (name, model) -> crud.add(cache.type, name, cacheAddress(cache, name), model, (n, a) -> reload()));
        dialog.show();
    }

    void saveCache(final Cache cache, final String name, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(cache.template);
        crud.save(cache.type, name, cacheAddress(cache, name), changedValues, metadata, this::reload);
    }

    void resetCache(final Cache cache, final String name, final Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(cache.template);
        crud.reset(cache.type, name, cacheAddress(cache, name), form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeCache(final Cache cache, final String name) {
        crud.remove(cache.type, name, cacheAddress(cache, name), this::reload);
    }

    void selectCache(final Cache cacheType, final String cacheName) {
        this.cacheType = cacheType;
        this.cacheName = cacheName;
    }

    String cacheSegment() {
        return cacheType.type + ": " + cacheName;
    }

    private ResourceAddress cacheAddress(final Cache cache, final String name) {
        // cannot use this.cacheType and this.cacheName here, since they might be null
        return SELECTED_CACHE_CONTAINER_TEMPLATE.append(cache.resource() + "=" + name).resolve(statementContext);
    }


    // ------------------------------------------------------ cache component

    void addCacheComponent(final Component component) {
        crud.addSingleton(component.type, cacheComponentAddress(component), null, address -> reload());
    }

    Operation readCacheComponent(final Component component) {
        if (cacheType != null && cacheName != null) {
            return new Operation.Builder(cacheComponentAddress(component), READ_RESOURCE_OPERATION).build();
        } else {
            return null;
        }
    }

    void saveCacheComponent(final Component component, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(COMPONENT + "=" + component.resource));
        crud.saveSingleton(component.type, cacheComponentAddress(component), changedValues, metadata, this::reload);
    }

    void resetCacheComponent(final Component component, final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(COMPONENT + "=" + component.resource));
        crud.resetSingleton(component.type, cacheComponentAddress(component), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeCacheComponent(final Component component, final Form<ModelNode> form) {
        crud.removeSingleton(component.type, cacheComponentAddress(component), new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    private ResourceAddress cacheComponentAddress(final Component component) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + component.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ cache backup

    void addCacheBackup() {
        Metadata metadata = metadataRegistry.lookup(cacheType.template
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=*"));
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cacheType.baseId, BACKUPS, Ids.ADD),
                resources.messages().addResourceTitle(Names.BACKUP), metadata,
                (name, model) -> {
                    ResourceAddress address = cacheBackupAddress(name);
                    crud.add(Names.BACKUP, name, address, model, (n, a) -> showCacheBackup());
                });
        dialog.show();
    }

    void showCacheBackup() {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + BACKUPS)
                .resolve(statementContext);
        crud.readChildren(address, BACKUP,
                children -> getView().updateCacheBackups(cacheType, asNamedNodes(children)));
    }

    void saveCacheBackup(final String name, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=*"));
        crud.save(Names.BACKUP, name, cacheBackupAddress(name), changedValues, metadata, this::showCacheBackup);
    }

    void resetCacheBackup(final String name, final Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=*"));
        crud.reset(Names.BACKUP, name, cacheBackupAddress(name), form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                showCacheBackup();
            }
        });
    }

    void removeCacheBackup(final String name) {
        crud.remove(Names.BACKUP, name, cacheBackupAddress(name), this::showCacheBackup);
    }

    private ResourceAddress cacheBackupAddress(final String name) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=" + name)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ cache store

    void addCacheStore(final Store store) {
        if (store.addWithDialog) {
            Metadata metadata = metadataRegistry.lookup(cacheType.template.append(STORE + "=" + store.resource));
            String id = Ids.build(cacheType.baseId, store.baseId, Ids.ADD);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata) // custom form w/o unbound name item
                    .fromRequestProperties()
                    .requiredOnly()
                    .build();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(store.type), form,
                    (name, model) -> {
                        Operation operation = new Operation.Builder(cacheStoreAddress(store), ADD)
                                .header(ALLOW_RESOURCE_SERVICE_RESTART, true)
                                .payload(model)
                                .build();
                        crud.addSingleton(store.type, operation, address -> showCacheStore());
                    });
            dialog.show();

        } else {
            Operation operation = new Operation.Builder(cacheStoreAddress(store), ADD)
                    .header(ALLOW_RESOURCE_SERVICE_RESTART, true)
                    .build();
            crud.addSingleton(store.type, operation, address -> showCacheStore());
        }
    }

    void showCacheStore() {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .resolve(statementContext);
        crud.readChildren(address, STORE, 2, children -> {
            if (children.isEmpty()) {
                store = null;
            } else {
                if (children.size() > 1) {
                    MessageEvent.fire(getEventBus(), Message.warning(resources.messages().moreThanOneCacheStore(),
                            resources.messages().moreThanOneCacheStoreDetails()));
                }
                store = Store.fromResource(children.get(0).getName());
            }
            getView().updateCacheStore(cacheType, children);
        });
    }

    void saveCacheStore(final Store store, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template.append(STORE + "=" + store.resource));
        crud.saveSingleton(store.type, cacheStoreAddress(store), changedValues, metadata, this::showCacheStore);
    }

    void resetCacheStore(final Store store, final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(cacheType.template.append(STORE + "=" + store.resource));
        crud.resetSingleton(store.type, cacheStoreAddress(store), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                showCacheStore();
            }
        });
    }

    void switchStore(final Store newStore) {
        if (newStore != null && newStore != this.store) {
            List<Operation> operations = new ArrayList<>();
            if (this.store != null) {
                operations.add(new Operation.Builder(cacheStoreAddress(this.store), REMOVE).build());
            }

            if (newStore.addWithDialog) {
                Metadata metadata = metadataRegistry.lookup(cacheType.template.append(STORE + "=" + newStore.resource));
                String id = Ids.build(cacheType.baseId, newStore.baseId, Ids.ADD);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata) // custom form w/o unbound name item
                        .fromRequestProperties()
                        .requiredOnly()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(newStore.type),
                        form, (name, model) -> {
                    operations.add(new Operation.Builder(cacheStoreAddress(newStore), ADD)
                            .payload(model)
                            .build());
                    Composite composite = new Composite(operations)
                            .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
                    dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSingleResourceSuccess(newStore.type)));
                        showCacheStore();
                    });
                });
                dialog.show();

            } else {
                operations.add(new Operation.Builder(cacheStoreAddress(newStore), ADD).build());
                Composite composite = new Composite(operations)
                        .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
                dispatcher.execute(composite, (CompositeResult result) -> {
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().addSingleResourceSuccess(newStore.type)));
                    showCacheStore();
                });
            }
        }
    }

    String storeSegment() {
        StringBuilder builder = new StringBuilder().append(Names.STORE);
        if (store != null) {
            builder.append(": ").append(store.type);
        }
        return builder.toString();
    }

    private ResourceAddress cacheStoreAddress(final Store store) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(STORE + "=" + store.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ write through / behind

    void addWrite(final Write write) {
        crud.addSingleton(write.type, writeAddress(write), null, address -> showCacheStore());
    }

    void saveWrite(final Write write, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(STORE + "=" + store.resource)
                .append(WRITE + "=" + write.resource));
        crud.saveSingleton(Names.WRITE_BEHIND, writeAddress(write), changedValues, metadata, this::showCacheStore);
    }

    void resetWrite(final Write write, final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(STORE + "=" + store.resource)
                .append(WRITE + "=" + write.resource));
        crud.resetSingleton(Names.WRITE_BEHIND, writeAddress(write), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                showCacheStore();
            }
        });
    }

    void switchWrite(final Write currentWrite, final Write newWrite) {
        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(writeAddress(currentWrite), REMOVE).build());
        operations.add(new Operation.Builder(writeAddress(newWrite), ADD).build());
        dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().addSingleResourceSuccess(newWrite.type)));
            showCacheStore();
        });
    }

    private ResourceAddress writeAddress(final Write write) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(STORE + "=" + store.resource)
                .append(WRITE + "=" + write.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ tables of jdbc stores

    void saveStoreTable(final Table table, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(STORE + "=" + store.resource)
                .append(TABLE + "=" + table.resource));
        crud.saveSingleton(table.type, storeTableAddress(store, table), changedValues, metadata, this::showCacheStore);
    }

    void resetStoreTable(final Table table, final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=*")
                .append(STORE + "=" + store.resource)
                .append(TABLE + "=" + table.resource));
        crud.resetSingleton(table.type, storeTableAddress(store, table), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        showCacheStore();
                    }
                });
    }

    private ResourceAddress storeTableAddress(final Store store, final Table table) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(STORE + "=" + store.resource)
                .append(TABLE + "=" + table.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ thread pool

    void addThreadPool(final ThreadPool threadPool) {
        crud.addSingleton(threadPool.type, threadPoolAddress(threadPool), null, address -> reload());
    }

    Operation readThreadPool(final ThreadPool threadPool) {
        return new Operation.Builder(threadPoolAddress(threadPool), READ_RESOURCE_OPERATION).build();
    }

    void saveThreadPool(final ThreadPool threadPool, final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + "=" + threadPool.resource));
        crud.saveSingleton(threadPool.type, threadPoolAddress(threadPool), changedValues, metadata, this::reload);
    }

    void resetThreadPool(final ThreadPool threadPool, final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + "=" + threadPool.resource));
        crud.resetSingleton(threadPool.type, threadPoolAddress(threadPool), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeThreadPool(final ThreadPool threadPool, final Form<ModelNode> form) {
        crud.removeSingleton(threadPool.type, threadPoolAddress(threadPool), new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    private ResourceAddress threadPoolAddress(final ThreadPool threadPool) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + "=" + threadPool.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ transport - jgroups

    void addJgroups() {
        crud.addSingleton(Names.JGROUPS, jgroupsAddress(), null, address -> reload());
    }

    void saveJgroups(final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE.append(TRANSPORT + "=" + JGROUPS));
        crud.saveSingleton(Names.JGROUPS, jgroupsAddress(), changedValues, metadata, this::reload);
    }

    void resetJgroups(final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE.append(TRANSPORT + "=" + JGROUPS));
        crud.resetSingleton(Names.JGROUPS, jgroupsAddress(), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    private ResourceAddress jgroupsAddress() {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(TRANSPORT + "=" + JGROUPS)
                .resolve(statementContext);
    }
}
