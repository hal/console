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
package org.jboss.hal.core.runtime.host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.Timeouts;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowStatus;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Collections.emptyList;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.MEDIUM;
import static org.jboss.hal.core.runtime.TimeoutHandler.repeatUntilTimeout;
import static org.jboss.hal.core.runtime.Timeouts.hostTimeout;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_SERVERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.hal.flow.FlowStatus.FAILURE;
import static org.jboss.hal.flow.FlowStatus.SUCCESS;
import static org.jboss.hal.resources.UIConstants.LONG_TIMEOUT;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

public class HostActions implements Timeouts {

    private static final Logger logger = LoggerFactory.getLogger(HostActions.class);

    private static AddressTemplate hostTemplate(Host host) {
        return AddressTemplate.of("/host=" + host.getAddressName());
    }

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final ServerActions serverActions;
    private final Resources resources;
    private final Map<String, Host> pendingHosts;

    @Inject
    public HostActions(EventBus eventBus,
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
        this.pendingHosts = new HashMap<>();
    }

    // ------------------------------------------------------ reload

    public void reload(Host host) {
        metadataProcessor.lookup(hostTemplate(host), progress.get())
                .then(metadata -> {
                    Form<ModelNode> form = new OperationFormBuilder<>(
                            Ids.build(RELOAD_HOST, host.getName(), Ids.FORM), metadata, RELOAD)
                            .include(RESTART_SERVERS)
                            .build();

                    SafeHtml question;
                    if (host.isDomainController()) {
                        question = resources.messages().reloadDomainControllerQuestion(host.getName());
                    } else {
                        question = resources.messages().reloadHostControllerQuestion(host.getName());
                    }
                    String message = resources.messages().reload(host.getName());
                    Dialog dialog = DialogFactory.buildConfirmation(message, question, form.element(), MEDIUM, () -> {
                        form.save();
                        boolean restartServers = form.getModel().get(RESTART_SERVERS).asBoolean();
                        prepare(host, restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                Action.RELOAD);
                        Operation operation = new Operation.Builder(host.getAddress(), RELOAD)
                                .param(RESTART_SERVERS, restartServers)
                                .build();

                        // execute the operation with a little delay ensuring the confirmation dialog is closed
                        // before the next dialog is opened (only one modal can be open at a time!)
                        setTimeout(__ -> {
                            if (host.isDomainController()) {
                                domainControllerOperation(host, operation, hostTimeout(host, Action.RELOAD),
                                        restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                        message,
                                        resources.messages().reloadDomainControllerPending(host.getName()),
                                        resources.messages().reloadHostSuccess(host.getName()),
                                        resources.messages().domainControllerTimeout(host.getName()),
                                        resources.messages().reloadHostError(host.getName()));
                            } else {
                                hostControllerOperation(host, operation, hostTimeout(host, Action.RELOAD),
                                        restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                        resources.messages().reloadHostSuccess(host.getName()),
                                        resources.messages().hostControllerTimeout(host.getName()),
                                        resources.messages().reloadHostError(host.getName()));
                            }
                        }, SHORT_TIMEOUT);
                    });
                    dialog.registerAttachable(form);
                    dialog.show();

                    ModelNode model = new ModelNode();
                    model.get(RESTART_SERVERS).set(true);
                    form.edit(model);
                    return null;
                });
    }

    // ------------------------------------------------------ restart

    public void restart(Host host) {
        SafeHtml question = host.isDomainController()
                ? resources.messages().restartDomainControllerQuestion(host.getName())
                : resources.messages().restartHostControllerQuestion(host.getName());
        restart(host, question);
    }

    public void restart(Host host, SafeHtml question) {
        DialogFactory.showConfirmation(resources.messages().restart(host.getName()), question, () -> {
            // execute the operation with a little delay ensuring the confirmation dialog is closed
            // before the next dialog is opened (only one modal can be open at a time!)
            setTimeout(__ -> {
                prepare(host, host.getServers(), Action.RESTART);
                Operation operation = new Operation.Builder(host.getAddress(), SHUTDOWN)
                        .param(RESTART, true)
                        .build();
                if (host.isDomainController()) {
                    domainControllerOperation(host, operation, hostTimeout(host, Action.RESTART), host.getServers(),
                            resources.messages().restart(host.getName()),
                            resources.messages().restartDomainControllerPending(host.getName()),
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().domainControllerTimeout(host.getName()),
                            resources.messages().restartHostError(host.getName()));

                } else {
                    hostControllerOperation(host, operation, hostTimeout(host, Action.RESTART), host.getServers(),
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().hostControllerTimeout(host.getName()),
                            resources.messages().restartHostError(host.getName()));
                }
            }, SHORT_TIMEOUT);
        });
    }

    // ------------------------------------------------------ helper methods

    private void prepare(Host host, List<Server> servers, Action action) {
        markAsPending(host); // mark as pending *before* firing the event!
        servers.forEach(serverActions::markAsPending);
        eventBus.fireEvent(new HostActionEvent(host, servers, action));
    }

    private void domainControllerOperation(Host host, Operation operation, int timeout, List<Server> servers,
            String title, SafeHtml pendingMessage, SafeHtml successMessage, SafeHtml timeoutMessage,
            SafeHtml errorMessage) {
        BlockingDialog pendingDialog = DialogFactory.buildLongRunning(title, pendingMessage);
        pendingDialog.show();

        dispatcher.execute(operation)
                .then(result -> repeatUntilTimeout(dispatcher, ping(host), timeout))
                .then(status -> {
                    pendingDialog.close();
                    switch (status) {
                        case SUCCESS:
                            // wait a bit before event handlers try to use the reloaded / restarted domain controller
                            return new Promise<>((resolve, reject) -> setTimeout(
                                    __ -> resolve.onInvoke(finish(host, servers, SUCCESS, Message.success(successMessage))),
                                    LONG_TIMEOUT));
                        case FAILURE:
                            return finish(host, servers, FAILURE, Message.error(errorMessage));
                        case TIMEOUT:
                            DialogFactory.buildBlocking(title, timeoutMessage).show();
                            return finish(host, servers, FlowStatus.TIMEOUT, null);
                        default:
                            throw new IllegalStateException("Invalid flow status: " + status);
                    }
                })
                .catch_(error -> {
                    pendingDialog.close();
                    return finish(host, servers, FAILURE, Message.error(errorMessage, String.valueOf(error)));
                });
    }

    private void hostControllerOperation(Host host, Operation operation, int timeout, List<Server> servers,
            SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {
        dispatcher.execute(operation)
                .then(__ -> repeatUntilTimeout(dispatcher, ping(host), timeout))
                .then(status -> finish(host, servers, status, successMessage, timeoutMessage, errorMessage))
                .catch_(error -> finish(host, servers, FAILURE, Message.error(errorMessage, String.valueOf(error))));
    }

    private Promise<Void> finish(Host host, List<Server> servers, FlowStatus status,
            SafeHtml successMessage, SafeHtml timeoutMessage, SafeHtml errorMessage) {
        switch (status) {
            case SUCCESS:
                return finish(host, servers, SUCCESS, Message.success(successMessage));
            case TIMEOUT:
                return finish(host, servers, FlowStatus.TIMEOUT, Message.error(timeoutMessage));
            case FAILURE:
                return finish(host, servers, FAILURE, Message.error(errorMessage));
            default:
                throw new IllegalStateException("Invalid flow status" + status);
        }
    }

    private Promise<Void> finish(Host host, List<Server> servers, FlowStatus status, Message message) {
        clearPending(host); // clear pending state *before* firing the event!
        servers.forEach(serverActions::clearPending);
        eventBus.fireEvent(new HostResultEvent(host, servers, status));
        if (message != null) {
            MessageEvent.fire(eventBus, message);
        }
        return Promise.resolve((Void) null);
    }

    private void markAsPending(Host host) {
        pendingHosts.put(host.getName(), host);
        logger.debug("Mark host {} as pending", host.getName());
    }

    private void clearPending(Host host) {
        pendingHosts.remove(host.getName());
        logger.debug("Clear pending state for host {}", host.getName());
    }

    public boolean isPending(Host host) {
        return pendingHosts.containsKey(host.getName());
    }

    private Operation ping(Host host) {
        ResourceAddress address = new ResourceAddress()
                .add(HOST, host.getName()); // do not use host.getAddressName() here!
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).build();

        if (host.hasServers(Server::isStarted)) {
            Operation[] operations = host.getServers(Server::isStarted).stream()
                    .map(server -> {
                        ResourceAddress serverAddress = host.getAddress().add(SERVER, server.getName());
                        return new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION).build();
                    }).toArray(Operation[]::new);
            operation = new Composite(operation, operations);
        } else {
            operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
        }
        return operation;
    }
}
