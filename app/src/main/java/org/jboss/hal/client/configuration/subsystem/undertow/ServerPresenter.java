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
package org.jboss.hal.client.configuration.subsystem.undertow;

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
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVLET_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDERTOW;

/**
 * @author Harald Pehl
 */
public class ServerPresenter
        extends ApplicationFinderPresenter<ServerPresenter.MyView, ServerPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(SERVER_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_SERVER)
    public interface MyProxy extends ProxyPlace<ServerPresenter> {}

    public interface MyView extends HalView, HasPresenter<ServerPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on

    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String serverName;

    @Inject
    public ServerPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> serverName);
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
        serverName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.SERVER),
                        resources.constants().settings(), Names.SERVER)
                .append(Ids.UNDERTOW_SERVER, Ids.undertowServer(serverName), Names.SERVER, serverName);
    }

    @Override
    protected void reload() {
        crud.readRecursive(SELECTED_SERVER_TEMPLATE.resolve(statementContext), result -> getView().update(result));
    }

    // ------------------------------------------------------ server, hosts & servlet container

    void saveServer(final Map<String, Object> changedValues) {
        crud.save(Names.SERVER, serverName, SELECTED_SERVER_TEMPLATE.resolve(statementContext), changedValues,
                this::reload);
    }

    void addHost() {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.UNDERTOW_HOST_ADD,
                resources.messages().addResourceTitle(Names.HOST), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name)
                            .resolve(statementContext);
                    crud.add(Names.HOST, name, address, model, (n, a) -> reload());
                });
        dialog.show();

    }

    void saveHost(final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name).resolve(statementContext);
        crud.save(Names.HOST, name, address, changedValues, this::reload);
    }

    void removeHost(final String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name).resolve(statementContext);
        crud.remove(Names.HOST, name, address, this::reload);
    }

    void addServletContainer() {
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.UNDERTOW_SERVLET_CONTAINER_ADD,
                resources.messages().addResourceTitle(Names.SERVLET_CONTAINER), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(SERVLET_CONTAINER + "=" + name)
                            .resolve(statementContext);
                    crud.add(Names.SERVLET_CONTAINER, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveServletContainer(final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(SERVLET_CONTAINER + "=" + name)
                .resolve(statementContext);
        crud.save(Names.SERVLET_CONTAINER, name, address, changedValues, this::reload);
    }

    void removeServletContainer(final String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(SERVLET_CONTAINER + "=" + name)
                .resolve(statementContext);
        crud.remove(Names.SERVLET_CONTAINER, name, address, this::reload);
    }

    // ------------------------------------------------------ listener

    void addListener(final Listener listenerType) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + "=*"));
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(listenerType.baseId, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(listenerType.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                            .resolve(statementContext);
                    crud.add(listenerType.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveListener(final Listener listenerType, final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                .resolve(statementContext);
        crud.save(listenerType.type, name, address, changedValues, this::reload);
    }

    void removeListener(final Listener listenerType, final String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                .resolve(statementContext);
        crud.remove(listenerType.type, name, address, this::reload);
    }

    // ------------------------------------------------------ getter

    StatementContext getStatementContext() {
        return statementContext;
    }
}
