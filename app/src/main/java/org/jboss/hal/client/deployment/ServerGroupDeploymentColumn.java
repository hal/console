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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.deployment.DeploymentTasks.AddServerGroupDeployment;
import org.jboss.hal.client.deployment.DeploymentTasks.AddUnmanagedDeployment;
import org.jboss.hal.client.deployment.DeploymentTasks.CheckDeployment;
import org.jboss.hal.client.deployment.DeploymentTasks.LoadContent;
import org.jboss.hal.client.deployment.DeploymentTasks.LoadDeploymentsFromRunningServer;
import org.jboss.hal.client.deployment.DeploymentTasks.ReadServerGroupDeployments;
import org.jboss.hal.client.deployment.DeploymentTasks.UploadOrReplace;
import org.jboss.hal.client.deployment.dialog.AddUnmanagedDialog;
import org.jboss.hal.client.deployment.dialog.DeployContentDialog2;
import org.jboss.hal.client.deployment.wizard.NamesStep;
import org.jboss.hal.client.deployment.wizard.UploadContext;
import org.jboss.hal.client.deployment.wizard.UploadDeploymentStep;
import org.jboss.hal.client.deployment.wizard.UploadState;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.deployment.Deployment.Status;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.deployment.ContentColumn.CONTENT_ADDRESS;
import static org.jboss.hal.client.deployment.ContentColumn.CONTENT_TEMPLATE;
import static org.jboss.hal.client.deployment.ServerGroupDeploymentColumn.SERVER_GROUP_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.wizard.UploadState.NAMES;
import static org.jboss.hal.client.deployment.wizard.UploadState.UPLOAD;
import static org.jboss.hal.core.deployment.Deployment.Status.OK;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.core.runtime.TopologyTasks.runningServers;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.pfIcon;

/** The deployments of a server group. */
@AsyncColumn(Ids.SERVER_GROUP_DEPLOYMENT)
@Requires(value = {CONTENT_ADDRESS, SERVER_GROUP_DEPLOYMENT_ADDRESS}, recursive = false)
public class ServerGroupDeploymentColumn extends FinderColumn<ServerGroupDeployment> {

    static final String SERVER_GROUP_DEPLOYMENT_ADDRESS = "/{selected.group}/deployment=*";
    static final AddressTemplate SERVER_GROUP_DEPLOYMENT_TEMPLATE = AddressTemplate
            .of(SERVER_GROUP_DEPLOYMENT_ADDRESS);

    private final Environment environment;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public ServerGroupDeploymentColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Environment environment,
            EventBus eventBus,
            Dispatcher dispatcher,
            Places places,
            CrudOperations crud,
            ServerActions serverActions,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            @Footer Provider<Progress> progress,
            Resources resources) {

        super(new FinderColumn.Builder<ServerGroupDeployment>(finder, Ids.SERVER_GROUP_DEPLOYMENT, Names.DEPLOYMENT)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .showCount()
                .withFilter());

        this.environment = environment;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.progress = progress;
        this.resources = resources;

        List<ColumnAction<ServerGroupDeployment>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<ServerGroupDeployment>(Ids.SERVER_GROUP_DEPLOYMENT_UPLOAD)
                .title(resources.constants().uploadNewDeployment())
                .handler(column -> uploadAndDeploy())
                .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))
                .build());
        addActions.add(new ColumnAction.Builder<ServerGroupDeployment>(Ids.SERVER_GROUP_DEPLOYMENT_ADD)
                .title(resources.constants().deployExistingContent())
                .handler(column -> addDeploymentFromContentRepository())
                .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))
                .build());
        addActions.add(new ColumnAction.Builder<ServerGroupDeployment>(Ids.SERVER_GROUP_DEPLOYMENT_UNMANAGED_ADD)
                .title(resources.messages().addResourceTitle(Names.UNMANAGED_DEPLOYMENT))
                .handler(column -> addUnmanaged())
                .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))
                .build());
        addColumnActions(Ids.SERVER_GROUP_DEPLOYMENT_ADD_ACTIONS, pfIcon("add-circle-o"), resources.constants().add(),
                addActions);
        addColumnAction(columnActionFactory.refresh(Ids.SERVER_GROUP_DEPLOYMENT_REFRESH));

        ItemsProvider<ServerGroupDeployment> itemsProvider = (context, callback) -> {
            List<Task<FlowContext>> tasks = new ArrayList<>();
            tasks.add(new ReadServerGroupDeployments(environment, dispatcher, statementContext.selectedServerGroup()));
            tasks.addAll(runningServers(environment, dispatcher,
                    properties(SERVER_GROUP, statementContext.selectedServerGroup())));
            tasks.add(new LoadDeploymentsFromRunningServer(environment, dispatcher));

            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new Outcome<FlowContext>() {
                        @Override
                        public void onError(FlowContext context, Throwable error) {
                            callback.onFailure(error);
                        }

                        @Override
                        public void onSuccess(FlowContext context) {
                            List<ServerGroupDeployment> serverGroupDeployments = context
                                    .get(DeploymentTasks.SERVER_GROUP_DEPLOYMENTS);
                            callback.onSuccess(serverGroupDeployments);
                        }
                    });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<ServerGroupDeployment>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<ServerGroupDeployment> result) {
                        // only running deployments w/ a reference server will show up in the breadcrumb dropdown
                        List<ServerGroupDeployment> deploymentsOnServer = result.stream()
                                .filter(ServerGroupDeployment::runningWithReferenceServer)
                                .collect(toList());
                        callback.onSuccess(deploymentsOnServer);
                    }
                }));

        setItemRenderer(item -> new ItemDisplay<ServerGroupDeployment>() {
            @Override
            public String getId() {
                return Ids.serverGroupDeployment(statementContext.selectedServerGroup(), item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getTooltip() {
                if (item.getDeployment() != null) {
                    if (item.getDeployment().getStatus() == Status.FAILED) {
                        return resources.constants().failed();
                    } else if (item.getDeployment().getStatus() == Status.STOPPED) {
                        return resources.constants().stopped();
                    } else if (item.getDeployment().getStatus() == OK) {
                        return resources.constants().activeLower();
                    } else {
                        return resources.constants().unknownState();
                    }
                } else {
                    return item.isEnabled() ? resources.constants().enabled() : resources.constants()
                            .disabled();
                }
            }

            @Override
            public HTMLElement getIcon() {
                if (item.getDeployment() != null) {
                    if (item.getDeployment().getStatus() == Status.FAILED) {
                        return Icons.error();
                    } else if (item.getDeployment().getStatus() == Status.STOPPED) {
                        return Icons.stopped();
                    } else if (item.getDeployment().getStatus() == OK) {
                        return Icons.ok();
                    } else {
                        return Icons.unknown();
                    }
                } else {
                    return item.isEnabled() ? Icons.ok() : Icons.disabled();
                }
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            public List<ItemAction<ServerGroupDeployment>> actions() {
                List<ItemAction<ServerGroupDeployment>> actions = new ArrayList<>();

                // view makes sense only for running deployments w/ a reference server
                if (item.runningWithReferenceServer()) {
                    actions.add(itemActionFactory.view(NameTokens.SERVER_GROUP_DEPLOYMENT,
                            Ids.SERVER_GROUP, statementContext.selectedServerGroup(),
                            Ids.DEPLOYMENT, item.getName()));
                }
                if (item.isEnabled()) {
                    actions.add(new ItemAction.Builder<ServerGroupDeployment>()
                            .title(resources.constants().disable())
                            .handler(itm -> disable(itm))
                            .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, UNDEPLOY))
                            .build());
                } else {
                    actions.add(new ItemAction.Builder<ServerGroupDeployment>()
                            .title(resources.constants().enable())
                            .handler(itm -> enable(itm))
                            .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, DEPLOY))
                            .build());
                }
                actions.add(new ItemAction.Builder<ServerGroupDeployment>()
                        .title(resources.constants().undeploy())
                        .handler(item -> crud.remove(Names.DEPLOYMENT, item.getName(), SERVER_GROUP_DEPLOYMENT_TEMPLATE,
                                () -> refresh(CLEAR_SELECTION)))
                        .constraint(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, REMOVE))
                        .build());
                return actions;
            }
        });

        setPreviewCallback(item -> new ServerGroupDeploymentPreview(this, item, places, resources, serverActions,
                environment));
    }

    @Override
    public void attach() {
        super.attach();
        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentTasks.uploadAndDeploy(this, environment, dispatcher, eventBus, progress,
                    event.dataTransfer.files, statementContext.selectedServerGroup(), resources));
        }
    }

    private void uploadAndDeploy() {
        Metadata metadata = metadataRegistry.lookup(SERVER_GROUP_DEPLOYMENT_TEMPLATE);
        Wizard<UploadContext, UploadState> wizard = new Wizard.Builder<UploadContext, UploadState>(
                resources.messages().addResourceTitle(resources.constants().content()), new UploadContext())

                .addStep(UPLOAD, new UploadDeploymentStep(resources))
                .addStep(NAMES, new NamesStep(environment, metadata, resources))

                .onBack((context, currentState) -> currentState == NAMES ? UPLOAD : null)
                .onNext((context, currentState) -> currentState == UPLOAD ? NAMES : null)

                .stayOpenAfterFinish()
                .onFinish((wzd, context) -> {
                    String name = context.name;
                    String runtimeName = context.runtimeName;
                    wzd.showProgress(resources.constants().deploymentInProgress(),
                            resources.messages().deploymentInProgress(name));

                    series(new FlowContext(progress.get()),
                            new CheckDeployment(dispatcher, name),
                            new UploadOrReplace(environment, dispatcher, name, runtimeName, context.file, false),
                            new AddServerGroupDeployment(environment, dispatcher, name, runtimeName,
                                    statementContext.selectedServerGroup()))
                            .subscribe(new Outcome<FlowContext>() {
                                @Override
                                public void onError(FlowContext context, Throwable error) {
                                    wzd.showError(resources.constants().deploymentError(),
                                            resources.messages().deploymentError(name), error.getMessage());
                                }

                                @Override
                                public void onSuccess(FlowContext context) {
                                    refresh(Ids.serverGroupDeployment(statementContext.selectedServerGroup(), name));
                                    wzd.showSuccess(resources.constants().deploymentSuccessful(),
                                            resources.messages().deploymentSuccessful(name),
                                            resources.messages().view(Names.DEPLOYMENT),
                                            cxt -> { /* nothing to do, content is already selected */ });
                                }
                            });
                })
                .build();
        wizard.show();
    }

    private void addDeploymentFromContentRepository() {
        Outcome<FlowContext> outcome = new Outcome<FlowContext>() {
            @Override
            public void onError(FlowContext context, Throwable error) {
                MessageEvent.fire(eventBus, Message.error(resources.messages().loadContentError(), error.getMessage()));
            }

            @Override
            public void onSuccess(FlowContext context) {
                // extract content which is not deployed on statementContext.selectedServerGroup()
                String serverGroup = statementContext.selectedServerGroup();
                List<Content> content = context.pop();
                List<Content> undeployedContentOnSelectedServerGroup = content.stream()
                        .filter(c -> !c.isDeployedTo(serverGroup))
                        .collect(toList());
                if (undeployedContentOnSelectedServerGroup.isEmpty()) {
                    MessageEvent.fire(eventBus,
                            Message.warning(resources.messages().allContentAlreadyDeployedToServerGroup(serverGroup)));
                } else {
                    new DeployContentDialog2(serverGroup, undeployedContentOnSelectedServerGroup, resources,
                            (sg, cnt, enable) -> {
                                List<Operation> operations = cnt.stream()
                                        .map(c -> {
                                            ResourceAddress resourceAddress = new ResourceAddress()
                                                    .add(SERVER_GROUP, serverGroup)
                                                    .add(DEPLOYMENT, c.getName());
                                            return new Operation.Builder(resourceAddress, ADD)
                                                    .param(RUNTIME_NAME, c.getRuntimeName())
                                                    .param(ENABLED, enable)
                                                    .build();
                                        })
                                        .collect(toList());
                                if (enable) {
                                    progress.get().reset();
                                    progress.get().tick();
                                }
                                dispatcher.execute(new Composite(operations), (CompositeResult cr) -> {
                                    if (enable) {
                                        progress.get().finish();
                                    }
                                    refresh(Ids.serverGroupDeployment(serverGroup, cnt.get(0).getName()));
                                    MessageEvent.fire(eventBus,
                                            Message.success(resources.messages().contentDeployed2(serverGroup)));
                                });
                            }).show();
                }
            }
        };
        series(new FlowContext(progress.get()), new LoadContent(dispatcher)).subscribe(outcome);
    }

    private void addUnmanaged() {
        Metadata metadata = metadataRegistry.lookup(CONTENT_TEMPLATE);
        AddUnmanagedDialog dialog = new AddUnmanagedDialog(metadata, resources,
                (name, model) -> {
                    if (model != null) {
                        String serverGroup = statementContext.selectedServerGroup();
                        String runtimeName = model.get(RUNTIME_NAME).asString();
                        series(new FlowContext(progress.get()),
                                new AddUnmanagedDeployment(dispatcher, name, model),
                                new AddServerGroupDeployment(environment, dispatcher, name, runtimeName, serverGroup))
                                .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        refresh(Ids.serverGroupDeployment(serverGroup, name));
                                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                .addResourceSuccess(Names.UNMANAGED_DEPLOYMENT, name)));
                                    }
                                });
                    }
                });
        dialog.getForm().<String>getFormItem(NAME).addValidationHandler(createUniqueValidation());
        dialog.show();
    }

    void enable(ServerGroupDeployment sgd) {
        enableDisable(sgd, DEPLOY, resources.messages().deploymentEnabledSuccess(sgd.getName()));
    }

    void disable(ServerGroupDeployment sgd) {
        enableDisable(sgd, UNDEPLOY, resources.messages().deploymentDisabledSuccess(sgd.getName()));
    }

    private void enableDisable(ServerGroupDeployment sgd, String operation, SafeHtml message) {
        String id = Ids.serverGroupDeployment(sgd.getServerGroup(), sgd.getName());
        ResourceAddress address = new ResourceAddress()
                .add(SERVER_GROUP, sgd.getServerGroup())
                .add(DEPLOYMENT, sgd.getName());
        Operation op = new Operation.Builder(address, operation).build();
        ItemMonitor.startProgress(id);
        dispatcher.execute(op, result -> {
            ItemMonitor.stopProgress(id);
            refresh(RESTORE_SELECTION);
            MessageEvent.fire(eventBus, Message.success(message));
        });
    }
}
