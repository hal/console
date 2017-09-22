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

import elemental2.dom.CSSProperties.MaxHeightUnionType;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Container which holds the last n messages. The user can review and clear the messages.
 *
 * @see <a href="http://www.patternfly.org/pattern-library/communication/notification-drawer/">http://www.patternfly.org/pattern-library/communication/notification-drawer/</a>
 */
class NotificationDrawer implements IsElement, HasPresenter<HeaderPresenter> {

    private final Resources resources;
    private final HTMLElement headerContainer;
    private final HTMLElement header;
    private final HTMLElement panelBody;
    private final HTMLElement actions;
    private final HTMLElement empty;
    private final HTMLElement markAllRead;
    private final HTMLElement root;
    private HeaderPresenter presenter;

    NotificationDrawer(Resources resources) {
        this.resources = resources;
        this.root = div().css(drawerPf, drawerPfNotificationsNonClickable, drawerPfHal, hide)
                .add(headerContainer = div().css(drawerPfTitle)
                        .add(a().css(drawerPfToggleExpand, fontAwesome("angle-double-left"), hiddenXs)
                                .on(click, event -> toggleWidth()))
                        .add(a().css(drawerPfClose, pfIcon("close"))
                                .on(click, event -> close()))
                        .add(header = h(3, resources.messages().notifications(0))
                                .css(textCenter)
                                .asElement())
                        .asElement())
                .add(div().css(panelGroup)
                        .add(div().css(panel, panelDefault)
                                .add(div().css(panelHeading, hidden))
                                .add(div().css(panelCollapse, collapse, in)
                                        .aria(UIConstants.EXPANDED, UIConstants.TRUE)
                                        .add(panelBody = div().css(CSS.panelBody)
                                                .style("overflow-y:auto")  //NON-NLS
                                                .asElement())
                                        .add(empty = div().css(blankSlatePf)
                                                .add(div().css(blankSlatePfIcon)
                                                        .add(span().css("pficon-info"))) //NON-NLS
                                                .add(h(1, resources.constants().noNotifications()))
                                                .asElement())
                                        .add(actions = div().css(drawerPfAction)
                                                .add(markAllRead = div().css(drawerPfActionLink)
                                                        .add(button(resources.constants().markAllRead())
                                                                .css(btn, btnLink)
                                                                .on(click, event -> markAllRead()))
                                                        .asElement())
                                                .add(div().css(drawerPfActionLink)
                                                        .add(button().css(btn, btnLink)
                                                                .on(click, event -> clear())
                                                                .add(span().css(pfIcon("close")))
                                                                .add(resources.constants().clearAll())))
                                                .asElement()))))
                .asElement();

        updateElements();
        window.onresize = (o) -> {
            adjustHeight();
            return null;
        };
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void setPresenter(final HeaderPresenter presenter) {
        this.presenter = presenter;
    }

    void add(Message message) {
        NotificationDrawerElement element = new NotificationDrawerElement(this, message, resources);
        panelBody.insertBefore(element.asElement(), panelBody.firstElementChild);
        updateElements();
        adjustHeight();
    }

    void toggle() {
        root.classList.toggle(hide);
    }

    int getMessageCount() {
        return (int) panelBody.childElementCount;
    }

    private int getUnreadCount() {
        NodeList<Element> nodes = root.querySelectorAll("." + drawerPfNotification + "." + unread);
        return nodes != null ? (int) nodes.length : 0;
    }


    // ------------------------------------------------------ event handler

    private void toggleWidth() {
        root.classList.toggle(drawerPfExpanded);
    }

    private void close() {
        root.classList.add(hide);
    }

    private void markAllRead() {
        Elements.stream(root.querySelectorAll("." + drawerPfNotification + "." + unread))
                .forEach(element -> element.classList.remove(unread));
        updateElements();
    }

    void remove(String id) {
        Element element = document.getElementById(id);
        Elements.failSafeRemove(panelBody, element);
        updateElements();
        presenter.onClearMessage();
    }

    private void clear() {
        Elements.removeChildrenFrom(panelBody);
        updateElements();
        presenter.onClearMessage();
    }


    // ------------------------------------------------------ helper methods

    void updateElements() {
        int count = getMessageCount();
        header.textContent = resources.messages().notifications(getMessageCount());
        Elements.setVisible(panelBody, count != 0);
        Elements.setVisible(actions, count != 0);
        Elements.setVisible(markAllRead, getUnreadCount() != 0);
        Elements.setVisible(empty, count == 0);
    }

    private void adjustHeight() {
        int height = (int) (root.offsetHeight - 26 - 42 - 2);
        panelBody.style.maxHeight = MaxHeightUnionType.of(px(height));
    }
}
