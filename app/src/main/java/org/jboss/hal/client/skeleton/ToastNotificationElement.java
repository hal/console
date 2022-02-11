/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.skeleton;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/** A message inside the {@link ToastNotifications} element. */
class ToastNotificationElement implements IsElement {

    private final HTMLElement root;

    ToastNotificationElement(ToastNotifications toastNotifications, Message message, Resources resources) {
        String[] cssIcon = cssIcon(message.getLevel());
        root = Elements.div().css(alert, cssIcon[0]).element();
        root.appendChild(button()
                .css(close)
                .aria(HIDDEN, TRUE)
                .aria(LABEL, "Close")
                .data(DISMISS, ALERT)
                .on(click, event -> toastNotifications.close(message))
                .add(span().css(pfIcon(close))).element());
        root.appendChild(span().css(pfIcon(cssIcon[1])).element());
        root.appendChild(span().innerHtml(message.getMessage()).element());
        if (message.hasAction() || message.getDetails() != null) {
            HTMLElement a;
            root.appendChild(span().css(marginLeft5)
                    .add(a = a()
                            .css(clickable, alertLink)
                            .data(DISMISS, ALERT).element())
                    .element());
            if (message.hasAction()) {
                a.textContent = message.getActionTitle();
                bind(a, click, event -> message.getCallback().execute());
            } else {
                a.textContent = resources.constants().details();
                bind(a, click, event -> showMessage(message));
            }
        }
    }

    private void showMessage(Message message) {
        new ToastNotificationDialog(message).show();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    static String[] cssIcon(Message.Level level) {
        String css = "";
        String icon = "";
        switch (level) {
            case ERROR:
                css = alertDanger;
                icon = errorCircleO;
                break;
            case WARNING:
                css = alertWarning;
                icon = warningTriangleO;
                break;
            case INFO:
                css = alertInfo;
                icon = info;
                break;
            case SUCCESS:
                css = alertSuccess;
                icon = ok;
                break;
            default:
                break;
        }
        return new String[] { css + " " + alertDismissable, icon };
    }
}
