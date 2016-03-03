/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.skeleton;

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class MessageElement implements IsElement {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Element root;

    MessageElement(final Message message) {
        String[] cssIcon = cssIcon(message.getLevel());
        if (message.isSticky()) {
            cssIcon[0] = cssIcon[0] + " " + alertDismissable;
        }

        Elements.Builder builder = new Elements.Builder()
                .div().css(toastPf, toastPfMaxWidth, toastPfTopRight, alert, cssIcon[0]);
        if (message.isSticky()) {
            //noinspection HardCodedStringLiteral
            builder.button().css(close).data("dismiss", "alert").aria(UIConstants.HIDDEN, String.valueOf(true))
                    .span().css(pfIcon(close)).end()
                    .end();
        }
        if (message.getDetails() != null) {
            builder.div().css(pullRight, toastPfAction)
                    .a().css(clickable).on(click, event -> showMessage(message))
                    .textContent(CONSTANTS.details()).end()
                    .end();
        }
        builder.span().css(pfIcon(cssIcon[1])).end();
        builder.span().textContent(message.getMessage()).end();
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
