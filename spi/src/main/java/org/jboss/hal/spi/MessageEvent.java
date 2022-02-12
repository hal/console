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
package org.jboss.hal.spi;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;

// No @GenEvent here due to naming conflicts
public class MessageEvent extends GwtEvent<MessageEvent.MessageHandler> {

    private static final Type<MessageHandler> TYPE = new Type<>();

    public static Type<MessageHandler> getType() {
        return TYPE;
    }

    public static void fire(EventBus eventBus, Message message) {
        eventBus.fireEvent(new MessageEvent(message));
    }

    private final Message message;

    public MessageEvent(final Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    protected void dispatch(MessageHandler handler) {
        handler.onMessage(this);
    }

    @Override
    public Type<MessageHandler> getAssociatedType() {
        return TYPE;
    }

    public interface MessageHandler extends EventHandler {

        void onMessage(MessageEvent event);
    }
}
