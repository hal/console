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

import java.util.HashMap;
import java.util.Map;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.resources.CSS.toastNotificationsListPf;
import static org.jboss.hal.resources.UIConstants.MESSAGE_TIMEOUT;

/**
 * A container around the messages / toast notifications which are shown to the user in the upper right corner.
 * Prevents overlapping of simultaneous messages and handles the mouse over / out events in order to pause the
 * automatic fade out time.
 *
 * @author Harald Pehl
 */
class MessagePanel implements IsElement {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Resources resources;
    private final Map<String, Integer> messageIds;
    private final Map<Long, Message> stickyMessages;
    private final Element root;

    MessagePanel(final Resources resources) {
        this.resources = resources;
        this.messageIds = new HashMap<>();
        this.stickyMessages = new HashMap<>();
        this.root = Browser.getDocument().createDivElement();
        this.root.getClassList().add(toastNotificationsListPf);
        Browser.getDocument().getBody().appendChild(root);
    }

    @Override
    public Element asElement() {
        return root;
    }

    void add(Message message) {
        if (message.isSticky() && containsStickyMessage(message)) {
            logger.debug("Swallow sticky message {}. The same message is already open", message);

        } else {
            String id = Ids.uniqueId();
            Element element = new MessagePanelElement(this, message, resources).asElement();
            element.setId(id);
            root.appendChild(element);

            if (!message.isSticky()) {
                startMessageTimeout(id);
                element.setOnmouseover(e1 -> stopMessageTimeout(id));
                element.setOnmouseout(e2 -> startMessageTimeout(id));
            } else {
                stickyMessages.put(message.getId(), message);
            }
        }
    }

    void closeSticky(Message message) {
        stickyMessages.remove(message.getId());
        logger.debug("Closed sticky message: {}", message);
    }

    private boolean containsStickyMessage(Message message) {
        return stickyMessages.containsKey(message.getId());
    }


    private void startMessageTimeout(String id) {
        int timeoutHandle = Browser.getWindow().setTimeout(() -> remove(id), MESSAGE_TIMEOUT);
        messageIds.put(id, timeoutHandle);
    }

    private void stopMessageTimeout(String id) {
        if (messageIds.containsKey(id)) {
            Browser.getWindow().clearTimeout(messageIds.get(id));
            messageIds.remove(id);
        }
    }

    private void remove(String id) {
        Element element = Browser.getDocument().getElementById(id);
        if (element != null && root.contains(element)) {
            root.removeChild(element);
            messageIds.remove(id);
        }
    }
}
