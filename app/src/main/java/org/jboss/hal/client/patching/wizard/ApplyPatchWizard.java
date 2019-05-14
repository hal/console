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
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import rx.Completable;

import static org.jboss.hal.client.patching.PatchesColumn.PATCHING_TEMPLATE;
import static org.jboss.hal.client.patching.wizard.PatchState.CHECK_SERVERS;
import static org.jboss.hal.client.patching.wizard.PatchState.CONFIGURE;
import static org.jboss.hal.client.patching.wizard.PatchState.UPLOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class ApplyPatchWizard extends PatchWizard {

    public ApplyPatchWizard(Resources resources, Environment environment, Metadata metadata,
            StatementContext statementContext, Dispatcher dispatcher,
            Provider<Progress> progress, ServerActions serverActions, Callback callback) {
        super(resources, environment, metadata, statementContext, dispatcher, progress, serverActions, callback);
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
                    .addStep(CONFIGURE, new ConfigurationStep(metadata, resources))

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
                            default:
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
                            default:
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
        public Completable call(FlowContext context) {
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
                opBuilder.param(OVERRIDE, patchContext.override.toArray(new String[0]));
            }
            if (patchContext.preserve != null) {
                opBuilder.param(PRESERVE, patchContext.preserve.toArray(new String[0]));
            }
            Operation operation = opBuilder.build();
            operation.get(CONTENT).add().get(INPUT_STREAM_INDEX).set(0); //NON-NLS

            return dispatcher.upload(patchContext.file, operation).toCompletable();
        }
    }
}
