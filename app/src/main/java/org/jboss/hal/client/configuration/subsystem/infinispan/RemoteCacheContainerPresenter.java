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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
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
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.COMPONENT_CONNECTION_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.COMPONENT_SECURITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CLUSTER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.THREAD_POOL_ASYNC_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INFINISPAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;

public class RemoteCacheContainerPresenter
        extends ApplicationFinderPresenter<RemoteCacheContainerPresenter.MyView, RemoteCacheContainerPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";
    private static final String EQ_WILDCARD = "=*";

    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String remoteCacheContainer;
    private CacheType cacheTypeType;
    private String cacheName;
    private Memory memory;
    private Store store;

    @Inject
    public RemoteCacheContainerPresenter(EventBus eventBus,
            RemoteCacheContainerPresenter.MyView view,
            RemoteCacheContainerPresenter.MyProxy myProxy,
            Finder finder,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> remoteCacheContainer);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        remoteCacheContainer = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(INFINISPAN)
                .append(Ids.CACHE_CONTAINER, Ids.remoteCacheContainer(remoteCacheContainer),
                        Names.REMOTE_CACHE_CONTAINER, remoteCacheContainer);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).param(RECURSIVE, true).build();
        dispatcher.execute(operation,
                result -> getView().update(new CacheContainer(remoteCacheContainer, true, result)));
    }

    // ------------------------------------------------------ remote cache container

    void saveRemoteCacheContainer(Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(REMOTE_CACHE_CONTAINER_TEMPLATE);
        crud.save(Names.REMOTE_CACHE_CONTAINER, remoteCacheContainer, address, changedValues, metadata, this::reload);
    }

    void resetRemoteCacheContainer(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(REMOTE_CACHE_CONTAINER_TEMPLATE);
        crud.reset(Names.REMOTE_CACHE_CONTAINER, remoteCacheContainer, address, form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    // ------------------------------------------------------ remote cluster

    void addRemoteCluster() {
        Metadata metadata = metadataRegistry.lookup(REMOTE_CLUSTER_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.REMOTE_CLUSTER_ADD,
                resources.messages().addResourceTitle(Names.REMOTE_CLUSTER), metadata,
                (name, model) -> crud.add(Names.REMOTE_CLUSTER, name, remoteClusterAddress(name), model,
                        (n, a) -> reload()));
        dialog.show();
    }

    void saveRemoteCluster(String name, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(REMOTE_CLUSTER_TEMPLATE);
        crud.save(Names.REMOTE_CLUSTER, name, remoteClusterAddress(name), changedValues, metadata, this::reload);
    }

    void removeRemoteCluster(String name) {
        crud.remove(Names.REMOTE_CLUSTER, name, remoteClusterAddress(name), this::reload);
    }

    private ResourceAddress remoteClusterAddress(String name) {
        return SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE
                .append("remote-cluster=*")
                .resolve(statementContext, name);
    }

    // ------------------------------------------------------ connection pool

    void addConnectionPool() {
        crud.addSingleton(Names.CONNECTION_POOL, connectionPoolAddress(), null, address -> reload());
    }

    Operation pingConnectionPool() {
        return new Operation.Builder(connectionPoolAddress(), READ_RESOURCE_OPERATION).build();
    }

    void saveConnectionPool(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(COMPONENT_CONNECTION_POOL_TEMPLATE);
        crud.saveSingleton(Names.CONNECTION_POOL, connectionPoolAddress(), changedValues, metadata, this::reload);
    }

    void resetConnectionPool(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(COMPONENT_CONNECTION_POOL_TEMPLATE);
        crud.resetSingleton(Names.CONNECTION_POOL, connectionPoolAddress(), form, metadata, this::reload);
    }

    void removeConnectionPool() {
        crud.removeSingleton(Names.CONNECTION_POOL, connectionPoolAddress(), this::reload);
    }

    private ResourceAddress connectionPoolAddress() {
        return SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE
                .append("component=connection-pool")
                .resolve(statementContext);
    }

    // ------------------------------------------------------ security

    void addSecurity() {
        crud.addSingleton(Names.SECURITY, securityAddress(), null, address -> reload());
    }

    Operation pingSecurity() {
        return new Operation.Builder(securityAddress(), READ_RESOURCE_OPERATION).build();
    }

    void saveSecurity(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(COMPONENT_SECURITY_TEMPLATE);
        crud.saveSingleton(Names.SECURITY, securityAddress(), changedValues, metadata, this::reload);
    }

    void resetSecurity(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(COMPONENT_SECURITY_TEMPLATE);
        crud.resetSingleton(Names.SECURITY, securityAddress(), form, metadata, this::reload);
    }

    void removeSecurity() {
        crud.removeSingleton(Names.SECURITY, securityAddress(), this::reload);
    }

    private ResourceAddress securityAddress() {
        return SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE
                .append("component=security")
                .resolve(statementContext);
    }

    // ------------------------------------------------------ thread pool

    void addThreadPool() {
        crud.addSingleton(Names.THREAD_POOL, threadPoolAddress(), null, address -> reload());
    }

    Operation pingThreadPool() {
        return new Operation.Builder(threadPoolAddress(), READ_RESOURCE_OPERATION).build();
    }

    void saveThreadPool(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(THREAD_POOL_ASYNC_TEMPLATE);
        crud.saveSingleton(Names.THREAD_POOL, threadPoolAddress(), changedValues, metadata, this::reload);
    }

    void resetThreadPool(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(THREAD_POOL_ASYNC_TEMPLATE);
        crud.resetSingleton(Names.THREAD_POOL, threadPoolAddress(), form, metadata, this::reload);
    }

    void removeThreadPool() {
        crud.removeSingleton(Names.THREAD_POOL, threadPoolAddress(), this::reload);
    }

    private ResourceAddress threadPoolAddress() {
        return SELECTED_REMOTE_CACHE_CONTAINER_TEMPLATE
                .append("thread-pool=async")
                .resolve(statementContext);
    }

    // ------------------------------------------------------ inner classes

    // @formatter:off
    @ProxyCodeSplit
    @Requires(REMOTE_CACHE_CONTAINER_ADDRESS)
    @NameToken(NameTokens.REMOTE_CACHE_CONTAINER)
    public interface MyProxy extends ProxyPlace<RemoteCacheContainerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<RemoteCacheContainerPresenter> {
        void update(CacheContainer cacheContainer);
    }
    // @formatter:on
}
