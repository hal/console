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
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Message;

import static org.jboss.hal.resources.CSS.toastNotificationsListPf;
import static org.jboss.hal.resources.UIConstants.MESSAGE_TIMEOUT;

/**
 * A container around the messages / toast notifications to prevent overlapping of simultaneous messages.
 *
 * @author Harald Pehl
 */
class MessagePanel implements IsElement {

    private final Map<String, Integer> messageIds;
    private final Element root;

    MessagePanel() {
        this.messageIds = new HashMap<>();
        this.root = Browser.getDocument().createDivElement();
        this.root.getClassList().add(toastNotificationsListPf);
        Browser.getDocument().getBody().appendChild(root);
    }


    @Override
    public Element asElement() {
        return root;
    }

    void add(Message message) {
        String id = Ids.uniqueId();
        Element element = new MessageElement(message).asElement();
        element.setId(id);
        root.appendChild(element);

        if (!message.isSticky()) {
            startMessageTimeout(id);
            element.setOnmouseover(e1 -> stopMessageTimeout(id));
            element.setOnmouseout(e2 -> startMessageTimeout(id));
        }
    }

    private void startMessageTimeout(String id) {
        int timeoutHandle = Browser.getWindow().setTimeout(() -> removeMessage(id), MESSAGE_TIMEOUT);
        messageIds.put(id, timeoutHandle);
    }

    private void stopMessageTimeout(String id) {
        if (messageIds.containsKey(id)) {
            Browser.getWindow().clearTimeout(messageIds.get(id));
            messageIds.remove(id);
        }
    }

    private void removeMessage(String id) {
        Element element = Browser.getDocument().getElementById(id);
        if (element != null && root.contains(element)) {
            root.removeChild(element);
            messageIds.remove(id);
        }
    }
}
