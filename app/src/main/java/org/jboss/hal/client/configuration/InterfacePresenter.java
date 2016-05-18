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
package org.jboss.hal.client.configuration;

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("SpellCheckingInspection")
public class InterfacePresenter extends MbuiPresenter<InterfacePresenter.MyView, InterfacePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.INTERFACE)
    public interface MyProxy extends ProxyPlace<InterfacePresenter> {}

    public interface MyView extends MbuiView<InterfacePresenter> {
        void update(ModelNode interfce);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/interface=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final OperationFactory operationFactory;
    private final Resources resources;
    private String interfce;

    @Inject
    public InterfacePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final OperationFactory operationFactory,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.operationFactory = operationFactory;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        interfce = request.getParameter(NAME, null);
    }

    @Override
    protected FinderPath finderPath() {
        return new FinderPath()
                .append(CONFIGURATION, Names.INTERFACES.toLowerCase(), Names.CONFIGURATION, Names.INTERFACES)
                .append(INTERFACE, interfce, Names.INTERFACE);
    }

    @Override
    protected void reload() {
        ResourceAddress address = ROOT_TEMPLATE.resolve(statementContext, interfce);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    @SuppressWarnings("UnusedParameters")
    void saveInterface(final Form<ModelNode> form, final Map<String, Object> changedValues) {
        Composite composite = operationFactory
                .fromChangeSet(ROOT_TEMPLATE.resolve(statementContext, interfce), changedValues);
        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifyResourceSuccess(Names.INTERFACE, interfce)));
            reload();
        });
    }
}
