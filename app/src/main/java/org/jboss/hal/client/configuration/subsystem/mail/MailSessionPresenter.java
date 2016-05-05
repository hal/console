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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.typeahead.TypeaheadProvider;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.SubsystemPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;

/**
 * @author Claudio Miranda
 */
public class MailSessionPresenter extends SubsystemPresenter<MailSessionPresenter.MyView, MailSessionPresenter.MyProxy> {

    public static final String MAIL_ADDRESS         = "/{selected.profile}/subsystem=mail";
    public static final String MAIL_SESSION_ADDRESS = "/{selected.profile}/subsystem=mail/mail-session=*";
    // this server address set to smtp, because the address is a singleton, so it does not load the wildcard.
    // the attributes for imap, smtp and pop3 are the same
    public static final String SERVER_ADDRESS       = "/{selected.profile}/subsystem=mail/mail-session=*/server=smtp";

    public static final AddressTemplate MAIL_TEMPLATE         = AddressTemplate.of(MAIL_ADDRESS);
    public static final AddressTemplate MAIL_SESSION_TEMPLATE = AddressTemplate.of(MAIL_SESSION_ADDRESS);
    public static final AddressTemplate SERVER_TEMPLATE       = AddressTemplate.of(SERVER_ADDRESS);
    
    public static final AddressTemplate SOCKET_BINDING_TEMPLATE = AddressTemplate
            .of("/socket-binding-group=*/remote-destination-outbound-socket-binding=*");

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MAIL_SESSION)
    @Requires({MAIL_ADDRESS, MAIL_SESSION_ADDRESS, SERVER_ADDRESS})
    public interface MyProxy extends ProxyPlace<MailSessionPresenter> {}

    public interface MyView extends PatternFlyView, HasVerticalNavigation, HasPresenter<MailSessionPresenter> {
        void reveal();
        void update(ModelNode mailData);
        void setMailSessionName(String name);
        boolean serverTypeExists(String serverType);
    }
    // @formatter:on

    private final Resources resources;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private final EventBus eventBus;
    private MetadataRegistry metadataRegistry;
    private String mailSessionName;
    private ResourceAddress socketBindindResourceAddress;

    @Inject
    public MailSessionPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Resources resources,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry) {
        super(eventBus, view, proxy, finder);

        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.operationFactory = new OperationFactory();
        this.eventBus = eventBus;
        this.socketBindindResourceAddress = SOCKET_BINDING_TEMPLATE.resolve(statementContext);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadMailSession();
    }
    
    @Override
    protected void onReveal() {
        super.onReveal();
        getView().reveal();
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath
                .subsystemPath(statementContext.selectedProfile(), ModelDescriptionConstants.MAIL)
                .append(Ids.MAIL_SESSION, Names.MAIL_SESSION, Names.MAIL_SESSION, mailSessionName);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        mailSessionName = request.getParameter(NAME, null);
    }

    void loadMailSession() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                // mailSessionName is retrieved from the GET parameter
                MAIL_SESSION_TEMPLATE.replaceWildcards(mailSessionName).resolve(statementContext))
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> {
                    getView().update(result);
                    getView().setMailSessionName(mailSessionName);
                });
    }

    void save(AddressTemplate addressTemplate, final Map<String, Object> changedValues) {
        ResourceAddress resourceAddress = addressTemplate.replaceWildcards(mailSessionName).resolve(statementContext);
        Composite composite = operationFactory.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.MAIL_SESSION, mailSessionName)));
            loadMailSession();
        });
    }

    void launchAddNewServer() {

        List<String> types = Arrays.asList("SMTP", "IMAP", "POP3");
        SingleSelectBoxItem serverTypes = new SingleSelectBoxItem(ModelDescriptionConstants.SERVER_TYPE, 
                resources.constants().type(), types);
        serverTypes.setRequired(true);
        
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(
                IdBuilder.build(ModelDescriptionConstants.SERVER, ModelDescriptionConstants.ADD, "form"), metadata)
                .unboundFormItem(serverTypes, 0)
                .include("outbound-socket-binding-ref", "username", "password", "ssl", "tls")
                .addFromRequestProperties()
                .unsorted()
                .build();

        form.getFormItem("outbound-socket-binding-ref").registerSuggestHandler(
                new TypeaheadProvider().from(socketBindindResourceAddress));

        form.getFormItem(ModelDescriptionConstants.SERVER_TYPE).addValidationHandler(value -> {
            String _serverType = value.toString();
            if (getView().serverTypeExists(_serverType.toLowerCase())) {
                return ValidationResult.invalid(resources.constants().mailServerTypeAlreadyExists());
            } else 
               return ValidationResult.OK;
            
        });
        
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.SERVER), form,
                (name, modelNode) -> {

                    String serverType = form.getFormItem(ModelDescriptionConstants.SERVER_TYPE).getText().toLowerCase();
                    
                    AddressTemplate resolvedServerAddress = MAIL_SESSION_TEMPLATE.replaceWildcards(mailSessionName)
                            .append(ModelDescriptionConstants.SERVER + "=" + serverType);
                    if (modelNode != null) {
                        ResourceAddress address = resolvedServerAddress.resolve(statementContext);
                        Operation operation = new Operation.Builder(ModelDescriptionConstants.ADD, address)
                                .payload(modelNode)
                                .param(ModelDescriptionConstants.SERVER, name)
                                .build();
                        dispatcher.execute(operation, result -> {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages()
                                            .addResourceSuccess(Names.SERVER, serverType)));
                            loadMailSession();
                        });
                    }
                });
        dialog.show();
    }

    public ResourceAddress getSocketBindindResourceAddress() {
        return socketBindindResourceAddress;
    }
}
