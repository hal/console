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
package org.jboss.hal.client.runtime.server;

import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_BOOT_ERRORS;

public class ServerBootErrorsPresenter
        extends ApplicationFinderPresenter<ServerBootErrorsPresenter.MyView, ServerBootErrorsPresenter.MyProxy> {

    static final String MANAGEMENT_ADDRESS = "/{selected.host}/{selected.server}/core-service=management";
    static final AddressTemplate MANAGEMENT_TEMPLATE = AddressTemplate.of(MANAGEMENT_ADDRESS);

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;

    @Inject
    public ServerBootErrorsPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            StatementContext statementContext,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory) {
        super(eventBus, view, myProxy, finder);
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath();
    }

    @Override
    protected void reload() {
        ResourceAddress address = AddressTemplate.of(MANAGEMENT_ADDRESS).resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_BOOT_ERRORS).build();
        dispatcher.execute(operation, result -> getView().update(result.asList()));
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.SERVER_BOOT_ERRORS)
    @Requires(value = MANAGEMENT_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<ServerBootErrorsPresenter> {}

    public interface MyView extends HalView {
        void update(List<ModelNode> bootErrors);
    }
    // @formatter:on
}
