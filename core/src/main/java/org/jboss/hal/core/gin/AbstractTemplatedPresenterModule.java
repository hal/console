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
package org.jboss.hal.core.gin;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.Proxy;

/**
 * @author Harald Pehl
 */
public abstract class AbstractTemplatedPresenterModule extends AbstractPresenterModule {

    protected <P extends PresenterWidget<?>, V extends View> void bindTemplatedSingletonPresenterWidget(
            Class<P> presenterImpl, Class<V> view, Class<? extends Provider<? extends V>> viewProvider) {
        bind(presenterImpl).in(Singleton.class);
        bind(view).toProvider(viewProvider).in(Singleton.class);
    }

    protected <P extends PresenterWidget<?>, V extends View> void bindTemplatedPresenterWidget(
            Class<P> presenterImpl, Class<V> view, Class<? extends Provider<? extends V>> viewProvider) {
        bind(presenterImpl);
        bind(view).toProvider(viewProvider);
    }

    protected <P extends Presenter<?, ?>, V extends View, Proxy_ extends Proxy<P>> void bindTemplatedPresenter(
            Class<P> presenterImpl, Class<V> view, Class<? extends Provider<? extends V>> viewProvider,
            Class<Proxy_> proxy) {
        bind(presenterImpl).in(Singleton.class);
        bind(view).toProvider(viewProvider).in(Singleton.class);
        bind(proxy).asEagerSingleton();
    }
}
