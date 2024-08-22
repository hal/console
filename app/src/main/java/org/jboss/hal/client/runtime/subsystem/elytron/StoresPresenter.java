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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.autocomplete.SuggestCapabilitiesAutoComplete;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
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
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CREDENTIAL_STORE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CREDENTIAL_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.ELYTRON_PROFILE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.FILTERING_KEY_STORE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.FILTERING_KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_STORE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.LDAP_KEY_STORE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.LDAP_KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECRET_KEY_CREDENTIAL_STORE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECRET_KEY_CREDENTIAL_STORE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_AUTHORITY_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANGE_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPORT_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPORT_SECRET_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GENERATE_CERTIFICATE_SIGNING_REQUEST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GENERATE_KEY_PAIR;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GENERATE_SECRET_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.IMPORT_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.IMPORT_SECRET_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_SIZE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OBTAIN_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ALIASES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVOKE_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SET_SECRET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHOULD_RENEW_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALIDATE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.inputGroup;
import static org.jboss.hal.resources.CSS.inputGroupBtn;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.UIConstants.BODY;
import static org.jboss.hal.resources.UIConstants.CONTAINER;
import static org.jboss.hal.resources.UIConstants.PLACEMENT;
import static org.jboss.hal.resources.UIConstants.TOGGLE;
import static org.jboss.hal.resources.UIConstants.TOOLTIP;
import static org.jboss.hal.resources.UIConstants.TOP;

public class StoresPresenter extends ApplicationFinderPresenter<StoresPresenter.MyView, StoresPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SPACE = " ";
    private final Environment environment;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Provider<Progress> progress;
    private final Dispatcher dispatcher;

    @Inject
    public StoresPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Resources resources,
            Finder finder,
            @Footer Provider<Progress> progress,
            Dispatcher dispatcher,
            Environment environment,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.progress = progress;
        this.dispatcher = dispatcher;
        this.environment = environment;
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
                .append(Ids.ELYTRON_RUNTIME, Ids.ELYTRON_STORES, Names.SECURITY, Names.STORES);
    }

    @Override
    protected void reload() {
        Composite composite = new Composite();
        composite.add(operation(CREDENTIAL_STORE_TEMPLATE));
        composite.add(operation(FILTERING_KEY_STORE_TEMPLATE));
        composite.add(operation(KEY_STORE_TEMPLATE));
        composite.add(operation(LDAP_KEY_STORE_TEMPLATE));
        composite.add(operation(SECRET_KEY_CREDENTIAL_STORE_TEMPLATE));
        dispatcher.execute(composite, (CompositeResult result) -> {
            int i = 0;
            getView().updateCredentialStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateFilteringKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateLdapKeystore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateSecretKeyCredentialStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
        });
    }

    // ----------------- credential store

    private Operation operation(AddressTemplate template) {
        return new Operation.Builder(template.getParent().resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(CHILD_TYPE, template.lastName())
                .build();
    }

    void reloadCredentialStore(String name) {
        Operation operation = new Operation.Builder(CREDENTIAL_STORE_TEMPLATE.resolve(statementContext, name), RELOAD)
                .build();
        String resource = Names.CREDENTIAL_STORE + " " + name;
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().reloadSuccess(resource)));
            reload();
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().reloadError(resource, failure))));
    }

    void addAlias(Metadata metadata, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        Metadata opMetadata = metadata.forOperation(ADD_ALIAS);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), ADD_ALIAS), opMetadata)
                .addOnly()
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
            }, (op, failure) -> MessageEvent.fire(getEventBus(),
                    Message.error(resources.messages().addError(ALIAS, alias, resource, failure))));

        });
        dialog.show();
    }

    void generateSecretKey(Metadata metadata, String storeType, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        Metadata opMetadata = metadata.forOperation(GENERATE_SECRET_KEY);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), GENERATE_SECRET_KEY), opMetadata)
                .addOnly()
                .build();
        form.attach();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().generateSecretKey(), form, (name1, model) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Composite composite = new Composite();

            if (!model.hasDefined(KEY_SIZE)) {
                model.remove(KEY_SIZE);
            }

            Operation addOp = new Operation.Builder(address, GENERATE_SECRET_KEY)
                    .payload(model)
                    .build();
            composite.add(addOp);
            Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                    .build();
            composite.add(operation);
            String alias = model.get(ALIAS).asString();
            String resource = storeType + SPACE + name;
            dispatcher.execute(composite, (CompositeResult result) -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().addSuccess(ALIAS, alias, resource)));
                ModelNode aliases = result.step(1).get(RESULT);
                if (aliases.isDefined()) {
                    callback.accept(aliases.asList());
                } else {
                    callback.accept(Collections.emptyList());
                }
            }, (op, failure) -> MessageEvent.fire(getEventBus(),
                    Message.error(resources.messages().addError(ALIAS, alias, resource, failure))));

        });
        dialog.show();
    }

    void exportSecretKey(Metadata metadata, String storeType, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = storeType + SPACE + name;

        ResourceAddress address = template.resolve(statementContext, name);
        ModelNode payload = new ModelNode();
        payload.get(ALIAS).set(alias);
        Operation operation = new Operation.Builder(address, EXPORT_SECRET_KEY)
                .payload(payload)
                .build();
        dispatcher.execute(operation, result -> {
            HTMLElement copyToClipboard;
            HTMLInputElement input;
            Dialog dialog = new Dialog.Builder(resources.constants().exportSecretKey())
                    .add(div().css(inputGroup)
                            .add(input = input("text").css(formControl)
                                    .readOnly(true)
                                    .element())
                            .add(span().css(inputGroupBtn)
                                    .add(copyToClipboard = button().css(btn, btnDefault)
                                            .data(TOGGLE, TOOLTIP)
                                            .data(CONTAINER, BODY)
                                            .data(PLACEMENT, TOP)
                                            .title(resources.constants().copyToClipboard())
                                            .add(i().css(fontAwesome("clipboard")))
                                            .element())
                                    .element())
                            .element())
                    .size(Dialog.Size.MEDIUM)
                    .closeOnEsc(true)
                    .cancel()
                    .build();

            input.value = result.get("key").asString();

            Clipboard.Options options = new Clipboard.Options();
            options.text = element -> input.value;
            options.container = dialog.element();

            Clipboard clipboard = new Clipboard(copyToClipboard, options);
            clipboard.on("success", event -> {
                Tooltip tooltip = Tooltip.element(copyToClipboard);
                tooltip.hide()
                        .setTitle(resources.constants().copied())
                        .show()
                        .onHide(() -> tooltip.setTitle(resources.constants().copyToClipboard()));
                setTimeout((o) -> tooltip.hide(), 3000);
            });

            dialog.show();
        },
                (op, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().exportSecretKeyError(alias, resource, failure))));
    }

    void importSecretKey(Metadata metadata, String storeType, String name, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        Metadata opMetadata = metadata.forOperation(IMPORT_SECRET_KEY);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), IMPORT_SECRET_KEY), opMetadata)
                .addOnly()
                .build();
        form.attach();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().importSecretKey(), form, (name1, model) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Composite composite = new Composite();
            Operation addOp = new Operation.Builder(address, IMPORT_SECRET_KEY)
                    .payload(model)
                    .build();
            composite.add(addOp);
            Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                    .build();
            composite.add(operation);
            String alias = model.get(ALIAS).asString();
            String resource = storeType + SPACE + name;
            dispatcher.execute(composite, (CompositeResult result) -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().addSuccess(ALIAS, alias, resource)));
                ModelNode aliases = result.step(1).get(RESULT);
                if (aliases.isDefined()) {
                    callback.accept(aliases.asList());
                } else {
                    callback.accept(Collections.emptyList());
                }
            }, (op, failure) -> MessageEvent.fire(getEventBus(),
                    Message.error(resources.messages().addError(ALIAS, alias, resource, failure))));

        });
        dialog.show();
    }

    // ----------------- key store

    void setSecret(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.CREDENTIAL_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(SET_SECRET);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), SET_SECRET), opMetadata)
                .build();
        form.attach();
        form.getFormItem(ALIAS).setEnabled(false);
        form.setSaveCallback((f, changedValues) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Operation op = new Operation.Builder(address, SET_SECRET)
                    .payload(f.getModel())
                    .build();
            dispatcher.execute(op, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().setSecretPasswordSuccess(alias, resource))),
                    (operation, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().setSecretPasswordError(alias, resource, failure))));
        });
        Dialog dialog = new Dialog.Builder(resources.constants().setSecret())
                .add(p().textContent(opMetadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().setSecret(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        ModelNode modelNode = new ModelNode();
        modelNode.get(ALIAS).set(alias);
        dialog.show();
        form.edit(modelNode);
    }

    void loadKeyStore(String name) {
        Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), LOAD)
                .build();
        String resource = Names.KEY_STORE + " " + name;
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().reloadSuccess(resource)));
            reload();
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().reloadError(resource, failure))));
    }

    void storeKeyStore(String name) {
        Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), STORE)
                .build();
        dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                Message.success(resources.messages().storeSuccess(name))),
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().storeError(name, failure))));
    }

    void changeAlias(Metadata metadata, String name, String alias, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        metadata = metadata.forOperation(CHANGE_ALIAS);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), CHANGE_ALIAS), metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            String newAlias = form.getModel().get("new-alias").asString();
            ResourceAddress address = template.resolve(statementContext, name);

            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, CHANGE_ALIAS)
                        .payload(form.getModel())
                        .build();
                return dispatcher.execute(operation).then(__ -> Promise.resolve(context));
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), STORE)
                        .build();
                return dispatcher.execute(operation).then(__ -> Promise.resolve(context));
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                        .build();
                return dispatcher.execute(operation).then(aliases -> context.resolve(aliases));
            });

            sequential(new FlowContext(progress.get()), tasks)
                    .then(context -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(
                                        resources.messages().changeAliasSuccess(alias, newAlias, resource)));
                        ModelNode aliases = context.pop(new ModelNode());
                        if (aliases.isDefined()) {
                            callback.accept(aliases.asList());
                        } else {
                            callback.accept(Collections.emptyList());
                        }
                        return null;
                    })
                    .catch_(error -> {
                        MessageEvent.fire(getEventBus(), Message.error(
                                resources.messages()
                                        .changeAliasError(alias, newAlias, resource, String.valueOf(error))));
                        return null;
                    });
        });
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        Dialog dialog = new Dialog.Builder(resources.constants().changeAlias())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().change(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(model);
    }

    void exportCertificate(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        metadata = metadata.forOperation(EXPORT_CERTIFICATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), EXPORT_CERTIFICATE), metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, EXPORT_CERTIFICATE)
                    .payload(form.getModel())
                    .build();
            String path = form.getModel().get(PATH).asString();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().exportCertificateSuccess(alias, path, resource))),
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().exportCertificateError(alias, path, resource, failure))));
        });
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        Dialog dialog = new Dialog.Builder(resources.constants().exportCertificate())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().export(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(model);
    }

    void generateCSR(Metadata metadata, String name, String alias) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        Metadata opMetadata = metadata.forOperation(GENERATE_CERTIFICATE_SIGNING_REQUEST);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(
                Ids.build(template.lastName(), GENERATE_CERTIFICATE_SIGNING_REQUEST), opMetadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, GENERATE_CERTIFICATE_SIGNING_REQUEST)
                    .payload(form.getModel())
                    .build();
            String path = form.getModel().get(PATH).asString();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().generateCSRSuccess(alias, path, resource))),
                    (op, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().generateCSRError(alias, path, resource, failure))));
        });
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        Dialog dialog = new Dialog.Builder(resources.constants().generateCSR())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().generate(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(model);

    }

    void generateKeyPair(Metadata metadata, String name) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        metadata = metadata.forOperation(GENERATE_KEY_PAIR);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), GENERATE_KEY_PAIR), metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = template.resolve(statementContext, name);
            String alias = form.getModel().get(ALIAS).asString();
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, GENERATE_KEY_PAIR)
                        .payload(form.getModel())
                        .build();
                return dispatcher.execute(operation).then(__ -> Promise.resolve(context));
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), STORE)
                        .build();
                return dispatcher.execute(operation).then(__ -> Promise.resolve(context));
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                        .build();
                return dispatcher.execute(operation).then(__ -> Promise.resolve(context));
            });

            sequential(new FlowContext(progress.get()), tasks)
                    .then(__ -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().generateKeyPairSuccess(alias, resource)));
                        return null;
                    })
                    .catch_(error -> {
                        MessageEvent.fire(getEventBus(),
                                Message.error(
                                        resources.messages()
                                                .generateKeyPairError(alias, resource, String.valueOf(error))));
                        return null;
                    });
        });

        Dialog dialog = new Dialog.Builder(resources.constants().generateKeyPair())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().generate(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    void importCertificate(Metadata metadata, String name) {
        AddressTemplate template = metadata.getTemplate();
        String resource = Names.KEY_STORE + SPACE + name;
        metadata = metadata.forOperation(IMPORT_CERTIFICATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), IMPORT_CERTIFICATE), metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ModelNode payload = form.getModel();
            String path = payload.get(PATH).asString();
            if (!payload.hasDefined(VALIDATE)) {
                payload.get(VALIDATE).set(false);
            }
            ResourceAddress address = template.resolve(statementContext, name);
            String alias = payload.get(ALIAS).asString();
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, IMPORT_CERTIFICATE)
                        .payload(payload)
                        .build();
                return dispatcher.execute(operation)
                        .then(__ -> Promise.resolve(context))
                        .catch_(error -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages()
                                            .importCertificateError(alias, path, resource, String.valueOf(error))));
                            return context.reject(String.valueOf(error));
                        });
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(KEY_STORE_TEMPLATE.resolve(statementContext, name), STORE)
                        .build();
                return dispatcher.execute(operation)
                        .then(__ -> Promise.resolve(context))
                        .catch_(error -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages().storeError(resource, String.valueOf(error))));
                            return context.reject(String.valueOf(error));
                        });
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                        .build();
                return dispatcher.execute(operation)
                        .then(__ -> Promise.resolve(context))
                        .catch_(error -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages().readAliasesError(resource, String.valueOf(error))));
                            return context.reject(String.valueOf(error));
                        });
            });

            sequential(new FlowContext(progress.get()), tasks)
                    .then(__ -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(
                                        resources.messages().importCertificateSuccess(alias, path, resource)));
                        return null;
                    })
                    .catch_(error -> {
                        MessageEvent.fire(getEventBus(),
                                Message.error(
                                        resources.messages().removeAliasError(alias, resource, String.valueOf(error))));
                        return null;
                    });
        });

        Dialog dialog = new Dialog.Builder(resources.constants().importCertificate())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().importt(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    void obtainCertificate(Metadata metadata, String name) {
        metadata = metadata.forOperation(OBTAIN_CERTIFICATE);
        String id = Ids.build(KEY_STORE, OBTAIN_CERTIFICATE, FORM);
        String title = new LabelBuilder().label(OBTAIN_CERTIFICATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = KEY_STORE_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, OBTAIN_CERTIFICATE)
                    .payload(form.getModel())
                    .build();
            String alias = form.getModel().get(ALIAS).asString();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().obtainCertificateSuccess(alias, name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().obtainCertificateError(alias, name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().obtain(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        if (!environment.isStandalone()) {
            // the capability reference the /profile=* resource and the template attached to the metadata
            // points to the {selected.host}/{selected.server}, so we need to register the template to the profile
            String capability = metadata.getDescription()
                    .attributes()
                    .capabilityReference(CERTIFICATE_AUTHORITY_ACCOUNT);
            form.getFormItem(CERTIFICATE_AUTHORITY_ACCOUNT)
                    .registerSuggestHandler(
                            new SuggestCapabilitiesAutoComplete(dispatcher, statementContext, capability,
                                    ELYTRON_PROFILE_TEMPLATE));
        }
        dialog.show();
        form.edit(new ModelNode());
    }

    void revokeCertificate(Metadata metadata, String name, String alias) {
        metadata = metadata.forOperation(REVOKE_CERTIFICATE);
        String id = Ids.build(KEY_STORE, REVOKE_CERTIFICATE, FORM);
        String title = new LabelBuilder().label(CERTIFICATE_AUTHORITY_ACCOUNT);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = KEY_STORE_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, REVOKE_CERTIFICATE)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().revokeCertificateSuccess(alias, name))),
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().revokeCertificateError(alias, name, failure))));
        });
        Dialog dialog = new Dialog.Builder(title)
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().revoke(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        if (!environment.isStandalone()) {
            // the capability reference the /profile=* resource and the template attached to the metadata
            // points to the {selected.host}/{selected.server}, so we need to register the template to the profile
            String capability = metadata.getDescription()
                    .attributes()
                    .capabilityReference(CERTIFICATE_AUTHORITY_ACCOUNT);
            form.getFormItem(CERTIFICATE_AUTHORITY_ACCOUNT)
                    .registerSuggestHandler(
                            new SuggestCapabilitiesAutoComplete(dispatcher, statementContext, capability,
                                    ELYTRON_PROFILE_TEMPLATE));
        }
        dialog.show();
        form.edit(model);
    }

    void verifyRenewCertificate(Metadata metadata, String name, String alias) {
        metadata = metadata.forOperation(SHOULD_RENEW_CERTIFICATE);
        String id = Ids.build(KEY_STORE, SHOULD_RENEW_CERTIFICATE, FORM);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .build();
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = KEY_STORE_TEMPLATE.resolve(statementContext, name);
            Operation operation = new Operation.Builder(address, SHOULD_RENEW_CERTIFICATE)
                    .payload(form.getModel())
                    .build();
            dispatcher.execute(operation, result -> {
                int days = result.get("days-to-expiry").asInt();
                Date dueDate = new Date();
                CalendarUtil.addDaysToDate(dueDate, days);

                HTMLElement content;
                if (days < 1) {
                    Alert warning = new Alert(Icons.WARNING, resources.messages().certificateExpired(alias));
                    content = div().add(warning).element();
                } else {
                    SafeHtml description = resources.messages()
                            .certificateShouldRenew(days, alias, Format.mediumDateTime(dueDate));
                    content = p().innerHtml(description).element();
                }

                new Dialog.Builder(resources.constants().verifyRenewCertificate())
                        .primary(resources.constants().ok(), null)
                        .size(Dialog.Size.MEDIUM)
                        .add(content)
                        .build()
                        .show();

            }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                    Message.error(resources.messages().verifyRenewError(alias, name, failure))));
        });
        Dialog dialog = new Dialog.Builder(resources.constants().verifyRenewCertificate())
                .add(p().textContent(metadata.getDescription().getDescription()).element())
                .add(form.element())
                .primary(resources.constants().verifyRenew(), form::save)
                .size(Dialog.Size.MEDIUM)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        ModelNode model = new ModelNode();
        model.get(ALIAS).set(alias);
        form.getFormItem(ALIAS).setEnabled(false);
        form.edit(model);
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
        }, (operation1, failure) -> MessageEvent.fire(getEventBus(),
                Message.error(resources.messages().readAliasesError(resourceName, failure))));

    }

    void removeAlias(Metadata metadata, String name, String alias, Consumer<List<ModelNode>> callback) {
        AddressTemplate template = metadata.getTemplate();
        LabelBuilder labelBuilder = new LabelBuilder();
        String resource = labelBuilder.label(template.lastName()) + SPACE + name;
        SafeHtml question = resources.messages().removeAliasQuestion(alias, resource);
        DialogFactory.showConfirmation(resources.constants().removeAlias(), question, () -> {
            ResourceAddress address = template.resolve(statementContext, name);

            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, REMOVE_ALIAS)
                        .param(ALIAS, alias)
                        .build();
                return dispatcher.execute(operation)
                        .then(__ -> Promise.resolve(context))
                        .catch_(error -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(
                                            resources.messages().removeAliasError(alias, resource, String.valueOf(error))));
                            return null;
                        });
            });
            tasks.add(context -> {
                Operation operation = new Operation.Builder(address, READ_ALIASES_OPERATION)
                        .build();
                return dispatcher.execute(operation)
                        .then(aliases -> context.resolve(aliases))
                        .catch_(error -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages().readAliasesError(resource, String.valueOf(error))));
                            return null;
                        });
            });

            sequential(new FlowContext(progress.get()), tasks)
                    .then(context -> {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().removeAliasSuccess(alias, resource)));
                        ModelNode aliases = context.pop();
                        if (aliases.isDefined()) {
                            callback.accept(aliases.asList());
                        } else {
                            callback.accept(Collections.emptyList());
                        }
                        return null;
                    })
                    .catch_(error -> {
                        MessageEvent.fire(getEventBus(),
                                Message.error(
                                        resources.messages().removeAliasError(alias, resource, String.valueOf(error))));
                        return null;
                    });
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
        dispatcher.execute(addOp, viewCallback,
                (operation, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasError(alias, resource, failure))));

    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.ELYTRON_RUNTIME_STORES)
    @Requires({ CREDENTIAL_STORE_ADDRESS, FILTERING_KEY_STORE_ADDRESS, KEY_STORE_ADDRESS, LDAP_KEY_STORE_ADDRESS,
            SECRET_KEY_CREDENTIAL_STORE_ADDRESS })
    public interface MyProxy extends ProxyPlace<StoresPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<StoresPresenter> {
        void updateCredentialStore(List<NamedNode> items);

        void updateFilteringKeystore(List<NamedNode> items);

        void updateKeystore(List<NamedNode> items);

        void updateLdapKeystore(List<NamedNode> items);

        void updateSecretKeyCredentialStore(List<NamedNode> items);

    }
    // @formatter:on
}
