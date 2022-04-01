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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_MANAGER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_MANAGER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECURITY_DOMAIN_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.TRUST_MANAGER_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.TRUST_MANAGER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_AUTHORITY_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANGE_ACCOUNT_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREATE_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEACTIVATE_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GET_METADATA;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_CERTIFICATE_REVOCATION_LIST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATE_ACCOUNT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.Ids.FORM;

public class SSLPresenter extends ApplicationFinderPresenter<SSLPresenter.MyView, SSLPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SPACE = " ";

    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Dispatcher dispatcher;

    @Inject
    public SSLPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Resources resources,
            Finder finder,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, SECURITY, resources.constants().monitor(), Names.SECURITY)
                .append(Ids.ELYTRON_RUNTIME, Ids.ELYTRON_SSL, Names.SECURITY, Names.SSL);
    }

    @Override
    protected void reload() {
        Composite composite = new Composite();
        composite.add(operation(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE));
        composite.add(operation(KEY_MANAGER_TEMPLATE));
        composite.add(operation(SECURITY_DOMAIN_TEMPLATE));
        composite.add(operation(TRUST_MANAGER_TEMPLATE));
        dispatcher.execute(composite, (CompositeResult result) -> {
            int i = 0;
            getView().updateCertificateAuthorityAccount(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateKeyManager(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateSecurityDomain(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateTrustManager(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
        });
    }

    private Operation operation(AddressTemplate template) {
        return new Operation.Builder(template.getParent().resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(CHILD_TYPE, template.lastName())
                .build();
    }

    // ----------------- certificate authority account

    void createAccount(String name) {
        Metadata metadata = metadataRegistry.lookup(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE)
                .forOperation(CREATE_ACCOUNT);
        String id = Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, FORM);
        String title = new LabelBuilder().label(CERTIFICATE_AUTHORITY_ACCOUNT);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, CREATE_ACCOUNT)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().createAccountSuccess(name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().createAccountError(name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(resources.messages().createAccountQuestion(name)).element())
                .add(form.element())
                .primary(resources.constants().create(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    void deactivateAccount(String name) {
        Metadata metadata = metadataRegistry.lookup(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE)
                .forOperation(DEACTIVATE_ACCOUNT);
        String id = Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, FORM);
        String title = new LabelBuilder().label(CERTIFICATE_AUTHORITY_ACCOUNT);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, DEACTIVATE_ACCOUNT)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().deactivateAccountSuccess(name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().deactivateAccountError(name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(resources.messages().deactivateAccountQuestion(name)).element())
                .add(form.element())
                .primary(resources.constants().deactivate(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    void updateAccount(String name) {
        Metadata metadata = metadataRegistry.lookup(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE)
                .forOperation(UPDATE_ACCOUNT);
        String id = Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, FORM);
        String title = new LabelBuilder().label(CERTIFICATE_AUTHORITY_ACCOUNT);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, UPDATE_ACCOUNT)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().updateAccountSuccess(name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().updateAccountError(name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(resources.messages().updateAccountQuestion(name)).element())
                .add(form.element())
                .primary(resources.constants().update(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    void getMetadata(String name, Consumer<String> callback) {
        ResourceAddress address = CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE.resolve(statementContext, name);
        Operation operation = new Operation.Builder(address, GET_METADATA)
                .build();
        dispatcher.execute(operation, result -> {
            callback.accept(result.toString());
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().getMetadataSuccess(name)));
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().getMetadataError(name, failure))));
    }

    void changeAccountKey(String name) {
        Metadata metadata = metadataRegistry.lookup(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE)
                .forOperation(CHANGE_ACCOUNT_KEY);
        String id = Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, FORM);
        String title = new LabelBuilder().label(CERTIFICATE_AUTHORITY_ACCOUNT);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, CHANGE_ACCOUNT_KEY)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().changeAccountKeySuccess(name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().changeAccountKeyError(name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(resources.messages().changeAccountKeyQuestion(name)).element())
                .add(form.element())
                .primary(resources.constants().change(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    // ----------------- SSL

    void initKeyManager(String name) {
        Operation operation = new Operation.Builder(KEY_MANAGER_TEMPLATE.resolve(statementContext, name), INIT)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().initKeyManagerSuccess(name)));
            reload();
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().initKeyManagerError(name, failure))));
    }

    void initTrustManager(String name) {
        Operation operation = new Operation.Builder(TRUST_MANAGER_TEMPLATE.resolve(statementContext, name), INIT)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().initTrustManagerSuccess(name)));
            reload();
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().initTrustManagerError(name, failure))));
    }

    void reloadCRL(String name) {
        Operation operation = new Operation.Builder(TRUST_MANAGER_TEMPLATE.resolve(statementContext, name),
                RELOAD_CERTIFICATE_REVOCATION_LIST)
                        .build();
        dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                Message.success(resources.messages().reloadCRLSuccess(name))),
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadCRLError(name, failure))));
    }

    void readIdentity(Metadata metadata, String name) {
        /*
         * AddressTemplate template = metadata.getTemplate(); LabelBuilder labelBuilder = new LabelBuilder(); String resource =
         * labelBuilder.label(template.lastName()) + SPACE + name; ResourceAddress address = template.resolve(statementContext,
         * name); Operation addOp = new Operation.Builder(address, READ_IDENTITY) .param(IDENTITY, name) .build();
         * dispatcher.execute(addOp, result -> {
         *
         * }, (operation, failure) -> MessageEvent.fire(getEventBus(), Message.error(resources.messages().readAliasError(name,
         * resource, failure))), (operation, ex) -> MessageEvent.fire(getEventBus(),
         * Message.error(resources.messages().readAliasError(name, resource, ex.getMessage()))));
         */

    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.ELYTRON_RUNTIME_SSL)
    @Requires({ CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS, KEY_MANAGER_ADDRESS, SECURITY_DOMAIN_ADDRESS, TRUST_MANAGER_ADDRESS })
    public interface MyProxy extends ProxyPlace<SSLPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<SSLPresenter> {
        void updateCertificateAuthorityAccount(List<NamedNode> items);

        void updateKeyManager(List<NamedNode> items);

        void updateSecurityDomain(List<NamedNode> items);

        void updateTrustManager(List<NamedNode> items);

    }
    // @formatter:on
}
