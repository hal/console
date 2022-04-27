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
package org.jboss.hal.core.runtime.group;

import java.util.Collections;
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
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerConfigStatus;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowStatus;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.MetadataProcessor.MetadataCallback;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.runtime.Action.RESUME;
import static org.jboss.hal.core.runtime.SuspendState.RUNNING;
import static org.jboss.hal.core.runtime.SuspendState.SUSPENDED;
import static org.jboss.hal.core.runtime.TimeoutHandler.repeatCompositeUntil;
import static org.jboss.hal.core.runtime.Timeouts.serverGroupTimeout;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.DISABLED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BLOCKING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COPY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESTROY_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KILL_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESUME_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STOP_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_TIMEOUT;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.ModelNodeHelper.getOrDefault;
import static org.jboss.hal.flow.FlowStatus.FAILURE;
import static org.jboss.hal.flow.FlowStatus.SUCCESS;

/** TODO Fire events for the servers of a server group as well. */
public class ServerGroupActions {

    private static final Logger logger = LoggerFactory.getLogger(ServerGroupActions.class);

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
                resources.messages().serverGroupTimeout(serverGroup.getName()),
                resources.messages().reloadServerGroupError(serverGroup.getName()));
    }

    public void restart(ServerGroup serverGroup) {
        reloadRestart(serverGroup,
                new Operation.Builder(serverGroup.getAddress(), RESTART_SERVERS).param(BLOCKING, false).build(),
                Action.RESTART,
                resources.messages().restart(serverGroup.getName()),
                resources.messages().restartServerGroupQuestion(serverGroup.getName()),
                resources.messages().restartServerGroupSuccess(serverGroup.getName()),
                resources.messages().serverGroupTimeout(serverGroup.getName()),
                resources.messages().restartServerGroupError(serverGroup.getName()));
    }

    private void reloadRestart(ServerGroup serverGroup, Operation operation, Action action,
            String title, SafeHtml question, SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {

        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        if (!startedServers.isEmpty()) {
            DialogFactory.showConfirmation(title, question, () -> {
                prepare(serverGroup, startedServers, action);
                dispatcher.execute(operation)
                        .then(__ -> repeatCompositeUntil(dispatcher, readServerConfigStatus(startedServers),
                                checkServerConfigStatus(startedServers.size(), STARTED),
                                serverGroupTimeout(serverGroup, action)))
                        .then(status -> finish(serverGroup, startedServers, status,
                                successMessage, timeoutMessage, errorMessage))
                        .catch_(error -> finish(serverGroup, startedServers, FAILURE, Message.error(
                                errorMessage, String.valueOf(error))));
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
                public void onMetadata(Metadata metadata) {
                    String id = Ids.build(SUSPEND_SERVERS, serverGroup.getName(), Ids.FORM);
                    Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, SUSPEND_SERVERS).build();

                    Dialog dialog = DialogFactory.buildConfirmation(
                            resources.messages().suspend(serverGroup.getName()),
                            resources.messages().suspendServerGroupQuestion(serverGroup.getName()),
                            form.element(),
                            () -> {
                                form.save();
                                int timeout = getOrDefault(form.getModel(), SUSPEND_TIMEOUT,
                                        () -> form.getModel().get(SUSPEND_TIMEOUT).asInt(), 0);
                                int uiTimeout = timeout + serverGroupTimeout(serverGroup, Action.SUSPEND);

                                prepare(serverGroup, startedServers, Action.SUSPEND);
                                Operation operation = new Operation.Builder(serverGroup.getAddress(), SUSPEND_SERVERS)
                                        .param(SUSPEND_TIMEOUT, timeout)
                                        .build();
                                dispatcher.execute(operation)
                                        .then(__ -> repeatCompositeUntil(dispatcher, readSuspendState(startedServers),
                                                checkSuspendState(startedServers.size(), SUSPENDED), uiTimeout))
                                        .then(status -> finish(serverGroup, startedServers, status,
                                                resources.messages().suspendServerGroupSuccess(serverGroup.getName()),
                                                resources.messages().serverGroupTimeout(serverGroup.getName()),
                                                resources.messages().suspendServerGroupError(serverGroup.getName())))
                                        .catch_(error -> finish(serverGroup, startedServers, FAILURE, Message.error(
                                                resources.messages().suspendServerGroupError(serverGroup.getName()),
                                                String.valueOf(error))));
                            });
                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(SUSPEND_TIMEOUT).set(0);
                    form.edit(model);
                }

                @Override
                public void onError(Throwable error) {
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
            dispatcher.execute(operation)
                    .then(__ -> repeatCompositeUntil(dispatcher, readSuspendState(suspendedServers),
                            checkSuspendState(suspendedServers.size(), RUNNING),
                            serverGroupTimeout(serverGroup, RESUME)))
                    .then(status -> finish(serverGroup, suspendedServers, status,
                            resources.messages().resumeServerGroupSuccess(serverGroup.getName()),
                            resources.messages().serverGroupTimeout(serverGroup.getName()),
                            resources.messages().resumeServerGroupError(serverGroup.getName())))
                    .catch_(error -> finish(serverGroup, suspendedServers, FAILURE, Message.error(
                            resources.messages().resumeServerGroupError(serverGroup.getName()),
                            String.valueOf(error))));

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
                public void onMetadata(Metadata metadata) {
                    String id = Ids.build(STOP_SERVERS, serverGroup.getName(), Ids.FORM);
                    Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, STOP_SERVERS)
                            .include(SUSPEND_TIMEOUT).build();

                    Dialog dialog = DialogFactory.buildConfirmation(
                            resources.messages().stop(serverGroup.getName()),
                            resources.messages().stopServerGroupQuestion(serverGroup.getName()),
                            form.element(),
                            () -> {
                                form.save();
                                int timeout = getOrDefault(form.getModel(), SUSPEND_TIMEOUT,
                                        () -> form.getModel().get(SUSPEND_TIMEOUT).asInt(), 0);
                                int uiTimeout = timeout + serverGroupTimeout(serverGroup, Action.STOP);

                                prepare(serverGroup, startedServers, Action.STOP);
                                Operation operation = new Operation.Builder(serverGroup.getAddress(), STOP_SERVERS)
                                        .param(SUSPEND_TIMEOUT, timeout)
                                        .param(BLOCKING, false)
                                        .build();
                                dispatcher.execute(operation)
                                        .then(__ -> repeatCompositeUntil(dispatcher,
                                                readServerConfigStatus(startedServers),
                                                checkServerConfigStatus(startedServers.size(), STOPPED, DISABLED),
                                                uiTimeout))
                                        .then(status -> finish(serverGroup, startedServers, status,
                                                resources.messages().stopServerGroupSuccess(serverGroup.getName()),
                                                resources.messages().serverGroupTimeout(serverGroup.getName()),
                                                resources.messages().stopServerGroupError(serverGroup.getName())))
                                        .catch_(error -> finish(serverGroup, startedServers, FAILURE, Message.error(
                                                resources.messages().stopServerGroupError(serverGroup.getName()),
                                                String.valueOf(error))));
                            });
                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(SUSPEND_TIMEOUT).set(0);
                    form.edit(model);
                }

                @Override
                public void onError(Throwable error) {
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
            dispatcher.execute(operation)
                    .then(__ -> repeatCompositeUntil(dispatcher, readServerConfigStatus(downServers),
                            checkServerConfigStatus(downServers.size(), STARTED),
                            serverGroupTimeout(serverGroup, Action.START)))
                    .then(status -> finish(serverGroup, downServers, status,
                            resources.messages().startServerGroupSuccess(serverGroup.getName()),
                            resources.messages().serverGroupTimeout(serverGroup.getName()),
                            resources.messages().startServerGroupError(serverGroup.getName())))
                    .catch_(error -> finish(serverGroup, downServers, FAILURE, Message.error(
                            resources.messages().startServerGroupError(serverGroup.getName()),
                            String.valueOf(error))));

        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStoppedServers(serverGroup.getName())));
        }
    }

    public void startInSuspendedMode(ServerGroup serverGroup) {
        List<Server> downServers = serverGroup.getServers(server -> server.isStopped() || server.isFailed());
        if (!downServers.isEmpty()) {
            prepare(serverGroup, downServers, Action.START);
            Operation operation = new Operation.Builder(serverGroup.getAddress(), START_SERVERS)
                    .param(START_MODE, SUSPEND)
                    .param(BLOCKING, false)
                    .build();
            dispatcher.execute(operation)
                    .then(__ -> repeatCompositeUntil(dispatcher, readServerConfigStatus(downServers),
                            checkServerConfigStatus(downServers.size(), STARTED),
                            serverGroupTimeout(serverGroup, Action.START)))
                    .then(status -> finish(serverGroup, downServers, status,
                            resources.messages().startServerGroupSuccess(serverGroup.getName()),
                            resources.messages().serverGroupTimeout(serverGroup.getName()),
                            resources.messages().startServerGroupError(serverGroup.getName())))
                    .catch_(error -> finish(serverGroup, downServers, FAILURE, Message.error(
                            resources.messages().startServerGroupError(serverGroup.getName()),
                            String.valueOf(error))));
        } else {
            MessageEvent.fire(eventBus,
                    Message.warning(resources.messages().serverGroupNoStoppedServers(serverGroup.getName())));
        }
    }

    public void destroy(ServerGroup serverGroup) {
        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        DialogFactory.showConfirmation(resources.messages().destroy(serverGroup.getName()),
                resources.messages().destroyServerGroupQuestion(serverGroup.getName()),
                () -> {
                    prepare(serverGroup, startedServers, Action.DESTROY);
                    Operation operation = new Operation.Builder(serverGroup.getAddress(), DESTROY_SERVERS).build();
                    dispatcher.execute(operation)
                            .then(__ -> repeatCompositeUntil(dispatcher, readServerConfigStatus(startedServers),
                                    checkServerConfigStatus(startedServers.size(), STOPPED, DISABLED),
                                    serverGroupTimeout(serverGroup, Action.DESTROY)))
                            .then(status -> finish(serverGroup, startedServers, status,
                                    resources.messages().destroyServerGroupSuccess(serverGroup.getName()),
                                    resources.messages().serverGroupTimeout(serverGroup.getName()),
                                    resources.messages().destroyServerGroupError(serverGroup.getName())))
                            .catch_(error -> finish(serverGroup, startedServers, FAILURE, Message.error(
                                    resources.messages().destroyServerError(serverGroup.getName()),
                                    String.valueOf(error))));
                });
    }

    public void kill(ServerGroup serverGroup) {
        List<Server> startedServers = serverGroup.getServers(Server::isStarted);
        DialogFactory.showConfirmation(resources.messages().kill(serverGroup.getName()),
                resources.messages().killServerGroupQuestion(serverGroup.getName()),
                () -> {
                    prepare(serverGroup, startedServers, Action.KILL);
                    Operation operation = new Operation.Builder(serverGroup.getAddress(), KILL_SERVERS).build();
                    dispatcher.execute(operation)
                            .then(__ -> repeatCompositeUntil(dispatcher, readServerConfigStatus(startedServers),
                                    checkServerConfigStatus(startedServers.size(), STOPPED, DISABLED),
                                    serverGroupTimeout(serverGroup, Action.KILL)))
                            .then(status -> finish(serverGroup, startedServers, status,
                                    resources.messages().killServerGroupSuccess(serverGroup.getName()),
                                    resources.messages().serverGroupTimeout(serverGroup.getName()),
                                    resources.messages().killServerGroupError(serverGroup.getName())))
                            .catch_(error -> finish(serverGroup, startedServers, FAILURE, Message.error(
                                    resources.messages().killServerError(serverGroup.getName()),
                                    String.valueOf(error))));
                });
    }

    public void remove(ServerGroup serverGroup) {
        List<Server> stoppedServers = serverGroup.getServers(Server::isStopped);

        DialogFactory.showConfirmation(
                resources.messages().removeConfirmationTitle(Names.SERVER_GROUP),
                resources.messages().removeConfirmationQuestion(serverGroup.getName()),
                () -> {
                    prepare(serverGroup, stoppedServers, Action.REMOVE);
                    Composite comp = new Composite();
                    for (Server server : stoppedServers) {
                        comp.add(new Operation.Builder(server.getServerConfigAddress(), REMOVE).build());
                    }
                    Operation operation = new Operation.Builder(serverGroup.getAddress(), REMOVE).build();
                    comp.add(operation);

                    dispatcher.execute(comp)
                            .then(__ -> finish(serverGroup, stoppedServers, SUCCESS, Message.success(
                                    resources.messages().removeResourceSuccess(Names.SERVER_GROUP, serverGroup.getName()))))
                            .catch_(error -> finish(serverGroup, stoppedServers, FAILURE, Message.error(
                                    resources.messages().removeError(serverGroup.getName(), String.valueOf(error)))));
                });
    }

    public void copy(ServerGroup serverGroup, FormItemValidation<String> nameItemValidator) {

        NameItem newNameItem = new NameItem();
        newNameItem.setValue(serverGroup.getName() + "_" + COPY);
        newNameItem.addValidationHandler(nameItemValidator);

        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(COPY, serverGroup.getName(), Ids.FORM),
                Metadata.empty())
                        .fromRequestProperties()
                        .unboundFormItem(newNameItem, 0)
                        .requiredOnly()
                        .build();

        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.SERVER_GROUP),
                form, (resource, payload) -> {
                    // read server-config recursively to retrieve nested resources
                    Operation opReadServerGroup = new Operation.Builder(serverGroup.getAddress(), READ_RESOURCE_OPERATION)
                            .param(RECURSIVE, true)
                            .build();

                    dispatcher.execute(opReadServerGroup, serverGroupModel -> {
                        ServerGroup newServerGroup = new ServerGroup(newNameItem.getValue(), serverGroupModel);

                        Operation opAddServer = new Operation.Builder(newServerGroup.getAddress(), ADD)
                                .payload(serverGroupModel)
                                .build();

                        Composite comp = new Composite();
                        comp.add(opAddServer);
                        addChildOperations(comp, newServerGroup, newServerGroup.getAddress(), 2);

                        dispatcher.execute(comp)
                                .then(__ -> finish(serverGroup, Collections.emptyList(), SUCCESS, Message.success(
                                        resources.messages().addResourceSuccess(Names.SERVER_GROUP, newNameItem.getValue()))))
                                .catch_(error -> finish(serverGroup, Collections.emptyList(), FAILURE, Message.error(
                                        resources.messages().addResourceError(newNameItem.getValue(), String.valueOf(error)))));
                    });
                });
        dialog.show();

        ModelNode model = new ModelNode();
        form.edit(model);
    }

    private static void addChildOperations(Composite composite, ModelNode rootModel,
            ModelNode baseAddress, int depth) {
        if (depth <= 0) {
            return;
        }

        for (Property property : rootModel.asPropertyList()) {
            if (ModelType.OBJECT.equals(property.getValue().getType())) {
                for (Property resource : property.getValue().asPropertyList()) {
                    if (ModelType.OBJECT.equals(resource.getValue().getType())) {
                        ModelNode resourceAddress = baseAddress.clone().add(property.getName(), resource.getName());

                        Operation operation = new Operation.Builder(new ResourceAddress(resourceAddress), ADD)
                                .payload(resource.getValue())
                                .build();

                        composite.add(operation);
                        addChildOperations(composite, resource.getValue(), resourceAddress, depth - 1);
                    }
                }
            }
        }
    }

    private void prepare(ServerGroup serverGroup, List<Server> servers, Action action) {
        markAsPending(serverGroup); // mark as pending *before* firing the event!
        servers.forEach(serverActions::markAsPending);
        eventBus.fireEvent(new ServerGroupActionEvent(serverGroup, servers, action));
    }

    private Promise<Void> finish(ServerGroup serverGroup, List<Server> servers, FlowStatus status,
            SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {
        switch (status) {
            case SUCCESS:
                return finish(serverGroup, servers, SUCCESS, Message.success(successMessage));
            case TIMEOUT:
                return finish(serverGroup, servers, FlowStatus.TIMEOUT, Message.error(timeoutMessage));
            case FAILURE:
                return finish(serverGroup, servers, FAILURE, Message.error(errorMessage));
            default:
                throw new IllegalStateException("Invalid flow status" + status);
        }
    }

    private Promise<Void> finish(ServerGroup serverGroup, List<Server> servers, FlowStatus status, Message message) {
        clearPending(serverGroup); // clear pending state *before* firing the event!
        servers.forEach(serverActions::clearPending);
        eventBus.fireEvent(new ServerGroupResultEvent(serverGroup, servers, status));
        MessageEvent.fire(eventBus, message);
        return Promise.resolve((Void) null);
    }

    private void markAsPending(ServerGroup serverGroup) {
        Core.setPendingLifecycleAction(true);
        pendingServerGroups.put(serverGroup.getName(), serverGroup);
        logger.debug("Mark server group {} as pending", serverGroup.getName());
    }

    private void clearPending(ServerGroup serverGroup) {
        Core.setPendingLifecycleAction(false);
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
