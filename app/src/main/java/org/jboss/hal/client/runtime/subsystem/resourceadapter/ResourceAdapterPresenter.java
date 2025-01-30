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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;

import static org.jboss.hal.client.runtime.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_STATS_ADDRESS;
import static org.jboss.hal.meta.token.NameTokens.RESOURCE_ADAPTER_RUNTIME;

public class ResourceAdapterPresenter
        extends BasePresenter<ResourceAdapterPresenter.MyView, ResourceAdapterPresenter.MyProxy> {

    @Inject
    public ResourceAdapterPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder, finderPathFactory, dispatcher, statementContext, resources);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(RESOURCE_ADAPTER_RUNTIME)
    @Requires({ RESOURCE_ADAPTER_STATS_ADDRESS })
    public interface MyProxy extends BasePresenter.MyProxy<ResourceAdapterPresenter> {
    }

    public interface MyView extends BasePresenter.MyView<ResourceAdapterPresenter> {
    }
    // @formatter:on
}
