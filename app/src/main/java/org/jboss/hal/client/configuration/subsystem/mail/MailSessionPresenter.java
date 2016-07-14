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
package org.jboss.hal.client.configuration.subsystem.mail;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
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
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.MAIL_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.MAIL_SESSION_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda
 */
public class MailSessionPresenter
        extends ApplicationPresenter<MailSessionPresenter.MyView, MailSessionPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MAIL_SESSION)
    @Requires({MAIL_ADDRESS, MAIL_SESSION_ADDRESS, SERVER_ADDRESS})
    public interface MyProxy extends ProxyPlace<MailSessionPresenter> {}

    public interface MyView extends PatternFlyView, HasVerticalNavigation, HasPresenter<MailSessionPresenter> {
        void update(MailSession mailSession);
    }
    // @formatter:on

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final OperationFactory operationFactory;
    private String mailSessionName;

    @Inject
    public MailSessionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Resources resources,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> mailSessionName);
        this.operationFactory = new OperationFactory();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        mailSessionName = request.getParameter(NAME, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadMailSession();
    }

    @Override
    protected FinderPath finderPath() {
        return finderPathFactory.subsystemPath(ModelDescriptionConstants.MAIL)
                .append(Ids.MAIL_SESSION, mailSessionName, Names.MAIL_SESSION, mailSessionName);
    }

    void loadMailSession() {
        ResourceAddress address = AddressTemplates.SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
            getView().update(new MailSession(mailSessionName, result));
        });
    }

    void save(final Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = AddressTemplates.SELECTED_MAIL_SESSION_TEMPLATE.resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.MAIL_SESSION, mailSessionName)));
            loadMailSession();
        });
    }

    void launchAddNewServer() {
        SortedSet<String> availableServers = new TreeSet<>(asList(SMTP.toUpperCase(),
                IMAP.toUpperCase(), POP3.toUpperCase()));
        ResourceAddress selectedSessionAddress = AddressTemplates.SELECTED_MAIL_SESSION_TEMPLATE
                .resolve(statementContext);
        Operation serverNamesOp = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, selectedSessionAddress)
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
                            resources.constants().type(), new ArrayList<>(availableServers));
                    serverTypeItem.setRequired(true);
                }

                Metadata metadata = metadataRegistry.lookup(AddressTemplates.SERVER_TEMPLATE);
                AddResourceDialog dialog = new AddResourceDialog(
                        Ids.MAIL_SERVER_DIALOG,
                        resources.messages().addResourceTitle(Names.SERVER), metadata,
                        asList(OUTBOUND_SOCKET_BINDING_REF, "username", "password", "ssl", "tls"), //NON-NLS
                        (name, modelNode) -> {

                            String serverType = serverTypeItem.getValue().toLowerCase();
                            ResourceAddress address = AddressTemplates.SELECTED_MAIL_SESSION_TEMPLATE
                                    .append(ModelDescriptionConstants.SERVER + "=" + serverType)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(ModelDescriptionConstants.ADD, address)
                                    .payload(modelNode)
                                    .param(ModelDescriptionConstants.SERVER, name)
                                    .build();
                            dispatcher.execute(operation, result -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages()
                                                .addResourceSuccess(Names.SERVER, serverType)));
                                loadMailSession();
                            });
                        });
                dialog.getForm().getFormItem(OUTBOUND_SOCKET_BINDING_REF).registerSuggestHandler(
                        new Typeahead(AddressTemplates.SOCKET_BINDING_TEMPLATE, statementContext));
                dialog.show();
            }
        });
    }

    String getMailSessionName() {
        return mailSessionName;
    }
}
