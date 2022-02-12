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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
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
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.JGROUPS_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.STACK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.JGROUPS_REMOTE_SITE;

public class JGroupsPresenter extends ApplicationFinderPresenter<JGroupsPresenter.MyView, JGroupsPresenter.MyProxy>
        implements SupportsExpertMode {

    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private EventBus eventBus;
    private Resources resources;
    private MetadataRegistry metadataRegistry;
    private Dispatcher dispatcher;
    private String selectedStack;
    private String currentChannel;
    private String selectedFork;
    private StatementContext filterStatementContext;

    @Inject
    public JGroupsPresenter(EventBus eventBus,
            MyView view, Resources resources,
            MyProxy myProxy, MetadataRegistry metadataRegistry,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            CrudOperations crud) {

        super(eventBus, view, myProxy, finder);
        this.eventBus = eventBus;
        this.resources = resources;
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.crud = crud;
        this.filterStatementContext = new FilteringStatementContext(statementContext,
                new FilteringStatementContext.Filter() {
                    @Override
                    @SuppressWarnings("HardCodedStringLiteral")
                    public String filter(String filterKey, AddressTemplate template) {
                        switch (filterKey) {
                            case "selected.channel":
                                return currentChannel;
                            case "selected.fork":
                                return selectedFork;
                            case "selected.stack":
                                return selectedStack;
                            default:
                                break;
                        }
                        return null;
                    }

                    @Override
                    public String[] filterTuple(String placeholder, AddressTemplate template) {
                        return null;
                    }
                });
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return JGROUPS_TEMPLATE.resolve(filterStatementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(JGROUPS);
    }

    // ------------------------------------------------------ jgroups subsystem

    @Override
    protected void reload() {
        reload(modelNode -> {
            // update the UI with the resulting model

            // root configuration, stacks, channel
            getView().update(modelNode);

            // stack / relays
            List<NamedNode> relayNode = asNamedNodes(
                    failSafePropertyList(modelNode, String.join("/", STACK, selectedStack, RELAY)));
            getView().updateRelays(relayNode);

            // stack / relay / remote-site
            // noinspection HardCodedStringLiteral
            List<NamedNode> remoteSite = asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", STACK, selectedStack, RELAY, "relay.RELAY2", JGROUPS_REMOTE_SITE)));
            getView().updateRemoteSite(remoteSite);

            // stack / protocols
            List<NamedNode> protocol = asNamedNodes(
                    failSafePropertyList(modelNode, String.join("/", STACK, selectedStack, PROTOCOL)));
            getView().updateProtocols(protocol);

            // stack / transport
            List<NamedNode> transport = asNamedNodes(
                    failSafePropertyList(modelNode, String.join("/", STACK, selectedStack, TRANSPORT)));
            getView().updateTransports(transport);

            // channel / fork
            List<NamedNode> forks = asNamedNodes(
                    failSafePropertyList(modelNode, String.join("/", CHANNEL, currentChannel, FORK)));
            getView().updateForks(forks);

            // channel / fork / protocol
            List<NamedNode> channelProtocols = asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", CHANNEL, currentChannel, FORK, selectedFork, PROTOCOL)));
            getView().updateChannelProtocols(channelProtocols);

        });
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(JGROUPS_TEMPLATE, payload::accept);
    }

    void saveSingleton(AddressTemplate template, Metadata metadata, Map<String, Object> changedValues,
            SafeHtml successMessage) {
        crud.saveSingleton(template.resolve(filterStatementContext), changedValues, metadata, successMessage,
                this::reload);
    }

    void saveResource(AddressTemplate template, String resourceName, Map<String, Object> changedValues,
            Metadata metadata, SafeHtml successMessage) {
        crud.save(template.resolve(filterStatementContext, resourceName), changedValues, metadata, successMessage,
                this::reload);
    }

    <T> void resetSingleton(AddressTemplate template, String type, Form<T> form, Metadata metadata) {
        crud.resetSingleton(type, template.resolve(filterStatementContext), form, metadata, new FinishReset<T>(form) {
            @Override
            public void afterReset(Form<T> form) {
                reload();
            }
        });
    }

    void resetResource(AddressTemplate template, String resourceName, String type, Form<NamedNode> form,
            Metadata metadata) {
        crud.reset(type, resourceName, template.resolve(filterStatementContext, resourceName), form, metadata,
                new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    void removeResource(AddressTemplate template, String name, String displayName) {
        ResourceAddress address = template.resolve(filterStatementContext, name);
        crud.remove(displayName, name, address, this::reload);
    }

    void addResourceDialog(AddressTemplate template, String resourceid, String displayName) {
        AddressTemplate addressTemplate = AddressTemplate.of(template.resolve(filterStatementContext));
        crud.add(resourceid, displayName, addressTemplate, (n, a) -> reload());
    }

    // stack resources

    @SuppressWarnings("ConstantConditions")
    void addStack() {
        Metadata metadata = metadataRegistry.lookup(STACK_TEMPLATE);
        Metadata transportMetadata = metadataRegistry.lookup(TRANSPORT_TEMPLATE).forOperation(ADD);
        transportMetadata.copyAttribute(SOCKET_BINDING, metadata);
        metadata.makeWritable(SOCKET_BINDING);

        NameItem nameItem = new NameItem();
        String transportLabel = new LabelBuilder().label(TRANSPORT);
        TextBoxItem transportItem = new TextBoxItem(TRANSPORT, transportLabel);
        transportItem.setRequired(true);
        String id = Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.ADD);
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .unboundFormItem(nameItem, 0)
                .unboundFormItem(transportItem, 2)
                .unsorted()
                .requiredOnly()
                .build();

        AddResourceDialog dialog = new AddResourceDialog(Names.STACK, form,
                (name, model) -> {
                    ResourceAddress stackAddress = STACK_TEMPLATE.resolve(filterStatementContext, name);

                    String transport = transportItem.getValue();
                    ResourceAddress transportAddress = TRANSPORT_TEMPLATE.resolve(filterStatementContext, name,
                            transport);

                    Operation addStackOperation = new Operation.Builder(stackAddress, ADD)
                            .build();
                    Operation addTransportOperation = new Operation.Builder(transportAddress, ADD)
                            .payload(model)
                            .build();

                    Composite composite = new Composite(addStackOperation, addTransportOperation);
                    dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().addResourceSuccess(Names.STACK, name)));
                        reload();
                    });

                });
        dialog.show();
    }

    void showStackInnerPage(String id) {
        getView().showStackInnerPage(id);
    }

    // relay resources

    void showRelays(NamedNode selectedStack) {
        this.selectedStack = selectedStack.getName();
        List<NamedNode> relayNode = asNamedNodes(failSafePropertyList(selectedStack.asModelNode(), RELAY));
        getView().updateRelays(relayNode);
    }

    void addRelay() {
        AddressTemplate addressTemplate = AddressTemplate.of(SELECTED_RELAY_TEMPLATE.resolve(filterStatementContext));
        crud.addSingleton(Ids.build(RELAY, Ids.ADD, Ids.FORM), Names.RELAY,
                addressTemplate, address -> reload());
    }

    // remote sites

    void showRemoteSites(NamedNode row) {
        List<NamedNode> model = asNamedNodes(failSafePropertyList(row.asModelNode(), JGROUPS_REMOTE_SITE));
        getView().updateRemoteSite(model);
    }

    // protocol resources

    void showProtocols(NamedNode selectedStack) {
        this.selectedStack = selectedStack.getName();
        getView().updateProtocols(asNamedNodes(failSafePropertyList(selectedStack, PROTOCOL)));
    }

    // transport resources

    void showTransports(NamedNode selectedStack) {
        this.selectedStack = selectedStack.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedStack.asModelNode(), TRANSPORT));
        getView().updateTransports(model);
    }

    void showForks(NamedNode selectedChannel) {
        currentChannel = selectedChannel.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedChannel.asModelNode(), FORK));
        getView().updateForks(model);
    }

    void showChannelInnerPage(String id) {
        getView().showChannelInnerPage(id);
    }

    void showChannelProtocol(NamedNode selectedFork) {
        this.selectedFork = selectedFork.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedFork.asModelNode(), PROTOCOL));
        getView().updateChannelProtocols(model);
    }

    String getSelectedFork() {
        return selectedFork;
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JGROUPS)
    @Requires(AddressTemplates.JGROUPS_ADDRESS)
    public interface MyProxy extends ProxyPlace<JGroupsPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<JGroupsPresenter> {
        void update(ModelNode payload);

        // stack resources
        void updateProtocols(List<NamedNode> model);

        void updateTransports(List<NamedNode> model);

        void updateRelays(List<NamedNode> model);

        void updateRemoteSite(List<NamedNode> model);

        void showStackInnerPage(String id);

        // channel resources
        void showChannelInnerPage(String id);

        void updateForks(List<NamedNode> model);

        void updateChannelProtocols(List<NamedNode> model);
    }
    // @formatter:on
}
