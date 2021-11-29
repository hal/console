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
package org.jboss.hal.client.runtime.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.client.runtime.managementinterface.ConstantHeadersPresenter;
import org.jboss.hal.client.runtime.managementinterface.HttpManagementInterfacePresenter;
import org.jboss.hal.client.shared.sslwizard.EnableSSLPresenter;
import org.jboss.hal.client.shared.sslwizard.EnableSSLWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.client.shared.sslwizard.AbstractConfiguration.SOCKET_BINDING_GROUP_TEMPLATE;
import static org.jboss.hal.core.runtime.TopologyTasks.reloadBlocking;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_MANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_ADD_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURE_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_SSL_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.START_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRUST_MANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.Ids.FORM;

public class StandaloneServerPresenter
        extends MbuiPresenter<StandaloneServerPresenter.MyView, StandaloneServerPresenter.MyProxy>
        implements EnableSSLPresenter, HttpManagementInterfacePresenter, ConstantHeadersPresenter, SupportsExpertMode {

    static final String ROOT_ADDRESS = "/";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);
    static final String HTTP_INTERFACE_ADDRESS = "/core-service=management/management-interface=http-interface";
    static final AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    private static final String DOT = ".";
    private static final AddressTemplate ELYTRON_TEMPLATE = AddressTemplate.of("/subsystem=elytron");

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final Provider<Progress> progress;
    private final Environment environment;
    private final Resources resources;

    @Inject
    public StandaloneServerPresenter(EventBus eventBus,
            StandaloneServerPresenter.MyView view,
            StandaloneServerPresenter.MyProxy proxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Dispatcher dispatcher,
            CrudOperations crud,
            @Footer Provider<Progress> progress,
            Environment environment,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.progress = progress;
        this.environment = environment;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ROOT_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath();
    }

    @Override
    public void reloadView() {
        reload();
    }

    @Override
    protected void reload() {
        dispatcher.execute(reloadComposite(), (CompositeResult result) -> {
            getView().updateAttributes(result.step(0).get(RESULT));
            getView().updateHttpInterface(result.step(1).get(RESULT), -1);
        });
    }

    void reloadHeaders(int pathIndex) {
        dispatcher.execute(reloadComposite(), (CompositeResult result) -> {
            getView().updateAttributes(result.step(0).get(RESULT));
            getView().updateHttpInterface(result.step(1).get(RESULT), pathIndex);
        });
    }

    private Composite reloadComposite() {
        Operation attrOperation = new Operation.Builder(resourceAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        ResourceAddress coreServiceAddress = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation mgmtInterfacesOp = new Operation.Builder(coreServiceAddress, READ_RESOURCE_OPERATION)
                .build();
        return new Composite(attrOperation, mgmtInterfacesOp);
    }

    public void save(String type, AddressTemplate template, Map<String, Object> changedValues) {
        crud.saveSingleton(type, template, changedValues, this::reload);
    }

    public void reset(String type, AddressTemplate template, Form<ModelNode> form, Metadata metadata) {
        crud.resetSingleton(type, template, form, metadata, this::reload);
    }

    @Override
    public void enableSslForManagementInterface() {
        // load some elytron resources in advance for later use in the wizard for form validation
        List<Task<FlowContext>> tasks = new ArrayList<>();

        Task<FlowContext> loadKeyStoreTask = loadResourceTask(KEY_STORE);
        tasks.add(loadKeyStoreTask);

        Task<FlowContext> loadKeyManagerTask = loadResourceTask(KEY_MANAGER);
        tasks.add(loadKeyManagerTask);

        Task<FlowContext> loadServerSslContextTask = loadResourceTask(SERVER_SSL_CONTEXT);
        tasks.add(loadServerSslContextTask);

        Task<FlowContext> loadTrustManagerTask = loadResourceTask(TRUST_MANAGER);
        tasks.add(loadTrustManagerTask);

        series(new FlowContext(progress.get()), tasks).subscribe(
                new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        Map<String, List<String>> existingResources = new HashMap<>();
                        flowContext.keys().forEach(key -> existingResources.put(key, flowContext.get(key)));

                        new EnableSSLWizard.Builder(existingResources, resources, getEventBus(), statementContext,
                                dispatcher, progress, StandaloneServerPresenter.this, environment)
                                .build()
                                .show();
                    }
                });
    }

    private Task<FlowContext> loadResourceTask(String resourceName) {
        return context -> {
            ResourceAddress address = ELYTRON_TEMPLATE.resolve(statementContext);
            Operation keyStoreOp = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, resourceName)
                    .build();

            return dispatcher.execute(keyStoreOp)
                    .doOnSuccess(result -> {
                        List<String> res = result.asList().stream()
                                .map(ModelNode::asString)
                                .collect(Collectors.toList());
                        if (!res.isEmpty()) {
                            context.set(resourceName, res);
                        }
                    })
                    .toCompletable();
        };
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void disableSslForManagementInterface() {
        Constants constants = resources.constants();
        String serverName = environment.isStandalone() ? Names.STANDALONE_SERVER : Names.DOMAIN_CONTROLLER;
        String label = constants.reload() + " " + serverName;
        SwitchItem reload = new SwitchItem(RELOAD, label);
        reload.setExpressionAllowed(false);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(RELOAD, FORM), Metadata.empty())
                .unboundFormItem(reload)
                .build();
        form.attach();
        HTMLElement formElement = form.element();

        ModelNode model = new ModelNode();
        model.setEmptyObject();
        form.edit(model);
        ResourceAddress httpAddress = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);

        DialogFactory.buildConfirmation(constants.disableSSL(),
                        resources.messages().disableSSLManagementQuestion(serverName), formElement, Dialog.Size.MEDIUM, () -> {

                            List<Task<FlowContext>> tasks = new ArrayList<>();
                            // load the http-interface resource to get the port, there are differente attributes for
                            // standalone and domain mode.
                            Task<FlowContext> loadHttpInterface = flowContext -> {
                                Operation readHttpInterface = new Operation.Builder(httpAddress, READ_RESOURCE_OPERATION)
                                        .build();
                                return dispatcher.execute(readHttpInterface)
                                        .doOnSuccess(value -> {
                                            if (value.hasDefined(SOCKET_BINDING)) {
                                                // standalone mode uses a socket-binding for port
                                                // store the socket-binding name in the flow context and on a later call
                                                // read the socket-binding-group=<s-b-g>/socket-binding=<http-binding> to
                                                // retrieve the port number
                                                flowContext.set(SOCKET_BINDING, value.get(SOCKET_BINDING).asString());
                                            }
                                        })
                                        .toCompletable();

                            };
                            tasks.add(loadHttpInterface);

                            // if standalone mode, read the socket-binding-group=<s-b-g>/socket-binding=<http-binding>
                            // to retrieve the port number
                            Task<FlowContext> readHttpPortTask = flowContext -> {
                                Operation op = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                                        .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                                        .build();
                                return dispatcher.execute(op)
                                        .doOnSuccess(result -> {
                                            String sbg = result.asList().get(0).asString();
                                            String httpBinding = flowContext.get(SOCKET_BINDING);
                                            ResourceAddress address = SOCKET_BINDING_GROUP_TEMPLATE.resolve(statementContext,
                                                    sbg, httpBinding);
                                            Operation readPort = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                                                    .param(NAME, PORT)
                                                    .param(RESOLVE_EXPRESSIONS, true)
                                                    .build();
                                            dispatcher.execute(readPort,
                                                    portResult -> flowContext.set(PORT, portResult.asString()));
                                        })
                                        .toCompletable();
                            };
                            tasks.add(readHttpPortTask);

                            // as part of the disable ssl task, undefine the secure-socket-binding
                            // the attribute only exists in standalone mode
                            Task<FlowContext> undefSslTask = flowContext -> {
                                Operation op = new Operation.Builder(httpAddress, UNDEFINE_ATTRIBUTE_OPERATION)
                                        .param(NAME, SECURE_SOCKET_BINDING)
                                        .build();
                                return dispatcher.execute(op)
                                        .toCompletable();

                            };
                            tasks.add(undefSslTask);

                            // as part of the disable ssl task, undefine the ssl-context
                            Task<FlowContext> undefineSslContextTask = flowContext -> {
                                Operation op = new Operation.Builder(httpAddress, UNDEFINE_ATTRIBUTE_OPERATION)
                                        .param(NAME, SSL_CONTEXT)
                                        .build();
                                return dispatcher.execute(op)
                                        .toCompletable();
                            };
                            tasks.add(undefineSslContextTask);

                            series(new FlowContext(progress.get()), tasks)
                                    .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                                        @Override
                                        public void onSuccess(FlowContext flowContext) {
                                            if (reload.getValue() != null && reload.getValue()) {
                                                String port = flowContext.get(PORT).toString();
                                                // extracts the url search path, so the reload shows the same view the use is on
                                                String urlSuffix = window.location.getHref();
                                                urlSuffix = urlSuffix.substring(urlSuffix.indexOf("//") + 2);
                                                urlSuffix = urlSuffix.substring(urlSuffix.indexOf("/"));
                                                // the location to redirect the browser to the unsecure URL
                                                // TODO Replace hardcoded scheme
                                                String location = "http://" + window.location.getHostname() + ":" + port + urlSuffix;
                                                reloadServer(null, location);
                                            } else {
                                                reload();
                                                MessageEvent.fire(getEventBus(),
                                                        Message.success(resources.messages().disableSSLManagementSuccess()));
                                            }
                                        }

                                        @Override
                                        public void onError(FlowContext context, Throwable throwable) {
                                            MessageEvent.fire(getEventBus(),
                                                    Message.error(resources.messages()
                                                            .disableSSLManagementError(throwable.getMessage())));
                                        }
                                    });
                        })
                .show();
    }

    @Override
    public void reloadServer(Host host, String urlConsole) {
        boolean adminMode = Server.STANDALONE.isAdminMode();
        boolean suspended = Server.STANDALONE.isSuspended();
        String startMode;
        if (adminMode || suspended) {
            startMode = adminMode ? "admin-only" : "suspend";
        } else {
            startMode = "normal";
        }

        Operation operation = new Operation.Builder(ResourceAddress.root(), RELOAD)
                .param(START_MODE, startMode)
                .build();

        String type = Names.STANDALONE_SERVER;
        String name = Server.STANDALONE.getName();
        reloadBlocking(dispatcher, getEventBus(), operation, type, name, urlConsole, resources);
    }

    @Override
    public void saveManagementInterface(AddressTemplate template, Map<String, Object> changedValues) {
        String type = resources.constants().httpManagementInterface();
        save(type, template, changedValues);
    }

    @Override
    public void resetManagementInterface(AddressTemplate template, Form<ModelNode> form, Metadata metadata) {
        String type = resources.constants().httpManagementInterface();
        reset(type, template, form, metadata);
    }

    @Override
    public void addConstantHeaderPath(ModelNode payload, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, LIST_ADD_OPERATION)
                .param(NAME, CONSTANT_HEADERS)
                .param(VALUE, payload)
                .build();
        dispatcher.execute(operation, result -> {
            reload();
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    @Override
    public void saveConstantHeaderPath(int index, String path, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, constantsHeadersIndex(index) + DOT + PATH)
                .param(VALUE, path)
                .build();
        dispatcher.execute(operation, result -> {
            reload();
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    @Override
    public void removeConstantHeaderPath(int index, String path, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, LIST_REMOVE_OPERATION)
                .param(NAME, CONSTANT_HEADERS)
                .param(INDEX, index)
                .build();
        dispatcher.execute(operation, result -> {
            reload();
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    @Override
    public void addHeader(int pathIndex, ModelNode model, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, LIST_ADD_OPERATION)
                .param(NAME, constantsHeadersIndex(pathIndex) + DOT + HEADERS)
                .param(VALUE, model)
                .build();
        dispatcher.execute(operation, result -> {
            reloadHeaders(pathIndex);
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    @Override
    public void saveHeader(int pathIndex, int index, String header, Metadata metadata,
            Map<String, Object> changedValues, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        OperationFactory operationFactory = new OperationFactory(
                name -> constantsHeadersIndex(pathIndex) + DOT + headersIndex(index) + DOT + name);
        Composite composite = operationFactory.fromChangeSet(address, changedValues, metadata);
        dispatcher.execute(composite, (CompositeResult result) -> {
            reloadHeaders(pathIndex);
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    @Override
    public void removeHeader(int pathIndex, int index, String header, SafeHtml successMessage) {
        ResourceAddress address = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, LIST_REMOVE_OPERATION)
                .param(NAME, CONSTANT_HEADERS + "[" + pathIndex + "]." + HEADERS)
                .param(INDEX, index)
                .build();
        dispatcher.execute(operation, result -> {
            reloadHeaders(pathIndex);
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
        });
    }

    private String constantsHeadersIndex(int index) {
        return CONSTANT_HEADERS + "[" + index + "]";
    }

    private String headersIndex(int index) {
        return HEADERS + "[" + index + "]";
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.STANDALONE_SERVER)
    @Requires(value = {ROOT_ADDRESS, HTTP_INTERFACE_ADDRESS}, recursive = false)
    public interface MyProxy extends ProxyPlace<StandaloneServerPresenter> {
    }

    public interface MyView extends MbuiView<StandaloneServerPresenter> {
        void updateAttributes(ModelNode attributes);
        void updateHttpInterface(ModelNode httpModel, int pathIndex);
    }
    // @formatter:on
}
