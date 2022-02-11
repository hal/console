/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.runtime.subsystem.elytron.wizardpassword.PasswordWizard;
import org.jboss.hal.core.SuccessfulOutcome;
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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.Names.IDENTITY_ATTRIBUTE_MAPPING;

public class RealmsPresenter extends ApplicationFinderPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SPACE = " ";

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private Resources resources;
    private Provider<Progress> progress;
    private Dispatcher dispatcher;

    @Inject
    public RealmsPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Resources resources,
            Finder finder,
            @Footer Provider<Progress> progress,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.progress = progress;
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
                .append(Ids.RUNTIME_SUBSYSTEM, SECURITY, resources.constants().monitor(), Names.SECURITY)
                .append(Ids.ELYTRON_RUNTIME, Ids.ELYTRON_SECURITY_REALMS, Names.SECURITY, Names.SECURITY_REALMS);
    }

    @Override
    protected void reload() {
        Composite composite = new Composite();
        composite.add(operation(CACHING_REALM_TEMPLATE));
        composite.add(operation(CUSTOM_MODIFIABLE_REALM_TEMPLATE));
        composite.add(operation(FILESYSTEM_REALM_TEMPLATE));
        composite.add(operation(LDAP_REALM_TEMPLATE));
        composite.add(operation(PROPERTIES_REALM_TEMPLATE));
        dispatcher.execute(composite, (CompositeResult result) -> {
            int i = 0;
            getView().updateCachingRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateCustomModifiableRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateFilesystemRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateLdapRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updatePropertiesRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
        });
    }

    private Operation operation(AddressTemplate template) {
        return new Operation.Builder(template.getParent().resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(CHILD_TYPE, template.lastName())
                .build();
    }

    void clearCache(String name) {
        Operation operation = new Operation.Builder(CACHING_REALM_TEMPLATE.resolve(statementContext, name), CLEAR_CACHE)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().clearCacheSuccess(name)));
            reload();
        },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().clearCacheError(name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().clearCacheError(name, exception.getMessage()))));
    }

    void loadProperties(String name) {
        Operation operation = new Operation.Builder(PROPERTIES_REALM_TEMPLATE.resolve(statementContext, name), LOAD)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().loadPropertiesRealmSuccess(name)));
            reload();
        },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().loadPropertiesRealmError(name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().loadPropertiesRealmError(name, exception.getMessage()))));
    }

    void addIdentity(AddressTemplate template, Metadata metadata, String name) {
        Metadata opMetadata = metadata.forOperation(ADD_IDENTITY);
        SafeHtml identityAttributeHelp = resources.messages().identityAttributeHelp();
        IdentityAttributeItem identityAttribute = new IdentityAttributeItem(Ids.asId(IDENTITY_ATTRIBUTE_MAPPING),
                IDENTITY_ATTRIBUTE_MAPPING);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), ADD_IDENTITY), opMetadata)
                .unboundFormItem(identityAttribute, 1, identityAttributeHelp)
                .build();
        form.attach();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().addIdentity(), form, (name1, model) -> {

            LabelBuilder labelBuilder = new LabelBuilder();
            String resourceName = labelBuilder.label(template.lastName()) + SPACE + name;
            String identity = model.get(IDENTITY).asString();
            ResourceAddress address = template.resolve(statementContext, name);
            List<Task<FlowContext>> tasks = new ArrayList<>();
            Task<FlowContext> addTask = flowContext -> {
                Operation addOp = new Operation.Builder(address, ADD_IDENTITY)
                        .param(IDENTITY, identity)
                        .build();

                return dispatcher.execute(addOp)
                        .doOnError(ex -> MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages()
                                        .addError(resources.constants().identity(), identity, resourceName,
                                                ex.getMessage()))))
                        .toCompletable();
            };
            tasks.add(addTask);

            if (identityAttribute.getValue() != null) {
                identityAttribute.getValue().forEach((key, values) -> {
                    Task<FlowContext> addAttribute = flowContext -> {
                        ModelNode modelValues = new ModelNode();
                        values.forEach(modelValues::add);
                        Operation addIdentAttributeOp = new Operation.Builder(address, ADD_IDENTITY_ATTRIBUTE)
                                .param(IDENTITY, identity)
                                .param(NAME, key)
                                .param(VALUE, modelValues)
                                .build();

                        return dispatcher.execute(addIdentAttributeOp)
                                .doOnError(ex -> MessageEvent.fire(getEventBus(),
                                        Message.error(resources.messages()
                                                .addError(resources.constants().identity(), identity, resourceName,
                                                        ex.getMessage()))))
                                .toCompletable();

                    };
                    tasks.add(addAttribute);
                });
            }

            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                        @Override
                        public void onSuccess(FlowContext flowContext) {
                            MessageEvent.fire(getEventBus(),
                                    Message.success(resources.messages()
                                            .addSuccess(resources.constants().identity(), identity, resourceName)));
                        }

                        @Override
                        public void onError(FlowContext context, Throwable throwable) {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages()
                                            .addError(resources.constants().identity(), identity, resourceName,
                                                    throwable.getMessage())));
                        }
                    });
        });
        dialog.show();
    }

    void editIdentity(Metadata metadata, String resource, String title, Consumer<ModelNode> callback) {
        AddressTemplate template = metadata.getTemplate();
        Metadata opMetadata = metadata.forOperation(READ_IDENTITY);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(template.lastName(), READ_IDENTITY), opMetadata)
                .build();
        form.attach();

        Dialog dialog = new Dialog.Builder(title)
                .add(form.element())
                .primary(resources.constants().search(), form::save)
                .size(Dialog.Size.MEDIUM)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        form.setSaveCallback((form1, changedValues) -> {
            ResourceAddress address = template.resolve(statementContext, resource);
            String identity = form.getModel().get(IDENTITY).asString();
            Operation operation = new Operation.Builder(address, READ_IDENTITY)
                    .param(IDENTITY, identity)
                    .build();
            LabelBuilder labelBuilder = new LabelBuilder();
            String resourceName = labelBuilder.label(template.lastName()) + SPACE + resource;
            dispatcher.execute(operation, callback::accept,
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().readIdentityError(identity, resourceName, failure))),
                    (operation1, exception) -> MessageEvent.fire(getEventBus(),
                            Message.error(
                                    resources.messages().readIdentityError(identity, resourceName, exception.getMessage()))));
        });
        form.edit(new ModelNode());
        dialog.show();
    }

    void saveIdentity(Metadata metadata, String resource, String identity, Map<String, List<String>> originalAttributes,
            Map<String, List<String>> attributes, Consumer<Boolean> viewCallback) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String resourceName = labelBuilder.label(metadata.getTemplate().lastName()) + SPACE + resource;
        ResourceAddress address = metadata.getTemplate().resolve(statementContext, resource);
        // remove the old attributes and add the attribute list
        List<Task<FlowContext>> tasks = new ArrayList<>();
        // compare the original values to the new values, to see which one to remove or add
        originalAttributes.forEach((key, originalValues) -> {
            List<String> newValues = attributes.get(key);
            if (!originalValues.equals(newValues)) {
                // remove the key tag if it is not present, the user deleted it

                Task<FlowContext> addTask = flowContext -> {
                    Operation operation = new Operation.Builder(address, REMOVE_IDENTITY_ATTRIBUTE)
                            .param(IDENTITY, identity)
                            .param(NAME, key)
                            .build();

                    return dispatcher.execute(operation)
                            .doOnError(ex -> MessageEvent.fire(getEventBus(), Message.error(
                                    resources.messages().saveIdentityError(identity, resourceName, ex.getMessage()))))
                            .toCompletable();
                };
                tasks.add(addTask);
            }
        });
        attributes.forEach((name, newValues) -> {
            List<String> originalValues = originalAttributes.get(name);
            if (!newValues.equals(originalValues)) {

                Task<FlowContext> addTask = flowContext -> {
                    ModelNode modelValues = new ModelNode();
                    newValues.forEach(modelValues::add);
                    Operation operation = new Operation.Builder(address, ADD_IDENTITY_ATTRIBUTE)
                            .param(IDENTITY, identity)
                            .param(NAME, name)
                            .param(VALUE, modelValues)
                            .build();

                    return dispatcher.execute(operation)
                            .doOnError(ex -> MessageEvent.fire(getEventBus(), Message.error(
                                    resources.messages().saveIdentityError(identity, resourceName, ex.getMessage()))))
                            .toCompletable();
                };
                tasks.add(addTask);
            }
        });
        if (tasks.isEmpty()) {
            MessageEvent.fire(getEventBus(), Message.warning(resources.messages().noChanges()));
        } else {

            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                        @Override
                        public void onSuccess(FlowContext flowContext) {
                            viewCallback.accept(true);
                            MessageEvent.fire(getEventBus(),
                                    Message.success(resources.messages().saveIdentitySuccess(identity, resourceName)));
                        }

                        @Override
                        public void onError(FlowContext context, Throwable throwable) {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages()
                                            .saveIdentityError(identity, resourceName, throwable.getMessage())));
                        }
                    });
        }
    }

    public void launchSetPasswordWizard(Metadata metadata, String selectedRealm, String selectedIdentity) {
        PasswordWizard wizard = new PasswordWizard(resources, statementContext, dispatcher, getEventBus(),
                metadata, selectedRealm, selectedIdentity);
        wizard.show();
    }

    public void removeIdentity(Metadata metadata, String realm, String identity, Consumer<Boolean> consumer) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String resourceName = labelBuilder.label(metadata.getTemplate().lastName()) + SPACE + realm;
        SafeHtml question = resources.messages().removeIdentityQuestion(identity, resourceName);
        DialogFactory.showConfirmation(resources.constants().removeIdentity(), question, () -> {
            ResourceAddress address = metadata.getTemplate().resolve(statementContext, realm);
            Operation operation = new Operation.Builder(address, REMOVE_IDENTITY)
                    .param(IDENTITY, identity)
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().removeIdentitySuccess(identity, resourceName)));
                consumer.accept(true);
            },
                    (operation1, failure) -> MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().removeIdentityError(identity, resourceName, failure))),
                    (operation1, exception) -> MessageEvent.fire(getEventBus(),
                            Message.error(
                                    resources.messages().removeIdentityError(identity, resourceName, exception.getMessage()))));

        });
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.ELYTRON_RUNTIME_SECURITY_REALMS)
    @Requires({ CACHING_REALM_ADDRESS, CUSTOM_MODIFIABLE_REALM_ADDRESS, FILESYSTEM_REALM_ADDRESS, LDAP_REALM_ADDRESS,
            PROPERTIES_REALM_ADDRESS })
    public interface MyProxy extends ProxyPlace<RealmsPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<RealmsPresenter> {
        void updateCachingRealm(List<NamedNode> items);

        void updateCustomModifiableRealm(List<NamedNode> items);

        void updateFilesystemRealm(List<NamedNode> items);

        void updateLdapRealm(List<NamedNode> items);

        void updatePropertiesRealm(List<NamedNode> items);

    }
    // @formatter:on
}
