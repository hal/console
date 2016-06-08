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
package org.jboss.hal.core;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Harald Pehl
 */
public class ServerGroupSelectionEvent extends GwtEvent<ServerGroupSelectionEvent.ServerGroupSelectionHandler> {

    public interface ServerGroupSelectionHandler extends EventHandler {

        void onServerGroupSelected(ServerGroupSelectionEvent event);
    }


    private static final Type<ServerGroupSelectionHandler> TYPE = new Type<>();

    public static Type<ServerGroupSelectionHandler> getType() {
        return TYPE;
    }

    private final String serverGroup;

    public ServerGroupSelectionEvent(final String serverGroup) {this.serverGroup = serverGroup;}

    public String getServerGroup() {
        return serverGroup;
    }

    @Override
    protected void dispatch(ServerGroupSelectionHandler handler) {
        handler.onServerGroupSelected(this);
    }

    @Override
    public Type<ServerGroupSelectionHandler> getAssociatedType() {
        return TYPE;
    }
}
