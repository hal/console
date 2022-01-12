/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class ClusteringPresenter
        extends ServerSettingsPresenter<ClusteringPresenter.MyView, ClusteringPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQ_WILDCARD = "=*";
    private final Dispatcher dispatcher;

    @Inject
    public ClusteringPresenter(
            EventBus eventBus,
            ClusteringPresenter.MyView view,
            ClusteringPresenter.MyProxy myProxy,
            Finder finder,
            MetadataRegistry metadataRegistry,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder, crud, metadataRegistry, finderPathFactory, statementContext, resources);
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER_CONFIGURATION, Ids.messagingServer(serverName),
                        Names.SERVER, serverName)
                .append(Ids.MESSAGING_SERVER_SETTINGS, Ids.MESSAGING_SERVER_CLUSTERING,
                        resources.constants().settings(), Names.CLUSTERING);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(BROADCAST_GROUP, DISCOVERY_GROUP,
                CLUSTER_CONNECTION, GROUPING_HANDLER, BRIDGE),
                result -> {
                    getView().updateBroadcastGroup(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateDiscoveryGroup(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateClusterConnection(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateGroupingHandler(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateBridge(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                });
    }

    void addClusterConnection(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .requiredOnly()
                .build();

        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + EQ_WILDCARD));
        form.getFormItem(CONNECTOR_NAME).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, templates));

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    void addBridge(ServerSubResource ssr) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD), metadata)
                .unboundFormItem(nameItem, 0)
                .fromRequestProperties()
                .include(QUEUE_NAME, DISCOVERY_GROUP, STATIC_CONNECTORS)
                .unsorted()
                .build();

        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + EQ_WILDCARD));
        form.getFormItem(DISCOVERY_GROUP).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext,
                        SELECTED_SERVER_TEMPLATE.append(DISCOVERY_GROUP + EQ_WILDCARD)));
        form.getFormItem(STATIC_CONNECTORS).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, templates));

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            name = nameItem.getValue();
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }

    ResourceAddress bridgeAddress(String bridgeName) {
        return bridgeName != null ? SELECTED_BRIDGE_TEMPLATE.resolve(statementContext, bridgeName) : null;
    }

    // Include JGROUPS_CLUSTER and SOCKET_BINDING fields in the Add form, as one of these fields is required.
    void addBroadcastGroup(ServerSubResource ssr) {
        List<AddressTemplate> templates = asList(
                SELECTED_SERVER_TEMPLATE.append(CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(IN_VM_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(HTTP_CONNECTOR + EQ_WILDCARD),
                SELECTED_SERVER_TEMPLATE.append(REMOTE_CONNECTOR + EQ_WILDCARD));

        showBroadcastOrDiscoveryGroupAddDialog(ssr,
                formBuilderAugmentor -> {
                    formBuilderAugmentor.include(CONNECTORS);
                },
                formAugmentor -> {
                    formAugmentor.getFormItem(CONNECTORS).registerSuggestHandler(
                            new ReadChildrenAutoComplete(dispatcher, statementContext, templates));
                });
    }

    // Include JGROUPS_CLUSTER and SOCKET_BINDING fields in the Add form, as one of these fields is required.
    void addDiscoveryGroup(ServerSubResource ssr) {
        showBroadcastOrDiscoveryGroupAddDialog(ssr, b -> { }, f -> { });
    }

    void showBroadcastOrDiscoveryGroupAddDialog(ServerSubResource ssr,
            Consumer<ModelNodeForm.Builder<ModelNode>> formBuilderAugmenter,
            Consumer<Form<ModelNode>> formAugmenter) {
        Metadata metadata = metadataRegistry.lookup(ssr.template);
        NameItem nameItem = new NameItem();
        ModelNodeForm.Builder<ModelNode> formBuilder = new ModelNodeForm.Builder<>(Ids.build(ssr.baseId, Ids.ADD), metadata)
                .unboundFormItem(nameItem, 0)
                .fromRequestProperties()
                .include(JGROUPS_CLUSTER, SOCKET_BINDING)
                .unsorted();
        formBuilderAugmenter.accept(formBuilder);

        Form<ModelNode> form = formBuilder.build();
        formAugmenter.accept(form);

        // validation that requires one of jgroups-cluster and socket-binding to be configured
        FormValidation<ModelNode> jgroupsOrSocketValidation = new FormValidation<ModelNode>() {
            @Override
            public ValidationResult validate(Form<ModelNode> form) {
                if (form.getFormItem(JGROUPS_CLUSTER).isEmpty() && form.getFormItem(SOCKET_BINDING).isEmpty()) {
                    return ValidationResult.invalid(resources.messages().jgroupsClusterOrSocketBindingMustBeSet());
                }
                return ValidationResult.OK;
            }
        };

        form.getFormItem(SOCKET_BINDING).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, SOCKET_BINDING_TEMPLATE));
        form.addFormValidation(jgroupsOrSocketValidation);

        new AddResourceDialog(resources.messages().addResourceTitle(ssr.type), form, (name, model) -> {
            name = nameItem.getValue();
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ssr.resource + "=" + name)
                    .resolve(statementContext);
            crud.add(ssr.type, name, address, model, (n, a) -> reload());
        }).show();
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires({BRIDGE_ADDRESS,
            BROADCAST_GROUP_ADDRESS,
            CLUSTER_CONNECTION_ADDRESS,
            DISCOVERY_GROUP_ADDRESS,
            GROUPING_HANDLER_ADDRESS})
    @NameToken(NameTokens.MESSAGING_SERVER_CLUSTERING)
    public interface MyProxy extends ProxyPlace<ClusteringPresenter> {
    }

    public interface MyView extends MbuiView<ClusteringPresenter> {
        void updateBroadcastGroup(List<NamedNode> broadcastGroups);
        void updateDiscoveryGroup(List<NamedNode> discoveryGroups);
        void updateClusterConnection(List<NamedNode> clusterConnections);
        void updateGroupingHandler(List<NamedNode> groupingHandlers);
        void updateBridge(List<NamedNode> bridges);
    }
    // @formatter:on
}
