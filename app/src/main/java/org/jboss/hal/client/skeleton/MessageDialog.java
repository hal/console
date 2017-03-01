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
class MessageDialog {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Dialog dialog;

    MessageDialog(final Message message) {
        String[] cssIcon = MessagePanelElement.cssIcon(message.getLevel());
        Elements.Builder elementBuilder = new Elements.Builder();

        // header
        elementBuilder.div().css(alert, cssIcon[0])
                .span().css(pfIcon(cssIcon[1])).end()
                .span().innerHtml(message.getMessage()).end()
                .end();

        // details
        String header = message.getDetails() != null ? CONSTANTS.details() : CONSTANTS.noDetails();
        elementBuilder.p().css(messageDetails)
                .span().textContent(header).end()
                .span().css(pullRight, timestamp).textContent(message.getTimestamp()).end()
                .end();
        if (message.getDetails() != null) {
            elementBuilder.start("pre").css(messageDetailsPre).innerHtml(SafeHtmlUtils.fromString(message.getDetails()))
                    .end();
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

