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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.Result;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.Dispatcher.ExceptionCallback;
import org.jboss.hal.dmr.dispatch.Dispatcher.FailedCallback;
import org.jboss.hal.dmr.dispatch.TimeoutHandler;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.core.runtime.RunningState.RUNNING;
import static org.jboss.hal.core.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.DISABLED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.ModelNodeHelper.getOrDefault;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

/**
 * @author Harald Pehl
 */
public class ServerActions {

    private class ServerTimeoutCallback implements TimeoutHandler.Callback {

        private final Server server;
        private final Action action;
        private final SafeHtml successMessage;

        ServerTimeoutCallback(final Server server, final Action action, final SafeHtml successMessage) {
            this.server = server;
            this.action = action;
            this.successMessage = successMessage;
        }

        @Override
        public void onSuccess() {
            // TODO Check for server boot errors
            if (Action.isStarting(action)) {
                ResourceAddress address = server.getServerAddress().add(CORE_SERVICE, MANAGEMENT);
                Operation operation = new Operation.Builder(address, READ_BOOT_ERRORS).build();
                dispatcher.execute(operation, result -> {
                    if (!result.asList().isEmpty()) {
                        finish(server, Result.ERROR,
                                Message.error(resources.messages().serverBootErrors(server.getName())));
                    } else {
                        finish(server, Result.SUCCESS, Message.success(successMessage));
                    }
                });
            } else {
                finish(server, Result.SUCCESS, Message.success(successMessage));
            }
        }

        @Override
        public void onTimeout() {
            finish(server, Result.TIMEOUT, Message.error(resources.messages().serverTimeout(server.getName())));
        }
    }


    private class ServerFailedCallback implements FailedCallback {

        private final Server server;
        private final SafeHtml errorMessage;

        ServerFailedCallback(final Server server, final SafeHtml errorMessage) {
            this.server = server;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onFailed(final Operation operation, final String failure) {
            finish(server, Result.ERROR, Message.error(errorMessage, failure));
        }
    }


    private class ServerExceptionCallback implements ExceptionCallback {

        private final Server server;
        private final SafeHtml errorMessage;

        ServerExceptionCallback(final Server server, SafeHtml errorMessage) {
            this.server = server;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onException(final Operation operation, final Throwable exception) {
            finish(server, Result.ERROR, Message.error(errorMessage, exception.getMessage()));
        }
    }


    public static final int SERVER_SUSPEND_TIMEOUT = 1; // not the timeout specified by the user, but the time the server needs to get into suspend mode
    public static final int SERVER_RESUME_TIMEOUT = 3;
    public static final int SERVER_START_TIMEOUT = 15;
    public static final int SERVER_STOP_TIMEOUT = 4;
    public static final int SERVER_RELOAD_TIMEOUT = 5;
    public static final int SERVER_RESTART_TIMEOUT = SERVER_STOP_TIMEOUT + SERVER_START_TIMEOUT;
    private static final int SERVER_KILL_TIMEOUT = 3;
    @NonNls private static final Logger logger = LoggerFactory.getLogger(ServerActions.class);

    private static AddressTemplate serverConfigTemplate(Server server) {
        return server.isStandalone()
                ? AddressTemplate.ROOT
                : AddressTemplate.of("/host=" + server.getHost() + "/server-config=*" + server.getName());
    }


    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final Map<String, Server> pendingServers;

    @Inject
    public ServerActions(final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.resources = resources;
        this.pendingServers = new HashMap<>();
    }

    public void reload(Server server) {
        reloadRestart(server,
                new Operation.Builder(server.getServerConfigAddress(), RELOAD).param(BLOCKING, false).build(),
                Action.RELOAD, SERVER_RELOAD_TIMEOUT,
                resources.messages().reload(server.getName()),
                resources.messages().reloadServerQuestion(server.getName()),
                resources.messages().reloadServerSuccess(server.getName()),
                resources.messages().reloadServerError(server.getName()));
    }

    public void restart(Server server) {
        if (server.isStandalone()) {
            restartStandalone(server);
        } else {
            reloadRestart(server,
                    new Operation.Builder(server.getServerConfigAddress(), RESTART).param(BLOCKING, false).build(),
                    Action.RESTART, SERVER_RESTART_TIMEOUT,
                    resources.messages().restart(server.getName()),
                    resources.messages().restartServerQuestion(server.getName()),
                    resources.messages().restartServerSuccess(server.getName()),
                    resources.messages().restartServerError(server.getName()));
        }
    }

    private void restartStandalone(Server server) {
        String title = resources.messages().restart(server.getName());
        DialogFactory.showConfirmation(title,
                resources.messages().restartStandaloneQuestion(server.getName()), () -> {
                    // execute the restart with a little delay to ensure the confirmation dialog is closed
                    // before the next dialog is opened (only one modal can be open at a time!)
                    Browser.getWindow().setTimeout(() -> {

                        prepare(server, Action.RESTART);
                        BlockingDialog pendingDialog = DialogFactory
                                .buildLongRunning(title,
                                        resources.messages().restartStandalonePending(server.getName()));
                        pendingDialog.show();
                        Operation operation = new Operation.Builder(ResourceAddress.root(), SHUTDOWN)
                                .param(RESTART, true)
                                .build();
                        Operation ping = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION).build();
                        dispatcher.execute(operation,

                                result -> new TimeoutHandler(dispatcher, SERVER_RESTART_TIMEOUT)
                                        .execute(ping, new TimeoutHandler.Callback() {
                                            @Override
                                            public void onSuccess() {
                                                // wait a little bit before event handlers try to use the restarted server
                                                Browser.getWindow().setTimeout(() -> {
                                                    pendingDialog.close();
                                                    finish(Server.STANDALONE, Result.SUCCESS, Message.success(
                                                            resources.messages()
                                                                    .restartServerSuccess(server.getName())));
                                                }, 666);
                                            }

                                            @Override
                                            public void onTimeout() {
                                                pendingDialog.close();
                                                DialogFactory.buildBlocking(title,
                                                        resources.messages().restartStandaloneTimeout(server.getName()))
                                                        .show();
                                                finish(Server.STANDALONE, Result.TIMEOUT, null);
                                            }
                                        }),
                                (o1, failure) -> finish(Server.STANDALONE, Result.ERROR,
                                        Message.error(resources.messages().restartServerError(server.getName()))),
                                (o2, exception) -> finish(Server.STANDALONE, Result.ERROR,
                                        Message.error(resources.messages().restartServerError(server.getName()))));

                    }, SHORT_TIMEOUT);
                });
    }

    private void reloadRestart(Server server, Operation operation, Action action, int timeout,
            String title, SafeHtml question, SafeHtml successMessage, SafeHtml errorMessage) {
        DialogFactory.showConfirmation(title, question, () -> {

            prepare(server, action);
            dispatcher.execute(operation,
                    result -> new TimeoutHandler(dispatcher, timeout).execute(
                            server.isStandalone() ? readServerState(server) : readServerConfigStatus(server),
                            server.isStandalone() ? checkServerState(RUNNING) : checkServerConfigStatus(STARTED),
                            new ServerTimeoutCallback(server, action, successMessage)),
                    new ServerFailedCallback(server, errorMessage),
                    new ServerExceptionCallback(server, errorMessage));

        });
    }

    public void suspend(Server server) {
        if (!ManagementModel.supportsSuspend(server.getManagementVersion())) {
            logger.error("Server {} using version {} does not support suspend operation", server.getName(),
                    server.getManagementVersion());
            return;
        }

        metadataProcessor.lookup(serverConfigTemplate(server), progress.get(),
                new MetadataProcessor.MetadataCallback() {
                    @Override
                    public void onMetadata(final Metadata metadata) {
                        String id = Ids.build(SUSPEND, server.getName(), Ids.FORM_SUFFIX);
                        Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, SUSPEND).build();

                        Dialog dialog = DialogFactory.buildConfirmation(
                                resources.messages().suspend(server.getName()),
                                resources.messages().suspendServerQuestion(server.getName()),
                                form.asElement(),
                                () -> {

                                    form.save();
                                    int timeout = getOrDefault(form.getModel(), TIMEOUT,
                                            () -> form.getModel().get(TIMEOUT).asInt(), 0);
                                    int uiTimeout = timeout + SERVER_SUSPEND_TIMEOUT;

                                    prepare(server, Action.SUSPEND);
                                    Operation operation = new Operation.Builder(server.getServerConfigAddress(), SUSPEND
                                    )
                                            .param(TIMEOUT, timeout)
                                            .build();
                                    dispatcher.execute(operation,
                                            result -> new TimeoutHandler(dispatcher, uiTimeout).execute(
                                                    readSuspendState(server),
                                                    checkSuspendState(SUSPENDED),
                                                    new ServerTimeoutCallback(server, Action.SUSPEND,
                                                            resources.messages()
                                                                    .suspendServerSuccess(server.getName()))),
                                            new ServerFailedCallback(server,
                                                    resources.messages().suspendServerError(server.getName())),
                                            new ServerExceptionCallback(server,
                                                    resources.messages().suspendServerError(server.getName())));
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
    }

    public void resume(Server server) {
        if (!ManagementModel.supportsSuspend(server.getManagementVersion())) {
            logger.error("Server {} using version {} does not support resume operation", server.getName(),
                    server.getManagementVersion());
            return;
        }

        prepare(server, Action.RESUME);
        ResourceAddress address = server.isStandalone() ? server.getServerAddress() : server.getServerConfigAddress();
        Operation operation = new Operation.Builder(address, RESUME).build();
        dispatcher.execute(operation,
                result -> new TimeoutHandler(dispatcher, SERVER_START_TIMEOUT).execute(
                        server.isStandalone() ? readServerState(server) : readServerConfigStatus(server),
                        server.isStandalone() ? checkServerState(RUNNING) : checkServerConfigStatus(STARTED),
                        new ServerTimeoutCallback(server, Action.RESUME,
                                resources.messages().resumeServerSuccess(server.getName()))),
                new ServerFailedCallback(server, resources.messages().resumeServerError(server.getName())),
                new ServerExceptionCallback(server, resources.messages().resumeServerError(server.getName())));
    }

    public void stop(Server server) {
        metadataProcessor.lookup(serverConfigTemplate(server), progress.get(),
                new MetadataProcessor.MetadataCallback() {
                    @Override
                    public void onMetadata(final Metadata metadata) {
                        String id = Ids.build(STOP, server.getName(), Ids.FORM_SUFFIX);
                        Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, STOP)
                                .include(TIMEOUT).build();

                        Dialog dialog = DialogFactory.buildConfirmation(
                                resources.messages().stop(server.getName()),
                                resources.messages().stopServerQuestion(server.getName()),
                                form.asElement(),
                                () -> {

                                    form.save();
                                    int timeout = getOrDefault(form.getModel(), TIMEOUT,
                                            () -> form.getModel().get(TIMEOUT).asInt(), 0);
                                    int uiTimeout = timeout + SERVER_STOP_TIMEOUT;

                                    prepare(server, Action.STOP);
                                    Operation operation = new Operation.Builder(server.getServerConfigAddress(), STOP)
                                            .param(TIMEOUT, timeout)
                                            .param(BLOCKING, false)
                                            .build();
                                    dispatcher.execute(operation,
                                            result -> new TimeoutHandler(dispatcher, uiTimeout).execute(
                                                    readServerConfigStatus(server),
                                                    checkServerConfigStatus(STOPPED, DISABLED),
                                                    new ServerTimeoutCallback(server, Action.STOP,
                                                            resources.messages().stopServerSuccess(server.getName()))),
                                            new ServerFailedCallback(server,
                                                    resources.messages().stopServerError(server.getName())),
                                            new ServerExceptionCallback(server,
                                                    resources.messages().stopServerError(server.getName())));
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
                                .fire(eventBus,
                                        Message.error(resources.messages().metadataError(), error.getMessage()));
                    }
                });
    }

    public void kill(Server server) {
        DialogFactory.showConfirmation(resources.messages().kill(server.getName()),
                resources.messages().killServerQuestion(server.getName()),
                () -> {
                    prepare(server, Action.KILL);
                    Operation operation = new Operation.Builder(server.getServerConfigAddress(), KILL).build();
                    dispatcher.execute(operation,
                            result -> new TimeoutHandler(dispatcher, SERVER_KILL_TIMEOUT).execute(
                                    readServerConfigStatus(server),
                                    checkServerConfigStatus(STOPPED, DISABLED),
                                    new ServerTimeoutCallback(server, Action.KILL,
                                            resources.messages().killServerSuccess(server.getName()))),
                            new ServerFailedCallback(server,
                                    resources.messages().killServerError(server.getName())),
                            new ServerExceptionCallback(server,
                                    resources.messages().killServerError(server.getName())));
                });
    }

    public void start(Server server) {
        prepare(server, Action.START);
        Operation operation = new Operation.Builder(server.getServerConfigAddress(), START)
                .param(BLOCKING, false)
                .build();
        dispatcher.execute(operation,
                result -> new TimeoutHandler(dispatcher, SERVER_START_TIMEOUT).execute(
                        readServerConfigStatus(server),
                        checkServerConfigStatus(STARTED),
                        new ServerTimeoutCallback(server, Action.START,
                                resources.messages().startServerSuccess(server.getName()))),
                new ServerFailedCallback(server, resources.messages().startServerError(server.getName())),
                new ServerExceptionCallback(server, resources.messages().startServerError(server.getName())));
    }

    private void prepare(Server server, Action action) {
        markAsPending(server); // mark as pending *before* firing the event!
        eventBus.fireEvent(new ServerActionEvent(server, action));
    }

    private void finish(Server server, Result result, Message message) {
        clearPending(server); // clear pending state *before* firing the event!
        eventBus.fireEvent(new ServerResultEvent(server, result));
        MessageEvent.fire(eventBus, message);
    }

    public void markAsPending(Server server) {
        Dispatcher.setPendingLifecycleAction(true);
        pendingServers.put(Ids.hostServer(server.getHost(), server.getName()), server);
        logger.debug("Mark server {} as pending", server.getName());
    }

    public void clearPending(Server server) {
        Dispatcher.setPendingLifecycleAction(false);
        pendingServers.remove(Ids.hostServer(server.getHost(), server.getName()));
        logger.debug("Clear pending state for server {}", server.getName());
    }

    public boolean isPending(Server server) {
        return pendingServers.containsKey(Ids.hostServer(server.getHost(), server.getName()));
    }

    private Operation readServerConfigStatus(Server server) {
        return new Operation.Builder(server.getServerConfigAddress(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATUS)
                .build();
    }

    private Operation readServerState(Server server) {
        return new Operation.Builder(server.getServerAddress(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, SERVER_STATE)
                .build();
    }

    private Predicate<ModelNode> checkServerConfigStatus(ServerConfigStatus first, ServerConfigStatus... rest) {
        return result -> {
            ServerConfigStatus status = asEnumValue(result, ServerConfigStatus::valueOf, ServerConfigStatus.UNDEFINED);
            return EnumSet.of(first, rest).contains(status);
        };
    }

    private Predicate<ModelNode> checkServerState(RunningState first, RunningState... rest) {
        return result -> {
            //noinspection Convert2MethodRef (method reference leads to an error!)
            RunningState state = asEnumValue(result, (name) -> RunningState.valueOf(name), RunningState.UNDEFINED);
            return EnumSet.of(first, rest).contains(state);
        };
    }

    private Operation readSuspendState(Server server) {
        return new Operation.Builder(server.getServerAddress(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, SUSPEND_STATE)
                .build();
    }

    private Predicate<ModelNode> checkSuspendState(SuspendState statusToReach) {
        return result -> statusToReach == asEnumValue(result, SuspendState::valueOf, SuspendState.UNDEFINED);
    }
}
