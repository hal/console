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
package org.jboss.hal.client.skeleton;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/** Container which holds the last n messages. The user can review and clear the messages. */
class MessageSink implements IsElement, HasPresenter<HeaderPresenter> {

    private static final int SIZE = 50;

    private final Resources resources;
    private final HTMLElement messagesHeader;
    private final HTMLElement panelBody;
    private final HTMLElement root;
    private HeaderPresenter presenter;

    MessageSink(Resources resources) {
        HTMLElement panelHeader, clear;

        this.resources = resources;
        this.root = div().css(drawerPf, drawerPfHal, drawerPfNotificationsNonClickable, hide)
                .add(div().css(drawerPfTitle)
                        .add(messagesHeader = h(3).css(textCenter)
                                .textContent(resources.messages().messages(0)).asElement()))
                .add(div().css(panelGroup)
                        .add(div().css(panel, panelDefault)
                                .add(panelHeader = div().css(panelHeading).asElement())
                                .add(div().css(panelCollapse, collapse, in)
                                        .add(panelBody = div().css(CSS.panelBody).asElement())
                                        .add(div().css(drawerPfAction)
                                                .add(clear = button(resources.constants().clearMessages())
                                                            .css(btn, btnLink, btnBlock, clickable)
                                                            .asElement()))
                                                .asElement())))
                .asElement();

        bind(clear, click, event -> presenter.clearMessages());
        Elements.setVisible(panelHeader, false); // not used
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    void add(Message message) {
        MessageSinkElement element = new MessageSinkElement(this, message, resources);
        panelBody.insertBefore(element.asElement(), panelBody.firstElementChild);
        int messageCount = (int) panelBody.childElementCount;
        if (messageCount > SIZE) {
            panelBody.removeChild(panelBody.lastElementChild);
        }
        updateHeader();
    }

    void remove(String id) {
        Element element = document.getElementById(id);
        Elements.failSafeRemove(panelBody, element);
        updateHeader();
    }

    void clear() {
        Elements.removeChildrenFrom(panelBody);
        updateHeader();
    }

    private void updateHeader() {
        messagesHeader.textContent = resources.messages().messages((int) panelBody.childElementCount);
    }
}
