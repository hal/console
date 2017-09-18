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
package org.jboss.hal.client.patching.wizard;

import java.util.Iterator;
import java.util.List;
import javax.inject.Provider;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.client.patching.PatchesColumn.PATCHING_TEMPLATE;
import static org.jboss.hal.client.patching.wizard.PatchState.CHECK_SERVERS;
import static org.jboss.hal.client.patching.wizard.PatchState.CONFIGURE;
import static org.jboss.hal.client.patching.wizard.PatchState.UPLOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class ApplyPatchWizard {

    static class UploadPatch implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private StatementContext statementContext;
        private ServerActions serverActions;
        private PatchContext patchContext;

        UploadPatch(StatementContext statementContext, Dispatcher dispatcher,
                ServerActions serverActions, PatchContext patchContext) {

            this.statementContext = statementContext;
            this.dispatcher = dispatcher;
            this.serverActions = serverActions;
            this.patchContext = patchContext;
        }

        @Override
        public void execute(FlowContext context, Control control) {
            if (patchContext.restartServers) {
                for (Property serverProp : patchContext.servers) {
                    Server server = new Server(statementContext.selectedHost(), serverProp);
                    serverActions.stopNow(server);
                }
            }

            ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
            Operation.Builder opBuilder = new Operation.Builder(address, PATCH)
                    .param(OVERRIDE_ALL, patchContext.overrideAll)
                    .param(OVERRIDE_MODULE, patchContext.overrideModules);
            if (patchContext.override != null) {
                opBuilder.param(OVERRIDE, patchContext.override.toArray(new String[patchContext.override.size()]));
            }
            if (patchContext.preserve != null) {
                opBuilder.param(PRESERVE, patchContext.preserve.toArray(new String[patchContext.preserve.size()]));
            }
            Operation operation = opBuilder.build();
            operation.get(CONTENT).add().get("input-stream-index").set(0); //NON-NLS

            dispatcher.upload(patchContext.file, operation,
                    result -> control.proceed(),
                    (op, failure) -> control.abort(failure),
                    (op, exception) -> control.abort(exception.getMessage()));
        }
    }


    private Resources resources;
    private Environment environment;
    private Metadata metadataOp;
    private StatementContext statementContext;
    private Dispatcher dispatcher;
    private Provider<Progress> progress;
    private ServerActions serverActions;
    private Callback callback;

    public ApplyPatchWizard(Resources resources, Environment environment, Metadata metadata,
            StatementContext statementContext, Dispatcher dispatcher, Provider<Progress> progress,
            ServerActions serverActions, Callback callback) {

        this.resources = resources;
        this.environment = environment;
        this.metadataOp = metadata;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.serverActions = serverActions;
        this.callback = callback;
    }

    public void show() {
        Messages messages = resources.messages();
        Wizard.Builder<PatchContext, PatchState> wb = new Wizard.Builder<>(
                messages.addResourceTitle(Names.PATCH), new PatchContext());

        checkServersState(servers -> {
            if (servers != null) {
                wb.addStep(CHECK_SERVERS, new CheckRunningServersStep(resources, servers,
                        statementContext.selectedHost()));
            }
            wb.addStep(UPLOAD, new UploadPatchStep(resources.constants().uploadPatch(), messages.noSelectedPatch()))
                    .addStep(CONFIGURE, new ConfigurationStep(metadataOp, resources))

                    .onBack((context, currentState) -> {
                        PatchState previous = null;
                        switch (currentState) {
                            case CHECK_SERVERS:
                                break;
                            case UPLOAD:
                                previous = CHECK_SERVERS;
                                break;
                            case CONFIGURE:
                                previous = UPLOAD;
                                break;
                        }
                        return previous;
                    })
                    .onNext((context, currentState) -> {
                        PatchState next = null;
                        switch (currentState) {
                            case CHECK_SERVERS:
                                next = UPLOAD;
                                break;
                            case UPLOAD:
                                next = CONFIGURE;
                                break;
                            case CONFIGURE:
                                break;
                        }
                        return next;
                    })

                    .stayOpenAfterFinish()
                    .onFinish((wzd, context) -> {
                        String name = context.file.name;
                        wzd.showProgress(resources.constants().patchInProgress(), messages.patchInProgress(name));

                        series(new FlowContext(progress.get()),
                                new UploadPatch(statementContext, dispatcher, serverActions, context))
                                .subscribe(new Outcome<FlowContext>() {
                                    @Override
                                    public void onError(FlowContext flowContext, Throwable error) {
                                        wzd.showError(resources.constants().patchError(),
                                                messages.patchAddError(name, error.getMessage()),
                                                error.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        callback.execute();
                                        wzd.showSuccess(
                                                resources.constants().patchSuccessful(),
                                                messages.patchSucessfullyApplied(name),
                                                messages.view(Names.PATCH),
                                                cxt -> { /* nothing to do, content is already selected */ });
                                    }
                                });
                    });
            Wizard<PatchContext, PatchState> wizard = wb.build();
            wizard.show();
        });
    }

    /**
     * Checks if each servers of a host is stopped, if the server is started, asks the user to stop them.
     * It is a good practice to apply/rollback a patch to a stopped server to prevent application and internal services
     * from failing.
     */
    private void checkServersState(Dispatcher.SuccessCallback<List<Property>> callback) {

        if (environment.isStandalone()) {
            callback.onSuccess(null);
        } else {

            String host = statementContext.selectedHost();
            ResourceAddress address = new ResourceAddress().add(HOST, host);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(CHILD_TYPE, SERVER_CONFIG)
                    .build();

            dispatcher.execute(operation, result -> {
                List<Property> servers = result.asPropertyList();
                boolean anyServerStarted = false;
                for (Iterator<Property> iter = servers.iterator(); iter.hasNext(); ) {
                    Property serverProp = iter.next();
                    Server server = new Server(host, serverProp);
                    if (!server.isStopped()) {
                        anyServerStarted = true;
                    } else {
                        iter.remove();
                    }
                }
                if (anyServerStarted) {
                    callback.onSuccess(servers);
                } else {
                    callback.onSuccess(null);
                }
            });
        }
    }

}
