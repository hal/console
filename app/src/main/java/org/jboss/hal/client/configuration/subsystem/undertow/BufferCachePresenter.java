/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.UNDERTOW_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BUFFER_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDERTOW;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class BufferCachePresenter
        extends MbuiPresenter<BufferCachePresenter.MyView, BufferCachePresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public BufferCachePresenter(EventBus eventBus,
            BufferCachePresenter.MyView view,
            BufferCachePresenter.MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return UNDERTOW_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.BUFFER_CACHES),
                        resources.constants().settings(), Names.BUFFER_CACHES);
    }

    @Override
    protected void reload() {
        crud.readChildren(UNDERTOW_SUBSYSTEM_TEMPLATE, BUFFER_CACHE, children -> getView().update(asNamedNodes(children)));
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.UNDERTOW_BUFFER_CACHE)
    @Requires(AddressTemplates.BUFFER_CACHE_ADDRESS)
    public interface MyProxy extends ProxyPlace<BufferCachePresenter> {
    }

    public interface MyView extends MbuiView<BufferCachePresenter> {
        void update(List<NamedNode> items);
    }
    // @formatter:on
}
