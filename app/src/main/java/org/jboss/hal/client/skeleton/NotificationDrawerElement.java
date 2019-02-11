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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/** One item in {@link NotificationDrawer} */
class NotificationDrawerElement implements IsElement {

    private final NotificationDrawer notificationDrawer;
    private final HTMLElement root;

    NotificationDrawerElement(NotificationDrawer notificationDrawer, Message message, Resources resources) {
        this.notificationDrawer = notificationDrawer;

        HTMLElement iconContainer;
        String id = Ids.uniqueId();
        String dropdownId = Ids.build("dropdown", id);
        root = div().css(drawerPfNotification, unread).id(id)
                .add(div().css(dropdown, pullRight, dropdownKebabPf)
                        .add(button().id(dropdownId)
                                .css(btn, btnLink, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(span().css(CSS.fontAwesome("ellipsis-v"))))
                        .add(ul().css(dropdownMenu, dropdownMenuRight)
                                .aria(UIConstants.LABELLED_BY, dropdownId)
                                .add(li()
                                        .add(a().css(clickable)
                                                .on(click, event -> view(message))
                                                .textContent(resources.constants().view())))
                                .add(li()
                                        .add(a().css(clickable)
                                                .on(click, event -> notificationDrawer.remove(id))
                                                .textContent(resources.constants().remove())))))
                .add(iconContainer = span().css(pullLeft).get())
                .add(div().css(drawerPfNotificationContent)
                        .add(span().css(drawerPfNotificationMessage).innerHtml(message.getMessage()))
                        .add(div().css(drawerPfNotificationInfo)
                                .add(span().css(date).textContent(message.getDate()))
                                .add(span().css(time).textContent(message.getTime()))))
                .get();

        String css = null;
        switch (message.getLevel()) {
            case ERROR:
                css = Icons.ERROR;
                break;
            case WARNING:
                css = Icons.WARNING;
                break;
            case INFO:
                css = Icons.INFO;
                break;
            case SUCCESS:
                css = Icons.OK;
                break;
            default:
                break;
        }
        iconContainer.className = css + " " + pullLeft;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void view(Message message) {
        root.classList.remove(unread);
        notificationDrawer.updateElements();
        new ToastNotificationDialog(message).show();
    }
}
