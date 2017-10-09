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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.ALERT;
import static org.jboss.hal.resources.UIConstants.DISMISS;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.TRUE;

/** A message inside the {@link ToastNotifications} element. */
class ToastNotificationElement implements IsElement {

    private final HTMLElement root;

    ToastNotificationElement(final ToastNotifications toastNotifications, final Message message, final Resources resources) {
        String[] cssIcon = cssIcon(message.getLevel());
        if (message.isSticky()) {
            cssIcon[0] = cssIcon[0] + " " + alertDismissable;
        }

        root = Elements.div().css(toastPf, alert, cssIcon[0])
                .asElement();
        if (message.isSticky()) {
            root.appendChild(button()
                    .css(close)
                    .data(DISMISS, ALERT)
                    .aria(HIDDEN, TRUE)
                    .on(click, event -> toastNotifications.closeSticky(message))
                    .add(span().css(pfIcon(close)))
                    .asElement());
        }
        if (message.hasAction() || message.getDetails() != null) {
            HTMLElement a;
            root.appendChild(div().css(pullRight, toastPfAction)
                    .add(a = a()
                            .css(clickable)
                            .data(DISMISS, ALERT)
                            .asElement())
                    .asElement());
            if (message.hasAction()) {
                a.textContent = message.getActionTitle();
                bind(a, click, event -> message.getCallback().execute());
            } else {
                a.textContent = resources.constants().details();
                bind(a, click, event -> showMessage(message));
            }
        }
        root.appendChild(span().css(pfIcon(cssIcon[1])).asElement());
        root.appendChild(span().innerHtml(message.getMessage()).asElement());
    }

    private void showMessage(final Message message) {
        new ToastNotificationDialog(message).show();
    }

    @Override
    public HTMLElement asElement() {
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
        }
        return new String[]{css, icon};
    }
}
