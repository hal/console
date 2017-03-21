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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.Map;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SELECTED_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

/**
 * @author Harald Pehl
 */
abstract class ServerSettingsPresenter<V extends MbuiView, Proxy_ extends ProxyPlace<?>> extends MbuiPresenter<V, Proxy_>
        implements SupportsExpertMode {

    final CrudOperations crud;
    final MetadataRegistry metadataRegistry;
    final FinderPathFactory finderPathFactory;
    final StatementContext statementContext;
    final Resources resources;
    String serverName;

    ServerSettingsPresenter(final EventBus eventBus,
            final V view,
            final Proxy_ proxy_,
            final Finder finder,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy_, finder);
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> serverName);
        this.resources = resources;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        serverName = request.getParameter(SERVER, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
    }

    void add(ServerSubResource ssr) {
        ssr.add(metadataRegistry, statementContext, crud, resources, (n, a) -> reload());
    }

    void save(ServerSubResource ssr, Form<NamedNode> form, Map<String, Object> changedValues) {
        ssr.save(form, changedValues, metadataRegistry, statementContext, crud, this::reload);
    }

    void reset(ServerSubResource ssr, Form<NamedNode> form) {
        ssr.reset(form, metadataRegistry, statementContext, crud, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void remove(ServerSubResource ssr, NamedNode item) {
        ssr.remove(item, statementContext, crud, this::reload);
    }
}
