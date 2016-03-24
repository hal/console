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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;

/**
 * The base presenter for HAL. Each presenter must extend from this presenter or one of its subclasses.
 *
 * @author Harald Pehl
 */
public abstract class HalPresenter<V extends HalView, Proxy_ extends Proxy<?>>
        extends Presenter<V, Proxy_>
        implements IsElement, HasElements {

    public HalPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
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
