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
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.PresenterType;
import org.jboss.hal.core.ui.Skeleton;

/**
 * Base presenter for all application presenters. The presenter fires a {@link HeaderModeEvent} in the {@link
 * #onReveal()} method. Depending on what additional interfaces this presenter implements, various information will be
 * part of the event's payload:
 * <ul>
 * <li>{@link HasTitle}: the title as returned by {@link HasTitle#getTitle()}</li>
 * <li>{@link SupportsExternalMode}: a flag indicating support for external mode</li>
 * <li>{@link SupportsExpertMode}: the resource address as returned by {@link SupportsExpertMode#resourceAddress()}</li>
 * </ul>
 * <p>
 * If the presenter's view implements {@link HasVerticalNavigation} this presenter takes care of calling {@link
 * VerticalNavigation#on()} when the presenter is revealed and {@link VerticalNavigation#off()} when the presenter is
 * hidden.
 *
 * @author Harald Pehl
 */
public abstract class ApplicationPresenter<V extends HalView, Proxy_ extends ProxyPlace<?>>
        extends HalPresenter<V, Proxy_> {

    private boolean external;

    protected ApplicationPresenter(final EventBus eventBus, final V view, final Proxy_ proxy) {
        super(eventBus, view, proxy, Slots.MAIN);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        external = Boolean.parseBoolean(request.getParameter(Places.EXTERNAL_PARAM, String.valueOf(false)));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        headerMode();

        // show vertical navigation?
        if (getView() instanceof HasVerticalNavigation) {
            VerticalNavigation navigation = ((HasVerticalNavigation) getView()).getVerticalNavigation();
            if (navigation != null) {
                navigation.on();
                navigation.showInitial();
            }
        }

        // switch to external mode?
        if (external) {
            Skeleton.externalMode();
        }
    }

    protected void headerMode() {
        HeaderModeEvent.Builder builder = new HeaderModeEvent.Builder(PresenterType.APPLICATION);
        if (this instanceof HasTitle) {
            builder.title(((HasTitle) this).getTitle());
        }
        if (this instanceof SupportsExternalMode) {
            builder.external(true);
        }
        if (this instanceof SupportsExpertMode) {
            builder.expertMode(((SupportsExpertMode) this).resourceAddress());
        }
        getEventBus().fireEvent(builder.build());
    }

    @Override
    protected void onHide() {
        super.onHide();
        if (getView() instanceof HasVerticalNavigation) {
            VerticalNavigation navigation = ((HasVerticalNavigation) getView()).getVerticalNavigation();
            if (navigation != null) {
                navigation.off();
            }
        }
    }

    public boolean isExternal() {
        return external;
    }
}
