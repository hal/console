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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Container which holds the last n messages. The user can review and clear the messages.
 *
 * @author Harald Pehl
 */
class MessageSink implements IsElement, HasPresenter<HeaderPresenter> {

    private static final String MESSAGES_HEADER = "messagesHeader";
    private static final String PANEL_HEADER = "panelHeader";
    private static final String PANEL_BODY = "panelBody";
    private static final int SIZE = 50;

    private final Resources resources;
    private final Element messagesHeader;
    private final Element panelBody;
    private final Element root;
    private HeaderPresenter presenter;

    MessageSink(Resources resources) {
        this.resources = resources;

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(drawerPf, drawerPfHal, drawerPfNotificationsNonClickable, hide)
                .div().css(drawerPfTitle)
                    .h(3).css(textCenter).rememberAs(MESSAGES_HEADER)
                        .textContent(resources.messages().messages(0))
                    .end()
                .end()
                .div().css(panelGroup)
                    .div().css(panel, panelDefault)
                        .div().css(panelHeading).rememberAs(PANEL_HEADER).end()
                        .div().css(panelCollapse, collapse, in)
                            .div().css(CSS.panelBody).rememberAs(PANEL_BODY).end()
                            .div().css(drawerPfAction)
                                .button().css(btn, btnLink, btnBlock, clickable)
                                    .textContent(resources.constants().clearMessages())
                                    .on(click, event -> presenter.clearMessages())
                                .end()
                            .end()
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        messagesHeader = builder.referenceFor(MESSAGES_HEADER);
        Element panelHeader = builder.referenceFor(PANEL_HEADER);
        panelBody = builder.referenceFor(PANEL_BODY);
        root = builder.build();

        Elements.setVisible(panelHeader, false); // not used
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Element asElement() {
        return root;
    }

    void add(Message message) {
        MessageSinkElement element = new MessageSinkElement(this, message, resources);
        panelBody.insertBefore(element.asElement(), panelBody.getFirstElementChild());
        int messageCount = panelBody.getChildElementCount();
        if (messageCount > SIZE) {
            panelBody.removeChild(panelBody.getLastElementChild());
        }
        updateHeader();
    }

    void remove(String id) {
        Element element = Browser.getDocument().getElementById(id);
        Elements.failSafeRemove(panelBody, element);
        updateHeader();
    }

    void clear() {
        Elements.removeChildrenFrom(panelBody);
        updateHeader();
    }

    private void updateHeader() {
        messagesHeader.setTextContent(resources.messages().messages(panelBody.getChildElementCount()));
    }
}
