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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
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
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class StoresPresenter extends ApplicationFinderPresenter<StoresPresenter.MyView, StoresPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SPACE = " ";

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private Resources resources;
    private Dispatcher dispatcher;

    @Inject
    public StoresPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Resources resources,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.dispatcher = dispatcher;
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
                .append(Ids.ELYTRON_RUNTIME, Ids.ELYTRON_SECURITY_REALMS,
                        Names.ELYTRON, Names.STORES);
    }

    @Override
    protected void reload() {
        Composite composite = new Composite();
        composite.add(operation(CREDENTIAL_STORE_TEMPLATE));
        composite.add(operation(FILTERING_KEY_STORE_TEMPLATE));
        composite.add(operation(KEY_STORE_TEMPLATE));
        composite.add(operation(LDAP_KEY_STORE_TEMPLATE));
        dispatcher.execute(composite, (CompositeResult result) -> {
            int i = 0;
            getView().updateCredentialStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateFilteringKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateLdapKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
        });
    }

    private Operation operation(AddressTemplate template) {
        return new Operation.Builder(template.getParent().resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(CHILD_TYPE, template.lastName())
                .build();
    }

    // ----------------- credential store

    void reloadCredentialStore(String name) {
        Operation operation = new Operation.Builder(CREDENTIAL_STORE_TEMPLATE.resolve(statementContext, name), RELOAD)
                .build();
        String resource = Names.CREDENTIAL_STORE + " " + name;
        dispatcher.execute(operation, result -> {
                    MessageEvent.fire(getEventBus(), Message.success(resources.messages().reloadSuccess(resource)));
                    reload();
                },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadError(resource, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadError(resource, exception.getMessage()))));
    }

    void addAlias(Metadata metadata, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        Metadata opMetadata = metadata.forOperation(ADD_ALIAS);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), ADD_ALIAS), opMetadata)
                .build();
        form.attach();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().addAlias(), form, (name1, model) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Composite composite = new Composite();
            Operation addOp = new Operation.Builder(address, ADD_ALIAS)
                    .payload(model)
                    .build();
            composite.add(addOp);
            Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                    .build();
            composite.add(operation);
            String alias = model.get(ALIAS).asString();
            String resource = Names.CREDENTIAL_STORE + SPACE + name;
            dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSuccess(ALIAS, alias, resource)));
                        ModelNode aliases = result.step(1).get(RESULT);
                        if (aliases.isDefined()) {
                            callback.accept(aliases.asList());
                        } else {
                            callback.accept(Collections.emptyList());
                        }
                    },
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().addError(ALIAS, alias, resource, failure))),
                    (op, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().addError(ALIAS, alias, resource, ex.getMessage()))));

        });
        dialog.show();
    }

    void setSecret(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.CREDENTIAL_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(SET_SECRET);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), SET_SECRET), opMetadata)
                .build();
        form.attach();
        HTMLElement formElement = form.asElement();
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        form.edit(model);
        DialogFactory.buildConfirmation(resources.constants().setSecret(), question, formElement, Dialog.Size.MEDIUM, () -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Operation addOp = new Operation.Builder(address, REMOVE_ALIAS)
                    .param(ALIAS, alias)
                    .build();
            dispatcher.execute(addOp, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().setSecretPasswordSuccess(alias, resource))),
                    (operation, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().setSecretPasswordError(alias, resource, failure))),
                    (operation, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().setSecretPasswordError(alias, resource, ex.getMessage()))));

        }).show();
    }


    // ----------------- key store

    void loadKeyStore(String name) {
        Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), LOAD)
                .build();
        String resource = Names.KEY_STORE + " " + name;
        dispatcher.execute(operation, result -> {
                    MessageEvent.fire(getEventBus(), Message.success(resources.messages().reloadSuccess(resource)));
                    reload();
                },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadError(resource, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadError(resource, exception.getMessage()))));
    }

    void storeKeyStore(String name) {
        Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), STORE)
                .build();
        dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().storeSuccess(name))),
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().storeError(name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().storeError(name, exception.getMessage()))));
    }

    void changeAlias(Metadata metadata, String name, String alias, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(CHANGE_ALIAS);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), CHANGE_ALIAS), opMetadata)
                .build();
        form.attach();
        HTMLElement formElement = form.asElement();
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        form.edit(model);
        DialogFactory.buildConfirmation(resources.constants().changeAlias(), question, formElement, Dialog.Size.MEDIUM, () -> {
            form.save();
            String newAlias = form.getModel().get("new-alias").asString();
            Composite composite = new Composite();
            ResourceAddress address = template.resolve(statementContext, name);
            Operation addOp = new Operation.Builder(address, CHANGE_ALIAS)
                    .payload(form.getModel())
                    .build();
            composite.add(addOp);
            Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                    .build();
            composite.add(operation);

            dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().changeAliasSuccess(alias, newAlias, resource)));
                        ModelNode aliases = result.step(1).get(RESULT);
                        if (aliases.isDefined()) {
                            callback.accept(aliases.asList());
                        } else {
                            callback.accept(Collections.emptyList());
                        }
                    },
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().changeAliasError(alias, newAlias, resource, failure))),
                    (op, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().changeAliasError(alias, newAlias, resource, ex.getMessage()))));

        }).show();
    }

    void exportCertificate(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(EXPORT_CERTIFICATE);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), EXPORT_CERTIFICATE), opMetadata)
                .build();
        form.attach();
        HTMLElement formElement = form.asElement();
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        form.edit(model);
        DialogFactory.buildConfirmation(resources.constants().exportCertificate(), question, formElement, Dialog.Size.MEDIUM, () -> {
            form.save();
            ResourceAddress address = template.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, EXPORT_CERTIFICATE)
                    .payload(form.getModel())
                    .build();
            String path = form.getModel().get(PATH).asString();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().exportCertificateSuccess(alias, path, resource))),
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().exportCertificateError(alias, path, resource, failure))),
                    (op, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().exportCertificateError(alias, path, resource, ex.getMessage()))));

        }).show();
    }

    void generateCSR(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(GENERATE_CERTIFICATE_SIGNING_REQUEST);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), GENERATE_CERTIFICATE_SIGNING_REQUEST), opMetadata)
                .build();
        form.attach();
        HTMLElement formElement = form.asElement();
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        form.edit(model);
        DialogFactory.buildConfirmation(resources.constants().generateCSR(), question, formElement, Dialog.Size.MEDIUM, () -> {
            form.save();
            ResourceAddress address = template.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, GENERATE_CERTIFICATE_SIGNING_REQUEST)
                    .payload(form.getModel())
                    .build();
            String path = form.getModel().get(PATH).asString();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().generateCSRSuccess(alias, path, resource))),
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().generateCSRError(alias, path, resource, failure))),
                    (op, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().generateCSRError(alias, path, resource, ex.getMessage()))));

        }).show();
    }

    void generateKeyPair(Metadata metadata, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(GENERATE_KEY_PAIR);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), GENERATE_KEY_PAIR), opMetadata)
                .build();
        form.attach();
        HTMLElement formElement = form.asElement();
        form.edit(new ModelNode());
        DialogFactory.buildConfirmation(resources.constants().generateKeyPair(), question, formElement,
                Dialog.Size.MEDIUM, () -> {
                    form.save();
                    ResourceAddress address = template.resolve(statementContext, name);
                    Composite composite = new Composite();
                    Operation operation = new Operation.Builder(address, GENERATE_KEY_PAIR)
                            .payload(form.getModel())
                            .build();
                    composite.add(operation);
                    Operation opRead = new Operation.Builder(address, READ_ALIASES_OPERATION)
                            .build();
                    composite.add(opRead);
                    String alias = form.getModel().get(ALIAS).asString();

                    dispatcher.execute(composite, (CompositeResult result) -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages().generateKeyPairSuccess(alias, resource)));
                                ModelNode aliases = result.step(1).get(RESULT);
                                if (aliases.isDefined()) {
                                    callback.accept(aliases.asList());
                                } else {
                                    callback.accept(Collections.emptyList());
                                }
                            },
                            (op, failure) -> MessageEvent.fire(getEventBus(),
                                    Message.error(
                                            resources.messages().generateKeyPairError(alias, resource, failure))),
                            (op, ex) -> MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages()
                                            .generateKeyPairError(alias, resource, ex.getMessage()))));

                }).show();
    }

    void importCertificate(Metadata metadata, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(IMPORT_CERTIFICATE);
        SafeHtml question = SafeHtmlUtils.fromString(opMetadata.getDescription().getDescription());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), IMPORT_CERTIFICATE), opMetadata)
                .build();
        HTMLElement formElement = form.asElement();
        form.attach();
        form.edit(new ModelNode());
        DialogFactory.buildConfirmation(resources.constants().importCertificate(), question, formElement,
                Dialog.Size.MEDIUM, () -> {
                    form.save();
                    ModelNode payload = form.getModel();
                    String path = payload.get(PATH).asString();
                    if (!payload.hasDefined(VALIDATE)) {
                        payload.get(VALIDATE).set(false);
                    }
                    ResourceAddress address = template.resolve(statementContext, name);
                    Composite composite = new Composite();
                    Operation operation = new Operation.Builder(address, IMPORT_CERTIFICATE)
                            .payload(payload)
                            .build();
                    composite.add(operation);
                    Operation opRead = new Operation.Builder(address, READ_ALIASES_OPERATION)
                            .build();
                    composite.add(opRead);
                    String alias = payload.get(ALIAS).asString();
                    dispatcher.execute(composite, (CompositeResult result) -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages().importCertificateSuccess(alias, path, resource)));
                                ModelNode aliases = result.step(1).get(RESULT);
                                if (aliases.isDefined()) {
                                    callback.accept(aliases.asList());
                                } else {
                                    callback.accept(Collections.emptyList());
                                }
                            },
                            (op, failure) -> MessageEvent.fire(getEventBus(),
                                    Message.error(
                                            resources.messages().importCertificateError(alias, path, resource, failure))),
                            (op, ex) -> MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages()
                                            .importCertificateError(alias, path, resource, ex.getMessage()))));

                }).show();
    }


    // ----------------- common methods

    void readAliases(AddressTemplate template, String resource, Consumer<List<ModelNode>> viewCallback) {
        ResourceAddress address = template.resolve(statementContext, resource);
        Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                .build();
        LabelBuilder labelBuilder = new LabelBuilder();
        String resourceName = labelBuilder.label(template.lastName()) + SPACE + resource;
        dispatcher.execute(operation, result -> {
                    if (result.isDefined()) {
                        viewCallback.accept(result.asList());
                    } else {
                        viewCallback.accept(Collections.emptyList());
                    }
                },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasesError(resourceName, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages()
                                .readAliasesError(resourceName, exception.getMessage()))));

    }

    void removeAlias(Metadata metadata, String name, String alias, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        LabelBuilder labelBuilder = new LabelBuilder();
        String resource = labelBuilder.label(template.lastName()) + SPACE + name;
        SafeHtml question = resources.messages().removeAliasQuestion(alias, resource);
        DialogFactory.showConfirmation(resources.constants().removeAlias(), question, () -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Composite composite = new Composite();
            Operation addOp = new Operation.Builder(address, REMOVE_ALIAS)
                    .param(ALIAS, alias)
                    .build();

            composite.add(addOp);
            Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                    .build();
            composite.add(operation);

            dispatcher.execute(composite, (CompositeResult result) -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSuccess(ALIAS, alias, resource)));
                        ModelNode aliases = result.step(1).get(RESULT);
                        if (aliases.isDefined()) {
                            callback.accept(aliases.asList());
                        } else {
                            callback.accept(Collections.emptyList());
                        }
                    },
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().addError(ALIAS, alias, resource, failure))),
                    (op, ex) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().addError(ALIAS, alias, resource, ex.getMessage()))));

        });
    }

    void readAlias(Metadata metadata, String name, String alias, Consumer<ModelNode> viewCallback) {
        AddressTemplate template = metadata.getTemplate();
        LabelBuilder labelBuilder = new LabelBuilder();
        String resource = labelBuilder.label(template.lastName()) + SPACE + name;
        ResourceAddress address = template.resolve(statementContext, name);
        Operation addOp = new Operation.Builder(address, READ_ALIAS)
                .param(ALIAS, alias)
                .build();
        dispatcher.execute(addOp, result -> viewCallback.accept(result),
                (operation, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasError(alias, resource, failure))),
                (operation, ex) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasError(alias, resource, ex.getMessage()))));

    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.ELYTRON_RUNTIME_STORES)
    @Requires({CREDENTIAL_STORE_ADDRESS, FILTERING_KEY_STORE_ADDRESS, KEY_STORE_ADDRESS, LDAP_KEY_STORE_ADDRESS})
    public interface MyProxy extends ProxyPlace<StoresPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<StoresPresenter> {
        void updateCredentialStore(List<NamedNode> items);
        void updateFilteringKeystore(List<NamedNode> items);
        void updateKeystore(List<NamedNode> items);
        void updateLdapKeystore(List<NamedNode> items);

    }
    // @formatter:on
}
