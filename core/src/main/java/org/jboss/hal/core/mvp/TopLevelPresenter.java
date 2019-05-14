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
package org.jboss.hal.core.mvp;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.PresenterType;

/**
 * Presenter for a top level category such as 'Homepage', 'Configuration' or 'Runtime'. The presenter fires a {@link
 * HeaderModeEvent} with its token in the {@link #onReveal()} method.
 */
public abstract class TopLevelPresenter<V extends HalView, Proxy_ extends ProxyPlace<?>>
        extends HalPresenter<V, Proxy_>
        implements TopLevelCategory {

    protected TopLevelPresenter(final EventBus eventBus, final V view, final Proxy_ proxy) {
        super(eventBus, view, proxy, Slots.MAIN);
    }

    @Override
    protected HeaderModeEvent headerMode() {
        return new HeaderModeEvent.Builder(PresenterType.TOP_LEVEL_CATEGORY)
                .token(getProxy().getNameToken())
                .build();
    }
}
