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
package org.jboss.hal.client.rhcp;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.FinderView;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.collect;
import static org.jboss.gwt.elemento.core.Elements.img;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.resources.CSS.preview;

@SuppressWarnings("HardCodedStringLiteral")
public class RhcpPresenter extends FinderPresenter<RhcpPresenter.MyView, RhcpPresenter.MyProxy> {

    @Inject
    public RhcpPresenter(EventBus eventBus, MyView view, MyProxy myProxy, Finder finder, Resources resources) {
        super(eventBus, view, myProxy, finder, resources);
    }

    @Override
    protected String initialColumn() {
        return "rhcp-color";
    }

    @Override
    protected PreviewContent initialPreview() {
        Iterable<HTMLElement> elements = collect()
                .add(p().textContent(
                        "w00t you found a secret page! The main purpose of this page is to test deeply nested columns. Therefore the discography of the Red Hot Chili Peppers is used (obviously one of the developers is a big RHCP fan ;-)"))
                .add(p().textContent(
                        "Have fun browsing through the albums (and don't forget to look under the bridge)."))
                .add(img(
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Redgotchilipeppers-logo.svg/240px-Redgotchilipeppers-logo.svg.png")
                        .css(preview))
                .get();
        return new PreviewContent("Red Hot Chili Peppers", elements);
    }


    // @formatter:off
    @ProxyStandard
    @NameToken("rhcp")
    public interface MyProxy extends ProxyPlace<RhcpPresenter> {
    }

    public interface MyView extends FinderView {
    }
    // @formatter:on
}
