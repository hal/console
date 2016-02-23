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
package org.jboss.hal.client.tools;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;

import javax.inject.Inject;

/**
 * Presenter for resources w/o a specific implementation in HAL. Relies on the model browser to manage a (sub)tree of
 * the management model starting at the resource specified as place request parameter.
 *
 * @author Harald Pehl
 */
public class ModelBrowserPresenter
        extends ApplicationPresenter<ModelBrowserPresenter.MyView, ModelBrowserPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MODEL_BROWSER)
    public interface MyProxy extends ProxyPlace<ModelBrowserPresenter> {}

    public interface MyView extends PatternFlyView {
        void setRoot(ResourceAddress root);
    }
    // @formatter:on


    public final static String ADDRESS_PARAM = "address";

    private final StatementContext statementContext;
    private ResourceAddress address;

    @Inject
    public ModelBrowserPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
            final StatementContext statementContext) {
        super(eventBus, view, proxy);
        this.statementContext = statementContext;
        this.address = ResourceAddress.ROOT;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        String parameter = request.getParameter(ADDRESS_PARAM, null);
        if (parameter != null) {
            address = AddressTemplate.of(parameter).resolve(statementContext);
        } else {
            address = ResourceAddress.ROOT;
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setRoot(address);
    }
}
