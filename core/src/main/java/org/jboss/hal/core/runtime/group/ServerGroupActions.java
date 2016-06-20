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
package org.jboss.hal.core.runtime.group;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.BLOCKING;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_EXCEPTIONAL_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_FAILED_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_OPERATION_CALLBACK;

/**
 * @author Harald Pehl
 */
public class ServerGroupActions {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Capabilities capabilities;
    private final Resources resources;

    @Inject
    public ServerGroupActions(final EventBus eventBus,
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

    public void reload(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileReloading) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void restart(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileRestarting) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void suspend(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileSuspending) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void resume(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileResume) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void stop(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileStopping) {
        Browser.getWindow().alert(Names.NYI);
    }

    public void start(ServerGroup serverGroup, final Scheduler.ScheduledCommand whileStarting) {

        eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, Action.START));
        Operation operation = new Operation.Builder("start-servers", serverGroup.getAddress())
                .param(BLOCKING, false)
                .build();
        dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK);
    }
}
