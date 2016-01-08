/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration.subsystem.jca;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.core.HasPresenter;
import org.jboss.hal.core.PatternFlyPresenter;
import org.jboss.hal.core.PatternFlyView;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.Slots;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNodesWithNames;
import static org.jboss.hal.resources.Names.PROFILE;

/**
 * @author Harald Pehl
 */
public class DataSourcePresenter extends
        PatternFlyPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy> {

    static final String ROOT_ADDRESS = "/{any.profile}/subsystem=datasources/data-source=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);


    // @formatter:off
    @ProxyStandard
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.DATASOURCE)
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<DataSourcePresenter> {
        void update(List<ModelNode> datasources);
    }
    // @formatter:on


    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public DataSourcePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, Slots.APPLICATION);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        String profile = request.getParameter(PROFILE, null);
        if (profile != null) {
            getEventBus().fireEvent(new ProfileSelectionEvent(profile));
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadDataSources();
    }

    private void loadDataSources() {
        AddressTemplate template = AddressTemplate.of("/{selected.profile}/subsystem=datasources");
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                template.resolve(statementContext))
                .param(CHILD_TYPE, "data-source")
                .build();
        dispatcher.execute(operation, result -> getView().update(asNodesWithNames(result.asPropertyList())));
    }
}

