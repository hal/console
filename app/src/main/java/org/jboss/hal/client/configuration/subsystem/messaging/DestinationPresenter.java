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

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class DestinationPresenter extends MbuiPresenter<DestinationPresenter.MyView, DestinationPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value = SERVER_ADDRESS)
    @NameToken(NameTokens.MESSAGING_SERVER_DESTINATION)
    public interface MyProxy extends ProxyPlace<DestinationPresenter> {}

    public interface MyView extends MbuiView<DestinationPresenter> {
        void updateCoreQueues(List<NamedNode> coreQueues);
        void updateJmsQueues(List<NamedNode> jmsQueues);
        void updateJmsTopics(List<NamedNode> jmsTopics);
        void updateSecuritySettings(List<NamedNode> securitySettings);
        void updateAddressSettings(List<NamedNode> addressSettings);
        void updateDiverts(List<NamedNode> diverts);
    }
    // @formatter:on


    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String serverName;

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
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> serverName);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        serverName = request.getParameter(SERVER, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
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
        Operation coreQueueOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, QUEUE).param(RECURSIVE, true).build();
        Operation jmsQueueOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, JMS_QUEUE).param(RECURSIVE, true).build();
        Operation jmsTopicOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, JMS_TOPIC).param(RECURSIVE, true).build();
        Operation securitySettingOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, SECURITY_SETTING).param(RECURSIVE, true).build();
        Operation addressSettingOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, ADDRESS_SETTING).param(RECURSIVE, true).build();
        Operation divertOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, address)
                .param(CHILD_TYPE, DIVERT).param(RECURSIVE, true).build();

        dispatcher.execute(
                new Composite(coreQueueOp, jmsQueueOp, jmsTopicOp, securitySettingOp, addressSettingOp, divertOp),
                (CompositeResult result) -> {
                    getView().updateCoreQueues(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateJmsQueues(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateJmsTopics(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateSecuritySettings(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateAddressSettings(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateDiverts(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                });
    }

    // ------------------------------------------------------ core queue

    void addCoreQueue() {
        Metadata metadata = metadataRegistry.lookup(CORE_QUEUE_TEMPLATE);
        new AddResourceDialog(Ids.CORE_QUEUE_ADD, Names.CORE_QUEUE, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(QUEUE + "=" + name).resolve(statementContext);
            crud.add(Names.CORE_QUEUE, name, address, model, (n, a) -> reload());
        }).show();
    }

    void saveCoreQueue(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(QUEUE + "=" + name).resolve(statementContext);
        crud.save(Names.CORE_QUEUE, name, address, changedValues, this::reload);
    }

    void removeCoreQueue(NamedNode item) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(QUEUE + "=" + name).resolve(statementContext);
        crud.remove(Names.CORE_QUEUE, name, address, this::reload);
    }

    // ------------------------------------------------------ jms queue

    void addJmsQueue() {
        Metadata metadata = metadataRegistry.lookup(JMS_QUEUE_TEMPLATE);
        new AddResourceDialog(Ids.JMS_QUEUE_ADD, Names.JMS_QUEUE, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_QUEUE + "=" + name).resolve(statementContext);
            crud.add(Names.JMS_QUEUE, name, address, model, (n, a) -> reload());
        }).show();
    }

    void saveJmsQueue(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_QUEUE + "=" + name).resolve(statementContext);
        crud.save(Names.JMS_QUEUE, name, address, changedValues, this::reload);
    }

    void removeJmsQueue(NamedNode item) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_QUEUE + "=" + name).resolve(statementContext);
        crud.remove(Names.JMS_QUEUE, name, address, this::reload);
    }

    // ------------------------------------------------------ jms topic

    void addJmsTopic() {
        Metadata metadata = metadataRegistry.lookup(JMS_TOPIC_TEMPLATE);
        new AddResourceDialog(Ids.JMS_TOPIC_ADD, Names.JMS_TOPIC, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_TOPIC + "=" + name).resolve(statementContext);
            crud.add(Names.JMS_TOPIC, name, address, model, (n, a) -> reload());
        }).show();
    }

    void saveJmsTopic(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_TOPIC + "=" + name).resolve(statementContext);
        crud.save(Names.JMS_TOPIC, name, address, changedValues, this::reload);
    }

    void removeJmsTopic(NamedNode item) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(JMS_TOPIC + "=" + name).resolve(statementContext);
        crud.remove(Names.JMS_TOPIC, name, address, this::reload);
    }

    // ------------------------------------------------------ address setting

    void addAddressSetting() {
        Metadata metadata = metadataRegistry.lookup(ADDRESS_SETTING_TEMPLATE);
        new AddResourceDialog(Ids.ADDRESS_SETTING_ADD, Names.ADDRESS_SETTING, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ADDRESS_SETTING + "=" + name)
                    .resolve(statementContext);
            crud.add(Names.ADDRESS_SETTING, name, address, model, (n, a) -> reload());
        }).show();
    }

    void saveAddressSetting(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ADDRESS_SETTING + "=" + name)
                .resolve(statementContext);
        crud.save(Names.ADDRESS_SETTING, name, address, changedValues, this::reload);
    }

    void removeAddressSetting(NamedNode item) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(ADDRESS_SETTING + "=" + name)
                .resolve(statementContext);
        crud.remove(Names.ADDRESS_SETTING, name, address, this::reload);
    }

    // ------------------------------------------------------ divert

    void addDivert() {
        Metadata metadata = metadataRegistry.lookup(DIVERT_TEMPLATE);
        new AddResourceDialog(Ids.DIVERT_ADD, Names.DIVERT, metadata, (name, model) -> {
            ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(DIVERT + "=" + name).resolve(statementContext);
            crud.add(Names.DIVERT, name, address, model, (n, a) -> reload());
        }).show();
    }

    void saveDivert(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(DIVERT + "=" + name).resolve(statementContext);
        crud.save(Names.DIVERT, name, address, changedValues, this::reload);
    }

    void removeDivert(NamedNode item) {
        String name = item.getName();
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(DIVERT + "=" + name).resolve(statementContext);
        crud.remove(Names.DIVERT, name, address, this::reload);
    }
}
