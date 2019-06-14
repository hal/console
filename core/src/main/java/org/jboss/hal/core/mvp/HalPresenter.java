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

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.header.HeaderModeEvent;

/**
 * The base presenter for HAL. Each presenter must extend from this presenter or one of its subclasses. Fires a {@link
 * HeaderModeEvent} as part of the {@link #onReveal()} method, if {@link #headerMode()} returns a non-null event.
 * <p>
 * The presenter calls {@link HalView#attach()} when it's {@linkplain #onReveal() revealed} and {@link
 * HalView#detach()} when it's {@linkplain #onHide() hidden}.
 */
public abstract class HalPresenter<V extends HalView, Proxy_ extends Proxy<?>>
        extends Presenter<V, Proxy_>
        implements IsElement, HasElements {

    protected HalPresenter(EventBus eventBus, V view, Proxy_ proxy, GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        if (headerMode() != null) {
            getEventBus().fireEvent(headerMode());
        }
        getView().attach();
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().detach();
    }

    /** Override this method and return a {@link HeaderModeEvent} to change the state of the header. */
    protected abstract HeaderModeEvent headerMode();

    @Override
    public HTMLElement asElement() {
        return getView().asElement();
    }

    @Override
    public Iterable<HTMLElement> asElements() {
        return getView().asElements();
    }
}
