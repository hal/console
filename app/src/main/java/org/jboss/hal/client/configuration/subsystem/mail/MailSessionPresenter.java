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
package org.jboss.hal.client.configuration.subsystem.mail;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
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
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
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
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class MailSessionPresenter
        extends ApplicationFinderPresenter<MailSessionPresenter.MyView, MailSessionPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private String mailSessionName;

    @Inject
    public MailSessionPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            Resources resources,
            Dispatcher dispatcher,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> mailSessionName);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        mailSessionName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MAIL)
                .append(Ids.MAIL_SESSION, Ids.mailSession(mailSessionName), Names.MAIL_SESSION, mailSessionName);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
        crud.readRecursive(address, result -> getView().update(new MailSession(mailSessionName, result)));
    }

    void saveMailSession(Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(MAIL_SESSION_TEMPLATE);
        crud.save(Names.MAIL_SESSION, mailSessionName, address, changedValues, metadata, this::reload);
    }

    void resetMailSession(Form<MailSession> form) {
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(MAIL_SESSION_TEMPLATE);
        crud.reset(Names.MAIL_SESSION, mailSessionName, address, form, metadata, new FinishReset<MailSession>(form) {
            @Override
            public void afterReset(Form<MailSession> form) {
                reload();
            }
        });
    }

    void launchAddServer() {
        SortedSet<String> availableServers = new TreeSet<>(asList(SMTP.toUpperCase(),
                IMAP.toUpperCase(), POP3.toUpperCase()));
        ResourceAddress selectedSessionAddress = SELECTED_MAIL_SESSION_TEMPLATE
                .resolve(statementContext);
        Operation serverNamesOp = new Operation.Builder(selectedSessionAddress, READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SERVER)
                .build();
        dispatcher.execute(serverNamesOp, serversResult -> {
            Set<String> existingServers = serversResult.asList().stream()
                    .map(node -> node.asString().toUpperCase())
                    .collect(toSet());
            availableServers.removeAll(existingServers);

            if (availableServers.isEmpty()) {
                MessageEvent.fire(getEventBus(), Message.error(resources.messages().allMailServersExist()));

            } else {
                FormItem<String> serverTypeItem;
                if (availableServers.size() == 1) {
                    serverTypeItem = new TextBoxItem(ModelDescriptionConstants.SERVER_TYPE,
                            resources.constants().type());
                    serverTypeItem.setValue(availableServers.first());
                    serverTypeItem.setEnabled(false);

                } else {
                    serverTypeItem = new SingleSelectBoxItem(ModelDescriptionConstants.SERVER_TYPE,
                            resources.constants().type(), new ArrayList<>(availableServers), false, null);
                    serverTypeItem.setRequired(true);
                }
                Metadata metadata = metadataRegistry.lookup(AddressTemplates.SERVER_TEMPLATE);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.MAIL_SERVER_DIALOG, metadata)
                        .unboundFormItem(serverTypeItem, 0)
                        .include(OUTBOUND_SOCKET_BINDING_REF, USERNAME, PASSWORD, "ssl", "tls")
                        .fromRequestProperties()
                        .requiredOnly()
                        .unsorted()
                        .build();
                form.getFormItem(OUTBOUND_SOCKET_BINDING_REF).registerSuggestHandler(
                        new ReadChildrenAutoComplete(dispatcher, statementContext,
                                AddressTemplates.SOCKET_BINDING_TEMPLATE));

                AddResourceDialog dialog = new AddResourceDialog(
                        resources.messages().addResourceTitle(Names.SERVER), form,
                        (name, modelNode) -> {
                            String serverType = serverTypeItem.getValue().toLowerCase();
                            ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE
                                    .append(SERVER + EQUALS + serverType)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(address, ModelDescriptionConstants.ADD)
                                    .payload(modelNode)
                                    .build();
                            dispatcher.execute(operation, result -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages()
                                                .addResourceSuccess(Names.SERVER, serverType)));
                                reload();
                            });
                        });
                dialog.show();
            }
        });
    }

    void saveServer(String mailServer, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE
                .append(SERVER + EQUALS + mailServer)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.save(Names.SERVER, mailServer, address, changedValues, metadata, this::reload);
    }

    ResourceAddress credentialReferenceTemplate(@Nullable String mailServer) {
        return mailServer != null
                ? SELECTED_MAIL_SESSION_TEMPLATE.append(SERVER + EQUALS + mailServer).resolve(statementContext)
                : null;
    }

    void resetServer(String mailServer, Form<NamedNode> form) {
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE
                .append(SERVER + EQUALS + mailServer)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.reset(Names.SERVER, mailSessionName, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeServer(NamedNode mailServer) {
        String name = mailServer.getName();
        ResourceAddress address = SELECTED_MAIL_SESSION_TEMPLATE
                .append(SERVER + EQUALS + name)
                .resolve(statementContext);
        crud.remove(Names.SERVER, name, address, this::reload);
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MAIL_SESSION)
    @Requires({MAIL_ADDRESS, MAIL_SESSION_ADDRESS, SERVER_ADDRESS})
    public interface MyProxy extends ProxyPlace<MailSessionPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<MailSessionPresenter> {
        void update(MailSession mailSession);
        void select(NamedNode mailServer);
    }
    // @formatter:on
}
