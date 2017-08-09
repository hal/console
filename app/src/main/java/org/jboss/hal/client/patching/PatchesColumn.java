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
package org.jboss.hal.client.patching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.patching.wizard.PatchContentStep;
import org.jboss.hal.client.patching.wizard.PatchContext;
import org.jboss.hal.client.patching.wizard.PatchNamesStep;
import org.jboss.hal.client.patching.wizard.PatchState;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Dialog.PRIMARY_POSITION;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.MEDIUM;
import static org.jboss.hal.client.patching.wizard.PatchState.NAMES;
import static org.jboss.hal.client.patching.wizard.PatchState.UPLOAD;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;
import static org.jboss.hal.resources.Ids.ADD_SUFFIX;
import static org.jboss.hal.resources.Ids.PATCHES_AGEOUT;

/**
 * @author Claudio Miranda
 */
@Column(Ids.PATCHING)
public class PatchesColumn extends FinderColumn<ModelNode> {

    static class UploadPatch implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private EventBus eventBus;
        private StatementContext statementContext;
        private Resources resources;
        private PatchContext patchContext;

        UploadPatch(final EventBus eventBus, final StatementContext statementContext, final Dispatcher dispatcher,
                final Resources resources, final PatchContext patchContext) {
            this.eventBus = eventBus;
            this.statementContext = statementContext;
            this.dispatcher = dispatcher;
            this.resources = resources;
            this.patchContext = patchContext;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
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
                    result -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().patchSucessfullyApplied(patchContext.file.name)));
                        control.proceed();
                    },

                    (op, failure) -> {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().patchAddError(patchContext.file.name, failure)));
                        control.proceed();
                    },

                    (op, exception) -> {
                        MessageEvent.fire(eventBus, Message.error(
                                resources.messages().patchAddError(patchContext.file.name, exception.getMessage())));
                        control.proceed();
                    });
        }
    }

    static final AddressTemplate PATCHING_TEMPLATE = AddressTemplate.of(SELECTED_HOST, "core-service=patching");

    private EventBus eventBus;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private Environment environment;
    private HostActions hostActions;
    private MetadataProcessor metadataProcessor;
    private ServerActions serverActions;
    private Provider<Progress> progress;
    private Resources resources;
    // flag set in the restart servers dialog
    private boolean restartServers;
    private BlockingDialog restartServersDialog;

    @Inject
    public PatchesColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Environment environment,
            final HostActions hostActions,
            final MetadataProcessor metadataProcessor,
            final ServerActions serverActions,
            @Footer final Provider<Progress> progress,
            final ColumnActionFactory columnActionFactory,
            final Resources resources) {

        super(new Builder<ModelNode>(finder, Ids.PATCHING, Names.PATCHES)

                .columnAction(columnActionFactory.refresh(Ids.PATCHES_REFRESH))

                .itemsProvider((context, callback) -> {
                    ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, SHOW_HISTORY_OPERATION).build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList()));
                })
                .onPreview(PatchesPreview::new)
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.environment = environment;
        this.hostActions = hostActions;
        this.metadataProcessor = metadataProcessor;
        this.serverActions = serverActions;
        this.progress = progress;
        this.resources = resources;

        setItemRenderer(item -> new ItemDisplay<ModelNode>() {
            @Override
            public String getId() {
                return Ids.build(HOST, PATCHING, item.get(PATCH_ID).asString());
            }

            @Override
            public String getTitle() {
                return item.get(PATCH_ID).asString();
            }

            @Override
            public List<ItemAction<ModelNode>> actions() {

                List<ItemAction<ModelNode>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<ModelNode>()
                        .title(resources.constants().rollback())
                        .handler(item1 -> rollback(item1.get(PATCH_ID).asString()))
                        .constraint(Constraint.executable(PATCHING_TEMPLATE, ROLLBACK_OPERATION))
                        .build());

                return actions;
            }
        });

        ColumnAction<ModelNode> applyPatchAction = new ColumnAction.Builder<ModelNode>(Ids.PATCH_ADD)
                .element(columnActionFactory.addButton(Names.PATCH))
                .handler(column -> applyPatch())
                .build();
        ColumnAction<ModelNode> ageoutAction = new ColumnAction.Builder<ModelNode>(
                Ids.build(HOSTS, PATCHES_AGEOUT, ADD_SUFFIX))
                .element(columnActionFactory.addButton(resources.messages().cleanPatchHistory(), "fa fa-eraser"))
                .handler(column -> ageoutHistory())
                .build();
        addColumnAction(applyPatchAction);
        addColumnAction(ageoutAction);

    }

    private void rollback(final String patchId) {

        // check the host controller (or standalone server) for restart-required
        checkHostState(() ->
                // check the servers (domain mode only), advise to stop them before apply/rollback a patch
                checkServersState(() -> {

                    metadataProcessor.lookup(PATCHING_TEMPLATE, progress.get(),
                            new SuccessfulMetadataCallback(eventBus, resources) {

                                @Override
                                public void onMetadata(final Metadata metadata) {
                                    ModelNode model = new ModelNode();
                                    model.get(PATCH_ID).set(patchId);
                                    ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
                                    Metadata operationMetadata = metadata.forOperation(ROLLBACK_OPERATION);
                                    String id = Ids.build(Ids.HOST, statementContext.selectedHost(), CORE_SERVICE, PATCHING, patchId, ROLLBACK_OPERATION);
                                    Form<ModelNode> form = new ModelNodeForm.Builder<>(id, operationMetadata)
                                            .unsorted()
                                            .build();
                                    form.getFormItem(PATCH_ID).setEnabled(false);
                                    Dialog dialog = new Dialog.Builder(resources.constants().rollback())
                                            .add(form.asElement())
                                            .closeIcon(true)
                                            .closeOnEsc(true)
                                            .primary(resources.constants().rollback(), () -> {
                                                if (form.save()) {
                                                    ModelNode payload = form.getModel();
                                                    // reset-configuration is a required attribute, if the user doesn't set it, meaning it should be false
                                                    // it will not be added into the payload, but we must forcibly set as false to satisfy the required=true metadata
                                                    if (!payload.hasDefined(RESET_CONFIGURATION)) {
                                                        payload.get(RESET_CONFIGURATION).set(false);
                                                    }
                                                    String idProgress = Ids.build(Ids.HOST, statementContext.selectedHost(), CORE_SERVICE, PATCHING, patchId, "rollback-progress");
                                                    ItemMonitor.startProgress(idProgress);
                                                    Operation operation = new Operation.Builder(address, ROLLBACK_OPERATION)
                                                            .payload(payload)
                                                            .build();
                                                    dispatcher.execute(operation, result2 -> {
                                                        ItemMonitor.stopProgress(idProgress);
                                                        MessageEvent.fire(eventBus,
                                                                Message.success(resources.messages()
                                                                        .patchSucessfullyRemoved(patchId)));
                                                        refresh(RESTORE_SELECTION);
                                                    }, (operation1, failure) -> {
                                                        ItemMonitor.stopProgress(idProgress);
                                                        MessageEvent.fire(eventBus,
                                                                Message.error(resources.messages()
                                                                        .patchRemovedError(failure)));
                                                        refresh(RESTORE_SELECTION);
                                                    }, (operation1, exception) -> {
                                                        ItemMonitor.stopProgress(idProgress);
                                                        MessageEvent.fire(eventBus,
                                                                Message.error(resources.messages()
                                                                        .patchRemovedError(exception.getMessage())));
                                                        refresh(RESTORE_SELECTION);
                                                    });
                                                    return true;
                                                }
                                                return false;
                                            })
                                            .cancel()
                                            .build();
                                    dialog.registerAttachable(form);
                                    dialog.show();
                                    form.edit(model);
                                }
                            });
                }));
    }

    private void ageoutHistory() {


        metadataProcessor
                .lookup(PATCHING_TEMPLATE, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
                    @Override
                    public void onMetadata(final Metadata metadata) {
                        ResourceAddress address = PATCHING_TEMPLATE.resolve(statementContext);
                        Metadata operationMetadata = metadata.forOperation(AGEOUT_HISTORY_OPERATION);
                        Messages messages = resources.messages();
                        // prepend the ageout-history description to let user know the intended action.
                        SafeHtml message = messages
                                .cleanPatchHistoryQuestion(operationMetadata.getDescription().getDescription());

                        DialogFactory.showConfirmation(messages.cleanPatchHistory(), message, () -> {
                            Operation operation = new Operation.Builder(address, AGEOUT_HISTORY_OPERATION).build();
                            dispatcher.execute(operation, result -> {
                                MessageEvent.fire(eventBus, Message.success(messages.cleanPatchHistorySuccess()));
                                refresh(RESTORE_SELECTION);
                            }, (operation1, failure) -> {
                                MessageEvent.fire(eventBus, Message.error(messages.cleanPatchHistoryFailure(failure)));
                                refresh(RESTORE_SELECTION);
                            });
                        });
                    }
                });
    }

    private void applyPatch() {

        // check the host controller for restart-required
        checkHostState(() ->
            // check the servers, advise to stop them before apply/rollback a patch
            checkServersState(() -> {

                metadataProcessor.lookup(PATCHING_TEMPLATE, progress.get(),
                    new SuccessfulMetadataCallback(eventBus, resources) {
                        @Override
                        public void onMetadata(final Metadata metadata) {
                            Metadata metadataOp = metadata.forOperation(PATCH);
                            final Messages messages = resources.messages();
                            Wizard<PatchContext, PatchState> wizard = new Wizard.Builder<PatchContext, PatchState>(messages.addResourceTitle(Names.PATCH), new PatchContext())

                                .addStep(UPLOAD, new PatchContentStep(resources))
                                .addStep(NAMES, new PatchNamesStep(environment, metadataOp, resources))

                                .onBack((context, currentState) -> currentState == NAMES ? UPLOAD : null)
                                .onNext((context, currentState) -> currentState == UPLOAD ? NAMES : null)

                                .stayOpenAfterFinish()
                                .onFinish((wzd, context) -> {
                                    String name = context.file.name;
                                    wzd.showProgress(resources.constants().uploadInProgress(), messages.uploadInProgress(name));

                                    Function[] functions = {
                                            new UploadPatch(eventBus, statementContext, dispatcher, resources, context)
                                    };
                                    new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                                        new Outcome<FunctionContext>() {
                                            @Override
                                            public void onFailure(final FunctionContext functionContext) {
                                                wzd.showError(resources.constants().uploadError(), messages.uploadError(name),functionContext.getError());
                                            }

                                            @Override
                                            public void onSuccess(final FunctionContext functionContext) {
                                                refresh(Ids.content(name));
                                                wzd.showSuccess(resources.constants().uploadSuccessful(), messages.uploadSuccessful(name), messages.view(Names.CONTENT),
                                                        cxt -> { /* nothing to do, content is already selected */ });
                                            }
                                        }, functions);
                                })
                                .build();
                            wizard.show();
                        }
                    });

            }));
    }

    /**
     * Checks if the host or standalone server is in restart mode, if yes then asks user to restart host/server, as it
     * must be restarted before a patch can be installed or to call a rollback on an installed patch.
     *
     */
    private void checkHostState(Callback callback) {

        Messages messages = resources.messages();
        if (environment.isStandalone()) {
            Operation operation = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();

            dispatcher.execute(operation, result -> {
                Server.STANDALONE.addServerAttributes(result);
                if (Server.STANDALONE.needsRestart()) {
                    serverActions.restartStandalone(Server.STANDALONE, messages.patchRestartStandaloneQuestion());
                } else {
                    callback.execute();
                }
            });
        } else {
            ResourceAddress address = new ResourceAddress().add(HOST, statementContext.selectedHost());
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();

            dispatcher.execute(operation, result -> {

                Property prop = new Property(statementContext.selectedHost(), result);
                Host host = new Host(prop);
                if (host.needsRestart()) {
                    SafeHtml question = host.isDomainController()
                            ? messages.patchRestartDomainControllerQuestion(host.getName())
                            : messages.patchRestartHostControllerQuestion(host.getName());

                    hostActions.restart(host, question);
                } else {
                    callback.execute();
                }
            });
        }

    }

    /**
     * Checks if each servers of a host is stopped, if the server is started, asks the user to stop them.
     * It is a good practice to apply/rollback a patch to a stopped server to prevent application and internal services
     * from failing.
     */
    private void checkServersState(Callback callback) {

        if (environment.isStandalone()) {
            callback.execute();
        } else {

            String host = statementContext.selectedHost();
            ResourceAddress address = new ResourceAddress().add(HOST, host);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(CHILD_TYPE, SERVER_CONFIG)
                    .build();

            dispatcher.execute(operation, result -> {
                List<Property> servers = result.asPropertyList();
                List<String> serversString = new ArrayList<>();
                boolean anyServerStarted = false;
                for (Iterator<Property> iter = servers.iterator(); iter.hasNext(); ) {
                    Property serverProp = iter.next();
                    Server server = new Server(host, serverProp);
                    if (!server.isStopped()) {
                        serversString.add(serverProp.getName());
                        anyServerStarted = true;
                    } else {
                        iter.remove();
                    }
                }

                if (anyServerStarted) {
                    String serversList = Joiner.on(", ").join(serversString);
                    SafeHtml question = resources.messages().patchStopAllServersQuestion(serversList, host);

                    askRestartServers(resources.messages().patchStopAllServersTitle(), question, () -> {
                        if (restartServers) {
                            for (Property serverProp : servers) {
                                Server server = new Server(host, serverProp);
                                serverActions.stopNow(server);
                            }
                        }
                        // programmatically closes the dialog as a wizard dialog will open before this ResultCallback
                        // returns
                        restartServersDialog.close();
                        // the callback will show a wizard dialog
                        callback.execute();
                        // returns false as the previous close call already closed the dialog
                        return false;
                    });
                } else {
                    callback.execute();
                }
            });
        }
    }

    private void askRestartServers(String title, SafeHtml question, Dialog.ResultCallback callback) {

        String radioName = Ids.build(HOST, PATCHING, "choose-restart");
        HTMLElement content = div().css(formHorizontal)
                .add(p().innerHtml(question))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id(Ids.build(HOST, PATCHING, "ok"))
                                        .attr(UIConstants.NAME, radioName)
                                        .on(click, e -> {
                                            restartServers = true;
                                            restartServersDialog.getButton(PRIMARY_POSITION).disabled = false;
                                        })
                                        .asElement())
                                .add(span().innerHtml(resources.messages().patchStopServersDialogMessage1()))))
                .add(div().css(radio)
                        .add(label()
                                .add(input(InputType.radio)
                                        .id(Ids.build(HOST, PATCHING, "no"))
                                        .attr(UIConstants.NAME, radioName)
                                        .on(click, e -> {
                                            restartServers = false;
                                            restartServersDialog.getButton(PRIMARY_POSITION).disabled = false;
                                        })
                                        .asElement())
                                .add(span().innerHtml(resources.messages().patchStopServersDialogMessage2()))))
                .asElement();


        Dialog.Builder dialogBuilder = new Dialog.Builder(title)
                .primary(resources.constants().next(), callback)
                .secondary(resources.constants().cancel(), null)
                .size(MEDIUM)
                .add(content);

        // BlockingDialog used as we want to call close method
        restartServersDialog = new BlockingDialog(dialogBuilder);
        restartServersDialog.getButton(PRIMARY_POSITION).disabled = true;
        restartServersDialog.show();
    }

}
