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
package org.jboss.hal.client.configuration;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("SpellCheckingInspection")
public class InterfacePresenter extends
        ApplicationPresenter<InterfacePresenter.MyView, InterfacePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.INTERFACE)
    public interface MyProxy extends ProxyPlace<InterfacePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<InterfacePresenter> {
        void update(ModelNode interfce);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/interface=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private String interfce;

    @Inject
    public InterfacePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        super(eventBus, view, proxy);
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
        interfce = request.getParameter(Names.interfce, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadInterface();
    }

    private void loadInterface() {
        ResourceAddress address = ROOT_TEMPLATE.resolve(statementContext, interfce);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    public void saveInterface(final Map<String, Object> changedValues) {
        loadInterface();
    }
}
