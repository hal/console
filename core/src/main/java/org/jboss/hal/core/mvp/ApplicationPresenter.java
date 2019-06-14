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
package org.jboss.hal.core.mvp;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.ballroom.Skeleton;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.PresenterType;

/**
 * Base presenter for all kind of application presenters. The presenter returns a {@link HeaderModeEvent} with various
 * information depending on the implemented interfaces:
 * <ul>
 * <li>{@link HasTitle}: the title as returned by {@link HasTitle#getTitle()}</li>
 * <li>{@link SupportsExternalMode}: a flag indicating support to open the presenter in an external tab / window</li>
 * <li>{@link SupportsExpertMode}: the resource address as returned by {@link SupportsExpertMode#resourceAddress()}</li>
 * </ul>
 */
public abstract class ApplicationPresenter<V extends HalView, Proxy_ extends ProxyPlace<?>>
        extends HalPresenter<V, Proxy_> {

    private boolean external;

    protected ApplicationPresenter(EventBus eventBus, V view, Proxy_ proxy) {
        super(eventBus, view, proxy, Slots.MAIN);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        external = Boolean.parseBoolean(request.getParameter(Places.EXTERNAL_PARAM, String.valueOf(false)));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        // if we run in external mode we need to adjust some global CSS styles
        if (isExternal()) {
            Skeleton.externalMode();
        }
    }

    /**
     * Returns a {@link HeaderModeEvent}. The payload of the event depends on the implemented interfaces:
     * <ul>
     * <li>{@link HasTitle}: the title as returned by {@link HasTitle#getTitle()}</li>
     * <li>{@link SupportsExternalMode}: a flag indicating support to open the presenter in an external tab /
     * window</li>
     * <li>{@link SupportsExpertMode}: the resource address as returned by {@link SupportsExpertMode#resourceAddress()}</li>
     * </ul>
     * <p>
     */
    protected HeaderModeEvent headerMode() {
        HeaderModeEvent.Builder builder = new HeaderModeEvent.Builder(PresenterType.APPLICATION);
        if (this instanceof HasTitle) {
            builder.title(((HasTitle) this).getTitle());
        }
        if (this instanceof SupportsExternalMode) {
            builder.supportsExternal(true);
        }
        if (this instanceof SupportsExpertMode) {
            builder.expertModeAddress(((SupportsExpertMode) this).resourceAddress());
        }
        return builder.build();
    }

    public boolean isExternal() {
        return external;
    }
}
