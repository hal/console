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

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.SELECTED_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class CacheContainerPresenter
        extends ApplicationFinderPresenter<CacheContainerPresenter.MyView, CacheContainerPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(CACHE_CONTAINER_ADDRESS)
    @NameToken(NameTokens.CACHE_CONTAINER)
    public interface MyProxy extends ProxyPlace<CacheContainerPresenter> {}

    public interface MyView extends HalView, HasPresenter<CacheContainerPresenter> {
        void update(CacheContainer cacheContainer, boolean jgroups);
        void updateCacheBackups(Cache cache, List<NamedNode> backups);
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
        return finderPathFactory.subsystemPath(INFINISPAN)
                .append(Ids.CACHE_CONTAINER, Ids.cacheContainer(cacheContainer),
                        Names.CACHE_CONTAINER, cacheContainer);
    }

    @Override
    protected void reload() {
        ResourceAddress profileAddress = new ResourceAddress().add(PROFILE, statementContext.selectedProfile());
        Operation subsystems = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, profileAddress)
                .param(CHILD_TYPE, SUBSYSTEM)
                .build();

        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
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
        crud.save(Names.CACHE_CONTAINER, cacheContainer, address, changedValues, this::reload);
    }


    // ------------------------------------------------------ cache

    void addCache(final Cache cache) {
        Metadata metadata = metadataRegistry.lookup(cache.template);
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cache.baseId, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(cache.type), metadata,
                (name, model) -> crud.add(cache.type, name, cacheAddress(cache, name), model, (n, a) -> reload()));
        dialog.show();
    }

    void saveCache(final Cache cache, final String name, final Map<String, Object> changedValues) {
        crud.save(cache.type, name, cacheAddress(cache, name), changedValues, this::reload);
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
        crud.addSingleton(component.type, cacheComponentAddress(component), null, (n, a) -> reload());
    }

    Operation readCacheComponent(final Component component) {
        if (cacheType != null && cacheName != null) {
            return new Operation.Builder(READ_RESOURCE_OPERATION, cacheComponentAddress(component)).build();
        } else {
            return null;
        }
    }

    void saveCacheComponent(final Component component,
            final Map<String, Object> changedValues) {
        crud.saveSingleton(component.type, cacheComponentAddress(component), changedValues, this::reload);
    }

    private ResourceAddress cacheComponentAddress(final Component component) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + component.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ cache backups

    void addCacheBackup() {
        Metadata metadata = metadataRegistry.lookup(cacheType.template
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=*"));
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cacheType.baseId, BACKUPS, Ids.TABLE_SUFFIX),
                resources.messages().addResourceTitle(Names.BACKUP), metadata,
                (name, model) -> {
                    ResourceAddress address = cacheBackupAddress(name);
                    crud.add(Names.BACKUP, name, address, model, (n, a) -> showCacheBackups());
                });
        dialog.show();
    }

    void showCacheBackups() {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + BACKUPS)
                .resolve(statementContext);
        crud.readChildren(address, BACKUP,
                children -> getView().updateCacheBackups(cacheType, asNamedNodes(children)));
    }

    void saveCacheBackup(final String name, final Map<String, Object> changedValues) {
        crud.save(Names.BACKUP, name, cacheBackupAddress(name), changedValues, this::showCacheBackups);
    }

    void removeCacheBackup(final String name) {
        crud.remove(Names.BACKUP, name, cacheBackupAddress(name), this::showCacheBackups);
    }

    private ResourceAddress cacheBackupAddress(final String name) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cacheType.resource() + "=" + cacheName)
                .append(COMPONENT + "=" + BACKUPS)
                .append(BACKUP + "=" + name)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ thread pool

    void addThreadPool(final ThreadPool threadPool) {
        crud.addSingleton(threadPool.type, threadPoolAddress(threadPool), null, (n, a) -> reload());
    }

    Operation readThreadPool(final ThreadPool threadPool) {
        return new Operation.Builder(READ_RESOURCE_OPERATION, threadPoolAddress(threadPool)).build();
    }

    void saveThreadPool(final ThreadPool threadPool, final Map<String, Object> changedValues) {
        crud.saveSingleton(threadPool.type, threadPoolAddress(threadPool), changedValues, this::reload);
    }

    private ResourceAddress threadPoolAddress(final ThreadPool threadPool) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + "=" + threadPool.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ transport - jgroups

    void addJgroups() {
        crud.addSingleton(Names.JGROUPS, jgroupsAddress(), null, (n, a) -> reload());
    }

    void saveJgroups(final Map<String, Object> changedValues) {
        crud.saveSingleton(Names.JGROUPS, jgroupsAddress(), changedValues, this::reload);
    }

    Operation readTransportChildren() {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        return new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                .param(CHILD_TYPE, TRANSPORT)
                .build();
    }

    private ResourceAddress jgroupsAddress() {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(TRANSPORT + "=" + JGROUPS)
                .resolve(statementContext);
    }
}
