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

import java.util.List;
import java.util.function.Predicate;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.Result;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.TimeoutHandler;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.runtime.SuspendState.RUNNING;
import static org.jboss.hal.core.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_EXCEPTIONAL_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_FAILED_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_OPERATION_CALLBACK;

/**
 * @author Harald Pehl
 */
public class ServerGroupActions {

    private class ServerGroupTimeoutCallback implements TimeoutHandler.Callback {

        private final ServerGroup serverGroup;
        private final SafeHtml successMessage;
        private final ScheduledCommand whileOperation;

        ServerGroupTimeoutCallback(final ServerGroup serverGroup, final ScheduledCommand whileOperation,
                final SafeHtml successMessage) {
            this.serverGroup = serverGroup;
            this.successMessage = successMessage;
            this.whileOperation = whileOperation;
        }

        @Override
        public void onSuccess() {
            MessageEvent.fire(eventBus, Message.success(successMessage));
            eventBus.fireEvent(new ServerGroupResultEvent(serverGroup, Result.SUCCESS));
        }

        @Override
        public void pending() {
            if (whileOperation != null) {
                whileOperation.execute();
            }
        }

        @Override
        public void onTimeout() {
            MessageEvent.fire(eventBus, Message.error(resources.messages().serverGroupTimeout(serverGroup.getName())));
            eventBus.fireEvent(new ServerGroupResultEvent(serverGroup, Result.TIMEOUT));
        }
    }


    private static final int DEFAULT_TIMEOUT = 5; // seconds

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

    public void reload(ServerGroup serverGroup, final ScheduledCommand whileReloading) {
        reloadRestart(serverGroup,
                new Operation.Builder(RELOAD_SERVERS, serverGroup.getAddress()).param(BLOCKING, false).build(),
                Action.RELOAD,
                whileReloading,
                resources.messages().reload(serverGroup.getName()),
                resources.messages().reloadServerGroupQuestion(serverGroup.getName()),
                resources.messages().reloadServerGroupSuccess(serverGroup.getName()));
    }

    public void restart(ServerGroup serverGroup, final ScheduledCommand whileRestarting) {
        reloadRestart(serverGroup,
                new Operation.Builder(RESTART_SERVERS, serverGroup.getAddress()).param(BLOCKING, false).build(),
                Action.RESTART,
                whileRestarting,
                resources.messages().restart(serverGroup.getName()),
                resources.messages().restartServerGroupQuestion(serverGroup.getName()),
                resources.messages().restartServerGroupSuccess(serverGroup.getName()));
    }

    private void reloadRestart(ServerGroup serverGroup, Operation operation, Action action,
            ScheduledCommand whileOperation, String title, SafeHtml question, SafeHtml successMessage) {

        List<Server> startedServers = serverGroup.getServers(STARTED);
        if (!startedServers.isEmpty()) {
            DialogFactory.confirmation(title, question, () -> {

                eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, action));
                dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
                        NOOP_EXCEPTIONAL_CALLBACK);

                new TimeoutHandler(dispatcher, timeout(serverGroup, action)).execute(
                        readServerConfigStatus(startedServers),
                        checkServerConfigStatus(startedServers.size(), STARTED),
                        new ServerGroupTimeoutCallback(serverGroup, whileOperation, successMessage));
                return true;
            });

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void suspend(ServerGroup serverGroup, final ScheduledCommand whileSuspending) {
        List<Server> startedServers = serverGroup.getServers(STARTED);
        if (!startedServers.isEmpty()) {
            Metadata serverGroupMetadata = metadataRegistry.lookup(AddressTemplate.of("/{selected.group}"));
            Form<ModelNode> form = new OperationFormBuilder<>(
                    IdBuilder.build(SUSPEND_SERVERS, serverGroup.getName(), "form"), serverGroupMetadata,
                    SUSPEND_SERVERS)
                    .build();

            Dialog dialog = DialogFactory
                    .confirmation(resources.messages().suspend(serverGroup.getName()),
                            resources.messages().suspendServerGroupQuestion(serverGroup.getName()), form.asElement(),
                            () -> {

                                eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, Action.SUSPEND));
                                form.save();
                                int timeout = form.getModel().get(TIMEOUT).asInt();
                                int uiTimeout = timeout + timeout(serverGroup, Action.SUSPEND);

                                Operation operation = new Operation.Builder(SUSPEND_SERVERS, serverGroup.getAddress())
                                        .param(TIMEOUT, timeout)
                                        .build();
                                dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
                                        NOOP_EXCEPTIONAL_CALLBACK);

                                new TimeoutHandler(dispatcher, uiTimeout).execute(
                                        readSuspendState(startedServers),
                                        checkSuspendState(startedServers.size(), SUSPENDED),
                                        new ServerGroupTimeoutCallback(serverGroup, whileSuspending,
                                                resources.messages().suspendServerGroupSuccess(serverGroup.getName())));
                                return true;
                            });
            dialog.registerAttachable(form);
            dialog.show();

            ModelNode model = new ModelNode();
            model.get(TIMEOUT).set(0);
            form.add(model);

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void resume(ServerGroup serverGroup, final ScheduledCommand whileResume) {
        List<Server> suspendedServers = serverGroup.getServers(SUSPENDED);
        if (!suspendedServers.isEmpty()) {
            eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, Action.RESUME));
            Operation operation = new Operation.Builder(RESUME_SERVERS, serverGroup.getAddress()).build();
            dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
                    NOOP_EXCEPTIONAL_CALLBACK);

            new TimeoutHandler(dispatcher, timeout(serverGroup, Action.RESUME)).execute(
                    readSuspendState(suspendedServers),
                    checkSuspendState(suspendedServers.size(), RUNNING),
                    new ServerGroupTimeoutCallback(serverGroup, whileResume,
                            resources.messages().resumeServerGroupSuccess(serverGroup.getName())));

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoSuspendedServers(serverGroup.getName())));
        }
    }

    public void stop(ServerGroup serverGroup, final ScheduledCommand whileStopping) {
        List<Server> startedServers = serverGroup.getServers(STARTED);
        if (!startedServers.isEmpty()) {
            Metadata serverGroupMetadata = metadataRegistry.lookup(AddressTemplate.of("/{selected.group}"));
            Form<ModelNode> form = new OperationFormBuilder<>(
                    IdBuilder.build(STOP_SERVERS, serverGroup.getName(), "form"), serverGroupMetadata, STOP_SERVERS)
                    .build();

            Dialog dialog = DialogFactory
                    .confirmation(resources.messages().stop(serverGroup.getName()),
                            resources.messages().stopServerGroupQuestion(serverGroup.getName()), form.asElement(),
                            () -> {

                                eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, Action.STOP));
                                form.save();
                                int timeout = form.getModel().get(TIMEOUT).asInt();
                                int uiTimeout = timeout + timeout(serverGroup, Action.STOP);

                                Operation operation = new Operation.Builder(SUSPEND_SERVERS, serverGroup.getAddress())
                                        .param(TIMEOUT, timeout)
                                        .param(BLOCKING, false)
                                        .build();
                                dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
                                        NOOP_EXCEPTIONAL_CALLBACK);

                                new TimeoutHandler(dispatcher, uiTimeout).execute(
                                        readServerConfigStatus(startedServers),
                                        checkServerConfigStatus(startedServers.size(), STOPPED),
                                        new ServerGroupTimeoutCallback(serverGroup, whileStopping,
                                                resources.messages().stopServerSuccess(serverGroup.getName())));
                                return true;
                            });
            dialog.registerAttachable(form);
            dialog.show();

            ModelNode model = new ModelNode();
            model.get(TIMEOUT).set(0);
            form.add(model);

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void start(ServerGroup serverGroup, final ScheduledCommand whileStarting) {
        List<Server> downServers = serverGroup
                .getServers(STOPPED, ServerConfigStatus.DISABLED, ServerConfigStatus.FAILED);
        if (!downServers.isEmpty()) {
            Operation operation = new Operation.Builder(SUSPEND_SERVERS, serverGroup.getAddress())
                    .param(BLOCKING, false)
                    .build();
            dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK,
                    NOOP_EXCEPTIONAL_CALLBACK);

            new TimeoutHandler(dispatcher, timeout(serverGroup, Action.START)).execute(
                    readServerConfigStatus(downServers),
                    checkServerConfigStatus(downServers.size(), STARTED),
                    new ServerGroupTimeoutCallback(serverGroup, whileStarting,
                            resources.messages().startServerGroupSuccess(serverGroup.getName())));

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStoppedServers(serverGroup.getName())));
        }
    }

    private int timeout(ServerGroup serverGroup, Action action) {
        int timeout = DEFAULT_TIMEOUT;
        switch (action) {
            case RELOAD:
                if (serverGroup.hasServers(STARTED)) {
                    timeout = serverGroup.getServers(STARTED).size() * ServerActions.SERVER_RELOAD_TIMEOUT;
                }
                break;
            case RESTART:
                if (serverGroup.hasServers(STARTED)) {
                    timeout = serverGroup.getServers(STARTED).size() * ServerActions.SERVER_RESTART_TIMEOUT;
                }
                break;
            case SUSPEND:
                if (serverGroup.hasServers(STARTED)) {
                    timeout = serverGroup.getServers(STARTED).size() * ServerActions.SERVER_SUSPEND_TIMEOUT;
                }
                break;
            case RESUME:
                if (serverGroup.hasServers(SUSPENDED)) {
                    timeout = serverGroup.getServers(SUSPENDED).size() * ServerActions.SERVER_RESUME_TIMEOUT;
                }
                break;
            case START:
                if (serverGroup.hasServers(STOPPED, ServerConfigStatus.DISABLED, ServerConfigStatus.FAILED)) {
                    timeout = serverGroup.getServers(STOPPED, ServerConfigStatus.DISABLED, ServerConfigStatus.FAILED)
                            .size() * ServerActions.SERVER_RESUME_TIMEOUT;
                }
                break;
            case STOP:
                break;
        }
        return timeout;
    }

    private Composite readServerConfigStatus(List<Server> servers) {
        return new Composite(servers.stream()
                .map(server -> new Operation.Builder(READ_ATTRIBUTE_OPERATION, server.getServerConfigAddress())
                        .param(NAME, STATUS)
                        .build())
                .collect(toList()));
    }

    private Predicate<CompositeResult> checkServerConfigStatus(long servers, ServerConfigStatus statusToReach) {
        return compositeResult -> {
            long statusCount = compositeResult.stream()
                    .map(step -> asEnumValue(step, RESULT, ServerConfigStatus::valueOf, ServerConfigStatus.UNDEFINED))
                    .filter(status -> status == statusToReach)
                    .count();
            return statusCount == servers;
        };
    }

    private Composite readSuspendState(List<Server> servers) {
        return new Composite(servers.stream()
                .map(server -> new Operation.Builder(READ_ATTRIBUTE_OPERATION, server.getServerAddress())
                        .param(NAME, SUSPEND_STATE)
                        .build())
                .collect(toList()));
    }

    private Predicate<CompositeResult> checkSuspendState(long servers, SuspendState statusToReach) {
        return compositeResult -> {
            long statusCount = compositeResult.stream()
                    .map(step -> asEnumValue(step, RESULT, SuspendState::valueOf, SuspendState.UNDEFINED))
                    .filter(status -> status == statusToReach)
                    .count();
            return statusCount == servers;
        };
    }
}
