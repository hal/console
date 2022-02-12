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
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JMS_BRIDGE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JMS_BRIDGE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_JMS_BRIDGE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGING_ACTIVEMQ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.Ids.JMS_BRIDGE_ITEM;

public class JmsBridgePresenter
        extends MbuiPresenter<JmsBridgePresenter.MyView, JmsBridgePresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private String jmsBridgeName;

    @Inject
    public JmsBridgePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> jmsBridgeName);
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
        jmsBridgeName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, JMS_BRIDGE_ITEM,
                        resources.constants().category(), Names.JMS_BRIDGE)
                .append(Ids.JMS_BRIDGE, Ids.jmsBridge(jmsBridgeName), Names.JMS_BRIDGE, jmsBridgeName);
    }

    @Override
    public void reload() {
        crud.readRecursive(SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext),
                result -> getView().update(new NamedNode(jmsBridgeName, result)));
    }

    void saveJmsBridge(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(JMS_BRIDGE_TEMPLATE);
        crud.save(Names.JMS_BRIDGE, jmsBridgeName, SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext),
                changedValues, metadata, this::reload);
    }

    void resetJmsBridge(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(JMS_BRIDGE_TEMPLATE);
        crud.reset(Names.JMS_BRIDGE, jmsBridgeName, SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext), form,
                metadata, new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JMS_BRIDGE)
    @Requires(JMS_BRIDGE_ADDRESS)
    public interface MyProxy extends ProxyPlace<JmsBridgePresenter> {
    }

    public interface MyView extends MbuiView<JmsBridgePresenter> {
        void update(NamedNode server);
    }
    // @formatter:on
}
