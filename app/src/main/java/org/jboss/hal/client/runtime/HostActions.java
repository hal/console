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
package org.jboss.hal.client.runtime;

import java.util.List;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.TimeoutHandler;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_EXCEPTIONAL_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_FAILED_CALLBACK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.NOOP_OPERATION_CALLBACK;

/**
 * @author Harald Pehl
 */
public class HostActions {

    private static final int DIALOG_TIMEOUT = 111;
    private static final int RELOAD_TIMEOUT = 8; // seconds
    private static final int RESTART_TIMEOUT = 12; // seconds
    private static final int SERVER_TIMEOUT = 4; // additional seconds per server

    private final Finder finder;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Capabilities capabilities;
    private final Resources resources;

    @Inject
    public HostActions(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataRegistry metadataRegistry,
            final Capabilities capabilities,
            final Resources resources) {
        this.finder = finder;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.capabilities = capabilities;
        this.resources = resources;
    }


    // ------------------------------------------------------ reload

    @SuppressWarnings("HardCodedStringLiteral")
    public void reload(final Host host, final boolean domainController,
            final ScheduledCommand beforeReload, final ScheduledCommand afterReload) {
        Metadata hostMetadata = metadataRegistry.lookup(AddressTemplate.of("/{selected.host}"));
        ModelNode modelNode = ModelNodeHelper.failSafeGet(hostMetadata.getDescription(),
                Joiner.on('.').join(OPERATIONS, RELOAD, REQUEST_PROPERTIES));
        ModelNode repackaged = new ModelNode();
        repackaged.get(ATTRIBUTES).set(modelNode);
        ResourceDescription reloadDescription = new ResourceDescription(repackaged);
        Metadata reloadMetadata = new Metadata(SecurityContext.RWX, reloadDescription, capabilities);
        Form<ModelNode> form = new ModelNodeForm.Builder<>("restart-host-form", reloadMetadata)
                .include("restart-servers")
                .addOnly()
                .build();

        Dialog dialog = DialogFactory
                .confirmation(resources.messages().reload(host.getName()),
                        resources.messages().reloadHostQuestion(host.getName()), form.asElement(),
                        () -> {
                            if (beforeReload != null) {
                                beforeReload.execute();
                            }
                            form.save();
                            boolean restartServers = form.getModel().get("restart-servers").asBoolean();
                            Operation operation = new Operation.Builder(RELOAD,
                                    new ResourceAddress().add(HOST, host.getName()))
                                    .param("restart-servers", restartServers)
                                    .build();

                            // execute the reload with a little delay to ensure the confirmation dialog is closed
                            // before the next dialog is opened (only one modal can be open at a time!)
                            Browser.getWindow().setTimeout(() -> {

                                if (domainController) {
                                    domainControllerOperation(host, operation, reloadTimeout(host), afterReload,
                                            resources.messages().reload(host.getName()),
                                            resources.messages().reloadDomainControllerPending(),
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().reloadDomainControllerError());

                                } else {
                                    hostControllerOperation(host, operation, reloadTimeout(host), afterReload,
                                            resources.messages().reloadHostSuccess(host.getName()),
                                            resources.messages().reloadHostError(host.getName()));
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

    public void restart(final Host host, final boolean domainController,
            final ScheduledCommand beforeRestart, final ScheduledCommand afterRestart) {
        SafeHtml question = domainController
                ? resources.messages().restartDomainControllerQuestion(host.getName())
                : resources.messages().restartHostControllerQuestion(host.getName());
        DialogFactory.confirmation(resources.messages().restart(host.getName()), question, () -> {
            // execute the restart with a little delay to ensure the confirmation dialog is closed
            // before the next dialog is opened (only one modal can be open at a time!)
            Browser.getWindow().setTimeout(() -> {

                if (beforeRestart != null) {
                    beforeRestart.execute();
                }
                Operation operation = new Operation.Builder(SHUTDOWN, new ResourceAddress().add(HOST, host.getName()))
                        .param("restart", true)
                        .build();
                if (domainController) {
                    domainControllerOperation(host, operation, restartTimeout(host), afterRestart,
                            resources.messages().restart(host.getName()),
                            resources.messages().restartDomainControllerPending(),
                            resources.messages().restartHostSuccessful(host.getName()),
                            resources.messages().restartDomainControllerError());

                } else {
                    hostControllerOperation(host, operation, restartTimeout(host), afterRestart,
                            resources.messages().restartHostSuccessful(host.getName()),
                            resources.messages().restartHostControllerError());
                }
            }, DIALOG_TIMEOUT);
            return true;
        }).show();
    }


    // ------------------------------------------------------ helper methods

    private void domainControllerOperation(Host host, Operation operation, int timeout, ScheduledCommand afterOperation,
            String title, SafeHtml pendingMessage, String successMessage, SafeHtml timeoutMessage) {
        BlockingDialog pendingDialog = DialogFactory.longRunning(title, pendingMessage);
        pendingDialog.show();

        dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK);
        new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
            @Override
            public void onSuccess() {
                afterOperation.execute();
                pendingDialog.close();
                MessageEvent.fire(eventBus, Message.success(successMessage));
            }

            @Override
            public void onTimeout() {
                afterOperation.execute();
                pendingDialog.close();
                DialogFactory.blocking(title, timeoutMessage).show();
            }
        });
    }

    private void hostControllerOperation(Host host, Operation operation, int timeout,
            final ScheduledCommand afterOperation, String successMessage, String timeoutMessage) {
        dispatcher.execute(operation, NOOP_OPERATION_CALLBACK, NOOP_FAILED_CALLBACK, NOOP_EXCEPTIONAL_CALLBACK);
        new TimeoutHandler(dispatcher, timeout).execute(ping(host), new TimeoutHandler.Callback() {
            @Override
            public void onSuccess() {
                finish(Message.success(successMessage));
            }

            @Override
            public void onTimeout() {
                finish(Message.error(timeoutMessage));
            }

            private void finish(Message message) {
                afterOperation.execute();
                MessageEvent.fire(eventBus, message);
            }
        });
    }

    private int reloadTimeout(Host host) {
        return RELOAD_TIMEOUT + host.getRunningServers().size() * SERVER_TIMEOUT;
    }

    private int restartTimeout(Host host) {
        return RESTART_TIMEOUT + host.getRunningServers().size() * SERVER_TIMEOUT;
    }

    private Operation ping(Host host) {
        ResourceAddress address = new ResourceAddress().add(HOST, host.getName());
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();

        if (host.hasRunningServers()) {
            List<Operation> pingServer = host.getRunningServers().stream()
                    .map(server -> {
                        ResourceAddress serverAddress = new ResourceAddress().add(HOST, host.getName())
                                .add(SERVER, server);
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
