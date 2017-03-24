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

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class SocketBindingGroupPresenter
        extends MbuiPresenter<SocketBindingGroupPresenter.MyView, SocketBindingGroupPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.SOCKET_BINDING_GROUP)
    public interface MyProxy extends ProxyPlace<SocketBindingGroupPresenter> {}

    public interface MyView extends MbuiView<SocketBindingGroupPresenter> {
        void update(NamedNode socketBindingGroup);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/socket-binding-group=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private String socketBindingGroup;

    @Inject
    public SocketBindingGroupPresenter(final EventBus eventBus,
            final SocketBindingGroupPresenter.MyView view,
            final SocketBindingGroupPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
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
        socketBindingGroup = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ROOT_TEMPLATE.resolve(statementContext, socketBindingGroup);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS), Names.CONFIGURATION, Names.SOCKET_BINDINGS)
                .append(Ids.SOCKET_BINDING_GROUP, socketBindingGroup, Names.SOCKET_BINDING_GROUP, socketBindingGroup);
    }

    @Override
    protected void reload() {
        crud.read(ROOT_TEMPLATE.resolve(statementContext, socketBindingGroup),
                result -> getView().update(new NamedNode(result)));
    }
}
