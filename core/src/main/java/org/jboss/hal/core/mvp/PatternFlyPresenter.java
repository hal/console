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
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;

/**
 * A presenter which calls {@link PatternFlyView#attach()} when it's {@linkplain #onReveal() revealed} and {@link
 * PatternFlyView#detach()} when it's {@linkplain #onHide() hidden}. Extend from this presenter if the view uses opt-in
 * features from PatternFly / Bootstrap like data tables, tooltips or select boxes.
 *
 * @author Harald Pehl
 */
abstract class PatternFlyPresenter<V extends PatternFlyView, Proxy_ extends Proxy<?>>
        extends HalPresenter<V, Proxy_>
        implements IsElement, HasElements {

    PatternFlyPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().attach();
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().detach();
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
