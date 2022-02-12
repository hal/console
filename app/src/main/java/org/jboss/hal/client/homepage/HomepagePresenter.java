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
package org.jboss.hal.client.homepage;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.mvp.TopLevelPresenter;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class HomepagePresenter extends TopLevelPresenter<HomepagePresenter.MyView, HomepagePresenter.MyProxy> {

    private final TourSetup tourSetup;
    private Tour tour;

    @Inject
    public HomepagePresenter(EventBus eventBus, MyView view, MyProxy proxy, AccessControl accessControl,
            Dispatcher dispatcher, Environment environment, PlaceManager placeManager, Places places,
            Resources resources) {
        super(eventBus, view, proxy);
        this.tourSetup = new TourSetup(eventBus, dispatcher, environment, placeManager, places, accessControl,
                resources);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    void launchGuidedTour() {
        if (tour == null) {
            tourSetup.initTour(t -> {
                tour = t;
                tour.start();
            });
        } else {
            if (!tour.running()) {
                tour.start();
            }
        }
    }

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.HOMEPAGE)
    public interface MyProxy extends ProxyPlace<HomepagePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<HomepagePresenter> {
    }
    // @formatter:on
}
