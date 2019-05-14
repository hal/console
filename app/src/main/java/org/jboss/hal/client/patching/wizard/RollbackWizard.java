/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.patching.wizard;

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
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import rx.Completable;

import static org.jboss.hal.client.patching.PatchesColumn.PATCHING_TEMPLATE;
import static org.jboss.hal.client.patching.wizard.PatchState.CHECK_SERVERS;
import static org.jboss.hal.client.patching.wizard.PatchState.ROLLBACK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class RollbackWizard extends PatchWizard {

    private final String patchId;

    public RollbackWizard(String patchId, Resources resources, Environment environment, Metadata metadata,
            StatementContext statementContext, Dispatcher dispatcher,
            Provider<Progress> progress, ServerActions serverActions, Callback callback) {
        super(resources, environment, metadata, statementContext, dispatcher, progress, serverActions, callback);
        this.patchId = patchId;
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
                            default:
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
                            default:
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
                                    public void onError(FlowContext context, Throwable error) {
                                        wzd.showError(resources.constants().rollbackError(),
                                                messages.rollbackError(error.getMessage()),
                                                error.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        callback.execute();
                                        wzd.showSuccess(
                                                resources.constants().rollbackSuccessful(),
                                                messages.rollbackSucessful(name));
                                    }
                                });
                    });
            Wizard<PatchContext, PatchState> wizard = wb.build();
            wizard.show();
        });
    }


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
        public Completable call(FlowContext context) {

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
                opBuilder.param(OVERRIDE, patchContext.override.toArray(new String[0]));
            }
            if (patchContext.preserve != null) {
                opBuilder.param(PRESERVE, patchContext.preserve.toArray(new String[0]));
            }
            Operation operation = opBuilder.build();

            return dispatcher.execute(operation).toCompletable();
        }
    }
}
