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
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
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
import rx.Completable;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.series;

public class DestinationPresenter
        extends ServerSettingsPresenter<DestinationPresenter.MyView, DestinationPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";

    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private String securitySetting;

    @Inject
    public DestinationPresenter(
            EventBus eventBus,
            DestinationPresenter.MyView view,
            DestinationPresenter.MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            Resources resources) {
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
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER_CONFIGURATION, Ids.messagingServer(serverName),
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

    // ------------------------------------------------------ core queue

    // the custom add resource dialog is necessary because the "durable" and "filter" attributes are read-only
    // after created
    void addCoreQueue() {
        Metadata metadata = metadataRegistry.lookup(CORE_QUEUE_TEMPLATE);
        NameItem nameItem = new NameItem();
        ModelNodeForm form = new ModelNodeForm.Builder<>(Ids.build(Ids.MESSAGING_CORE_QUEUE, ADD), metadata)
                .fromRequestProperties()
                .unboundFormItem(nameItem, 0)
                .include(DURABLE, FILTER)
                .unsorted()
                .build();

        new AddResourceDialog(resources.messages().addResourceTitle(Names.CORE_QUEUE), form,
                (name, model) -> {
                    AddressTemplate template = SELECTED_SERVER_TEMPLATE.append("queue=" + nameItem.getValue());
                    ResourceAddress address = template.resolve(statementContext);
                    crud.add(Names.CORE_QUEUE, nameItem.getValue(), address, model, (name1, address1) -> reload());
                }).show();
    }

    // ------------------------------------------------------ jms queue

    // the custom add resource dialog is necessary because the "durable" and "selector" attributes are read-only
    // after created
    void addJMSQueue() {
        Metadata metadata = metadataRegistry.lookup(JMS_QUEUE_TEMPLATE);
        NameItem nameItem = new NameItem();
        ModelNodeForm form = new ModelNodeForm.Builder<>(Ids.build(Ids.MESSAGING_JMS_QUEUE, ADD), metadata)
                .fromRequestProperties()
                .unboundFormItem(nameItem, 0)
                .include(DURABLE, SELECTOR)
                .unsorted()
                .build();

        new AddResourceDialog(resources.messages().addResourceTitle(Names.JMS_QUEUE), form,
                (name, model) -> {
                    AddressTemplate template = SELECTED_SERVER_TEMPLATE.append("jms-queue=" + nameItem.getValue());
                    ResourceAddress address = template.resolve(statementContext);
                    crud.add(Names.JMS_QUEUE, nameItem.getValue(), address, model, (name1, address1) -> reload());
                }).show();
    }

    // ------------------------------------------------------ security setting

    void selectSecuritySetting(String securitySetting) {
        this.securitySetting = securitySetting;
    }

    void addSecuritySettingRole() {
        Metadata metadata = metadataRegistry.lookup(ROLE_TEMPLATE);
        TextBoxItem patternItem = new TextBoxItem(PATTERN, Names.PATTERN);
        patternItem.setRequired(true);
        TextBoxItem roleItem = new TextBoxItem(ROLE, resources.constants().role());
        roleItem.setRequired(true);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.MESSAGING_SECURITY_SETTING_ROLE_ADD, metadata)
                .unboundFormItem(patternItem, 0)
                .unboundFormItem(roleItem, 1)
                .fromRequestProperties()
                .requiredOnly()
                .build();

        new AddResourceDialog(Names.SECURITY_SETTING, form, (name, model) -> {
            String pattern = patternItem.getValue();
            ResourceAddress securitySettingAddress = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + EQUALS + pattern)
                    .resolve(statementContext);
            ResourceAddress roleAddress = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + EQUALS + pattern)
                    .append(ROLE + EQUALS + roleItem.getValue())
                    .resolve(statementContext);

            ResourceCheck check = new ResourceCheck(dispatcher, securitySettingAddress);
            Task<FlowContext> add = context -> {
                Operation addSecuritySetting = new Operation.Builder(securitySettingAddress, ADD).build();
                Operation addRole = new Operation.Builder(roleAddress, ADD).payload(model).build();

                int status = context.pop();
                if (status == 404) {
                    return dispatcher.execute(new Composite(addSecuritySetting, addRole)).toCompletable();
                } else {
                    return dispatcher.execute(addRole).toCompletable();
                }
            };

            series(new FlowContext(progress.get()), check, add)
                    .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                        @Override
                        public void onSuccess(FlowContext context) {
                            MessageEvent.fire(getEventBus(), Message.success(resources.messages()
                                    .addResourceSuccess(Names.SECURITY_SETTING, pattern + "/" + name)));
                            reload();
                        }
                    });
        }).show();
    }

    void saveSecuritySettingRole(Form<NamedNode> form, Map<String, Object> changedValues) {
        if (securitySetting != null) {
            String name = form.getModel().getName();
            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + EQUALS + securitySetting)
                    .append(ROLE + EQUALS + name)
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
            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                    .append(SECURITY_SETTING + EQUALS + securitySetting)
                    .append(ROLE + EQUALS + name)
                    .resolve(statementContext);
            Metadata metadata = metadataRegistry.lookup(ROLE_TEMPLATE);
            crud.reset(Names.SECURITY_SETTING, securitySetting + "/" + name, address, form, metadata,
                    new FinishReset<NamedNode>(form) {
                        @Override
                        public void afterReset(final Form<NamedNode> form) {
                            reload();
                        }
                    });
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
                        Task<FlowContext> removeRole = context -> {
                            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                    .append(SECURITY_SETTING + EQUALS + securitySetting)
                                    .append(ROLE + EQUALS + roleName)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(address, REMOVE).build();
                            return dispatcher.execute(operation).toCompletable();
                        };

                        Task<FlowContext> readRemainingRoles = context -> {
                            ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                    .append(SECURITY_SETTING + EQUALS + securitySetting)
                                    .resolve(statementContext);
                            Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                                    .param(CHILD_TYPE, ROLE)
                                    .build();
                            return dispatcher.execute(operation)
                                    .doOnSuccess(result -> context.push(result.asList()))
                                    .toCompletable();
                        };

                        Task<FlowContext> removeSecuritySetting = context -> {
                            List<ModelNode> roles = context.pop();
                            if (roles.isEmpty()) {
                                ResourceAddress address = SELECTED_SERVER_TEMPLATE
                                        .append(SECURITY_SETTING + EQUALS + securitySetting)
                                        .resolve(statementContext);
                                Operation operation = new Operation.Builder(address, REMOVE).build();
                                return dispatcher.execute(operation).toCompletable();
                            } else {
                                return Completable.complete();
                            }
                        };

                        series(new FlowContext(progress.get()),
                                removeRole, readRemainingRoles, removeSecuritySetting)
                                .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        MessageEvent.fire(getEventBus(), Message.success(resources.messages()
                                                .removeResourceSuccess(Names.SECURITY_SETTING, combinedName)));
                                        reload();
                                    }
                                });
                    });

        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noSecuritySettingSelected()));
        }
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires({ADDRESS_SETTING_ADDRESS,
            CORE_QUEUE_ADDRESS,
            DIVERT_ADDRESS,
            JMS_QUEUE_ADDRESS,
            JMS_TOPIC_ADDRESS,
            SECURITY_SETTING_ADDRESS})
    @NameToken(NameTokens.MESSAGING_SERVER_DESTINATION)
    public interface MyProxy extends ProxyPlace<DestinationPresenter> {
    }

    public interface MyView extends MbuiView<DestinationPresenter> {
        void updateCoreQueue(List<NamedNode> coreQueues);
        void updateJmsQueue(List<NamedNode> jmsQueues);
        void updateJmsTopic(List<NamedNode> jmsTopics);
        void updateSecuritySetting(List<NamedNode> securitySettings);
        void updateAddressSetting(List<NamedNode> addressSettings);
        void updateDivert(List<NamedNode> diverts);
    }
    // @formatter:on
}
