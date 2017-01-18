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
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPONENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INFINISPAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.THREAD_POOL;

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
        void update(CacheContainer cacheContainer);
    }
    // @formatter:on


    private final MetadataRegistry metadataRegistry;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String cacheContainer;

    @Inject
    public CacheContainerPresenter(final EventBus eventBus,
            final CacheContainerPresenter.MyView view,
            final CacheContainerPresenter.MyProxy myProxy,
            final Finder finder,
            final MetadataRegistry metadataRegistry,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.metadataRegistry = metadataRegistry;
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
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        crud.readRecursive(address, result -> getView().update(new CacheContainer(cacheContainer, result)));
    }


    // ------------------------------------------------------ cache container

    void saveCacheContainer(Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        crud.save(Names.CACHE_CONTAINER, cacheContainer, address, changedValues, this::reload);
    }


    // ------------------------------------------------------ cache

    void addCache(Cache cache) {
        Metadata metadata = metadataRegistry.lookup(cache.template);
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(cache.baseId, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(cache.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE
                            .append(cache.resource() + "=" + name)
                            .resolve(statementContext);
                    crud.add(cache.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveCache(final Cache cache, final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.append(cache.resource() + "=" + name)
                .resolve(statementContext);
        crud.save(cache.type, name, address, changedValues, this::reload);
    }

    void removeCache(final Cache cache, final String name) {
        ResourceAddress address = SELECTED_CACHE_CONTAINER_TEMPLATE.append(cache.resource() + "=" + name)
                .resolve(statementContext);
        crud.remove(cache.type, name, address, this::reload);
    }


    // ------------------------------------------------------ cache component

    void addCacheComponent(final Cache cache, final String name, final Component component) {
        ResourceAddress address = cacheComponentAddress(cache, name, component);
        crud.addSingleton(component.type, address, null, (n, a) -> reload());
    }

    Operation readCacheComponent(final Cache cache, final String name, final Component component) {
        ResourceAddress address = cacheComponentAddress(cache, name, component);
        return new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
    }

    void saveCacheComponent(final Cache cache, final String name, final Component component,
            final Map<String, Object> changedValues) {
        ResourceAddress address = cacheComponentAddress(cache, name, component);
        crud.saveSingleton(component.type, address, changedValues, this::reload);
    }

    private ResourceAddress cacheComponentAddress(final Cache cache, final String name, final Component component) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(cache.resource() + "=" + name)
                .append(COMPONENT + "=" + component.resource)
                .resolve(statementContext);
    }


    // ------------------------------------------------------ thread pool

    void addThreadPool(final ThreadPool threadPool) {
        ResourceAddress address = threadPoolAddress(threadPool);
        crud.addSingleton(threadPool.type, address, null, (n, a) -> reload());
    }

    Operation readThreadPool(final ThreadPool threadPool) {
        ResourceAddress address = threadPoolAddress(threadPool);
        return new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
    }

    void saveThreadPool(final ThreadPool threadPool, final Map<String, Object> changedValues) {
        ResourceAddress address = threadPoolAddress(threadPool);
        crud.saveSingleton(threadPool.type, address, changedValues, this::reload);
    }

    private ResourceAddress threadPoolAddress(final ThreadPool threadPool) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + "=" + threadPool.resource)
                .resolve(statementContext);
    }
}
