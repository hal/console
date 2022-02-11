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

import java.util.HashMap;
import java.util.Map;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.mouseout;
import static org.jboss.gwt.elemento.core.EventType.mouseover;
import static org.jboss.hal.resources.CSS.toastNotificationsListPf;
import static org.jboss.hal.resources.UIConstants.MESSAGE_TIMEOUT;

/**
 * A container around the messages / toast notifications which are shown to the user in the upper right corner. Prevents
 * overlapping of simultaneous messages and handles the mouse over / out events in order to pause the automatic fade out time.
 *
 * @see <a href=
 *      "https://www.patternfly.org/pattern-library/communication/toast-notifications/">https://www.patternfly.org/pattern-library/communication/toast-notifications/</a>
 */
class ToastNotifications implements IsElement {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Resources resources;
    private final Map<String, Double> messageIds;
    private final Map<Long, Message> stickyMessages;
    private final HTMLElement root;

    ToastNotifications(Resources resources) {
        this.resources = resources;
        this.messageIds = new HashMap<>();
        this.stickyMessages = new HashMap<>();
        this.root = div().css(toastNotificationsListPf).element();
        document.body.appendChild(root);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void add(Message message) {
        if (message.isSticky() && containsStickyMessage(message)) {
            logger.debug("Swallow sticky message {}. The same message is already open", message);

        } else {
            String id = Ids.uniqueId();
            HTMLElement element = new ToastNotificationElement(this, message, resources).element();
            element.id = id;
            root.appendChild(element);

            if (!message.isSticky()) {
                startMessageTimeout(id);
                bind(element, mouseover, e1 -> stopMessageTimeout(id));
                bind(element, mouseout, e2 -> startMessageTimeout(id));
            } else {
                stickyMessages.put(message.getId(), message);
            }
        }
    }

    void close(Message message) {
        if (message.isSticky()) {
            stickyMessages.remove(message.getId());
            logger.debug("Closed sticky message: {}", message);
        }
    }

    private boolean containsStickyMessage(Message message) {
        return stickyMessages.containsKey(message.getId());
    }

    private void startMessageTimeout(String id) {
        double timeoutHandle = setTimeout((o) -> remove(id), MESSAGE_TIMEOUT);
        messageIds.put(id, timeoutHandle);
    }

    private void stopMessageTimeout(String id) {
        if (messageIds.containsKey(id)) {
            clearTimeout(messageIds.get(id));
            messageIds.remove(id);
        }
    }

    private void remove(String id) {
        Element element = document.getElementById(id);
        Elements.failSafeRemove(root, element);
        messageIds.remove(id);
    }
}
