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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.spi.Message;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class MessageDialog {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Dialog dialog;

    public MessageDialog(final Message message) {
        String[] cssIcon = MessageElement.cssIcon(message.getLevel());
        Elements.Builder elementBuilder = new Elements.Builder();

        // header
        elementBuilder.div().css(alert, cssIcon[0])
                .span().css(pfIcon(cssIcon[1])).end()
                .span().innerText(message.getMessage()).end()
                .end();

        // details
        String header = message.getDetails() != null ? CONSTANTS.details() : CONSTANTS.noDetails();
        elementBuilder.p().css(messageDetails)
                .span().innerText(header).end()
                .span().css(pullRight, timestamp).innerText(message.getTimestamp()).end()
                .end();
        if (message.getDetails() != null) {
            elementBuilder.start("pre").innerHtml(SafeHtmlUtils.fromString(message.getDetails())).end();
        }

        dialog = new Dialog.Builder(CONSTANTS.message())
                .closeOnly()
                .closeIcon(true)
                .closeOnEsc(true)
                .add(elementBuilder.elements())
                .build();
    }

    public void show() {
        dialog.show();
    }
}

