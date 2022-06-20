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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.LIVE_ONLY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.REPLICATION_COLOCATED_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.REPLICATION_COLOCATED_PRIMARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.REPLICATION_COLOCATED_SECONDARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.REPLICATION_PRIMARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.REPLICATION_SECONDARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SHARED_STORE_COLOCATED_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SHARED_STORE_COLOCATED_PRIMARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SHARED_STORE_COLOCATED_SECONDARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SHARED_STORE_PRIMARY_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SHARED_STORE_SECONDARY_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGING_ACTIVEMQ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

public class HaPolicyPresenter
        extends ApplicationFinderPresenter<HaPolicyPresenter.MyView, HaPolicyPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final Logger logger = LoggerFactory.getLogger(HaPolicyPresenter.class);

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private String serverName;
    private HaPolicy haPolicy; // the 'top-level' policy selected in the wizard - not one of the *_COLOCATED_(PRIMARY|SECONDARY)
                               // policies

    @Inject
    public HaPolicyPresenter(EventBus eventBus,
            HaPolicyPresenter.MyView view,
            HaPolicyPresenter.MyProxy proxy_,
            Finder finder,
            Dispatcher dispatcher,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, proxy_, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> serverName);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        serverName = request.getParameter(SERVER, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.SERVER),
                        resources.constants().category(), Names.SERVER)
                .append(Ids.MESSAGING_SERVER_CONFIGURATION, Ids.messagingServer(serverName),
                        Names.SERVER, serverName)
                .append(Ids.MESSAGING_SERVER_SETTINGS, Ids.MESSAGING_SERVER_HA_POLICY,
                        resources.constants().settings(), Names.HA_POLICY);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.resolve(statementContext);
        HaPolicy.readChildren(crud, address, 2, children -> {
            if (children.isEmpty()) {
                haPolicy = null;
                getView().empty();
            } else {
                Property child = children.get(0);
                haPolicy = HaPolicy.fromResourceName(child.getName());
                getView().update(haPolicy, child.getValue());
            }
        });
    }

    void addHaPolicy() {
        new HaPolicyWizard(resources, (wizard, context) -> context.haPolicy.add(dispatcher, statementContext,
                () -> {
                    MessageEvent.fire(getEventBus(),
                            Message.success(resources.messages().addSingleResourceSuccess(context.haPolicy.type)));
                    reload();
                })).show();
    }

    void saveHaPolicy(HaPolicy haPolicy, Map<String, Object> changedValues) {
        haPolicy.save(changedValues, metadataRegistry, statementContext, crud, this::reload);
    }

    void resetHaPolicy(HaPolicy haPolicy, Form<ModelNode> form) {
        haPolicy.reset(form, metadataRegistry, statementContext, crud, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void resetHaPolicy() {
        if (haPolicy != null) {
            haPolicy.remove(dispatcher, statementContext, resources, () -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().removeSingletonSuccess(haPolicy.type)));
                reload();
            });
        } else {
            logger.error("Unable to remove HA policy: presenter.haPolicy == null");
        }
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires({ LIVE_ONLY_ADDRESS,
            REPLICATION_COLOCATED_ADDRESS,
            REPLICATION_COLOCATED_PRIMARY_ADDRESS,
            REPLICATION_COLOCATED_SECONDARY_ADDRESS,
            REPLICATION_PRIMARY_ADDRESS,
            REPLICATION_SECONDARY_ADDRESS,
            SHARED_STORE_COLOCATED_ADDRESS,
            SHARED_STORE_COLOCATED_PRIMARY_ADDRESS,
            SHARED_STORE_COLOCATED_SECONDARY_ADDRESS,
            SHARED_STORE_PRIMARY_ADDRESS,
            SHARED_STORE_SECONDARY_ADDRESS })
    @NameToken(NameTokens.MESSAGING_SERVER_HA_POLICY)
    public interface MyProxy extends ProxyPlace<HaPolicyPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<HaPolicyPresenter> {
        void empty();

        void update(HaPolicy haPolicy, ModelNode modelNode);
    }
    // @formatter:on
}
