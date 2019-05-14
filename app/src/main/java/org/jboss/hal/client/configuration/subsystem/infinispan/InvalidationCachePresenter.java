/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.infinispan;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.infinispan.AddressTemplates.INVALIDATION_CACHE_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.infinispan.CacheType.INVALIDATION;

public class InvalidationCachePresenter
        extends CachePresenter<InvalidationCachePresenter.MyView, InvalidationCachePresenter.MyProxy>
        implements SupportsExpertMode {

    @Inject
    public InvalidationCachePresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            CrudOperations crud,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder, finderPathFactory, crud, dispatcher, metadataRegistry, statementContext,
                resources, INVALIDATION);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(INVALIDATION_CACHE_ADDRESS)
    @NameToken(NameTokens.INVALIDATION_CACHE)
    public interface MyProxy extends ProxyPlace<InvalidationCachePresenter> {
    }

    public interface MyView extends CacheView<InvalidationCachePresenter> {
    }
    // @formatter:on
}
