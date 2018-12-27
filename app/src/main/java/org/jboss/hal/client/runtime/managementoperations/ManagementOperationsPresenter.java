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
package org.jboss.hal.client.runtime.managementoperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;
import rx.Completable;
import rx.Single;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.meta.token.NameTokens.MANAGEMENT_OPERATIONS;

public class ManagementOperationsPresenter extends
        ApplicationFinderPresenter<ManagementOperationsPresenter.MyView, ManagementOperationsPresenter.MyProxy> {

    private static final String EQ = "=";
    private static final String WILDCARD = "*";
    private static final String WFLYDM_0089 = "WFLYDM0089";
    private AddressTemplate MGMT_OPERATIONS_TEMPLATE = AddressTemplate.of("/core-service=management/service=management-operations");
    public static final String MANAGEMENT_OPERATIONS_ADDRESS = "{selected.host}/core-service=management/service=management-operations";
    private static final String ACTIVE_OPERATIONS_ADDRESS = "{selected.host}/core-service=management/service=management-operations/active-operation=*";
    public static final AddressTemplate MANAGEMENT_OPERATIONS_TEMPLATE = AddressTemplate.of(MANAGEMENT_OPERATIONS_ADDRESS);
    static final AddressTemplate ACTIVE_OPERATIONS_TEMPLATE = AddressTemplate.of(ACTIVE_OPERATIONS_ADDRESS);

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private Provider<Progress> progress;
    private final Resources resources;
    private EventBus eventBus;
    private Environment environment;

    @Inject
    public ManagementOperationsPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Environment environment,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.eventBus = eventBus;
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.progress = progress;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        if (environment.isStandalone()) {
            // in standalone mode, the Management Operations option is on a drop down as the StandaloneServerColumn
            // generic type is Server, doesn't accomodate a static option to add the Management Operations
            // and changing the column to a StaticItemColumn would require too much change for little return
            return finderPathFactory.runtimeServerPath()
                    .append(Ids.RUNTIME_SUBSYSTEM, MANAGEMENT_OPERATIONS,
                            resources.constants().monitor(), Names.MANAGEMENT_OPERATIONS);
        } else {
            return new FinderPath()
                    .append(Ids.DOMAIN_BROWSE_BY, Ids.MANAGEMENT_OPERATIONS,
                            Names.RUNTIME, Names.MANAGEMENT_OPERATIONS);
        }
    }

    public StatementContext getStatementContext() {
        return statementContext;
    }

    @Override
    protected void reload() {
        if (environment.isStandalone()) {

            ResourceAddress addressFindNP = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
            Operation operationFindNP = new Operation.Builder(addressFindNP, FIND_NON_PROGRESSING_OPERATION)
                    .build();

            ResourceAddress addressMO = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
            Operation operationMO = new Operation.Builder(addressMO, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, ACTIVE_OPERATION)
                    .build();

            dispatcher.execute(new Composite(operationFindNP, operationMO), (CompositeResult result) -> {
                ModelNode resultNP = result.step(0).get(RESULT);
                ModelNode resultOperations = result.step(1).get(RESULT);
                final String nonProgressingId = resultNP.isDefined() ? resultNP.asString() : null;

                List<ManagementOperations> activeOps = asNamedNodes(resultOperations.asPropertyList()).stream()
                        .map(ManagementOperations::new)
                        .peek(activeOp -> {
                            if (nonProgressingId != null && nonProgressingId.equals(activeOp.getName())) {
                                activeOp.setAsNonProgressing();
                            }
                        })
                        .collect(toList());

                getView().update(activeOps);
            });
        } else {

            // return available hosts, to later call a find-non-progressing-operation on each host
            Task<FlowContext> hostsTask = context -> {
                ResourceAddress address = new ResourceAddress();
                Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, HOST)
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            List<String> hosts = result.asList().stream()
                                    .map(ModelNode::asString)
                                    .collect(toList());
                            context.set(HOSTS, hosts);
                        })
                        .toCompletable();
            };

            // return running servers, to later call a find-non-progressing-operation on each runtime server
            Task<FlowContext> serversTask = context -> {
                // /host=*/server=*:query(select=[host,name],where={server-state=running})
                ResourceAddress address = new ResourceAddress()
                        .add(HOST, WILDCARD)
                        .add(SERVER, WILDCARD);
                Operation operation = new Operation.Builder(address, QUERY)
                        .param(SELECT, new ModelNode().add(HOST).add(NAME))
                        .param(WHERE, new ModelNode().set(SERVER_STATE, "running"))
                        .build();
                return dispatcher.execute(operation)
                        .doOnSuccess(result -> {
                            List<String> servers = Collections.emptyList();
                            if (result != null && result.isDefined()) {
                                servers = result.asList().stream()
                                        .map(r -> hostServerAddress(r.get(RESULT)))
                                        .collect(toList());
                            }
                            context.set("servers", servers);
                        })
                        .toCompletable();
            };

            // call find-non-progressing-operation and read-resource of active operations
            // on each host and server
            Task<FlowContext> findNonProgressingTask = context -> {

                List<String> hosts = context.get(HOSTS);
                List<String> servers = context.get("servers");

                Composite composite = new Composite();
                for (String host : hosts) {
                    ResourceAddress address = new ResourceAddress().add(HOST, host)
                            .add(CORE_SERVICE, MANAGEMENT)
                            .add(SERVICE, MANAGEMENT_OPERATIONS);
                    Operation operation = new Operation.Builder(address, FIND_NON_PROGRESSING_OPERATION).build();
                    composite.add(operation);

                    ResourceAddress ad = new ResourceAddress(address.clone()).add(ACTIVE_OPERATION, WILDCARD);
                    Operation operationMO = new Operation.Builder(ad, READ_RESOURCE_OPERATION)
                            .build();
                    composite.add(operationMO);
                }
                if (!servers.isEmpty()) {
                    for (String server : servers) {
                        ResourceAddress address = AddressTemplate.of(server)
                                .append(MGMT_OPERATIONS_TEMPLATE)
                                .resolve(statementContext);
                        Operation findOp = new Operation.Builder(address, FIND_NON_PROGRESSING_OPERATION).build();
                        composite.add(findOp);

                        ResourceAddress ad = new ResourceAddress(address.clone()).add(ACTIVE_OPERATION, WILDCARD);
                        Operation operation = new Operation.Builder(ad, READ_RESOURCE_OPERATION).build();
                        composite.add(operation);
                    }
                }
                return dispatcher.execute(composite)
                        .doOnSuccess(response -> {
                            List<String> nonProgressingOps = new ArrayList<>();
                            List<ManagementOperations> ops = new ArrayList<>();
                            for (ModelNode r : response) {
                                ModelNode result = r.get(RESULT);
                                if (result != null && result.isDefined()) {
                                    ModelType type = result.getType();
                                    // if model is LIST it is the list of active operations
                                    if (ModelType.LIST.equals(type)) {
                                        for (ModelNode op : result.asList()) {
                                            ModelNode opResult = op.get(RESULT);
                                            // the result has two addresses
                                            // 1) the active-operation address itself, example
                                            //  /host=master/server=server-three/core-service=management/service=management-operations/active-operation=1940701884
                                            // 2) the resource address
                                            //  /host=master/server=server-three/subsystem=elytron/filesystem-realm=file1
                                            // the active-operation address should be store to later use it to cancel, if needed
                                            // the resource address is displayed to the user
                                            ModelNode activeOpAddress = op.get(ADDRESS);
                                            opResult.get(HAL_ACTIVE_OP_ADDRESS).set(activeOpAddress);
                                            String opId = null;
                                            List<Property> activeOperationAddressList = activeOpAddress.asPropertyList();
                                            for (Property p : activeOperationAddressList) {
                                                if (p.getName().equals(ACTIVE_OPERATION)) {
                                                    opId = p.getValue().asString();
                                                }
                                                // the result doesn't show the full address of a running server
                                                // store the host and server to later show in the view
                                                if (p.getName().equals(HOST)) {
                                                    opResult.get(HAL_ACTIVE_ADDRESS_HOST).set(p.getValue().asString());
                                                }
                                                if (p.getName().equals(SERVER)) {
                                                    opResult.get(HAL_ACTIVE_ADDRESS_SERVER).set(p.getValue().asString());
                                                }
                                            }
                                            NamedNode node = new NamedNode(opId, opResult);
                                            ManagementOperations activeOp = new ManagementOperations(node);
                                            ops.add(activeOp);
                                        }
                                    } else {
                                        nonProgressingOps.add(result.asString());
                                    }
                                }
                            }
                            // if there are non progressing operations, mark them in the list
                            if (!nonProgressingOps.isEmpty()) {
                                Collections.sort(nonProgressingOps);
                                for (ManagementOperations mop : ops) {
                                    if (nonProgressingOps.indexOf(mop.getName()) > -1) {
                                        mop.setAsNonProgressing();
                                    }
                                }
                            }
                            context.set("active-operations", ops);
                        })
                        .toCompletable();
            };

            series(new FlowContext(progress.get()), hostsTask, serversTask, findNonProgressingTask)
                    .subscribe(new Outcome<FlowContext>() {
                        @Override
                        public void onError(FlowContext context, Throwable error) {
                            MessageEvent.fire(getEventBus(), Message.error(SafeHtmlUtils.fromString(
                                    "Error loading management operations: " + error.getMessage())));
                        }

                        @Override
                        public void onSuccess(FlowContext context) {
                            List<ManagementOperations> ops = context.get("active-operations");
                            getView().update(ops);
                        }
                    });
        }
    }

    void cancelNonProgressingOperation() {
        if (environment.isStandalone()) {
            ResourceAddress address = MANAGEMENT_OPERATIONS_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, CANCEL_NON_PROGRESSING_OPERATION)
                    .build();
            dispatcher.execute(operation,
                    result -> {
                        MessageEvent.fire(eventBus,
                                Message.info(resources.messages().cancelledOperation(result.asString())));
                        reload();
                    },
                    (operation1, failure) -> {
                        // the cancel-non-progressing-operation returns an exception message if there are no
                        // operation to cancel, handle this a non error in HAL
                        if (failure.contains(WFLYDM_0089)) {
                            MessageEvent.fire(eventBus, Message.success(SafeHtmlUtils.fromString(failure)));
                        } else {
                            MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromString(failure)));
                        }
                        reload();
                    },
                    (operation1, ex) -> {
                        // the cancel-non-progressing-operation returns an exception message if there are no
                        // operation to cancel, handle this a non error in HAL
                        if (ex.getMessage().contains(WFLYDM_0089)) {
                            MessageEvent.fire(eventBus, Message.success(SafeHtmlUtils.fromString(ex.getMessage())));
                        } else {
                            MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromString(ex.getMessage())));
                        }
                        reload();
                    });
        } else {
            Composite composite = new Composite();
            // return running hosts, to later call a cancel-non-progressing-operation on each host
            ResourceAddress rootAddress = new ResourceAddress();
            Operation opHosts = new Operation.Builder(rootAddress, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, HOST)
                    .build();
            composite.add(opHosts);

            ResourceAddress address = new ResourceAddress()
                    .add(HOST, WILDCARD)
                    .add(SERVER, WILDCARD);
            Operation opRunningServers = new Operation.Builder(address, QUERY)
                    .param(SELECT, new ModelNode().add(HOST).add(NAME))
                    .param(WHERE, new ModelNode().set(SERVER_STATE, "running"))
                    .build();
            composite.add(opRunningServers);

            dispatcher.execute(composite, (CompositeResult compositeResult) -> {

                // available hosts
                List<String> hosts = compositeResult.step(0).get(RESULT).asList().stream()
                        .map(ModelNode::asString)
                        .collect(toList());

                // runing servers
                List<String> servers = Collections.emptyList();
                ModelNode result = compositeResult.step(1);
                if (result != null && result.isDefined()) {
                    servers = result.get(RESULT).asList().stream()
                            .map(r -> hostServerAddress(r.get(RESULT)))
                            .collect(toList());
                }

                // run each :cancel-non-progressing-operation on a specific task
                // because the :cancel-non-progressing-operation returns as a failure
                // for this case, continue to run the next task
                List<Task<FlowContext>> tasks = new ArrayList<>(hosts.size());
                for (String host : hosts) {
                    // call cancel-non-progressing-operation on each host
                    Task<FlowContext> task = context -> {
                        ResourceAddress hostAddress = new ResourceAddress().add(HOST, host)
                                .add(CORE_SERVICE, MANAGEMENT)
                                .add(SERVICE, MANAGEMENT_OPERATIONS);
                        return buildCancelOperation(hostAddress, context);
                    };
                    tasks.add(task);
                }

                for (String server : servers) {
                    // call cancel-non-progressing-operation on each server
                    Task<FlowContext> task = context -> {
                        ResourceAddress serverAddress = AddressTemplate.of(server)
                                .append(MGMT_OPERATIONS_TEMPLATE)
                                .resolve(statementContext);
                        return buildCancelOperation(serverAddress, context);
                    };
                    tasks.add(task);
                }

                series(new FlowContext(progress.get()), tasks)
                        .subscribe(new Outcome<FlowContext>() {
                            @Override
                            public void onError(FlowContext context, Throwable error) {
                                MessageEvent.fire(getEventBus(), Message.error(SafeHtmlUtils.fromString(
                                        "Error loading management operations: " + error.getMessage())));
                            }

                            @Override
                            public void onSuccess(FlowContext context) {
                                if (context.emptyStack()) {
                                    // display the standard message if there is no cancelled operation
                                    MessageEvent.fire(eventBus,
                                            Message.success(SafeHtmlUtils.fromString(context.get(WFLYDM_0089))));
                                } else {
                                    // display the cancelled non progressing operation ids
                                    List<String> canceledOps = new ArrayList<>();
                                    while (!context.emptyStack()) {
                                        canceledOps.add(context.pop());
                                    }
                                    String ids = Joiner.on(", ").join(canceledOps);
                                    MessageEvent.fire(eventBus,
                                            Message.success(resources.messages().cancelledOperation(ids)));
                                }
                                reload();
                            }
                        });
            });
        }
    }

    private Completable buildCancelOperation(ResourceAddress address, FlowContext context) {
        Operation operation = new Operation.Builder(address, CANCEL_NON_PROGRESSING_OPERATION).build();
        return dispatcher.execute(operation)
                .doOnSuccess(result -> {
                    if (result.isDefined()) {
                        context.push(result.asString());
                    }
                })
                .onErrorResumeNext(ex -> {
                    // the cancel-non-progressing-operation returns an exception message if there are no
                    // operation to cancel, handle this a non error in HAL
                    if (ex.getMessage().contains(WFLYDM_0089)) {
                        context.set(WFLYDM_0089, ex.getMessage());
                        return Single.just(new ModelNode());
                    } else {
                        return Single.error(ex);
                    }
                })
                .toCompletable();
    }

    public void cancel(ManagementOperations item) {
        DialogFactory.showConfirmation(resources.constants().cancelActiveOperation(),
                resources.messages().cancelActiveOperation(item.getName()),
                () -> {
                    ResourceAddress address;
                    if (environment.isStandalone()) {
                        address = ACTIVE_OPERATIONS_TEMPLATE.resolve(statementContext, item.getName());
                    } else {
                        address = new ResourceAddress(item.get(HAL_ACTIVE_OP_ADDRESS));
                    }
                    Operation operation = new Operation.Builder(address, CANCEL_OPERATION).build();
                    dispatcher.execute(operation, result -> reload());
                });
    }

    private String hostServerAddress(ModelNode model) {
        return HOST + EQ + model.get(HOST).asString() + "/" + SERVER + EQ + model.get(NAME).asString();
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(MANAGEMENT_OPERATIONS)
    @Requires({MANAGEMENT_OPERATIONS_ADDRESS, ACTIVE_OPERATIONS_ADDRESS})
    public interface MyProxy extends ProxyPlace<ManagementOperationsPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ManagementOperationsPresenter> {
        void update(List<ManagementOperations> activeOperations);
    }
    // @formatter:on
}
