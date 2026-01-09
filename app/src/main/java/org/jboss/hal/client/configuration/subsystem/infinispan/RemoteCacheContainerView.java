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

import javax.inject.Inject;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.joining;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.COMPONENT_CONNECTION_POOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.COMPONENT_SECURITY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CACHE_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.REMOTE_CLUSTER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.THREAD_POOL_ASYNC_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTE_CLUSTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDINGS;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

public class RemoteCacheContainerView extends HalViewImpl implements RemoteCacheContainerPresenter.MyView {

    private final Form<ModelNode> configurationForm;
    private final Table<NamedNode> remoteClusterTable;
    private final Form<NamedNode> remoteClusterForm;
    private final Form<ModelNode> connectionPoolForm;
    private final Form<ModelNode> threadPoolForm;
    private final Form<ModelNode> securityForm;
    private final VerticalNavigation navigation;
    private RemoteCacheContainerPresenter presenter;

    @Inject
    public RemoteCacheContainerView(MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory,
            Resources resources) {
        Metadata metadata = metadataRegistry.lookup(REMOTE_CACHE_CONTAINER_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.REMOTE_CACHE_CONTAINER_CONFIGURATION_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveRemoteCacheContainer(changedValues))
                .prepareReset(form -> presenter.resetRemoteCacheContainer(form))
                .build();
        HTMLElement configurationSection = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(configurationForm).element();

        metadata = metadataRegistry.lookup(REMOTE_CLUSTER_TEMPLATE);
        remoteClusterTable = new ModelNodeTable.Builder<NamedNode>(Ids.REMOTE_CLUSTER_TABLE, metadata)
                .button(tableButtonFactory.add(REMOTE_CLUSTER_TEMPLATE, table -> presenter.addRemoteCluster()))
                .button(tableButtonFactory.remove(REMOTE_CLUSTER_TEMPLATE,
                        table -> presenter.removeRemoteCluster(table.selectedRow().getName())))
                .nameColumn()
                .column(Names.SOCKET_BINDINGS, (cell, type, row, meta) -> {
                    ModelNode socketBindings = row.get(SOCKET_BINDINGS);
                    if (socketBindings.isDefined()) {
                        return SafeHtmlUtils.fromString(
                                socketBindings.asList().stream().map(ModelNode::asString).collect(joining(", ")))
                                .asString();
                    }
                    return "";
                })
                .build();
        remoteClusterForm = new ModelNodeForm.Builder<NamedNode>(Ids.REMOTE_CLUSTER_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveRemoteCluster(form.getModel().getName(), changedValues))
                .build();
        HTMLElement remoteClusterSection = section()
                .add(h(1).textContent(Names.REMOTE_CLUSTER))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(remoteClusterTable)
                .add(remoteClusterForm).element();

        metadata = metadataRegistry.lookup(COMPONENT_CONNECTION_POOL_TEMPLATE);
        connectionPoolForm = new ModelNodeForm.Builder<>(Ids.CONNECTION_POOL_FORM, metadata)
                .singleton(() -> presenter.pingConnectionPool(), () -> presenter.addConnectionPool())
                .onSave((form, changedValues) -> presenter.saveConnectionPool(changedValues))
                .prepareReset(form -> presenter.resetConnectionPool(form))
                .prepareRemove(form -> presenter.removeConnectionPool())
                .build();
        HTMLElement connectionPoolSection = section()
                .add(h(1).textContent(Names.CONNECTION_POOL))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(connectionPoolForm).element();

        metadata = metadataRegistry.lookup(THREAD_POOL_ASYNC_TEMPLATE);
        threadPoolForm = new ModelNodeForm.Builder<>(Ids.THREAD_POOL_FORM, metadata)
                .singleton(() -> presenter.pingThreadPool(), () -> presenter.addThreadPool())
                .onSave((form, changedValues) -> presenter.saveThreadPool(changedValues))
                .prepareReset(form -> presenter.resetThreadPool(form))
                .prepareRemove(form -> presenter.removeThreadPool())
                .build();
        HTMLElement threadPoolSection = section()
                .add(h(1).textContent(Names.THREAD_POOL))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(threadPoolForm).element();

        metadata = metadataRegistry.lookup(COMPONENT_SECURITY_TEMPLATE);
        securityForm = new ModelNodeForm.Builder<>(Ids.SECURITY_FORM, metadata)
                .singleton(() -> presenter.pingSecurity(), () -> presenter.addSecurity())
                .onSave((form, changedValues) -> presenter.saveSecurity(changedValues))
                .prepareReset(form -> presenter.resetSecurity(form))
                .prepareRemove(form -> presenter.removeSecurity())
                .build();
        HTMLElement securitySection = section()
                .add(h(1).textContent(Names.SECURITY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(securityForm).element();

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.REMOTE_CACHE_CONTAINER_ITEM, Names.CONFIGURATION, pfIcon("settings"),
                configurationSection);
        navigation.addPrimary(Ids.REMOTE_CLUSTER_ITEM, Names.REMOTE_CLUSTER, pfIcon("cluster"),
                remoteClusterSection);
        navigation.addPrimary(Ids.CONNECTION_POOL_ITEM, Names.CONNECTION_POOL, pfIcon("connected"),
                connectionPoolSection);
        navigation.addPrimary(Ids.THREAD_POOL_ITEM, Names.THREAD_POOL, pfIcon("resource-pool"),
                threadPoolSection);
        navigation.addPrimary(Ids.SECURITY_ITEM, Names.SECURITY, fontAwesome("shield"),
                securitySection);

        registerAttachable(navigation);
        registerAttachable(configurationForm);
        registerAttachable(remoteClusterTable, remoteClusterForm);
        registerAttachable(connectionPoolForm, threadPoolForm, securityForm);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void attach() {
        super.attach();
        remoteClusterTable.bindForm(remoteClusterForm);
    }

    @Override
    public void setPresenter(RemoteCacheContainerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(CacheContainer cacheContainer) {
        configurationForm.view(cacheContainer);
        remoteClusterForm.clear();
        remoteClusterTable.update(asNamedNodes(failSafePropertyList(cacheContainer, REMOTE_CLUSTER)));
        connectionPoolForm.view(failSafeGet(cacheContainer, "component/connection-pool"));
        threadPoolForm.view(failSafeGet(cacheContainer, "thread-pool/async"));
        securityForm.view(failSafeGet(cacheContainer, "component/security"));
    }
}
