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
package org.jboss.hal.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.IsSingleSlot;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.core.HasPresenter;
import org.jboss.hal.core.Slots;

/**
 * @author Harald Pehl
 */
public class RootPresenter extends Presenter<RootPresenter.MyView, RootPresenter.MyProxy> implements Slots {

    // @formatter:off
    @ProxyStandard
    interface MyProxy extends Proxy<RootPresenter> {}

    interface MyView extends View, HasPresenter<RootPresenter> {}
    // @formatter:on


    static final IsSingleSlot<HeaderPresenter> SLOT_HEADER_CONTENT = new PermanentSlot<>();
    static final IsSingleSlot<FooterPresenter> SLOT_FOOTER_CONTENT = new PermanentSlot<>();

    private final HeaderPresenter headerPresenter;
    private final FooterPresenter footerPresenter;

    @Inject
    public RootPresenter(EventBus eventBus, MyView view, MyProxy proxy,
            HeaderPresenter headerPresenter, FooterPresenter footerPresenter) {
        super(eventBus, view, proxy, RevealType.Root);
        this.headerPresenter = headerPresenter;
        this.footerPresenter = footerPresenter;
    }

    @Override
    protected void onBind() {
        getView().setPresenter(this);
        setInSlot(SLOT_HEADER_CONTENT, headerPresenter);
        setInSlot(SLOT_FOOTER_CONTENT, footerPresenter);
    }

    void tlcMode() {
        headerPresenter.tlcMode();
    }

    void applicationMode() {
        headerPresenter.applicationMode();
    }
}
