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
package org.jboss.hal.core.runtime.server;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
public class ServerActions {

    public static final int SERVER_SUSPEND_TIMEOUT = 1; // not the timeout specified by the user, but the time the server needs to get into suspend mode
    public static final int SERVER_RESUME_TIMEOUT = 3;
    public static final int SERVER_START_TIMEOUT = 4;
    public static final int SERVER_STOP_TIMEOUT = 2;
    public static final int SERVER_RELOAD_TIMEOUT = 4;
    public static final int SERVER_RESTART_TIMEOUT = SERVER_STOP_TIMEOUT + SERVER_START_TIMEOUT;

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Capabilities capabilities;
    private final Resources resources;

    @Inject
    public ServerActions(final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataRegistry metadataRegistry,
            final Capabilities capabilities,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.capabilities = capabilities;
        this.resources = resources;
    }

    public void start(Server server, final Scheduler.ScheduledCommand whileStarting) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void stop(Server server, final Scheduler.ScheduledCommand whileStopping) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void reload(Server server, final Scheduler.ScheduledCommand whileReloading) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void restart(Server server, final Scheduler.ScheduledCommand whileRestarting) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void suspend(Server server, final Scheduler.ScheduledCommand whileSuspending) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void resume(Server server, final Scheduler.ScheduledCommand whileResume) {
        Browser.getWindow().alert(Names.NYI);
    }
}
