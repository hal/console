/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Provider;

import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.deployment.dialog.AddUnmanagedDialog;
import org.jboss.hal.client.deployment.dialog.CreateEmptyDialog;
import org.jboss.hal.client.deployment.dialog.DeployContentDialog1;
import org.jboss.hal.client.deployment.dialog.DeployContentDialog2;
import org.jboss.hal.client.deployment.wizard.DeploymentContext;
import org.jboss.hal.client.deployment.wizard.DeploymentState;
import org.jboss.hal.client.deployment.wizard.NamesStep;
import org.jboss.hal.client.deployment.wizard.UploadDeploymentStep;
import org.jboss.hal.client.shared.uploadwizard.UploadElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.ColumnActionHandler;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemActionHandler;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import rx.Single;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.client.deployment.wizard.DeploymentState.NAMES;
import static org.jboss.hal.client.deployment.wizard.DeploymentState.UPLOAD;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EMPTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FULL_REPLACE_DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEPLOY;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.spi.MessageEvent.fire;

public abstract class AbstractDeploymentColumn<T extends Content> extends FinderColumn<T> {

    static final String DEPLOYMENT_ADDRESS = "/deployment=*";
    static final AddressTemplate DEPLOYMENT_TEMPLATE = AddressTemplate.of(DEPLOYMENT_ADDRESS);

    static final String SERVER_GROUP_DEPLOYMENT_ADDRESS = "/server-group=*/deployment=*";
    static final AddressTemplate SERVER_GROUP_DEPLOYMENT_TEMPLATE = AddressTemplate.of(
            SERVER_GROUP_DEPLOYMENT_ADDRESS);

    static final String SELECTED_SERVER_GROUP_DEPLOYMENT_ADDRESS = "/{selected.group}/deployment=*";
    static final AddressTemplate SELECTED_SERVER_GROUP_DEPLOYMENT_TEMPLATE = AddressTemplate
            .of(SELECTED_SERVER_GROUP_DEPLOYMENT_ADDRESS);

    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final EventBus eventBus;
    private final ItemActionFactory itemActionFactory;
    private final MetadataRegistry metadataRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final StatementContext statementContext;

    private final ColumnProps columnProps;

    protected final ColumnProps STANDALONE_COLUMN;
    protected final ColumnProps CONTENT_COLUMN;
    protected final ColumnProps SERVER_GROUP_COLUMN;

    private final ColumnAction<T> COLUMN_UPLOAD_ACTION;
    private final ColumnAction<T> COLUMN_ADD_UNMANAGED_ACTION;
    private final ColumnAction<T> COLUMN_ADD_EMPTY_ACTION;
    private final ColumnAction<T> COLUMN_ADD_FROM_REPOSITORY_ACTION;

    private final ItemAction<T> ITEM_DISABLE_ACTION;
    private final ItemAction<T> ITEM_ENABLE_ACTION;
    private final ItemAction<T> ITEM_EXPLODE_ACTION;
    private final ItemAction<T> ITEM_EXPLODE_SUBS_ACTION;
    private final ItemAction<T> ITEM_REMOVE_ACTION;
    private final ItemAction<T> ITEM_REPLACE_ACTION;
    private final ItemAction<T> ITEM_DEPLOY_ACTION;
    private final ItemAction<T> ITEM_UNDEPLOY_ACTION;

    private enum ColumnType {
        STANDALONE, CONTENT, SERVER_GROUP
    }

    public AbstractDeploymentColumn(Builder<T> builder, ColumnActionFactory columnActionFactory,
            CrudOperations crud, Dispatcher dispatcher, Environment environment, EventBus eventBus,
            ItemActionFactory itemActionFactory, MetadataRegistry metadataRegistry,
            SecurityContextRegistry securityContextRegistry, Provider<Progress> progress,
            Resources resources, StatementContext statementContext) {
        super(builder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.eventBus = eventBus;
        this.itemActionFactory = itemActionFactory;
        this.metadataRegistry = metadataRegistry;
        this.securityContextRegistry = securityContextRegistry;
        this.progress = progress;
        this.resources = resources;
        this.statementContext = statementContext;

        STANDALONE_COLUMN = new ColumnProps() {
            {
                columnType = ColumnType.STANDALONE;
                resourceType = Names.DEPLOYMENT;
                template = DEPLOYMENT_TEMPLATE;
                getNamedId = (name, __) -> Ids.deployment(name);
                actionsId = Ids.DEPLOYMENT_ADD_ACTIONS;
                refreshId = Ids.DEPLOYMENT_REFRESH;
                actionDeploymentId = Ids.DEPLOYMENT_UPLOAD;
                actionDeploymentTitle = resources.constants().uploadDeployment();
                actionUnmanagedId = Ids.DEPLOYMENT_UNMANAGED_ADD;
                deploymentProgressTitle = resources.constants().deploymentInProgress();
                getDeploymentProgressText = resources.messages()::deploymentInProgress;
                deploymentSuccessTitle = resources.constants().deploymentSuccessful();
                getDeploymentSuccessText = resources.messages()::deploymentSuccessful;
                deploymentErrorTitle = resources.constants().deploymentError();
                getDeploymentErrorText = resources.messages()::deploymentError;
                noDeploymentText = resources.messages().noDeployment();
                replaceDeploymentTitle = resources.constants().replaceDeployment();
            }
        };
        CONTENT_COLUMN = new ColumnProps() {
            {
                columnType = ColumnType.CONTENT;
                resourceType = resources.constants().content();
                template = DEPLOYMENT_TEMPLATE;
                getNamedId = (name, __) -> Ids.content(name);
                actionsId = Ids.CONTENT_ADD_ACTIONS;
                refreshId = Ids.CONTENT_REFRESH;
                actionDeploymentId = Ids.CONTENT_ADD;
                actionDeploymentTitle = resources.constants().uploadContent();
                actionUnmanagedId = Ids.CONTENT_UNMANAGED_ADD;
                deploymentProgressTitle = resources.constants().uploadInProgress();
                getDeploymentProgressText = resources.messages()::uploadInProgress;
                deploymentSuccessTitle = resources.constants().uploadSuccessful();
                getDeploymentSuccessText = resources.messages()::uploadSuccessful;
                deploymentErrorTitle = resources.constants().uploadError();
                getDeploymentErrorText = resources.messages()::uploadError;
                noDeploymentText = resources.messages().noContent();
                replaceDeploymentTitle = resources.constants().replaceContent();
            }
        };
        SERVER_GROUP_COLUMN = new ColumnProps() {
            {
                columnType = ColumnType.SERVER_GROUP;
                resourceType = Names.DEPLOYMENT;
                template = SELECTED_SERVER_GROUP_DEPLOYMENT_TEMPLATE;
                getNamedId = Ids::serverGroupDeployment;
                actionsId = Ids.SERVER_GROUP_DEPLOYMENT_ADD_ACTIONS;
                refreshId = Ids.SERVER_GROUP_DEPLOYMENT_REFRESH;
                actionDeploymentId = Ids.SERVER_GROUP_DEPLOYMENT_UPLOAD;
                actionDeploymentTitle = resources.constants().uploadDeployment();
                actionUnmanagedId = Ids.SERVER_GROUP_DEPLOYMENT_UNMANAGED_ADD;
                deploymentProgressTitle = resources.constants().deploymentInProgress();
                getDeploymentProgressText = resources.messages()::deploymentInProgress;
                deploymentSuccessTitle = resources.constants().deploymentSuccessful();
                getDeploymentSuccessText = resources.messages()::deploymentSuccessful;
                deploymentErrorTitle = resources.constants().deploymentError();
                getDeploymentErrorText = resources.messages()::deploymentError;
                noDeploymentText = resources.messages().noDeployment();
                replaceDeploymentTitle = resources.constants().replaceDeployment();
            }
        };

        this.columnProps = getColumnProps();

        COLUMN_UPLOAD_ACTION = createColumnAction(columnProps.actionDeploymentId, columnProps.actionDeploymentTitle,
                column -> uploadDeployment());
        COLUMN_ADD_UNMANAGED_ACTION = createColumnAction(columnProps.actionUnmanagedId,
                resources.messages().addResourceTitle(Names.UNMANAGED_DEPLOYMENT), column -> addUnmanaged());
        COLUMN_ADD_EMPTY_ACTION = createColumnAction(Ids.DEPLOYMENT_EMPTY_CREATE, resources.constants().deploymentEmptyCreate(),
                column -> createEmpty());
        COLUMN_ADD_FROM_REPOSITORY_ACTION = createColumnAction(Ids.SERVER_GROUP_DEPLOYMENT_ADD,
                resources.constants().deployExistingContent(), column -> addDeploymentFromContentRepository());

        addColumnActions(columnActionFactory);

        ITEM_DISABLE_ACTION = createItemAction(resources.constants().disable(), this::disable, UNDEPLOY);
        ITEM_ENABLE_ACTION = createItemAction(resources.constants().enable(), this::enable, DEPLOY);
        ITEM_EXPLODE_ACTION = createItemAction(resources.constants().explode(), this::explode, EXPLODE);
        ITEM_REMOVE_ACTION = createItemAction(resources.constants().undeploy(), this::remove, REMOVE);
        ITEM_REPLACE_ACTION = createItemAction(resources.constants().replace(), this::replace, FULL_REPLACE_DEPLOYMENT, ADD);
        // standalone-only
        ITEM_EXPLODE_SUBS_ACTION = createItemAction(resources.constants().explodeSubdeployments(), this::explodeSubs, EXPLODE);
        // content-only
        ITEM_DEPLOY_ACTION = createItemAction(resources.constants().deploy(), this::deploy, SERVER_GROUP_DEPLOYMENT_TEMPLATE,
                ADD);
        ITEM_UNDEPLOY_ACTION = createItemAction(resources.constants().undeploy(), this::undeploy,
                SERVER_GROUP_DEPLOYMENT_TEMPLATE, REMOVE);
    }

    abstract ColumnProps getColumnProps();

    private ItemAction<T> createItemAction(String title, ItemActionHandler<T> handler, String constraint) {
        return new ItemAction.Builder<T>()
                .title(title)
                .handler(handler)
                .constraint(Constraint.executable(columnProps.template, constraint))
                .build();
    }

    private ItemAction<T> createItemAction(String title, ItemActionHandler<T> handler, AddressTemplate template,
            String constraint) {
        return new ItemAction.Builder<T>()
                .title(title)
                .handler(handler)
                .constraint(Constraint.executable(template, constraint))
                .build();
    }

    private ItemAction<T> createItemAction(String title, ItemActionHandler<T> handler, String constraint1, String constraint2) {
        return new ItemAction.Builder<T>()
                .title(title)
                .handler(handler)
                .constraint(Constraint.executable(AddressTemplate.ROOT, constraint1))
                .constraint(Constraint.executable(columnProps.template, constraint2))
                .build();
    }

    private ColumnAction<T> createColumnAction(String id, String title, ColumnActionHandler<T> handler) {
        return new ColumnAction.Builder<T>(id)
                .title(title)
                .handler(handler)
                .constraint(Constraint.executable(columnProps.template, ADD))
                .build();
    }

    private void addColumnActions(ColumnActionFactory columnActionFactory) {
        /**
         * <pre>
         * column actions
         *   standalone   - upload,                      add_unmanaged, add_empty
         *   content      - upload,                      add_unmanaged, add_empty
         *   server_group - upload, add_from_repository, add_unmanaged
         * </pre>
         */
        List<ColumnAction<T>> addActions = new ArrayList<>();
        addActions.add(COLUMN_UPLOAD_ACTION);
        if (columnProps.columnType == ColumnType.SERVER_GROUP) {
            addActions.add(COLUMN_ADD_FROM_REPOSITORY_ACTION);
        }
        addActions.add(COLUMN_ADD_UNMANAGED_ACTION);
        if (columnProps.columnType != ColumnType.SERVER_GROUP) {
            addActions.add(COLUMN_ADD_EMPTY_ACTION);
        }
        addColumnActions(columnProps.actionsId, pfIcon("add-circle-o"), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(columnProps.refreshId));
    }

    protected List<ItemAction<T>> getItemActions(T item) {
        /**
         * <pre>
         * item actions
         *   standalone    - view, enable/disable, replace, explode, explode_subs,     remove
         *   content       - view,                 replace, explode, deploy, download, remove/undeploy
         *   server_groups - view, enable/disable,                                     remove
         * </pre>
         */
        List<ItemAction<T>> actions = new ArrayList<>();

        // view
        switch (columnProps.columnType) {
            case STANDALONE:
                actions.add(itemActionFactory.view(NameTokens.DEPLOYMENT, Ids.DEPLOYMENT, item.getName()));
                break;
            case CONTENT:
                if (item.isManaged()) {
                    actions.add(itemActionFactory.view(NameTokens.BROWSE_CONTENT, CONTENT, item.getName()));
                }
                break;
            case SERVER_GROUP:
                if (((ServerGroupDeployment) item).runningWithReferenceServer()) {
                    actions.add(itemActionFactory.view(NameTokens.SERVER_GROUP_DEPLOYMENT,
                            Ids.SERVER_GROUP, statementContext.selectedServerGroup(),
                            Ids.DEPLOYMENT, item.getName()));
                }
                break;
            default:
        }

        // enable/disable
        if (columnProps.columnType != ColumnType.CONTENT) {
            if (item.isEnabled()) {
                actions.add(ITEM_DISABLE_ACTION);
            } else {
                actions.add(ITEM_ENABLE_ACTION);
            }
        }

        // replace + explode, explode_subs | deploy + download
        if (columnProps.columnType != ColumnType.SERVER_GROUP) {
            if (item.isManaged()) {
                actions.add(ITEM_REPLACE_ACTION);
            }
            boolean supported = ManagementModel.supportsExplodeDeployment(environment.getManagementVersion());
            boolean allowed = false;
            switch (columnProps.columnType) {
                case STANDALONE:
                    allowed = !item.isExploded() && !item.isEnabled();
                    break;
                case CONTENT:
                    allowed = item.getServerGroupDeployments().isEmpty() && !item.isExploded();
                    break;
                default:
            }
            if (supported && allowed) {
                actions.add(ITEM_EXPLODE_ACTION);
            }

            switch (columnProps.columnType) {
                case STANDALONE:
                    Deployment deployment = (Deployment) item;
                    if (supported && deployment.isExploded() && deployment.isEnabled() && deployment.hasSubdeployments()) {
                        actions.add(ITEM_EXPLODE_SUBS_ACTION);
                    }
                    break;
                case CONTENT:
                    actions.add(ITEM_DEPLOY_ACTION);
                    if (ManagementModel.supportsReadContentFromDeployment(environment.getManagementVersion())
                            && item.isManaged() && !item.isExploded()) {
                        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, item.getName());
                        Operation operation = new Operation.Builder(address, READ_CONTENT).build();
                        actions.add(new ItemAction.Builder<T>()
                                .title(resources.constants().download())
                                .href(dispatcher.downloadUrl(operation))
                                .constraint(Constraint.executable(columnProps.template, READ_CONTENT))
                                .build());
                    }
                    break;
                default:
            }
        }

        // remove/undeploy
        switch (columnProps.columnType) {
            case STANDALONE:
            case SERVER_GROUP:
                actions.add(ITEM_REMOVE_ACTION);
                break;
            case CONTENT:
                if (item.getServerGroupDeployments().isEmpty()) {
                    actions.add(ITEM_REMOVE_ACTION);
                } else {
                    actions.add(ITEM_UNDEPLOY_ACTION);
                }
                break;
            default:
        }

        return actions;
    }

    public DeploymentTasks.AddServerGroupDeployment getServerGroupDeploymentTask(String name, String runtimeName) {
        return new DeploymentTasks.AddServerGroupDeployment(environment, dispatcher, name, runtimeName,
                statementContext.selectedServerGroup());
    }

    /* basic deployment */
    protected void uploadDeployment() {
        Metadata metadata = metadataRegistry.lookup(columnProps.template);
        Wizard<DeploymentContext, DeploymentState> wizard = new Wizard.Builder<DeploymentContext, DeploymentState>(
                resources.messages().addResourceTitle(columnProps.resourceType), new DeploymentContext())

                .addStep(UPLOAD, new UploadDeploymentStep(resources))
                .addStep(NAMES, new NamesStep(environment, metadata, resources))

                .onBack((context, currentState) -> currentState == NAMES ? (context.name != null ? NAMES : UPLOAD) : null)
                .onNext((context, currentState) -> currentState == UPLOAD ? NAMES : null)

                .stayOpenAfterFinish()
                .onFinish((wzd, wzdContext) -> {
                    String name = wzdContext.name;
                    wzd.showProgress(columnProps.deploymentProgressTitle, columnProps.getDeploymentProgressText.apply(name));

                    Task<FlowContext> confirmReplacement = context -> Single.create(sub -> {
                        int result = context.peek();

                        if (result == 404) {
                            sub.onSuccess(context);
                            return;
                        }

                        wzd.showWarning(columnProps.replaceDeploymentTitle,
                                resources.messages().deploymentReplaceConfirmation(name), resources.constants().replace(),
                                __ -> sub.onSuccess(context), false);
                    }).toCompletable();

                    List<Task<FlowContext>> tasks = new ArrayList<>();
                    tasks.add(new DeploymentTasks.CheckDeployment(dispatcher, name));
                    tasks.add(confirmReplacement);
                    tasks.add(new DeploymentTasks.UploadOrReplace(environment, dispatcher, name, wzdContext.runtimeName,
                                    wzdContext.file, wzdContext.enabled));
                    if (columnProps.columnType == ColumnType.SERVER_GROUP) {
                        tasks.add(getServerGroupDeploymentTask(name, wzdContext.runtimeName));
                    }

                    series(new FlowContext(progress.get()), tasks)
                            .subscribe(new Outcome<FlowContext>() {
                                @Override
                                public void onError(FlowContext context, Throwable error) {
                                    wzd.showError(columnProps.deploymentErrorTitle,
                                            columnProps.getDeploymentErrorText.apply(name), error.getMessage());
                                }

                                @Override
                                public void onSuccess(FlowContext context) {
                                    refreshByName(name);
                                    refreshByName(name);
                                    wzd.showSuccess(columnProps.deploymentSuccessTitle,
                                            columnProps.getDeploymentSuccessText.apply(name),
                                            resources.messages().view(columnProps.resourceType),
                                            __ -> {
                                                /* nothing to do, deployment is already selected */
                                            });
                                }
                            });
                })
                .build();
        wizard.show();
    }

    /* unmanaged deployment */
    protected void addUnmanaged() {
        Metadata metadata = metadataRegistry.lookup(columnProps.template);
        AddUnmanagedDialog dialog = new AddUnmanagedDialog(metadata, resources,
                (name, model) -> {
                    if (model != null) {
                        String runtimeName = model.get(RUNTIME_NAME).asString();
                        List<Task<FlowContext>> tasks = new ArrayList<>();
                        tasks.add(new DeploymentTasks.AddUnmanagedDeployment(dispatcher, name, model));
                        if (columnProps.columnType == ColumnType.SERVER_GROUP) {
                            tasks.add(getServerGroupDeploymentTask(name, runtimeName));
                        }
                        series(new FlowContext(progress.get()), tasks)
                                .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        refreshByName(name);
                                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                .addResourceSuccess(Names.UNMANAGED_DEPLOYMENT, name)));
                                    }
                                });
                    }
                });
        dialog.getForm().<String>getFormItem(NAME).addValidationHandler(createUniqueValidation());
        dialog.show();
    }

    /* empty deployment */
    protected void createEmpty() {
        CreateEmptyDialog dialog = new CreateEmptyDialog(resources, name -> {
            ResourceAddress address = columnProps.template.resolve(statementContext, name);
            ModelNode contentNode = new ModelNode();
            contentNode.get(EMPTY).set(true);
            Operation operation = new Operation.Builder(address, ADD)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();
            dispatcher.execute(operation, result -> {
                refreshByName(name);
                MessageEvent.fire(eventBus, Message.success(resources.messages().deploymentEmptySuccess(name)));
            });
        });
        dialog.addValidationHandlerForNameItem(createUniqueValidation());
        dialog.show();
    }

    /* add from repository */
    protected void addDeploymentFromContentRepository() {
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
        series(new FlowContext(progress.get()), new DeploymentTasks.LoadContent(dispatcher)).subscribe(outcome);
    }

    /* replace */
    protected void replace(T item) {
        UploadElement uploadElement = new UploadElement(columnProps.noDeploymentText);
        Dialog dialog = new Dialog.Builder(columnProps.replaceDeploymentTitle)
                .add(uploadElement.element())
                .cancel()
                .primary(resources.constants().replace(), () -> {
                    boolean valid = uploadElement.validate();
                    if (valid) {
                        ReplaceDeploymentPanel replaceDeploymentPanel = new ReplaceDeploymentPanel();
                        replaceDeploymentPanel.on();
                        // To replace an existing deployment, the original name and runtime-name must be preserved.
                        List<Task<FlowContext>> tasks = asList(
                                new DeploymentTasks.CheckDeployment(dispatcher, item.getName()),
                                new DeploymentTasks.UploadOrReplace(environment, dispatcher, item.getName(),
                                        item.getRuntimeName(), uploadElement.getFiles().item(0), false));
                        series(new FlowContext(progress.get()), tasks)
                                .subscribe(new Outcome<FlowContext>() {
                                    @Override
                                    public void onError(FlowContext context, Throwable error) {
                                        replaceDeploymentPanel.off();
                                        MessageEvent.fire(eventBus, Message.error(
                                                resources.messages().contentReplaceError(item.getName()),
                                                error.getMessage()));
                                    }

                                    @Override
                                    public void onSuccess(FlowContext context) {
                                        refreshByName(item.getName());
                                        replaceDeploymentPanel.off();
                                        MessageEvent.fire(eventBus, Message.success(
                                                resources.messages().contentReplaceSuccess(item.getName())));
                                    }
                                });
                    }
                    return valid;
                })
                .build();
        dialog.show();
    }

    // other methods

    protected void enable(T deployment) {
        enableDisable(deployment, DEPLOY,
                resources.messages().deploymentEnabledSuccess(deployment.getName()),
                resources.messages().deploymentEnabledError(deployment.getName()));
    }

    protected void disable(T deployment) {
        enableDisable(deployment, UNDEPLOY,
                resources.messages().deploymentDisabledSuccess(deployment.getName()),
                resources.messages().deploymentDisabledError(deployment.getName()));
    }

    private void enableDisable(T deployment, String operation, SafeHtml successMessage,
            SafeHtml errorMessage) {
        boolean hasServerGroup = columnProps.columnType == ColumnType.SERVER_GROUP;
        String serverGroup = hasServerGroup ? ((ServerGroupDeployment) deployment).getServerGroup() : null;
        String id = columnProps.getNamedId.apply(deployment.getName(), serverGroup);
        ResourceAddress address = new ResourceAddress();
        if (hasServerGroup) {
            address.add(SERVER_GROUP, serverGroup);
        }
        address.add(DEPLOYMENT, deployment.getName());
        Operation op = new Operation.Builder(address, operation).build();
        ItemMonitor.startProgress(id);
        dispatcher.execute(op,
                result -> {
                    ItemMonitor.stopProgress(id);
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus, Message.success(successMessage));
                },
                (o, failure) -> {
                    ItemMonitor.stopProgress(id);
                    MessageEvent.fire(eventBus, Message.error(errorMessage, failure));
                });
    }

    protected void remove(T deployment) {
        crud.remove(Names.DEPLOYMENT, deployment.getName(), columnProps.template,
                () -> refresh(CLEAR_SELECTION));
    }

    private void explode(T deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation operation = new Operation.Builder(address, EXPLODE).build();
        dispatcher.execute(operation, result -> {
            refresh(RESTORE_SELECTION);
            MessageEvent
                    .fire(eventBus, Message.success(resources.messages().deploymentExploded(deployment.getName())));
        });
    }

    private void explodeSubs(T deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation disable = new Operation.Builder(address, UNDEPLOY).build();
        Composite op = new Composite(disable);

        String id = Ids.deployment(deployment.getName());
        ItemMonitor.startProgress(id);

        ((Deployment) deployment).getSubdeployments().forEach(
                subdeployment -> {
                    Operation explode = new Operation.Builder(address, EXPLODE).param(PATH, subdeployment.getName()).build();
                    op.add(explode);
                });

        dispatcher.execute(op, (Consumer<CompositeResult>) result -> {
            enable(deployment);
            MessageEvent
                    .fire(eventBus, Message.success(resources.messages().deploymentExploded(deployment.getName())));
        }, (operation, failure) -> {
            ItemMonitor.stopProgress(id);
            SafeHtml message = failure.contains("WFLYDR0015") ? resources.messages().deploymentSubAlreadyExploded()
                    : resources.messages().lastOperationException();
            MessageEvent.fire(eventBus, Message.error(message, failure));
        });
    }

    void deploy(Content content) {
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SERVER_GROUP)
                .build();
        dispatcher.execute(operation, result -> {
            Set<String> serverGroupsWithoutContent = result.asList().stream()
                    .map(ModelNode::asString)
                    .collect(toSet());
            Set<String> serverGroupsWithContent = content.getServerGroupDeployments().stream()
                    .map(ServerGroupDeployment::getServerGroup)
                    .collect(toSet());
            serverGroupsWithoutContent.removeAll(serverGroupsWithContent);

            if (serverGroupsWithoutContent.isEmpty()) {
                MessageEvent.fire(eventBus, Message.warning(
                        resources.messages().contentAlreadyDeployedToAllServerGroups(content.getName())));

            } else {
                new DeployContentDialog1(content, serverGroupsWithoutContent, resources,
                        (cnt, serverGroups, enable) -> {
                            List<Operation> operations = serverGroups.stream()
                                    .map(serverGroup -> {
                                        ResourceAddress resourceAddress = new ResourceAddress()
                                                .add(SERVER_GROUP, serverGroup)
                                                .add(DEPLOYMENT, content.getName());
                                        return new Operation.Builder(resourceAddress, ADD)
                                                .param(RUNTIME_NAME, content.getRuntimeName())
                                                .param(ENABLED, enable)
                                                .build();
                                    })
                                    .collect(toList());
                            if (enable) {
                                ItemMonitor.startProgress(Ids.content(cnt.getName()));
                            }
                            dispatcher.execute(new Composite(operations), (CompositeResult cr) -> {
                                if (enable) {
                                    ItemMonitor.stopProgress(Ids.content(cnt.getName()));
                                }
                                refresh(CLEAR_SELECTION);
                                MessageEvent.fire(eventBus,
                                        Message.success(resources.messages().contentDeployed1(content.getName())));
                            });
                        }).show();
            }
        });
    }

    private void undeploy(T content) {
        if (!content.getServerGroupDeployments().isEmpty()) {
            Set<String> serverGroupsWithContent = content.getServerGroupDeployments().stream()
                    .map(ServerGroupDeployment::getServerGroup)
                    .collect(toSet());
            new DeployContentDialog1(content, serverGroupsWithContent, resources, (cnt, serverGroups) -> {
                List<Operation> operations = serverGroups.stream()
                        .map(serverGroup -> {
                            ResourceAddress resourceAddress = new ResourceAddress()
                                    .add(SERVER_GROUP, serverGroup)
                                    .add(DEPLOYMENT, content.getName());
                            return new Operation.Builder(resourceAddress, REMOVE).build();
                        })
                        .collect(toList());
                dispatcher.execute(new Composite(operations), (CompositeResult cr) -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().contentUndeployed(content.getName())));
                });
            }).show();

        } else {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().undeployedContent(content.getName())));
        }
    }

    void undeploy(T content, String serverGroup) {
        ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup)
                .add(DEPLOYMENT, content.getName());
        Operation operation = new Operation.Builder(address, REMOVE).build();
        dispatcher.execute(operation, result -> {
            refresh(RESTORE_SELECTION);
            fire(eventBus, Message.success(
                    resources.messages().contentUndeployedFromServerGroup(content.getName(), serverGroup)));
        });
    }

    private void refreshByName(String name) {
        refresh(columnProps.getNamedId.apply(name, statementContext.selectedServerGroup()));
    }

    @Override
    public void attach() {
        super.attach();
        Constraints deployConstraints = Constraints.and(
                Constraint.executable(AddressTemplate.ROOT, FULL_REPLACE_DEPLOYMENT),
                Constraint.executable(columnProps.template, ADD));
        if (JsHelper.supportsAdvancedUpload() &&
                AuthorisationDecision.from(environment, securityContextRegistry).isAllowed(deployConstraints)) {
            String serverGroup = columnProps.columnType == ColumnType.SERVER_GROUP ? statementContext.selectedServerGroup()
                    : null;
            setOnDrop(event -> DeploymentTasks.upload(this, environment, dispatcher, eventBus, progress,
                    event.dataTransfer.files, serverGroup, resources));
        }
    }

    static class ColumnProps {
        ColumnType columnType;
        String resourceType;
        AddressTemplate template;

        BiFunction<String, String, String> getNamedId;
        String actionsId;
        String refreshId;

        // column actions - uploadDeploymentAction
        String actionDeploymentId;
        String actionDeploymentTitle;
        // column actions - addUnmanagedDeploymentAction
        String actionUnmanagedId;

        // deployment wizard - progress
        String deploymentProgressTitle;
        Function<String, SafeHtml> getDeploymentProgressText;
        // deployment wizard - showSuccess
        String deploymentSuccessTitle;
        Function<String, SafeHtml> getDeploymentSuccessText;
        // deployment wizard - showError
        String deploymentErrorTitle;
        Function<String, SafeHtml> getDeploymentErrorText;
        // replace dialog
        SafeHtml noDeploymentText;
        String replaceDeploymentTitle;
    }
}
