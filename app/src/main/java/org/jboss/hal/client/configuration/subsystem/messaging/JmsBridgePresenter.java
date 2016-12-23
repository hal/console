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

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JMS_BRIDGE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_JMS_BRIDGE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MESSAGING_ACTIVEMQ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class JmsBridgePresenter
        extends MbuiPresenter<JmsBridgePresenter.MyView, JmsBridgePresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JMS_BRIDGE)
    @Requires(value = JMS_BRIDGE_ADDRESS)
    public interface MyProxy extends ProxyPlace<JmsBridgePresenter> {}

    public interface MyView extends MbuiView<JmsBridgePresenter> {
        void update(NamedNode server);
    }
    // @formatter:on

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String jmsBridgeName;

    @Inject
    public JmsBridgePresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> jmsBridgeName);
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
        jmsBridgeName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(MESSAGING_ACTIVEMQ)
                .append(Ids.MESSAGING_CATEGORY, Ids.asId(Names.JMS_BRIDGE),
                        resources.constants().category(), Names.JMS_BRIDGE)
                .append(Ids.JMS_BRIDGE, Ids.jmsBridge(jmsBridgeName), Names.JMS_BRIDGE, jmsBridgeName);
    }

    @Override
    protected void reload() {
        crud.readRecursive(SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext),
                result -> getView().update(new NamedNode(jmsBridgeName, result)));
    }

    void saveJmsBridge(final Map<String, Object> changedValues) {
        crud.save(Names.JMS_BRIDGE, jmsBridgeName, SELECTED_JMS_BRIDGE_TEMPLATE.resolve(statementContext),
                changedValues, this::reload);
    }
}
