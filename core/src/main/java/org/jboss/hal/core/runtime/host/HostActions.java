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
package org.jboss.hal.core.runtime.host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.TimeoutHandler;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

/**
 * @author Harald Pehl
 */
public class HostActions {

    private class HostFailedCallback implements Dispatcher.FailedCallback {

        private final Host host;
        private final List<Server> servers;
        private final SafeHtml errorMessage;

        HostFailedCallback(final Host host, final List<Server> servers, final SafeHtml errorMessage) {
            this.host = host;
            this.servers = servers;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onFailed(final Operation operation, final String failure) {
            finish(host, servers, Result.ERROR, Message.error(errorMessage, failure));
        }
    }


    private class HostExceptionCallback implements Dispatcher.ExceptionCallback {

        private final Host host;
        private final List<Server> servers;
        private final SafeHtml errorMessage;

        HostExceptionCallback(final Host host, final List<Server> servers, final SafeHtml errorMessage) {
            this.host = host;
            this.servers = servers;
            this.errorMessage = errorMessage;
        }

        @Override
        public void onException(final Operation operation, final Throwable exception) {
            finish(host, servers, Result.ERROR, Message.error(errorMessage, exception.getMessage()));
        }
    }


    private static final int RELOAD_TIMEOUT = 10; // seconds w/o servers
    private static final int RESTART_TIMEOUT = 15; // seconds w/o servers
    @NonNls private static final Logger logger = LoggerFactory.getLogger(HostActions.class);

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final ServerActions serverActions;
    private final Resources resources;
    private final Map<String, Host> pendingHosts;

    @Inject
    public HostActions(final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final ServerActions serverActions,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.serverActions = serverActions;
        this.resources = resources;
        this.pendingHosts = new HashMap<>();
    }


    // ------------------------------------------------------ reload

    @SuppressWarnings("HardCodedStringLiteral")
    public void reload(final Host host) {
        metadataProcessor.lookup(AddressTemplate.of("/host=" + host.getName()), progress.get(), new MetadataCallback() {
            @Override
            public void onMetadata(final Metadata metadata) {
                Form<ModelNode> form = new OperationFormBuilder<>(
                        Ids.build(RELOAD_HOST, host.getName(), Ids.FORM_SUFFIX),
                        metadata, RELOAD)
                        .include(RESTART_SERVERS)
                        .build();

                SafeHtml question;
                if (host.isDomainController()) {
                    question = resources.messages().reloadDomainControllerQuestion(host.getName());
                } else {
                    question = resources.messages().reloadHostControllerQuestion(host.getName());
                }
                Dialog dialog = DialogFactory.buildConfirmation(
                        resources.messages().reload(host.getName()), question, form.asElement(),
                        () -> {
                            form.save();
                            boolean restartServers = form.getModel().get(RESTART_SERVERS).asBoolean();
                            prepare(host, restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                    Action.RELOAD);
                            Operation operation = new Operation.Builder(RELOAD, host.getAddress())
                                    .param(RESTART_SERVERS, restartServers)
                                    .build();

                            // execute the reload with a little delay to ensure the confirmation dialog is closed
                            // before the next dialog is opened (only one modal can be open at a time!)
                            Browser.getWindow().setTimeout(() -> {

                                if (host.isDomainController()) {
                                    domainControllerOperation(host, operation, reloadTimeout(host),
                                            restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                            resources.messages().reload(host.getName()),
                                            resources.messages().reloadDomainControllerPending(host.getName()),
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().reloadHostError(host.getName()),
                                            resources.messages().domainControllerTimeout(host.getName()));

                                } else {
                                    hostControllerOperation(host, operation, reloadTimeout(host),
                                            restartServers ? host.getServers(Server::isStarted) : emptyList(),
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().reloadHostError(host.getName()),
                                            resources.messages().hostControllerTimeout(host.getName()));
                                }
                            }, SHORT_TIMEOUT);
                        });
                dialog.registerAttachable(form);
                dialog.show();

                ModelNode model = new ModelNode();
                model.get(RESTART_SERVERS).set(true);
                form.edit(model);
            }

            @Override
            public void onError(final Throwable error) {
                MessageEvent.fire(eventBus, Message.error(resources.messages().metadataError(), error.getMessage()));
            }
        });
    }


    // ------------------------------------------------------ restart

    public void restart(final Host host) {
        SafeHtml question = host.isDomainController()
                ? resources.messages().restartDomainControllerQuestion(host.getName())
                : resources.messages().restartHostControllerQuestion(host.getName());
        DialogFactory.showConfirmation(resources.messages().restart(host.getName()), question, () -> {
            // execute the restart with a little delay to ensure the confirmation dialog is closed
            // before the next dialog is opened (only one modal can be open at a time!)
            Browser.getWindow().setTimeout(() -> {

                prepare(host, host.getServers(), Action.RESTART);
                Operation operation = new Operation.Builder(SHUTDOWN, host.getAddress())
                        .param(RESTART, true)
                        .build();
                if (host.isDomainController()) {
                    domainControllerOperation(host, operation, restartTimeout(host), host.getServers(),
                            resources.messages().restart(host.getName()),
                            resources.messages().restartDomainControllerPending(host.getName()),
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().restartHostError(host.getName()),
                            resources.messages().domainControllerTimeout(host.getName()));

                } else {
                    hostControllerOperation(host, operation, restartTimeout(host), host.getServers(),
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().restartHostError(host.getName()),
                            resources.messages().hostControllerTimeout(host.getName()));
                }
            }, SHORT_TIMEOUT);
        });
    }


    // ------------------------------------------------------ helper methods

    private void domainControllerOperation(Host host, Operation operation, int timeout, List<Server> servers,
            String title, SafeHtml pendingMessage, SafeHtml successMessage, SafeHtml errorMessage,
            SafeHtml timeoutMessage) {
        BlockingDialog pendingDialog = DialogFactory.buildLongRunning(title, pendingMessage);
        pendingDialog.show();

        dispatcher.execute(operation,
                result -> new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
                    @Override
                    public void onSuccess() {
                        // wait a little bit before event handlers try to use the reloaded / restarted domain controller
                        Browser.getWindow().setTimeout(() -> {
                            pendingDialog.close();
                            finish(host, servers, Result.SUCCESS, Message.success(successMessage));
                        }, 666);
                    }

                    @Override
                    public void onTimeout() {
                        pendingDialog.close();
                        DialogFactory.buildBlocking(title, timeoutMessage).show();
                        finish(host, servers, Result.TIMEOUT, null);
                    }
                }),
                new HostFailedCallback(host, servers, errorMessage),
                new HostExceptionCallback(host, servers, errorMessage));
    }

    private void hostControllerOperation(Host host, Operation operation, int timeout, List<Server> servers,
            SafeHtml successMessage, SafeHtml errorMessage, SafeHtml timeoutMessage) {
        dispatcher.execute(operation,
                result -> new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
                    @Override
                    public void onSuccess() {
                        finish(host, servers, Result.SUCCESS, Message.success(successMessage));
                    }

                    @Override
                    public void onTimeout() {
                        finish(host, servers, Result.TIMEOUT, Message.error(timeoutMessage));
                    }
                }),
                new HostFailedCallback(host, servers, errorMessage),
                new HostExceptionCallback(host, servers, errorMessage));
    }

    private void prepare(Host host, List<Server> servers, Action action) {
        markAsPending(host); // mark as pending *before* firing the event!
        servers.forEach(serverActions::markAsPending);
        eventBus.fireEvent(new HostActionEvent(host, servers, action));
    }

    private void finish(Host host, List<Server> servers, Result result, Message message) {
        clearPending(host); // clear pending state *before* firing the event!
        servers.forEach(serverActions::clearPending);
        eventBus.fireEvent(new HostResultEvent(host, servers, result));
        if (message != null) {
            MessageEvent.fire(eventBus, message);
        }
    }

    private void markAsPending(Host host) {
        Dispatcher.setPendingLifecycleAction(true);
        pendingHosts.put(host.getName(), host);
        logger.debug("Mark host {} as pending", host.getName());
    }

    private void clearPending(Host host) {
        Dispatcher.setPendingLifecycleAction(false);
        pendingHosts.remove(host.getName());
        logger.debug("Clear pending state for host {}", host.getName());
    }

    public boolean isPending(Host host) {
        return pendingHosts.containsKey(host.getName());
    }

    private int reloadTimeout(Host host) {
        return RELOAD_TIMEOUT + host.getServers(Server::isStarted).size() * ServerActions.SERVER_RELOAD_TIMEOUT;
    }

    private int restartTimeout(Host host) {
        return RESTART_TIMEOUT + host.getServers(Server::isStarted).size() * ServerActions.SERVER_RESTART_TIMEOUT;
    }

    private Operation ping(Host host) {
        ResourceAddress address = new ResourceAddress()
                .add(HOST, host.getName()); // do not use host.getAddressName() here!
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();

        if (host.hasServers(Server::isStarted)) {
            List<Operation> pingServer = host.getServers(Server::isStarted).stream()
                    .map(server -> {
                        ResourceAddress serverAddress = host.getAddress().add(SERVER, server.getName());
                        return new Operation.Builder(READ_RESOURCE_OPERATION, serverAddress).build();
                    })
                    .collect(toList());
            operation = new Composite(operation, pingServer.toArray(new Operation[pingServer.size()]));
        } else {
            operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
        }
        return operation;
    }
}
