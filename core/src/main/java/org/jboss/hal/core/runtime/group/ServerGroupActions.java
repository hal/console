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
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;

/**
 * @author Harald Pehl
 */
public class ServerGroupActions {

    private static final int RELOAD_TIMEOUT = 5; // seconds
    private static final int RESTART_TIMEOUT = 6; // seconds

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
        // if (serverGroup.hasServers(STARTED)) {
        //     DialogFactory.confirmation(resources.constants().reload(),
        //             resources.messages().reloadServerGroupQuestion(serverGroup.getName()), () -> {
        //                 eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, Action.RELOAD));
        //                 Operation operation = new Operation.Builder("reload-servers", serverGroup.getAddress())
        //                         .param(BLOCKING, false)
        //                         .build();
        //                 dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
        //                         NOOP_EXCEPTIONAL_CALLBACK);
        //                 new TimeoutHandler(dispatcher, reloadTimeout(serverGroup))
        //                         .execute(ping(serverGroup, STARTED), new TimeoutHandler.Callback() {
        //                             @Override
        //                             public void onSuccess() {
        //                                 host.setHostState(RunningState.RUNNING);
        //                                 MessageEvent.fire(eventBus, Message.success(successMessage));
        //                                 eventBus.fireEvent(new HostResultEvent(host, Result.SUCCESS));
        //                             }
        //
        //                             @Override
        //                             public void pending() {
        //                                 if (whileOperation != null) {
        //                                     whileOperation.execute();
        //                                 }
        //                             }
        //
        //                             @Override
        //                             public void onTimeout() {
        //                                 host.setHostState(RunningState.TIMEOUT);
        //                                 MessageEvent.fire(eventBus, Message.error(timeoutMessage));
        //                                 eventBus.fireEvent(new HostResultEvent(host, Result.TIMEOUT));
        //                             }
        //                         })
        //                 return true;
        //             });
        // } else {
        //     MessageEvent.fire(eventBus,
        //             Message.error(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        // }
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
        Browser.getWindow().alert(Names.NYI);
    }

    private int reloadTimeout(ServerGroup serverGroup) {
        if (serverGroup.hasServers(STARTED)) {
            return serverGroup.getServers(STARTED).size() * ServerActions.SERVER_RELOAD_TIMEOUT;
        } else {
            return RELOAD_TIMEOUT;
        }
    }

    private int restartTimeout(ServerGroup serverGroup) {
        if (serverGroup.hasServers(STARTED)) {
            return serverGroup.getServers(STARTED).size() * ServerActions.SERVER_RESTART_TIMEOUT;
        } else {
            return RESTART_TIMEOUT;
        }
    }

    private Operation ping(ServerGroup serverGroup, ServerConfigStatus statusCheck) {
        return null;
    }
}
