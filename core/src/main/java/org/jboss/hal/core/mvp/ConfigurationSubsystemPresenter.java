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
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.finder.Finder;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;

/**
 * Base class for all subsystem presenters in configuration. Takes care of extracting the profile from the place
 * request and emitting a {@link org.jboss.hal.core.ProfileSelectionEvent}.
 *
 * @author Harald Pehl
 */
public abstract class ConfigurationSubsystemPresenter<V extends PatternFlyView, Proxy_ extends ProxyPlace<?>>
        extends ApplicationPresenter<V, Proxy_> {

    protected ConfigurationSubsystemPresenter(final EventBus eventBus, final V view,
            final Proxy_ proxy_, final Finder finder) {
        super(eventBus, view, proxy_, finder);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        String profile = request.getParameter(PROFILE, null);
        if (profile != null) {
            getEventBus().fireEvent(new ProfileSelectionEvent(profile));
        }
    }
}
