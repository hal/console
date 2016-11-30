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
package org.jboss.hal.client.configuration.subsystem.webservice;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.WEBSERVICES_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WEBSERVICES;

/**
 * @author Harald Pehl
 */
public class WebservicePresenter
        extends MbuiPresenter<WebservicePresenter.MyView, WebservicePresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.WEBSERVICES)
    @Requires(AddressTemplates.WEBSERVICES_ADDRESS)
    public interface MyProxy extends ProxyPlace<WebservicePresenter> {}

    public interface MyView extends MbuiView<WebservicePresenter> {
        void update(ModelNode data);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private final StatementContext statementContext;

    @Inject
    public WebservicePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final CrudOperations crud,
            final StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.crud = crud;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return WEBSERVICES_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(WEBSERVICES);
    }

    @Override
    protected void reload() {
        crud.readRecursive(WEBSERVICES_TEMPLATE, result -> getView().update(result));
    }
}
