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
package org.jboss.hal.spi;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Harald Pehl
 */
public class MessageEvent extends GwtEvent<MessageEvent.MessageHandler> {

    public interface MessageHandler extends EventHandler {

        void onMessage(MessageEvent event);
    }


    private static final Type<MessageHandler> TYPE = new Type<>();

    public static Type<MessageHandler> getType() {
        return TYPE;
    }

    private final Message message;

    public MessageEvent(final Message message) {this.message = message;}

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
}
