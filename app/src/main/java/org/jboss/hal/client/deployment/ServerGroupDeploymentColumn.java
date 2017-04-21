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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.deployment.Deployment.Status;
import org.jboss.hal.client.deployment.DeploymentFunctions.AddServerGroupDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.AddUnmanagedDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.CheckDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.LoadContent;
import org.jboss.hal.client.deployment.DeploymentFunctions.LoadDeploymentsFromRunningServer;
import org.jboss.hal.client.deployment.DeploymentFunctions.ReadServerGroupDeployments;
import org.jboss.hal.client.deployment.DeploymentFunctions.UploadOrReplace;
import org.jboss.hal.client.deployment.dialog.AddUnmanagedDialog;
import org.jboss.hal.client.deployment.dialog.DeployContentDialog2;
import org.jboss.hal.client.deployment.wizard.NamesStep;
import org.jboss.hal.client.deployment.wizard.UploadContext;
import org.jboss.hal.client.deployment.wizard.UploadDeploymentStep;
import org.jboss.hal.client.deployment.wizard.UploadState;
import org.jboss.hal.config.Environment;
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
import org.jboss.hal.core.runtime.TopologyFunctions.RunningServersQuery;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.SuccessfulOutcome;
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
import static org.jboss.hal.client.deployment.Deployment.Status.OK;
import static org.jboss.hal.client.deployment.ServerGroupDeploymentColumn.SERVER_GROUP_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.wizard.UploadState.NAMES;
import static org.jboss.hal.client.deployment.wizard.UploadState.UPLOAD;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * The deployments of a server group.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.SERVER_GROUP_DEPLOYMENT)
@Requires(value = {CONTENT_ADDRESS, SERVER_GROUP_DEPLOYMENT_ADDRESS}, recursive = false)
public class ServerGroupDeploymentColumn extends FinderColumn<ServerGroupDeployment> {

    static final String SERVER_GROUP_DEPLOYMENT_ADDRESS = "/server-group=*/deployment=*";
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
    public ServerGroupDeploymentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final Places places,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final @Footer Provider<Progress> progress,
            final Resources resources) {

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
            Function[] functions = new Function[]{
                    new ReadServerGroupDeployments(environment, dispatcher, statementContext.selectedServerGroup()),
                    new RunningServersQuery(environment, dispatcher,
                            new ModelNode().set(SERVER_GROUP, statementContext.selectedServerGroup())),
                    new LoadDeploymentsFromRunningServer(environment, dispatcher)
            };

            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getException());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<ServerGroupDeployment> serverGroupDeployments = context
                                    .get(DeploymentFunctions.SERVER_GROUP_DEPLOYMENTS);
                            callback.onSuccess(serverGroupDeployments);
                        }
                    }, functions);

        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<ServerGroupDeployment>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final List<ServerGroupDeployment> result) {
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
            public Element getIcon() {
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
                AddressTemplate template = SERVER_GROUP_DEPLOYMENT_TEMPLATE
                        .replaceWildcards(statementContext.selectedServerGroup());
                actions.add(itemActionFactory.remove(Names.DEPLOYMENT, item.getName(),
                        template, SERVER_GROUP_DEPLOYMENT_TEMPLATE, ServerGroupDeploymentColumn.this));
                return actions;
            }
        });

        setPreviewCallback(item -> new ServerGroupDeploymentPreview(this, item, places, resources));

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.uploadAndDeploy(this, environment, dispatcher, eventBus, progress,
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

                    Function[] functions = {
                            new CheckDeployment(dispatcher, name),
                            new UploadOrReplace(environment, dispatcher, name, runtimeName, context.file, false),
                            new AddServerGroupDeployment(environment, dispatcher, name, runtimeName,
                                    statementContext.selectedServerGroup())
                    };
                    new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                            new Outcome<FunctionContext>() {
                                @Override
                                public void onFailure(final FunctionContext functionContext) {
                                    wzd.showError(resources.constants().deploymentError(),
                                            resources.messages().deploymentError(name), functionContext.getError());
                                }

                                @Override
                                public void onSuccess(final FunctionContext functionContext) {
                                    refresh(Ids.serverGroupDeployment(statementContext.selectedServerGroup(), name));
                                    wzd.showSuccess(resources.constants().deploymentSuccessful(),
                                            resources.messages().deploymentSuccessful(name),
                                            resources.messages().view(Names.DEPLOYMENT),
                                            cxt -> { /* nothing to do, content is already selected */ });
                                }
                            }, functions);
                })
                .build();
        wizard.show();
    }

    private void addDeploymentFromContentRepository() {
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                MessageEvent.fire(eventBus, Message.error(resources.messages().loadContentError(), context.getError()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
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
                                            return new Operation.Builder(ADD, resourceAddress)
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
        new Async<FunctionContext>(progress.get())
                .single(new FunctionContext(), outcome, new LoadContent(dispatcher));
    }

    private void addUnmanaged() {
        Metadata metadata = metadataRegistry.lookup(CONTENT_TEMPLATE);
        AddUnmanagedDialog dialog = new AddUnmanagedDialog(metadata, resources,
                (name, model) -> {
                    if (model != null) {
                        String serverGroup = statementContext.selectedServerGroup();
                        String runtimeName = model.get(RUNTIME_NAME).asString();
                        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                                new SuccessfulOutcome(eventBus, resources) {
                                    @Override
                                    public void onSuccess(final FunctionContext context) {
                                        refresh(Ids.serverGroupDeployment(serverGroup, name));
                                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                .addResourceSuccess(Names.UNMANAGED_DEPLOYMENT, name)));
                                    }
                                },
                                new AddUnmanagedDeployment(dispatcher, name, model),
                                new AddServerGroupDeployment(environment, dispatcher, name, runtimeName, serverGroup));
                    }
                });
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
        Operation op = new Operation.Builder(operation, address).build();
        ItemMonitor.startProgress(id);
        dispatcher.execute(op, result -> {
            ItemMonitor.stopProgress(id);
            refresh(RESTORE_SELECTION);
            MessageEvent.fire(eventBus, Message.success(message));
        });
    }
}
