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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Provider;
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
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.MetadataProcessor.MetadataCallback;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.CompletableSubscriber;
import rx.Subscription;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.runtime.Action.RESUME;
import static org.jboss.hal.core.runtime.SuspendState.RUNNING;
import static org.jboss.hal.core.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.DISABLED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BLOCKING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESUME_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STOP_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMEOUT;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.ModelNodeHelper.getOrDefault;
import static org.jboss.hal.dmr.dispatch.TimeoutHandler.repeatCompositeUntil;

/** TODO Fire events for the servers of a server group as well. */
public class ServerGroupActions {

    private class ServerGroupTimeoutCallback implements CompletableSubscriber {

        private final ServerGroup serverGroup;
        private final List<Server> servers;
        private final SafeHtml successMessage;

        ServerGroupTimeoutCallback(final ServerGroup serverGroup, final List<Server> servers,
                final SafeHtml successMessage) {
            this.serverGroup = serverGroup;
            this.servers = servers;
            this.successMessage = successMessage;
        }

        @Override
        public void onCompleted() {
            finish(serverGroup, servers, Result.SUCCESS, Message.success(successMessage));
        }

        @Override
        public void onError(Throwable e) {
            finish(serverGroup, servers, Result.TIMEOUT,
                    Message.error(resources.messages().serverGroupTimeout(serverGroup.getName())));
        }
        @Override public void onSubscribe(Subscription d) {}
    }


    private class ServerGroupFailedCallback implements Dispatcher.FailedCallback {

        private final ServerGroup serverGroup;
        private final List<Server> servers;
        private final SafeHtml errorMessage;

        ServerGroupFailedCallback(final ServerGroup serverGroup, final List<Server> servers,
                final SafeHtml errorMessage) {
            this.serverGroup = serverGroup;
            this.servers = servers;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onFailed(final Operation operation, final String failure) {
            finish(serverGroup, servers, Result.ERROR, Message.error(errorMessage, failure));
        }
    }


    private class ServerGroupExceptionCallback implements Dispatcher.ExceptionCallback {

        private final ServerGroup serverGroup;
        private final List<Server> servers;
        private final SafeHtml errorMessage;

        ServerGroupExceptionCallback(final ServerGroup serverGroup, final List<Server> servers, SafeHtml errorMessage) {
            this.serverGroup = serverGroup;
            this.servers = servers;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onException(final Operation operation, final Throwable exception) {
            finish(serverGroup, servers, Result.ERROR, Message.error(errorMessage, exception.getMessage()));
        }
    }


    private static final int DEFAULT_TIMEOUT = 10; // seconds
    @NonNls private static final Logger logger = LoggerFactory.getLogger(ServerGroupActions.class);

    private static AddressTemplate serverGroupTemplate(ServerGroup serverGroup) {
        return AddressTemplate.of("/server-group=" + serverGroup.getName());
    }

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final ServerActions serverActions;
    private final Resources resources;
    private final Map<String, ServerGroup> pendingServerGroups;

    @Inject
    public ServerGroupActions(EventBus eventBus,
            Dispatcher dispatcher,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            ServerActions serverActions,
            Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.serverActions = serverActions;
        this.resources = resources;
        this.pendingServerGroups = new HashMap<>();
    }

    public void reload(ServerGroup serverGroup) {
        reloadRestart(serverGroup,
                new Operation.Builder(serverGroup.getAddress(), RELOAD_SERVERS).param(BLOCKING, false).build(),
                Action.RELOAD,
                resources.messages().reload(serverGroup.getName()),
                resources.messages().reloadServerGroupQuestion(serverGroup.getName()),
                resources.messages().reloadServerGroupSuccess(serverGroup.getName()),
                resources.messages().reloadServerGroupError(serverGroup.getName()));
    }

    public void restart(ServerGroup serverGroup) {
        reloadRestart(serverGroup,
                new Operation.Builder(serverGroup.getAddress(), RESTART_SERVERS).param(BLOCKING, false).build(),
                Action.RESTART,
                resources.messages().restart(serverGroup.getName()),
                resources.messages().restartServerGroupQuestion(serverGroup.getName()),
                resources.messages().restartServerGroupSuccess(serverGroup.getName()),
                resources.messages().restartServerGroupError(serverGroup.getName()));
    }

    private void reloadRestart(ServerGroup serverGroup, Operation operation, Action action,
            String title, SafeHtml question, SafeHtml successMessage, SafeHtml errorMessage) {

        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        if (!startedServers.isEmpty()) {
            DialogFactory.showConfirmation(title, question, () -> {
                prepare(serverGroup, startedServers, action);
                dispatcher.execute(operation, result -> repeatCompositeUntil(dispatcher, timeout(serverGroup, action),
                        readServerConfigStatus(startedServers), checkServerConfigStatus(startedServers.size(), STARTED))
                                .subscribe(new ServerGroupTimeoutCallback(serverGroup, startedServers, successMessage)),
                        new ServerGroupFailedCallback(serverGroup, startedServers, errorMessage),
                        new ServerGroupExceptionCallback(serverGroup, startedServers, errorMessage));
            });

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void suspend(ServerGroup serverGroup) {
        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        if (!startedServers.isEmpty()) {
            metadataProcessor.lookup(serverGroupTemplate(serverGroup), progress.get(), new MetadataCallback() {
                @Override
                public void onMetadata(final Metadata metadata) {
                    String id = Ids.build(SUSPEND_SERVERS, serverGroup.getName(), Ids.FORM_SUFFIX);
                    Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, SUSPEND_SERVERS).build();

                    Dialog dialog = DialogFactory.buildConfirmation(
                            resources.messages().suspend(serverGroup.getName()),
                            resources.messages().suspendServerGroupQuestion(serverGroup.getName()),
                            form.asElement(),
                            () -> {

                                form.save();
                                int timeout = getOrDefault(form.getModel(), TIMEOUT,
                                        () -> form.getModel().get(TIMEOUT).asInt(), 0);
                                int uiTimeout = timeout + timeout(serverGroup, Action.SUSPEND);

                                prepare(serverGroup, startedServers, Action.SUSPEND);
                                Operation operation = new Operation.Builder(serverGroup.getAddress(), SUSPEND_SERVERS)
                                        .param(TIMEOUT, timeout)
                                        .build();
                                dispatcher.execute(operation, result -> repeatCompositeUntil(dispatcher, uiTimeout,
                                        readSuspendState(startedServers),
                                        checkSuspendState(startedServers.size(), SUSPENDED))
                                                .subscribe(new ServerGroupTimeoutCallback(serverGroup, startedServers,
                                                        resources.messages().suspendServerGroupSuccess(serverGroup.getName()))),
                                        new ServerGroupFailedCallback(serverGroup, startedServers, resources
                                                .messages().suspendServerGroupError(serverGroup.getName())),
                                        new ServerGroupExceptionCallback(serverGroup, startedServers, resources
                                                .messages().suspendServerGroupError(serverGroup.getName())));
                            });
                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(TIMEOUT).set(0);
                    form.edit(model);
                }

                @Override
                public void onError(final Throwable error) {
                    MessageEvent.fire(eventBus,
                            Message.error(resources.messages().metadataError(), error.getMessage()));
                }
            });

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void resume(ServerGroup serverGroup) {
        List<Server> suspendedServers = serverGroup.getServers(Server::isSuspended);
        if (!suspendedServers.isEmpty()) {
            prepare(serverGroup, suspendedServers, RESUME);
            Operation operation = new Operation.Builder(serverGroup.getAddress(), RESUME_SERVERS).build();
            dispatcher.execute(operation, result -> repeatCompositeUntil(dispatcher, timeout(serverGroup, RESUME),
                    readSuspendState(suspendedServers), checkSuspendState(suspendedServers.size(), RUNNING))
                            .subscribe(new ServerGroupTimeoutCallback(serverGroup, suspendedServers,
                                    resources.messages().resumeServerGroupSuccess(serverGroup.getName()))),
                    new ServerGroupFailedCallback(serverGroup, suspendedServers,
                            resources.messages().resumeServerGroupError(serverGroup.getName())),
                    new ServerGroupExceptionCallback(serverGroup, suspendedServers,
                            resources.messages().resumeServerGroupError(serverGroup.getName())));

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoSuspendedServers(serverGroup.getName())));
        }
    }

    public void stop(ServerGroup serverGroup) {
        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        if (!startedServers.isEmpty()) {
            metadataProcessor.lookup(serverGroupTemplate(serverGroup), progress.get(), new MetadataCallback() {
                @Override
                public void onMetadata(final Metadata metadata) {
                    String id = Ids.build(STOP_SERVERS, serverGroup.getName(), Ids.FORM_SUFFIX);
                    Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, STOP_SERVERS)
                            .include(TIMEOUT).build();

                    Dialog dialog = DialogFactory.buildConfirmation(
                            resources.messages().stop(serverGroup.getName()),
                            resources.messages().stopServerGroupQuestion(serverGroup.getName()),
                            form.asElement(),
                            () -> {

                                form.save();
                                int timeout = getOrDefault(form.getModel(), TIMEOUT,
                                        () -> form.getModel().get(TIMEOUT).asInt(), 0);
                                int uiTimeout = timeout + timeout(serverGroup, Action.STOP);

                                prepare(serverGroup, startedServers, Action.STOP);
                                Operation operation = new Operation.Builder(serverGroup.getAddress(), STOP_SERVERS
                                )
                                        .param(TIMEOUT, timeout)
                                        .param(BLOCKING, false)
                                        .build();
                                dispatcher.execute(operation, result -> repeatCompositeUntil(dispatcher, uiTimeout,
                                        readServerConfigStatus(startedServers),
                                        checkServerConfigStatus(startedServers.size(), STOPPED, DISABLED))
                                                .subscribe(new ServerGroupTimeoutCallback(serverGroup, startedServers,
                                                        resources.messages().stopServerGroupSuccess(serverGroup.getName()))),
                                        new ServerGroupFailedCallback(serverGroup, startedServers, resources
                                                .messages().stopServerGroupError(serverGroup.getName())),
                                        new ServerGroupExceptionCallback(serverGroup, startedServers, resources
                                                .messages().stopServerGroupError(serverGroup.getName())));
                            });
                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(TIMEOUT).set(0);
                    form.edit(model);
                }

                @Override
                public void onError(final Throwable error) {
                    MessageEvent
                            .fire(eventBus, Message.error(resources.messages().metadataError(), error.getMessage()));
                }
            });

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStartedServers(serverGroup.getName())));
        }
    }

    public void start(ServerGroup serverGroup) {
        List<Server> downServers = serverGroup.getServers(server -> server.isStopped() || server.isFailed());
        if (!downServers.isEmpty()) {
            prepare(serverGroup, downServers, Action.START);
            Operation operation = new Operation.Builder(serverGroup.getAddress(), START_SERVERS)
                    .param(BLOCKING, false)
                    .build();
            dispatcher.execute(operation,
                    result -> repeatCompositeUntil(dispatcher, timeout(serverGroup, Action.START),
                            readServerConfigStatus(downServers), checkServerConfigStatus(downServers.size(), STARTED))
                            .subscribe(new ServerGroupTimeoutCallback(serverGroup, downServers,
                                    resources.messages().startServerGroupSuccess(serverGroup.getName()))),
                    new ServerGroupFailedCallback(serverGroup, downServers,
                            resources.messages().startServerGroupError(serverGroup.getName())),
                    new ServerGroupExceptionCallback(serverGroup, downServers,
                            resources.messages().startServerGroupError(serverGroup.getName())));

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStoppedServers(serverGroup.getName())));
        }
    }

    private int timeout(ServerGroup serverGroup, Action action) {
        int timeout = DEFAULT_TIMEOUT;
        switch (action) {
            case RELOAD:
                if (serverGroup.hasServers(Server::isStarted)) {
                    timeout = serverGroup.getServers(Server::isStarted).size() * ServerActions.SERVER_RELOAD_TIMEOUT;
                }
                break;
            case RESTART:
                if (serverGroup.hasServers(Server::isStarted)) {
                    timeout = serverGroup.getServers(Server::isStarted).size() * ServerActions.SERVER_RESTART_TIMEOUT;
                }
                break;
            case SUSPEND:
                if (serverGroup.hasServers(Server::isStarted)) {
                    timeout = serverGroup.getServers(Server::isStarted).size() * ServerActions.SERVER_SUSPEND_TIMEOUT;
                }
                break;
            case RESUME:
                if (serverGroup.hasServers(Server::isSuspended)) {
                    timeout = serverGroup.getServers(Server::isSuspended).size() * ServerActions.SERVER_RESUME_TIMEOUT;
                }
                break;
            case START:
                if (serverGroup.hasServers(server -> server.isStopped() || server.isFailed())) {
                    timeout = serverGroup.getServers(server -> server.isStopped() || server.isFailed())
                            .size() * ServerActions.SERVER_START_TIMEOUT;
                }
                break;
            case STOP:
                if (serverGroup.hasServers(Server::isStarted)) {
                    timeout = serverGroup.getServers(Server::isStarted).size() * ServerActions.SERVER_STOP_TIMEOUT;
                }
                break;
        }
        return timeout;
    }

    private void prepare(ServerGroup serverGroup, List<Server> servers, Action action) {
        markAsPending(serverGroup); // mark as pending *before* firing the event!
        servers.forEach(serverActions::markAsPending);
        eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, servers, action));
    }

    private void finish(ServerGroup serverGroup, List<Server> servers, Result result, Message message) {
        clearPending(serverGroup); // clear pending state *before* firing the event!
        servers.forEach(serverActions::clearPending);
        eventBus.fireEvent(new ServerGroupResultEvent(serverGroup, servers, result));
        MessageEvent.fire(eventBus, message);
    }

    private void markAsPending(ServerGroup serverGroup) {
        Dispatcher.setPendingLifecycleAction(true);
        pendingServerGroups.put(serverGroup.getName(), serverGroup);
        logger.debug("Mark server group {} as pending", serverGroup.getName());
    }

    private void clearPending(ServerGroup serverGroup) {
        Dispatcher.setPendingLifecycleAction(false);
        pendingServerGroups.remove(serverGroup.getName());
        logger.debug("Clear pending state for server group {}", serverGroup.getName());
    }

    public boolean isPending(ServerGroup serverGroup) {
        return pendingServerGroups.containsKey(serverGroup.getName());
    }

    private Composite readServerConfigStatus(List<Server> servers) {
        return new Composite(servers.stream()
                .map(server -> new Operation.Builder(server.getServerConfigAddress(), READ_ATTRIBUTE_OPERATION)
                        .param(NAME, STATUS)
                        .build())
                .collect(toList()));
    }

    private Predicate<CompositeResult> checkServerConfigStatus(long servers, ServerConfigStatus first,
            ServerConfigStatus... rest) {
        return compositeResult -> {
            long statusCount = compositeResult.stream()
                    .map(step -> asEnumValue(step, RESULT, ServerConfigStatus::valueOf, ServerConfigStatus.UNDEFINED))
                    .filter(status -> EnumSet.of(first, rest).contains(status))
                    .count();
            return statusCount == servers;
        };
    }

    private Composite readSuspendState(List<Server> servers) {
        return new Composite(servers.stream()
                .map(server -> new Operation.Builder(server.getServerAddress(), READ_ATTRIBUTE_OPERATION)
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
