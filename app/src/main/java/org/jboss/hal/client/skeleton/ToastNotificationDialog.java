/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.skeleton;

import org.jboss.elemento.ElementsBag;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.spi.Message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import static org.jboss.elemento.Elements.bag;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.messageDetails;
import static org.jboss.hal.resources.CSS.messageDetailsPre;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.CSS.timestamp;

class ToastNotificationDialog {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Dialog dialog;

    ToastNotificationDialog(Message message) {
        String[] cssIcon = ToastNotificationElement.cssIcon(message.getLevel());
        ElementsBag builder = bag();

        // header
        builder.add(div().css(alert, cssIcon[0])
                .add(span().css(pfIcon(cssIcon[1])))
                .add(span().innerHtml(message.getMessage())));

        // details
        String header = message.getDetails() != null ? CONSTANTS.details() : CONSTANTS.noDetails();
        builder.add(p().css(messageDetails)
                .add(span().textContent(header))
                .add(span().css(pullRight, timestamp).textContent(message.getTimestamp())));
        if (message.getDetails() != null) {
            builder.add(pre().css(messageDetailsPre).innerHtml(SafeHtmlUtils.fromString(message.getDetails())));
        }

        dialog = new Dialog.Builder(CONSTANTS.message())
                .closeOnly()
                .add(builder.elements())
                .build();
    }

    public void show() {
        dialog.show();
    }
}
