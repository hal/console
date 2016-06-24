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

import java.util.List;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.runtime.Action;
import org.jboss.hal.core.runtime.Result;
import org.jboss.hal.core.runtime.RunningState;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.TimeoutHandler;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.runtime.server.ServerConfigStatus.STARTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_EXCEPTIONAL_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_FAILED_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_OPERATION_CALLBACK;

/**
 * @author Harald Pehl
 */
public class HostActions {

    private static final int DIALOG_TIMEOUT = 111;
    private static final int RELOAD_TIMEOUT = 10; // seconds
    private static final int RESTART_TIMEOUT = 15; // seconds

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Capabilities capabilities;
    private final Resources resources;

    @Inject
    public HostActions(final EventBus eventBus,
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


    // ------------------------------------------------------ reload

    @SuppressWarnings("HardCodedStringLiteral")
    public void reload(final Host host, final ScheduledCommand whileReloading) {
        Metadata hostMetadata = metadataRegistry.lookup(AddressTemplate.of("/{selected.host}"));
        Form<ModelNode> form = new OperationFormBuilder<>(IdBuilder.build(RELOAD_HOST, host.getName(), "form"),
                hostMetadata, RELOAD)
                .include(RESTART_SERVERS)
                .build();

        SafeHtml question;
        if (host.isDomainController()) {
            question = resources.messages().reloadDomainControllerQuestion(host.getName());
        } else {
            question = resources.messages().reloadHostControllerQuestion(host.getName());
        }
        Dialog dialog = DialogFactory
                .confirmation(resources.messages().reload(host.getName()), question, form.asElement(),
                        () -> {
                            eventBus.fireEvent(new HostActionEvent(host, Action.RELOAD));
                            form.save();
                            boolean restartServers = form.getModel().get("restart-servers").asBoolean();
                            Operation operation = new Operation.Builder(RELOAD, host.getAddress())
                                    .param("restart-servers", restartServers)
                                    .build();

                            // execute the reload with a little delay to ensure the confirmation dialog is closed
                            // before the next dialog is opened (only one modal can be open at a time!)
                            Browser.getWindow().setTimeout(() -> {

                                if (host.isDomainController()) {
                                    domainControllerOperation(host, operation, reloadTimeout(host), whileReloading,
                                            resources.messages().reload(host.getName()),
                                            resources.messages().reloadHostPending(host.getName()),
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().domainControllerTimeout(host.getName()));

                                } else {
                                    hostControllerOperation(host, operation, reloadTimeout(host), whileReloading,
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().hostControllerTimeout(host.getName()));
                                }
                            }, DIALOG_TIMEOUT);
                            return true;
                        });
        dialog.registerAttachable(form);
        dialog.show();

        ModelNode model = new ModelNode();
        model.get("restart-servers").set(true);
        form.add(model);
    }


    // ------------------------------------------------------ restart

    public void restart(final Host host, final ScheduledCommand whileReloading) {
        SafeHtml question = host.isDomainController()
                ? resources.messages().restartDomainControllerQuestion(host.getName())
                : resources.messages().restartHostControllerQuestion(host.getName());
        DialogFactory.confirmation(resources.messages().restart(host.getName()), question, () -> {
            // execute the restart with a little delay to ensure the confirmation dialog is closed
            // before the next dialog is opened (only one modal can be open at a time!)
            Browser.getWindow().setTimeout(() -> {

                eventBus.fireEvent(new HostActionEvent(host, Action.RESTART));
                Operation operation = new Operation.Builder(SHUTDOWN, host.getAddress())
                        .param("restart", true)
                        .build();
                if (host.isDomainController()) {
                    domainControllerOperation(host, operation, restartTimeout(host), whileReloading,
                            resources.messages().restart(host.getName()),
                            resources.messages().restartHostPending(host.getName()),
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().domainControllerTimeout(host.getName()));

                } else {
                    hostControllerOperation(host, operation, restartTimeout(host), whileReloading,
                            resources.messages().restartHostSuccess(host.getName()),
                            resources.messages().hostControllerTimeout(host.getName()));
                }
            }, DIALOG_TIMEOUT);
            return true;
        }).show();
    }


    // ------------------------------------------------------ helper methods

    private void domainControllerOperation(Host host, Operation operation, int timeout, ScheduledCommand whileOperation,
            String title, SafeHtml pendingMessage, SafeHtml successMessage, SafeHtml timeoutMessage) {
        BlockingDialog pendingDialog = DialogFactory.longRunning(title, pendingMessage);
        pendingDialog.show();

        // The 'host-state' attribute is not updated during the operation. So we change it manually.
        host.setHostState(RunningState.STARTING);

        dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK);
        new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
            @Override
            public void onSuccess() {
                host.setHostState(RunningState.RUNNING);
                pendingDialog.close();
                MessageEvent.fire(eventBus, Message.success(successMessage));
                eventBus.fireEvent(new HostResultEvent(host, Result.SUCCESS));
            }

            @Override
            public void pending() {
                if (whileOperation != null) {
                    whileOperation.execute();
                }
            }

            @Override
            public void onTimeout() {
                pendingDialog.close();
                DialogFactory.blocking(title, timeoutMessage).show();
                eventBus.fireEvent(new HostResultEvent(host, Result.TIMEOUT));
            }
        });
    }

    private void hostControllerOperation(Host host, Operation operation, int timeout, ScheduledCommand whileOperation,
            SafeHtml successMessage, SafeHtml timeoutMessage) {
        // The 'host-state' attribute is not updated during the operation. So we change it manually.
        host.setHostState(RunningState.STARTING);

        dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK);
        new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
            @Override
            public void onSuccess() {
                host.setHostState(RunningState.RUNNING);
                MessageEvent.fire(eventBus, Message.success(successMessage));
                eventBus.fireEvent(new HostResultEvent(host, Result.SUCCESS));
            }

            @Override
            public void pending() {
                if (whileOperation != null) {
                    whileOperation.execute();
                }
            }

            @Override
            public void onTimeout() {
                MessageEvent.fire(eventBus, Message.error(timeoutMessage));
                eventBus.fireEvent(new HostResultEvent(host, Result.TIMEOUT));
            }
        });
    }

    private int reloadTimeout(Host host) {
        return RELOAD_TIMEOUT + host.getServers(STARTED).size() * ServerActions.SERVER_RELOAD_TIMEOUT;
    }

    private int restartTimeout(Host host) {
        return RESTART_TIMEOUT + host.getServers(STARTED).size() * ServerActions.SERVER_RESTART_TIMEOUT;
    }

    private Operation ping(Host host) {
        ResourceAddress address = new ResourceAddress()
                .add(HOST, host.getName()); // do not use host.getAddressName() here!
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();

        if (host.hasServers(STARTED)) {
            List<Operation> pingServer = host.getServers(STARTED).stream()
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
