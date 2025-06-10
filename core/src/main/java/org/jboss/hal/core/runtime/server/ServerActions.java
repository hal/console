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
package org.jboss.hal.core.runtime.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.core.runtime.SuspendState;
import org.jboss.hal.core.runtime.Timeouts;
import org.jboss.hal.core.runtime.server.ServerUrlTasks.ReadSocketBinding;
import org.jboss.hal.core.runtime.server.ServerUrlTasks.ReadSocketBindingGroup;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.FlowStatus;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.runtime.RunningState.RUNNING;
import static org.jboss.hal.core.runtime.TimeoutHandler.repeatOperationUntil;
import static org.jboss.hal.core.runtime.TimeoutHandler.repeatUntilTimeout;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.DISABLED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STOPPED;
import static org.jboss.hal.core.runtime.server.ServerUrlTasks.SERVER_URL_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTO_START;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BLOCKING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESTROY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JVM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KILL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_BOOT_ERRORS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESUME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCHEME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_DEFAULT_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_PORT_OFFSET;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STOP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_TIMEOUT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SYSTEM_PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATE_AUTO_START_WITH_SERVER_STATUS;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.ModelNodeHelper.getOrDefault;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.flow.FlowStatus.FAILURE;
import static org.jboss.hal.flow.FlowStatus.SUCCESS;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

public class ServerActions implements Timeouts {

    private static final ServerUrlResources RESOURCES = GWT.create(ServerUrlResources.class);
    private static final Logger logger = LoggerFactory.getLogger(ServerActions.class);

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
    private final ServerUrlStorage serverUrlStorage;
    private final StatementContext statementContext;

    @Inject
    public ServerActions(EventBus eventBus,
            Dispatcher dispatcher,
            ServerUrlStorage serverUrlStorage,
            StatementContext statementContext,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.serverUrlStorage = serverUrlStorage;
        this.statementContext = statementContext;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.resources = resources;
        this.pendingServers = new HashMap<>();
    }

    // ------------------------------------------------------ server operations

    public void copyServer(Server server, Callback callback) {
        List<String> hosts = new ArrayList<>();
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, HOST)
                .build();
        dispatcher.execute(operation)
                .then(result -> {
                    result.asList().forEach(m -> hosts.add(m.asString()));
                    // get the first host only to retrieve the r-r-d for server-config
                    // as /host=*/server-config=*:read-operation-description(name=add) does not work
                    AddressTemplate template = AddressTemplate.of("/host=" + hosts.get(0) + "/server-config=*");
                    return metadataProcessor.lookup(template, progress.get());
                })
                .then(metadata -> {
                    String id = Ids.build(SERVER_GROUP, statementContext.selectedServerGroup(), SERVER,
                            FORM);
                    SingleSelectBoxItem hostFormItem = new SingleSelectBoxItem(HOST, Names.HOST, hosts,
                            false);
                    hostFormItem.setRequired(true);
                    NameItem nameItem = new NameItem();

                    ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                            .fromRequestProperties()
                            .unboundFormItem(nameItem, 0)
                            .unboundFormItem(hostFormItem, 1, resources.messages().addServerHostHelp())
                            .exclude(AUTO_START, SOCKET_BINDING_DEFAULT_INTERFACE,
                                    SOCKET_BINDING_GROUP, UPDATE_AUTO_START_WITH_SERVER_STATUS)
                            .build();

                    String title = resources.messages().copyServerTitle();
                    AddResourceDialog dialog = new AddResourceDialog(title, form, (resource, payload) -> {
                        // read server-config recursively to retrieve nested resources
                        ModelNode serverConfigModel = new ModelNode();
                        serverConfigModel.get(HOST).set(server.getHost());
                        serverConfigModel.get(SERVER_CONFIG).set(server.getName());

                        ResourceAddress serverAddress = new ResourceAddress(serverConfigModel);
                        Operation opReadServer = new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION)
                                .param(RECURSIVE, true)
                                .build();

                        dispatcher.execute(opReadServer, new Consumer<ModelNode>() {
                            @Override
                            public void accept(ModelNode newServerModel) {
                                String newServerName = nameItem.getValue();
                                // set the chosen group in the model
                                newServerModel.get(GROUP).set(payload.get(GROUP).asString());
                                if (payload.hasDefined(SOCKET_BINDING_PORT_OFFSET)) {
                                    newServerModel.get(SOCKET_BINDING_PORT_OFFSET)
                                            .set(payload.get(SOCKET_BINDING_PORT_OFFSET).asLong());
                                }
                                newServerModel.get(NAME).set(newServerName);

                                ModelNode newServerModelAddress = new ModelNode();
                                newServerModelAddress.get(HOST).set(hostFormItem.getValue());
                                newServerModelAddress.get(SERVER_CONFIG).set(newServerName);

                                Operation opAddServer = new Operation.Builder(
                                        new ResourceAddress(newServerModelAddress), ADD)
                                        .payload(newServerModel)
                                        .build();
                                Composite comp = new Composite();
                                comp.add(opAddServer);

                                // create operation for each nested resource of the source server
                                createOperation(comp, JVM, newServerModel, newServerModelAddress);
                                createOperation(comp, INTERFACE, newServerModel, newServerModelAddress);
                                createOperation(comp, PATH, newServerModel, newServerModelAddress);
                                createOperation(comp, SYSTEM_PROPERTY, newServerModel, newServerModelAddress);
                                createOperation(comp, SSL, newServerModel, newServerModelAddress);

                                dispatcher.execute(comp, (CompositeResult __) -> {
                                    MessageEvent.fire(eventBus, Message.success(
                                            resources.messages().addResourceSuccess(Names.SERVER, newServerName)));
                                    callback.execute();
                                }, (__, failure) -> {
                                    MessageEvent.fire(eventBus, Message.error(
                                            resources.messages().addResourceError(newServerName, failure)));
                                    callback.execute();
                                });
                            }

                            private void createOperation(Composite composite, String resource, ModelNode model,
                                    ModelNode baseAddress) {
                                if (model.hasDefined(resource)) {
                                    List<Property> props = model.get(resource).asPropertyList();
                                    props.forEach(p -> {
                                        String propname = p.getName();
                                        ModelNode _address = baseAddress.clone();
                                        _address.get(resource).set(propname);
                                        Operation operation = new Operation.Builder(
                                                new ResourceAddress(_address), ADD)
                                                .payload(p.getValue())
                                                .build();
                                        composite.add(operation);
                                    });
                                }
                            }
                        });
                    });
                    dialog.show();
                    return null;
                });
    }

    // ------------------------------------------------------ lifecycle operations

    private void restartStandalone(Server server) {
        restartStandalone(server, resources.messages().restartStandaloneQuestion(server.getName()));
    }

    public void restartStandalone(Server server, SafeHtml question) {
        String title = resources.messages().restart(server.getName());
        DialogFactory.showConfirmation(title, question, () -> {
            // execute the restart with a little delay ensuring the confirmation dialog is closed
            // before the next dialog is opened (only one modal can be open at a time!)
            prepare(server, Action.RESTART);
            setTimeout(__ -> {
                BlockingDialog pendingDialog = DialogFactory.buildLongRunning(
                        title, resources.messages().restartStandalonePending(server.getName()));
                pendingDialog.show();
                Operation operation = new Operation.Builder(ResourceAddress.root(), SHUTDOWN)
                        .param(RESTART, true)
                        .build();
                Operation ping = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION).build();
                dispatcher.execute(operation)
                        .then(___ -> repeatUntilTimeout(dispatcher, ping, SERVER_RESTART_TIMEOUT))
                        .then(status -> {
                            pendingDialog.close();
                            switch (status) {
                                case SUCCESS:
                                    return finish(Server.STANDALONE, SUCCESS, Message.success(
                                            resources.messages().restartServerSuccess(server.getName())));
                                case FAILURE:
                                    return finish(Server.STANDALONE, FAILURE, Message.error(
                                            resources.messages().restartServerError(server.getName())));
                                case TIMEOUT:
                                    DialogFactory.buildBlocking(title,
                                            resources.messages().restartStandaloneTimeout(server.getName())).show();
                                    return finish(Server.STANDALONE, FlowStatus.TIMEOUT, null);
                                default:
                                    throw new IllegalStateException("Invalid flow status: " + status);
                            }
                        })
                        .catch_(error -> {
                            pendingDialog.close();
                            return finish(Server.STANDALONE, FAILURE, Message.error(
                                    resources.messages().restartServerError(server.getName()), String.valueOf(error)));
                        });
            }, SHORT_TIMEOUT);
        });
    }

    public void reload(Server server) {
        Operation operation = new Operation.Builder(server.getServerConfigAddress(), RELOAD)
                .param(BLOCKING, false)
                .build();
        reloadRestart(server, operation, Action.RELOAD, SERVER_RELOAD_TIMEOUT,
                resources.messages().reload(server.getName()),
                resources.messages().reloadServerQuestion(server.getName()),
                resources.messages().reloadServerSuccess(server.getName()),
                resources.messages().serverTimeout(server.getName()),
                resources.messages().serverBootErrors(server.getName()));
    }

    public void restart(Server server) {
        if (server.isStandalone()) {
            restartStandalone(server);
        } else {
            Operation operation = new Operation.Builder(server.getServerConfigAddress(), RESTART)
                    .param(BLOCKING, false)
                    .build();
            reloadRestart(server, operation, Action.RESTART, SERVER_RESTART_TIMEOUT,
                    resources.messages().restart(server.getName()),
                    resources.messages().restartServerQuestion(server.getName()),
                    resources.messages().restartServerSuccess(server.getName()),
                    resources.messages().restartStandaloneTimeout(server.getName()),
                    resources.messages().restartServerError(server.getName()));
        }
    }

    private void reloadRestart(Server server, Operation operation, Action action, int timeout,
            String title, SafeHtml question, SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {
        DialogFactory.showConfirmation(title, question, () -> {
            prepare(server, action);
            dispatcher.execute(operation)
                    .then(__ -> repeatOperationUntil(dispatcher,
                            server.isStandalone() ? readServerState(server) : readServerConfigStatus(server),
                            server.isStandalone() ? checkRunningState() : checkServerConfigStatus(STARTED),
                            timeout))
                    .then(status -> finish(server, action, status, successMessage, timeoutMessage, errorMessage))
                    .catch_(error -> finish(server, FAILURE, Message.error(errorMessage, String.valueOf(error))));
        });
    }

    public void suspend(Server server) {
        if (!ManagementModel.supportsSuspend(server.getManagementVersion())) {
            logger.error("Server {} using version {} does not support suspend operation", server.getName(),
                    server.getManagementVersion());
            return;
        }

        metadataProcessor.lookup(serverConfigTemplate(server), progress.get()).then(metadata -> {
            String id = Ids.build(SUSPEND, server.getName(), FORM);
            Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, SUSPEND).build();

            Dialog dialog = DialogFactory.buildConfirmation(
                    resources.messages().suspend(server.getName()),
                    resources.messages().suspendServerQuestion(server.getName()),
                    form.element(),
                    Dialog.Size.MEDIUM,
                    () -> {
                        form.save();
                        int timeout = getOrDefault(form.getModel(), SUSPEND_TIMEOUT,
                                () -> form.getModel().get(SUSPEND_TIMEOUT).asInt(), 0);
                        int uiTimeout = timeout + SERVER_SUSPEND_TIMEOUT;

                        prepare(server, Action.SUSPEND);
                        ResourceAddress address = server.getServerConfigAddress();
                        Operation operation = new Operation.Builder(address, SUSPEND)
                                .param(SUSPEND_TIMEOUT, timeout)
                                .build();
                        dispatcher.execute(operation)
                                .then(__ -> repeatOperationUntil(dispatcher,
                                        readSuspendState(server), checkSuspendState(), uiTimeout))
                                .then(status -> finish(server, Action.SUSPEND, status,
                                        resources.messages().suspendServerSuccess(server.getName()),
                                        resources.messages().serverTimeout(server.getName()),
                                        resources.messages().suspendServerError(server.getName())))
                                .catch_(error -> finish(server, FAILURE, Message.error(
                                        resources.messages().suspendServerError(server.getName()),
                                        String.valueOf(error))));
                    });

            dialog.registerAttachable(form);
            dialog.show();

            ModelNode model = new ModelNode();
            model.get(SUSPEND_TIMEOUT).set(0);
            form.edit(model);
            return null;
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
        dispatcher.execute(operation)
                .then(__ -> repeatOperationUntil(dispatcher,
                        server.isStandalone() ? readServerState(server) : readServerConfigStatus(server),
                        server.isStandalone() ? checkRunningState() : checkServerConfigStatus(STARTED),
                        SERVER_START_TIMEOUT))
                .then(status -> finish(server, Action.RESUME, status,
                        resources.messages().resumeServerSuccess(server.getName()),
                        resources.messages().serverTimeout(server.getName()),
                        resources.messages().resumeServerError(server.getName())))
                .catch_(error -> finish(server, FAILURE, Message.error(
                        resources.messages().resumeServerError(server.getName()),
                        String.valueOf(error))));
    }

    public void stop(Server server) {
        metadataProcessor.lookup(serverConfigTemplate(server), progress.get())
                .then(metadata -> {
                    String id = Ids.build(STOP, server.getName(), FORM);
                    Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, STOP)
                            .include(SUSPEND_TIMEOUT)
                            .build();

                    Dialog dialog = DialogFactory.buildConfirmation(
                            resources.messages().stop(server.getName()),
                            resources.messages().stopServerQuestion(server.getName()),
                            form.element(),
                            Dialog.Size.MEDIUM,
                            () -> {
                                form.save();
                                int timeout = getOrDefault(form.getModel(), SUSPEND_TIMEOUT,
                                        () -> form.getModel().get(SUSPEND_TIMEOUT).asInt(), 0);
                                int uiTimeout = timeout + SERVER_STOP_TIMEOUT;

                                prepare(server, Action.STOP);
                                Operation operation = new Operation.Builder(server.getServerConfigAddress(), STOP)
                                        .param(SUSPEND_TIMEOUT, timeout)
                                        .param(BLOCKING, false)
                                        .build();
                                dispatcher.execute(operation)
                                        .then(__ -> repeatOperationUntil(dispatcher,
                                                readServerConfigStatus(server),
                                                checkServerConfigStatus(STOPPED, DISABLED),
                                                uiTimeout))
                                        .then(status -> finish(server, Action.STOP, status,
                                                resources.messages().stopServerSuccess(server.getName()),
                                                resources.messages().serverTimeout(server.getName()),
                                                resources.messages().stopServerError(server.getName())))
                                        .catch_(error -> finish(server, FAILURE, Message.error(
                                                resources.messages().stopServerError(server.getName()),
                                                String.valueOf(error))));
                            });

                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(SUSPEND_TIMEOUT).set(0);
                    form.edit(model);
                    return null;
                });
    }

    /**
     * Call <code>/host={host}/server-config={sever}:stop(blocking=false)</code> the intended action is to immediately stop the
     * server.
     */
    public void stopNow(Server server) {
        prepare(server, Action.STOP);
        Operation operation = new Operation.Builder(server.getServerConfigAddress(), STOP)
                .param(BLOCKING, false)
                .build();
        dispatcher.execute(operation)
                .then(__ -> repeatOperationUntil(dispatcher, readServerConfigStatus(server),
                        checkServerConfigStatus(STOPPED, DISABLED), SERVER_STOP_TIMEOUT))
                .then(status -> finish(server, Action.STOP, status,
                        resources.messages().stopServerSuccess(server.getName()),
                        resources.messages().serverTimeout(server.getName()),
                        resources.messages().stopServerError(server.getName())))
                .catch_(error -> finish(server, FAILURE, Message.error(
                        resources.messages().stopServerError(server.getName()),
                        String.valueOf(error))));
    }

    public void destroy(Server server) {
        SafeHtml question = resources.messages().destroyServerQuestion(server.getName());
        DialogFactory.showConfirmation(resources.messages().destroy(server.getName()), question, () -> {
            prepare(server, Action.DESTROY);
            Operation operation = new Operation.Builder(server.getServerConfigAddress(), DESTROY).build();
            dispatcher.execute(operation)
                    .then(__ -> repeatOperationUntil(dispatcher, readServerConfigStatus(server),
                            checkServerConfigStatus(STOPPED, DISABLED), SERVER_DESTROY_TIMEOUT))
                    .then(status -> finish(server, Action.DESTROY, status,
                            resources.messages().destroyServerSuccess(server.getName()),
                            resources.messages().serverTimeout(server.getName()),
                            resources.messages().destroyServerError(server.getName())))
                    .catch_(error -> finish(server, FAILURE, Message.error(
                            resources.messages().destroyServerError(server.getName()), String.valueOf(error))));
        });
    }

    public void kill(Server server) {
        SafeHtml question = resources.messages().killServerQuestion(server.getName());
        DialogFactory.showConfirmation(resources.messages().kill(server.getName()), question, () -> {
            prepare(server, Action.KILL);
            Operation operation = new Operation.Builder(server.getServerConfigAddress(), KILL).build();
            dispatcher.execute(operation)
                    .then(__ -> repeatOperationUntil(dispatcher, readServerConfigStatus(server),
                            checkServerConfigStatus(STOPPED, DISABLED), SERVER_KILL_TIMEOUT))
                    .then(status -> finish(server, Action.KILL, status,
                            resources.messages().killServerSuccess(server.getName()),
                            resources.messages().serverTimeout(server.getName()),
                            resources.messages().killServerError(server.getName())))
                    .catch_(error -> finish(server, FAILURE, Message.error(
                            resources.messages().killServerError(server.getName()), String.valueOf(error))));
        });
    }

    public void start(Server server) {
        prepare(server, Action.START);
        Operation operation = new Operation.Builder(server.getServerConfigAddress(), START)
                .param(BLOCKING, false)
                .build();
        dispatcher.execute(operation)
                .then(__ -> repeatOperationUntil(dispatcher, readServerConfigStatus(server),
                        checkServerConfigStatus(STARTED), SERVER_START_TIMEOUT))
                .then(status -> finish(server, Action.START, status,
                        resources.messages().startServerSuccess(server.getName()),
                        resources.messages().serverTimeout(server.getName()),
                        resources.messages().startServerError(server.getName())))
                .catch_(error -> finish(server, FAILURE, Message.error(
                        resources.messages().startServerError(server.getName()), String.valueOf(error))));
    }

    public void startInSuspendedMode(Server server) {
        prepare(server, Action.START);
        Operation operation = new Operation.Builder(server.getServerConfigAddress(), START)
                .param(START_MODE, SUSPEND)
                .param(BLOCKING, false)
                .build();
        dispatcher.execute(operation)
                .then(__ -> repeatOperationUntil(dispatcher, readServerConfigStatus(server),
                        checkServerConfigStatus(STARTED), SERVER_START_TIMEOUT))
                .then(status -> finish(server, Action.START, status,
                        resources.messages().startServerSuccess(server.getName()),
                        resources.messages().serverTimeout(server.getName()),
                        resources.messages().startServerError(server.getName())))
                .catch_(error -> finish(server, FAILURE, Message.error(
                        resources.messages().startServerError(server.getName()), String.valueOf(error))));
    }

    // ------------------------------------------------------ server url methods

    /** Reads the URL and updates the specified HTML element */
    public void readUrl(Server server, HTMLElement element) {
        readUrl(server, new AsyncCallback<ServerUrl>() {
            @Override
            public void onFailure(Throwable caught) {
                Elements.removeChildrenFrom(element);
                element.textContent = Names.NOT_AVAILABLE;
            }

            @Override
            public void onSuccess(ServerUrl url) {
                Elements.removeChildrenFrom(element);
                element.appendChild(a(url.getUrl())
                        .apply(a -> a.target = server.getId())
                        .innerHtml(SafeHtmlUtils.fromString(url.getUrl())).element());
                String icon;
                String tooltip;
                if (url.isCustom()) {
                    icon = fontAwesome("external-link");
                    tooltip = resources.constants().serverUrlCustom();
                } else {
                    icon = pfIcon("server");
                    tooltip = resources.constants().serverUrlManagementModel();
                }
                element.appendChild(span().css(icon, marginLeft5).style("cursor:help").title(tooltip).element()); // NON-NLS
            }
        });
    }

    /** Reads the URL using the information from the specified server instance */
    private void readUrl(Server server, AsyncCallback<ServerUrl> callback) {
        readUrl(server.isStandalone(), server.getHost(), server.getServerGroup(), server.getName(), callback);
    }

    /** Reads the URL using the provided parameters */
    public void readUrl(boolean standalone, String host, String serverGroup, String server,
            AsyncCallback<ServerUrl> callback) {
        if (serverUrlStorage.hasUrl(host, server)) {
            ServerUrl serverUrl = serverUrlStorage.load(host, server);
            callback.onSuccess(serverUrl);
        } else {
            List<Task<FlowContext>> tasks = Arrays.asList(
                    new ReadSocketBindingGroup(standalone, serverGroup, dispatcher),
                    new ReadSocketBinding(standalone, host, server, dispatcher));
            sequential(new FlowContext(), tasks)
                    .then(context -> {
                        callback.onSuccess(context.get(SERVER_URL_KEY));
                        return null;
                    })
                    .catch_(error -> {
                        String message = String.valueOf(error);
                        logger.error(message);
                        callback.onFailure(new RuntimeException(message));
                        return null;
                    });
        }
    }

    public void editUrl(Server server, Callback callback) {
        Metadata metadata = Metadata.staticDescription(RESOURCES.serverUrl());
        Alert readUrlError = new Alert(Icons.ERROR, resources.messages().serverUrlError());
        HTMLElement info = p().element();
        ButtonItem reset = new ButtonItem(Ids.build(Ids.SERVER_URL_FORM, "reset"), resources.constants().reset());
        Form<ServerUrl> form = new ModelNodeForm.Builder<ServerUrl>(Ids.SERVER_URL_FORM, metadata)
                .include(SCHEME, HOST, PORT)
                .unboundFormItem(reset)
                .unsorted()
                .onSave((f, changedValues) -> {
                    ServerUrl serverUrl = f.getModel();
                    if (!changedValues.isEmpty()) {
                        serverUrl.makeCustom();
                    }
                    serverUrlStorage.save(server.getHost(), server.getName(), serverUrl);
                    callback.execute();
                })
                .build();
        Dialog dialog = new Dialog.Builder(resources.constants().editURL())
                .add(readUrlError.element())
                .add(info)
                .add(form.element())
                .primary(form::save)
                .cancel()
                .closeIcon(true)
                .closeOnEsc(true)
                .build();
        dialog.registerAttachable(form);
        Elements.setVisible(readUrlError.element(), false);
        Elements.setVisible(info, false);
        reset.onClick((event) -> {
            serverUrlStorage.remove(server.getHost(), server.getName());
            dialog.close();
            callback.execute();
        });

        readUrl(server, new AsyncCallback<ServerUrl>() {
            @Override
            public void onFailure(Throwable caught) {
                Elements.setVisible(readUrlError.element(), true);
                show(null);
            }

            @Override
            public void onSuccess(ServerUrl serverUrl) {
                if (serverUrl.isCustom()) {
                    info.innerHTML = resources.messages().serverUrlCustom().asString();
                } else {
                    info.innerHTML = resources.messages().serverUrlManagementModel().asString();
                }
                Elements.setVisible(info, true);
                show(serverUrl);
            }

            private void show(ServerUrl serverUrl) {
                dialog.show();
                form.edit(serverUrl);
            }
        });
    }

    // ------------------------------------------------------ helper methods

    private void prepare(Server server, Action action) {
        markAsPending(server); // mark as pending *before* firing the event!
        eventBus.fireEvent(new ServerActionEvent(server, action));
    }

    private Promise<Void> finish(Server server, Action action, FlowStatus status,
            SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {
        switch (status) {
            case SUCCESS:
                if (Action.isStart(action)) {
                    ResourceAddress address = server.getServerAddress().add(CORE_SERVICE, MANAGEMENT);
                    Operation operation = new Operation.Builder(address, READ_BOOT_ERRORS).build();
                    return dispatcher.execute(operation).then(result -> {
                        if (result.asList().isEmpty()) {
                            return finish(server, SUCCESS, Message.success(successMessage));
                        } else {
                            return finish(server, FAILURE, Message.error(
                                    resources.messages().serverBootErrors(server.getName())));
                        }
                    });
                } else if (Action.isStop(action)) {
                    ResourceAddress address = server.getServerConfigAddress();
                    Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                            .param(ATTRIBUTES_ONLY, true)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    return dispatcher.execute(operation).then(result -> {
                        Server stoppedServer = new Server(server.getHost(), result);
                        return finish(stoppedServer, SUCCESS, Message.success(successMessage));
                    });
                } else {
                    return finish(server, SUCCESS, Message.success(successMessage));
                }
            case FAILURE:
                return finish(server, FAILURE, Message.error(errorMessage));
            case TIMEOUT:
                return finish(server, FlowStatus.TIMEOUT, Message.error(timeoutMessage));
            default:
                throw new IllegalStateException("Invalid flow status: " + status);
        }
    }

    private Promise<Void> finish(Server server, FlowStatus status, Message message) {
        clearPending(server); // clear pending state *before* firing the event!
        eventBus.fireEvent(new ServerResultEvent(server, status));
        MessageEvent.fire(eventBus, message);
        return Promise.resolve((Void) null);
    }

    public void markAsPending(Server server) {
        pendingServers.put(Ids.hostServer(server.getHost(), server.getName()), server);
        logger.debug("Mark server {} as pending", server.getName());
    }

    public void clearPending(Server server) {
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

    private Predicate<ModelNode> checkRunningState() {
        return result -> RUNNING == asEnumValue(result, RunningState::valueOf, RunningState.UNDEFINED);
    }

    private Operation readSuspendState(Server server) {
        return new Operation.Builder(server.getServerAddress(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, SUSPEND_STATE)
                .build();
    }

    private Predicate<ModelNode> checkSuspendState() {
        return result -> SuspendState.SUSPENDED == asEnumValue(result, SuspendState::valueOf, SuspendState.UNDEFINED);
    }

    private Promise<List<ModelNode>> readBootErrors(FlowStatus status, Server server) {
        if (status == SUCCESS) {
            ResourceAddress address = server.getServerAddress().add(CORE_SERVICE, MANAGEMENT);
            Operation operation = new Operation.Builder(address, READ_BOOT_ERRORS).build();
            return dispatcher.execute(operation).then(result -> Promise.resolve(result.asList()));
        } else {
            return Promise.resolve(emptyList());
        }
    }
}
