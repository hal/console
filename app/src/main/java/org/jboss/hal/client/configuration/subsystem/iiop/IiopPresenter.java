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
package org.jboss.hal.client.configuration.subsystem.iiop;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * @author Harald Pehl
 */
public class IiopPresenter extends MbuiPresenter<IiopPresenter.MyView, IiopPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.IIOP)
    public interface MyProxy extends ProxyPlace<IiopPresenter> {}

    public interface MyView extends MbuiView<IiopPresenter> {
        void update(ModelNode modelNode);
        void clear();
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/{selected.profile}/subsystem=iiop-openjdk";
    private static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Resources resources;

    @Inject
    public IiopPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
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
        String profile = request.getParameter(PROFILE, null);
        if (profile != null) {
            getEventBus().fireEvent(new ProfileSelectionEvent(profile));
        }
    }
    @Override
    protected FinderPath finderPath() {
        return FinderPath.subsystemPath(statementContext.selectedProfile(), ROOT_TEMPLATE.lastValue());
    }

    @Override
    protected void reload() {
        ResourceAddress address = ROOT_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                .build();
        dispatcher.execute(operation,
                result -> getView().update(result),
                (o, failure) -> {
                    MessageEvent.fire(getEventBus(), Message.error(resources.constants().unknownResource(),
                            resources.messages().unknownResource(address.toString(), failure)));
                    getView().clear();
                });
    }
}
