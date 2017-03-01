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

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.ALERT;
import static org.jboss.hal.resources.UIConstants.DISMISS;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.TRUE;

/**
 * A message inside the {@link MessagePanel} element.
 *
 * @author Harald Pehl
 */
class MessagePanelElement implements IsElement {

    private final Element root;

    MessagePanelElement(final MessagePanel messagePanel, final Message message, final Resources resources) {
        String[] cssIcon = cssIcon(message.getLevel());
        if (message.isSticky()) {
            cssIcon[0] = cssIcon[0] + " " + alertDismissable;
        }

        Elements.Builder builder = new Elements.Builder()
                .div().css(toastPf, alert, cssIcon[0]);
        if (message.isSticky()) {
            builder.button()
                    .css(close)
                    .data(DISMISS, ALERT)
                    .aria(HIDDEN, TRUE)
                    .on(click, event -> messagePanel.closeSticky(message))
                    .span().css(pfIcon(close)).end()
                    .end();
        }

        if (message.hasAction() || message.getDetails() != null) {
            // @formatter:off
            builder.div().css(pullRight, toastPfAction)
                .a().css(clickable).data(DISMISS, ALERT);
                    if (message.hasAction()) {
                        builder.on(click, event -> message.getCallback().execute())
                                .textContent(message.getActionTitle());
                    } else {
                        builder.on(click, event -> showMessage(message))
                                .textContent(resources.constants().details());
                    }
                builder.end()
            .end();
            // @formatter:on
        }

        builder.span().css(pfIcon(cssIcon[1])).end();
        builder.span().innerHtml(message.getMessage()).end();
        builder.end(); // </div>

        root = builder.build();
    }

    private void showMessage(final Message message) {
        new MessageDialog(message).show();
    }

    @Override
    public Element asElement() {
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
