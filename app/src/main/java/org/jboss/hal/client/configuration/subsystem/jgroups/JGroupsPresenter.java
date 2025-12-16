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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
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
import org.jboss.hal.meta.description.ResourceDescription;
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

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.AUTH_TOKEN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.JGROUPS_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.PROTOCOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_AUTH_TOKEN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_PROTOCOL_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.STACK_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.TRANSPORT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANNEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FORK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JGROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROTOCOL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELAY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STACK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSPORT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.JGROUPS_REMOTE_SITE;

public class JGroupsPresenter extends ApplicationFinderPresenter<JGroupsPresenter.MyView, JGroupsPresenter.MyProxy>
        implements SupportsExpertMode {

    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private final EventBus eventBus;
    private final Resources resources;
    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final StatementContext filterStatementContext;
    private String selectedStack;
    private String currentChannel;
    private String selectedFork;

    public static final String AUTH = "AUTH";
    private static final String PROTOCOL_AUTH = PROTOCOL + "=" + AUTH;
    private static final String[] AUTH_ATTRIBUTES = { ADD_INDEX, MODULE, PROPERTIES, STATISTICS_ENABLED };

    private Map<String, Metadata> stackSingletons;

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

    public void processStackSingletons() {
        Operation readProtocolSingletons = new Operation.Builder(STACK_TEMPLATE.resolve(filterStatementContext),
                READ_CHILDREN_TYPES_OPERATION)
                .param(INCLUDE_SINGLETONS, true)
                .build();

        Operation readTokenSingletons = new Operation.Builder(
                PROTOCOL_TEMPLATE.resolve(filterStatementContext, "*", AUTH),
                READ_CHILDREN_TYPES_OPERATION)
                .param(INCLUDE_SINGLETONS, true)
                .build();

        Metadata authMd = metadataRegistry.lookup(PROTOCOL_TEMPLATE.replaceWildcards("*", AUTH));

        dispatcher.execute(new Composite(readProtocolSingletons, readTokenSingletons), (CompositeResult result) -> {
            stackSingletons = result.step(0).get(RESULT).asList().stream()
                    .map(s -> {
                        String name = s.asString();
                        if (!name.contains("=")) {
                            name += "=*";
                        }
                        return metadataRegistry.lookup(STACK_TEMPLATE.append(name));
                    })
                    .filter(metadata -> !metadata.getDescription().has(DEPRECATED))
                    .collect(Collectors.toMap(m -> m.getTemplate().lastName() + "=" + m.getTemplate().lastValue(),
                            m -> m, (m1, m2) -> null, TreeMap::new));

            // AUTH has child singletons, we present them as separate AUTH options
            result.step(1).get(RESULT).asList().forEach(token -> {
                Metadata mdCopy = new Metadata(authMd.getTemplate(), authMd::getSecurityContext,
                        new ResourceDescription(authMd.getDescription()), authMd.getCapabilities());
                String tokenName = token.asString().substring(6);

                Metadata tokenMd = metadataRegistry
                        .lookup(AUTH_TOKEN_TEMPLATE.replaceWildcards("*", tokenName));
                mdCopy.getDescription().requestProperties().addAll(tokenMd.getDescription().requestProperties());
                stackSingletons.put(PROTOCOL_AUTH + " (" + tokenName + ")", mdCopy);
            });
            stackSingletons.remove(PROTOCOL_AUTH);
        });
    }

    private Set<String> getNamesOf(String resource) {
        return stackSingletons.keySet().stream()
                .filter(name -> name.startsWith(resource))
                .map(name -> name.substring(resource.length() + 1))
                .collect(Collectors.toSet());
    }

    public Set<String> getProtocolNames() {
        return getNamesOf(PROTOCOL);
    }

    public Set<String> getTransportNames() {
        return getNamesOf(TRANSPORT);
    }

    public Metadata getAuthTokenMetadata(String token) {
        return metadataRegistry.lookup(AUTH_TOKEN_TEMPLATE.replaceWildcards("*", token));
    }

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
        transportItem.registerSuggestHandler(new StaticAutoComplete(new ArrayList<>(getTransportNames())));
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

    public Metadata getProtocolMetadata(String name) {
        Metadata metadata = stackSingletons.get(PROTOCOL + "=" + name);
        if (metadata == null) {
            metadata = stackSingletons.get(PROTOCOL + "=*");
        }
        return metadata;
    }

    public void addProtocol(Set<String> currentProtocols) {
        Set<String> availableProtocols = getProtocolNames().stream()
                .filter(prot -> !currentProtocols.contains(prot)
                        && !((prot.startsWith(AUTH + " (") && currentProtocols.contains(AUTH))))
                .collect(Collectors.toSet());
        new ProtocolWizard(resources, availableProtocols, this::getProtocolMetadata,
                (wizard, context) -> addProtocolSingleton(dispatcher, filterStatementContext, context.protocolName,
                        context.payload, (n, a) -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.success(resources.messages().addResourceSuccess(PROTOCOL, context.protocolName)));
                            reload();
                        }))
                .show();
    }

    private void addProtocolSingleton(Dispatcher dispatcher, StatementContext statementContext, String protocolName,
            ModelNode payload, CrudOperations.AddCallback callback) {
        if (!protocolName.startsWith(AUTH + " (")) {
            crud.add(PROTOCOL, protocolName,
                    SELECTED_PROTOCOL_TEMPLATE.resolve(filterStatementContext, protocolName),
                    payload, (n, a) -> reload());
            return;
        }

        List<Operation> operations = new ArrayList<>();
        ModelNode protocolPayload = new ModelNode();
        for (String s : AUTH_ATTRIBUTES) {
            if (payload.has(s)) {
                ModelNode value = payload.remove(s);
                protocolPayload.get(s).set(value);
            }
        }
        final String tokenName = protocolName.substring(AUTH.length() + 2, protocolName.length() - 1);
        operations.add(new Operation.Builder(SELECTED_PROTOCOL_TEMPLATE.resolve(statementContext, AUTH), ADD)
                .payload(protocolPayload).build());
        operations.add(new Operation.Builder(SELECTED_AUTH_TOKEN_TEMPLATE.resolve(statementContext, tokenName), ADD)
                .payload(payload).build());
        dispatcher.execute(new Composite(operations), (CompositeResult result) -> callback.execute(null, null));
    }

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
