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

import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class CacheContainerPresenter
        extends ApplicationFinderPresenter<CacheContainerPresenter.MyView, CacheContainerPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";

    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private String cacheContainer;

    @Inject
    public CacheContainerPresenter(EventBus eventBus,
            CacheContainerPresenter.MyView view,
            CacheContainerPresenter.MyProxy myProxy,
            Finder finder,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> cacheContainer);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
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
                .append(Ids.CACHE_CONTAINER, Ids.cacheContainer(cacheContainer), Names.CACHE_CONTAINER, cacheContainer);
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

            CacheContainer cc = new CacheContainer(this.cacheContainer, false, result.step(1).get(RESULT));
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
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    // ------------------------------------------------------ thread pool

    void addThreadPool(ThreadPool threadPool) {
        crud.addSingleton(threadPool.type, threadPoolAddress(threadPool), null, address -> reload());
    }

    Operation readThreadPool(ThreadPool threadPool) {
        return new Operation.Builder(threadPoolAddress(threadPool), READ_RESOURCE_OPERATION).build();
    }

    void saveThreadPool(ThreadPool threadPool, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + EQUALS + threadPool.resource));
        crud.saveSingleton(threadPool.type, threadPoolAddress(threadPool), changedValues, metadata, this::reload);
    }

    void resetThreadPool(ThreadPool threadPool, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + EQUALS + threadPool.resource));
        crud.resetSingleton(threadPool.type, threadPoolAddress(threadPool), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeThreadPool(ThreadPool threadPool, Form<ModelNode> form) {
        crud.removeSingleton(threadPool.type, threadPoolAddress(threadPool), new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }

    private ResourceAddress threadPoolAddress(ThreadPool threadPool) {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(THREAD_POOL + EQUALS + threadPool.resource)
                .resolve(statementContext);
    }

    // ------------------------------------------------------ transport - jgroups

    void addJgroups() {
        crud.addSingleton(Names.JGROUPS, jgroupsAddress(), null, address -> reload());
    }

    void saveJgroups(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE.append(TRANSPORT + EQUALS + JGROUPS));
        crud.saveSingleton(Names.JGROUPS, jgroupsAddress(), changedValues, metadata, this::reload);
    }

    void resetJgroups(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CACHE_CONTAINER_TEMPLATE.append(TRANSPORT + EQUALS + JGROUPS));
        crud.resetSingleton(Names.JGROUPS, jgroupsAddress(), form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    private ResourceAddress jgroupsAddress() {
        return SELECTED_CACHE_CONTAINER_TEMPLATE
                .append(TRANSPORT + EQUALS + JGROUPS)
                .resolve(statementContext);
    }

    // ------------------------------------------------------ inner classes

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.CACHE_CONTAINER)
    @Requires(value = { CACHE_CONTAINER_ADDRESS,
            THREAD_POOL_ASYNC_OPERATIONS,
            THREAD_POOL_EXPIRATION,
            THREAD_POOL_LISTENER,
            THREAD_POOL_PERSISTENCE,
            THREAD_POOL_REMOTE_COMMAND,
            THREAD_POOL_SITE_TRANSFER,
            THREAD_POOL_TRANSPORT,
            TRANSPORT_JGROUPS_ADDRESS }, recursive = false)
    public interface MyProxy extends ProxyPlace<CacheContainerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<CacheContainerPresenter> {
        void update(CacheContainer cacheContainer, boolean jgroups);
    }
    // @formatter:on
}
