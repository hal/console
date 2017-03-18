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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.ResourceCheck;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.ROLE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class DestinationPresenter
        extends ServerSettingsPresenter<DestinationPresenter.MyView, DestinationPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    // TODO Replace with
    // TODO value = {CORE_QUEUE_ADDRESS, JMS_QUEUE_ADDRESS, JMS_TOPIC_ADDRESS,
    // TODO         SECURITY_SETTING_ADDRESS, ADDRESS_SETTING_ADDRESS, DIVERT_ADDRESS}
    // TODO once WFCORE-2022 is resolved
    @Requires(value = SERVER_ADDRESS)
    @NameToken(NameTokens.MESSAGING_SERVER_DESTINATION)
    public interface MyProxy extends ProxyPlace<DestinationPresenter> {}

    public interface MyView extends MbuiView<DestinationPresenter> {
        void updateCoreQueue(List<NamedNode> coreQueues);
        void updateJmsQueue(List<NamedNode> jmsQueues);
        void updateJmsTopic(List<NamedNode> jmsTopics);
        void updateSecuritySetting(List<NamedNode> securitySettings);
        void updateAddressSetting(List<NamedNode> addressSettings);
        void updateDivert(List<NamedNode> diverts);
    }
    // @formatter:on


    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private String securitySetting;

    @Inject
    public DestinationPresenter(
            final EventBus eventBus,
            final DestinationPresenter.MyView view,
            final DestinationPresenter.MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        super(eventBus, view, myProxy, finder, crud, metadataRegistry, finderPathFactory, statementContext, resources);
        this.dispatcher = dispatcher;
        this.progress = progress;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER, Ids.messagingServer(serverName),
                        Names.SERVER, serverName)
                .append(Ids.MESSAGING_SERVER_SETTINGS, Ids.MESSAGING_SERVER_DESTINATION,
                        resources.constants().settings(), Names.DESTINATIONS);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(QUEUE, JMS_QUEUE, JMS_TOPIC, SECURITY_SETTING, ADDRESS_SETTING, DIVERT), 2,
                result -> {
                    getView().updateCoreQueue(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateJmsQueue(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateJmsTopic(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateSecuritySetting(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateAddressSetting(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateDivert(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                });
    }

    // ------------------------------------------------------ security setting

    void selectSecuritySetting(String securitySetting) {
        this.securitySetting = securitySetting;
    }

    void addSecuritySettingRole() {
        Metadata metadata = metadataRegistry.lookup(ROLE_TEMPLATE);
        TextBoxItem patternItem = new TextBoxItem(PATTERN, Names.PATTERN);
        patternItem.setRequired(true);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.MESSAGING_SECURITY_SETTING_ROLE_ADD, metadata)
                .unboundFormItem(patternItem, 0)
                .unboundFormItem(nameItem, 1)
                .addFromRequestProperties()
                .requiredOnly()
                .build();

        new AddResourceDialog(Names.SECURITY_SETTING, form, (name, model) -> {
            String pattern = patternItem.getValue();
            ResourceAddress securitySettingAddress = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + "=" + pattern)
                    .resolve(statementContext);
            ResourceAddress roleAddress = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + "=" + pattern)
                    .append(ROLE + "=" + name)
                    .resolve(statementContext);

            ResourceCheck check = new ResourceCheck(dispatcher, securitySettingAddress);
            Function<FunctionContext> add = control -> {
                Operation addSecuritySetting = new Operation.Builder(ADD, securitySettingAddress).build();
                Operation addRole = new Operation.Builder(ADD, roleAddress).payload(model).build();

                int status = control.getContext().pop();
                if (status == 404) {
                    dispatcher.executeInFunction(control, new Composite(addSecuritySetting, addRole),
                            (CompositeResult result) -> control.proceed());
                } else {
                    dispatcher.executeInFunction(control, addRole, result -> control.proceed());
                }
            };

            new Async<FunctionContext>(progress.get()).waterfall(
                    new FunctionContext(),
                    new SuccessfulOutcome(getEventBus(), resources) {
                        @Override
                        public void onSuccess(final FunctionContext context) {
                            MessageEvent.fire(getEventBus(), Message.success(resources.messages()
                                    .addResourceSuccess(Names.SECURITY_SETTING, pattern + "/" + name)));
                            reload();
                        }
                    },
                    check, add);
        }).show();
    }

    void saveSecuritySettingRole(Form<NamedNode> form, Map<String, Object> changedValues) {
        if (securitySetting != null) {
            String name = form.getModel().getName();
            ResourceAddress address = SERVER_TEMPLATE
                    .append(SECURITY_SETTING + "=" + securitySetting)
                    .append(ROLE + "=" + name)
                    .resolve(statementContext);
            Metadata metadata = metadataRegistry.lookup(ROLE_TEMPLATE);
            crud.save(Names.SECURITY_SETTING, securitySetting + "/" + name, address, changedValues, metadata,
                    this::reload);
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noSecuritySettingSelected()));
        }
    }

    void resetSecuritySettingRole(Form<NamedNode> form) {
        if (securitySetting != null) {
            String name = form.getModel().getName();
            ResourceAddress address = SERVER_TEMPLATE
                    .append(SECURITY_SETTING + "=" + securitySetting)
                    .append(ROLE + "=" + name)
                    .resolve(statementContext);
            Metadata metadata = metadataRegistry.lookup(ROLE_TEMPLATE);
            crud.reset(Names.SECURITY_SETTING, securitySetting + "/" + name, address, form, metadata,
                    this::reload);
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noSecuritySettingSelected()));
        }
    }

    void removeSecuritySettingRole(NamedNode role) {
        if (securitySetting != null) {
            String roleName = role.getName();
            String combinedName = securitySetting + "/" + roleName;

            DialogFactory.showConfirmation(
                    resources.messages().removeConfirmationTitle(Names.SECURITY_SETTING),
                    resources.messages().removeConfirmationQuestion(combinedName),
                    () -> {
                        Function<FunctionContext> removeRole = control -> {
                            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                    .append(SECURITY_SETTING + "=" + securitySetting)
                                    .append(ROLE + "=" + roleName)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(REMOVE, address).build();
                            dispatcher.executeInFunction(control, operation, result -> control.proceed());
                        };

                        Function<FunctionContext> readRemainingRoles = control -> {
                            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                    .append(SECURITY_SETTING + "=" + securitySetting)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                                    .param(CHILD_TYPE, ROLE)
                                    .build();
                            dispatcher.executeInFunction(control, operation, result -> {
                                control.getContext().push(result.asList());
                                control.proceed();
                            });
                        };

                        Function<FunctionContext> removeSecuritySetting = control -> {
                            List<ModelNode> roles = control.getContext().pop();
                            if (roles.isEmpty()) {
                                ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                        .append(SECURITY_SETTING + "=" + securitySetting)
                                        .resolve(statementContext);
                                Operation operation = new Operation.Builder(REMOVE, address).build();
                                dispatcher.executeInFunction(control, operation, result -> control.proceed());
                            } else {
                                control.proceed();
                            }
                        };

                        new Async<FunctionContext>(progress.get()).waterfall(
                                new FunctionContext(),
                                new SuccessfulOutcome(getEventBus(), resources) {
                                    @Override
                                    public void onSuccess(final FunctionContext context) {
                                        MessageEvent.fire(getEventBus(), Message.success(resources.messages()
                                                .removeResourceSuccess(Names.SECURITY_SETTING, combinedName)));
                                        reload();
                                    }
                                },
                                removeRole, readRemainingRoles, removeSecuritySetting);
                    });

        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noSecuritySettingSelected()));
        }
    }
}
