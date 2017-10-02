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
import java.util.function.Consumer;
import javax.inject.Provider;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
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
import io.reactivex.Completable;

import static org.jboss.hal.client.patching.PatchesColumn.PATCHING_TEMPLATE;
import static org.jboss.hal.client.patching.wizard.PatchState.CHECK_SERVERS;
import static org.jboss.hal.client.patching.wizard.PatchState.ROLLBACK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class RollbackWizard {

    static class RollbackTask implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private StatementContext statementContext;
        private ServerActions serverActions;
        private PatchContext patchContext;

        RollbackTask(StatementContext statementContext, Dispatcher dispatcher,
                ServerActions serverActions, PatchContext patchContext) {

            this.statementContext = statementContext;
            this.dispatcher = dispatcher;
            this.serverActions = serverActions;
            this.patchContext = patchContext;
        }

        @Override
        public Completable apply(FlowContext context) {

            if (patchContext.restartServers) {
                for (Property serverProp : patchContext.servers) {
                    Server server = new Server(statementContext.selectedHost(), serverProp);
                    serverActions.stopNow(server);
                }
            }

            ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
            Operation.Builder opBuilder = new Operation.Builder(address, ROLLBACK_OPERATION)
                    .param(PATCH_ID, patchContext.patchId)
                    .param(ROLLBACK_TO, patchContext.rollbackTo)
                    .param(RESET_CONFIGURATION, patchContext.resetConfiguration)
                    .param(OVERRIDE_ALL, patchContext.overrideAll)
                    .param(OVERRIDE_MODULE, patchContext.overrideModules);

            if (patchContext.override != null) {
                opBuilder.param(OVERRIDE, patchContext.override.toArray(new String[patchContext.override.size()]));
            }
            if (patchContext.preserve != null) {
                opBuilder.param(PRESERVE, patchContext.preserve.toArray(new String[patchContext.preserve.size()]));
            }
            Operation operation = opBuilder.build();

            return dispatcher.execute(operation).toCompletable();
        }
    }


    private Resources resources;
    private Environment environment;
    private String patchId;
    private Metadata metadata;
    private StatementContext statementContext;
    private Dispatcher dispatcher;
    private Provider<Progress> progress;
    private ServerActions serverActions;
    private Callback callback;

    public RollbackWizard(Resources resources, Environment environment, String patchId, Metadata metadata,
            StatementContext statementContext, Dispatcher dispatcher, Provider<Progress> progress,
            ServerActions serverActions, Callback callback) {

        this.resources = resources;
        this.environment = environment;
        this.patchId = patchId;
        this.metadata = metadata;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.serverActions = serverActions;
        this.callback = callback;
    }

    public void show() {
        Messages messages = resources.messages();
        Wizard.Builder<PatchContext, PatchState> wb = new Wizard.Builder<>(resources.constants().rollback(),
                new PatchContext());

        checkServersState(servers -> {
            if (servers != null) {
                wb.addStep(CHECK_SERVERS, new CheckRunningServersStep(resources, servers,
                        statementContext.selectedHost()));
            }
            wb.addStep(ROLLBACK, new org.jboss.hal.client.patching.wizard.RollbackStep(metadata, resources,
                    statementContext.selectedHost(), patchId))

                    .onBack((context, currentState) -> {
                        PatchState previous = null;
                        switch (currentState) {
                            case CHECK_SERVERS:
                                break;
                            case ROLLBACK:
                                previous = CHECK_SERVERS;
                                break;
                        }
                        return previous;
                    })
                    .onNext((context, currentState) -> {
                        PatchState next = null;
                        switch (currentState) {
                            case CHECK_SERVERS:
                                next = ROLLBACK;
                                break;
                            case ROLLBACK:
                                break;
                        }
                        return next;
                    })

                    .stayOpenAfterFinish()
                    .onFinish((wzd, context) -> {
                        String name = context.patchId;
                        wzd.showProgress(resources.constants().rollbackInProgress(), messages.rollbackInProgress(name));

                        series(new FlowContext(progress.get()),
                                new RollbackTask(statementContext, dispatcher, serverActions, context))
                                .subscribe(new Outcome<FlowContext>() {
                                    @Override
                                    public void onError(Throwable error) {
                                        wzd.showError(resources.constants().rollbackError(),
                                                messages.rollbackError(error.getMessage()),
                                                error.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        callback.execute();
                                        wzd.showSuccess(
                                                resources.constants().rollbackSuccessful(),
                                                messages.rollbackSucessful(name),
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
    private void checkServersState(Consumer<List<Property>> callback) {
        if (environment.isStandalone()) {
            callback.accept(null);
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
                    callback.accept(servers);
                } else {
                    callback.accept(null);
                }
            });
        }
    }

}
