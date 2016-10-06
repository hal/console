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
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.ui.Skeleton;

import static org.jboss.hal.core.mvp.ApplicationPresenter.EXTERNAL;

/**
 * Base class for all presenters which are displayed full screen in {@link Slots#MAIN}, but which are
 * <strong>not</strong> applications.
 *
 * @author Harald Pehl
 */
public abstract class FullscreenPresenter<V extends PatternFlyView, Proxy_ extends Proxy<?>>
        extends PatternFlyPresenter<V, Proxy_> implements ExternalMode {

    private final String title;
    private boolean external;

    public FullscreenPresenter(final EventBus eventBus, final V view, final Proxy_ proxy, final String title) {
        super(eventBus, view, proxy, Slots.MAIN);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        external = Boolean.parseBoolean(request.getParameter(EXTERNAL, String.valueOf(false)));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        if (external) {
            Skeleton.externalMode();
        }
    }

    /**
     * @return {@code true} if this presenter is currently running in external mode, {@code false} otherwise.
     */
    public boolean isExternal() {
        return external;
    }

    @Override
    public boolean supportsExternalMode() {
        return false;
    }
}
