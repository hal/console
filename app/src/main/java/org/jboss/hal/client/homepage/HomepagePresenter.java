/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.homepage;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.client.ApplicationPresenter;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.core.HasPresenter;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class HomepagePresenter extends Presenter<HomepagePresenter.MyView, HomepagePresenter.MyProxy> {

    // @formatter:off
    @NoGatekeeper
    @ProxyStandard
    @NameToken(NameTokens.Homepage)
    public interface MyProxy extends ProxyPlace<HomepagePresenter> {}

    public interface MyView extends View, HasPresenter<HomepagePresenter> {}
    // @formatter:on


    private final PlaceManager placeManager;

    @Inject
    public HomepagePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN_CONTENT);
        this.placeManager = placeManager;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    public void goTo(final String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        placeManager.revealPlace(placeRequest);
    }

    public void launchGuidedTour() {
        Window.alert("Guided tour not yet implemented");
    }
}
