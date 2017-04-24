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
package org.jboss.hal.client.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
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

import static java.util.Collections.emptyList;
import static org.jboss.hal.client.configuration.SocketBinding.INBOUND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class SocketBindingGroupPresenter
        extends MbuiPresenter<SocketBindingGroupPresenter.MyView, SocketBindingGroupPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.SOCKET_BINDING_GROUP)
    public interface MyProxy extends ProxyPlace<SocketBindingGroupPresenter> {}

    public interface MyView extends MbuiView<SocketBindingGroupPresenter> {
        void reveal();
        void update(NamedNode socketBindingGroup);
        void showClientMappings(List<NamedNode> clientMappings);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/socket-binding-group=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);
    private static final String SELECTED_ADDRESS = "/socket-binding-group=" + SelectionAwareStatementContext.SELECTION_EXPRESSION;
    private static final AddressTemplate SELECTED_TEMPLATE = AddressTemplate.of(SELECTED_ADDRESS);

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private String socketBindingGroup;
    String inbound;

    @Inject
    public SocketBindingGroupPresenter(final EventBus eventBus,
            final SocketBindingGroupPresenter.MyView view,
            final SocketBindingGroupPresenter.MyProxy proxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> socketBindingGroup);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().reveal();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        socketBindingGroup = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ROOT_TEMPLATE.resolve(statementContext, socketBindingGroup);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS), Names.CONFIGURATION, Names.SOCKET_BINDINGS)
                .append(Ids.SOCKET_BINDING_GROUP, socketBindingGroup, Names.SOCKET_BINDING_GROUP, socketBindingGroup);
    }

    @Override
    protected void reload() {
        reload(result -> getView().update(new NamedNode(result)));
    }

    private void reload(Consumer<ModelNode> consumer) {
        crud.readRecursive(SELECTED_TEMPLATE.resolve(statementContext), consumer::accept);
    }


    // ------------------------------------------------------ socket binding group

    void saveSocketBindingGroup(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);
        ResourceAddress address = SELECTED_TEMPLATE.resolve(statementContext);
        crud.saveSingleton(Names.SOCKET_BINDING_GROUP, address, changedValues, metadata, this::reload);
    }

    void resetSocketBindingGroup(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);
        ResourceAddress address = SELECTED_TEMPLATE.resolve(statementContext);
        crud.resetSingleton(Names.SOCKET_BINDING_GROUP, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }


    // ------------------------------------------------------ nested socket binding resources

    void addSocketBinding(SocketBinding socketBinding) {
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE.append(socketBinding.templateSuffix()));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(socketBinding.baseId, Ids.ADD_SUFFIX), metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        FormItem<Object> formItem = form.getFormItem(SOCKET_BINDING_REF);
        if (formItem != null) {
            AddressTemplate template = ROOT_TEMPLATE.append(INBOUND.templateSuffix());
            formItem.registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, template));
        }
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(socketBinding.type), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_TEMPLATE.append(socketBinding.templateSuffix())
                            .resolve(statementContext, name);
                    crud.add(socketBinding.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveSocketBinding(SocketBinding socketBinding, Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE.append(socketBinding.templateSuffix()));
        ResourceAddress address = SELECTED_TEMPLATE.append(socketBinding.templateSuffix())
                .resolve(statementContext, name);
        crud.save(socketBinding.type, name, address, changedValues, metadata, this::reload);
    }

    void resetSocketBinding(SocketBinding socketBinding, Form<NamedNode> form) {
        String name = form.getModel().getName();
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE.append(socketBinding.templateSuffix()));
        ResourceAddress address = SELECTED_TEMPLATE.append(socketBinding.templateSuffix())
                .resolve(statementContext, name);
        crud.reset(socketBinding.type, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeSocketBinding(SocketBinding socketBinding, String name) {
        ResourceAddress address = SELECTED_TEMPLATE.append(socketBinding.templateSuffix())
                .resolve(statementContext, name);
        crud.remove(socketBinding.type, name, address, this::reload);
    }


    // ------------------------------------------------------ client mappings

    void addClientMapping(Metadata metadata) {
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.SOCKET_BINDING_GROUP_INBOUND_CLIENT_MAPPING_ADD,
                metadata)
                .addOnly()
                .include(SOURCE_NETWORK, DESTINATION_ADDRESS, DESTINATION_PORT)
                .unsorted()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.CLIENT_MAPPINGS),
                form, (name, modelNode) -> {
            ResourceAddress address = SELECTED_TEMPLATE.append(INBOUND.templateSuffix())
                    .resolve(statementContext, inbound);
            Operation operation = new Operation.Builder(address, LIST_ADD)
                    .param(NAME, CLIENT_MAPPINGS)
                    .param(VALUE, modelNode)
                    .build();
            dispatcher.execute(operation, result -> reloadClientMappings());
        });
        dialog.show();
    }

    private void reloadClientMappings() {
        ResourceAddress address = SELECTED_TEMPLATE.append(INBOUND.templateSuffix()).resolve(statementContext, inbound);
        crud.readRecursive(address, result -> showClientMappings(new NamedNode(result)));
    }

    void showClientMappings(NamedNode inbound) {
        this.inbound = inbound.getName();
        List<ModelNode> clientMappings = inbound.hasDefined(CLIENT_MAPPINGS)
                ? inbound.get(CLIENT_MAPPINGS).asList()
                : emptyList();
        int index = 0;
        List<NamedNode> indexAsName = new ArrayList<>();
        for (ModelNode clientMapping : clientMappings) {
            clientMapping.get(INDEX).set(index);
            indexAsName.add(new NamedNode(String.valueOf(index), clientMapping));
            index++;
        }
        getView().showClientMappings(indexAsName);
    }

    void saveClientMapping(Metadata metadata, ModelNode clientMapping, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_TEMPLATE.append(INBOUND.templateSuffix()).resolve(statementContext, inbound);
        OperationFactory operationFactory = new OperationFactory(
                name -> CLIENT_MAPPINGS + "[" + clientMapping.get(INDEX).asInt() + "]." + name);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        dispatcher.execute(operations, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifySingleResourceSuccess(Names.CLIENT_MAPPING)));
            reloadClientMappings();
        });
    }

    void removeClientMapping(final int index) {
        DialogFactory.showConfirmation(resources.messages().removeConfirmationTitle(Names.CLIENT_MAPPING),
                resources.messages().removeSingletonConfirmationQuestion(), () -> {
                    ResourceAddress address = SELECTED_TEMPLATE.append(INBOUND.templateSuffix())
                            .resolve(statementContext, inbound);
                    Operation operation = new Operation.Builder(address, LIST_REMOVE)
                            .param(NAME, CLIENT_MAPPINGS)
                            .param(INDEX, index)
                            .build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(getEventBus(), Message.success(
                                resources.messages().removeSingletonResourceSuccess(Names.CLIENT_MAPPING)));
                        reloadClientMappings();
                    });
                });
    }
}
