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
package org.jboss.hal.client.configuration.subsystem;

import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.SubsystemPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.Names.IIOP_OPENJDK;

/**
 * @author Harald Pehl
 */
public class IiopPresenter extends SubsystemPresenter<IiopPresenter.MyView, IiopPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.IIOP)
    public interface MyProxy extends ProxyPlace<IiopPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<IiopPresenter> {
        void update(ModelNode modelNode);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/{selected.profile}/subsystem=iiop-openjdk";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final OperationFactory operationFactory;
    private final Resources resources;

    @Inject
    public IiopPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final OperationFactory operationFactory,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.operationFactory = operationFactory;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        load();
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath.subsystemPath(statementContext.selectedProfile(), ROOT_TEMPLATE.lastValue());
    }

    private void load() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, ROOT_TEMPLATE.resolve(statementContext))
                .build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    public void save(final Map<String, Object> changedValues) {
        Composite composite = operationFactory.fromChangeSet(ROOT_TEMPLATE.resolve(statementContext), changedValues);
        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifySingleResourceSuccess(IIOP_OPENJDK))); //NON-NLS
            load();
        });
    }
}
