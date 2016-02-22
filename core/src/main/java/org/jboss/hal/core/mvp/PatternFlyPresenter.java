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
package org.jboss.hal.core.mvp;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;

/**
 * A presenter which calls {@link PatternFlyView#attach()} when it's {@linkplain #onReveal() revealed}. Extend from
 * this presenter if the view uses opt-in features from PatternFly / Bootstrap like data tables, tooltips or select
 * boxes.
 *
 * @author Harald Pehl
 */
public abstract class PatternFlyPresenter<V extends PatternFlyView, Proxy_ extends Proxy<?>>
        extends Presenter<V, Proxy_>
        implements IsElement, HasElements {

    public PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().attach();
    }

    @Override
    public Element asElement() {
        return getView().asElement();
    }

    @Override
    public Iterable<Element> asElements() {
        return getView().asElements();
    }
}
