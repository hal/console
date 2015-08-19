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
package org.jboss.hal.core;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import org.jboss.hal.ballroom.PatternFly;

/**
 * A presenter which calls {@link PatternFly#initOptIns()} when it's {@linkplain #onReveal() revealed}. Extends from
 * this presenter if its view uses opt-in features from PatternFly / Bootstrap like tooltips or select boxes.
 *
 * @author Harald Pehl
 */
public abstract class PatternFlyPresenter<V extends View, Proxy_ extends Proxy<?>> extends Presenter<V, Proxy_> {

    public PatternFlyPresenter(final boolean autoBind, final EventBus eventBus, final V view, final Proxy_ proxy) {
        super(autoBind, eventBus, view, proxy);
    }

    public PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy) {
        super(eventBus, view, proxy);
    }

    public PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy, final RevealType revealType) {
        super(eventBus, view, proxy, revealType);
    }

    public PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy, final RevealType revealType,
            final GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, revealType, slot);
    }

    public PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        PatternFly.initOptIns();
    }
}
