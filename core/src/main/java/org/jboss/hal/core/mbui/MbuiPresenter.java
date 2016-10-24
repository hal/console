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
package org.jboss.hal.core.mbui;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;

/**
 * @author Harald Pehl
 */
public abstract class MbuiPresenter<V extends MbuiView, Proxy_ extends ProxyPlace<?>>
        extends ApplicationFinderPresenter<V, Proxy_> {

    protected MbuiPresenter(final EventBus eventBus, final V view,
            final Proxy_ proxy_, final Finder finder) {
        super(eventBus, view, proxy_, finder);
    }

    @Override
    protected void onReset() {
        super.onReset();
        reload();
    }

    protected abstract void reload();
}
