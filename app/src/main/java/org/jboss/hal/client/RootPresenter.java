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
package org.jboss.hal.client;

import javax.inject.Inject;

import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.mvp.Slots;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.IsSingleSlot;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;

/** Presenter which assembles the root layout: header, main content and footer. */
public class RootPresenter extends Presenter<RootPresenter.MyView, RootPresenter.MyProxy> implements Slots {

    static final IsSingleSlot<HeaderPresenter> SLOT_HEADER_CONTENT = new PermanentSlot<>();
    static final IsSingleSlot<FooterPresenter> SLOT_FOOTER_CONTENT = new PermanentSlot<>();

    private final Places places;
    private final PlaceManager placeManager;
    private final HeaderPresenter headerPresenter;
    private final FooterPresenter footerPresenter;

    @Inject
    public RootPresenter(EventBus eventBus, MyView view, MyProxy proxy,
            Places places, PlaceManager placeManager,
            HeaderPresenter headerPresenter, FooterPresenter footerPresenter) {
        super(eventBus, view, proxy, RevealType.Root);
        this.places = places;
        this.placeManager = placeManager;
        this.headerPresenter = headerPresenter;
        this.footerPresenter = footerPresenter;
    }

    @Override
    protected void onBind() {
        super.onBind();
        if (!places.isExternal(placeManager.getCurrentPlaceRequest())) {
            setInSlot(SLOT_HEADER_CONTENT, headerPresenter);
            setInSlot(SLOT_FOOTER_CONTENT, footerPresenter);
        }
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    protected void onReveal() {
        super.onReveal();
        getEventBus().fireEvent(new ApplicationReadyEvent());
    }

    // @formatter:off
    @ProxyStandard
    public interface MyProxy extends Proxy<RootPresenter> {
    }

    public interface MyView extends View {
    }
    // @formatter:on
}
