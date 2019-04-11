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
package org.jboss.hal.client.runtime.host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.client.shared.sslwizard.EnableSSLPresenter;
import org.jboss.hal.client.shared.sslwizard.EnableSSLWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
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
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.Ids.FORM;

public class HostPresenter
        extends MbuiPresenter<HostPresenter.MyView, HostPresenter.MyProxy>
        implements SupportsExpertMode, EnableSSLPresenter {

    static final String HOST_ADDRESS = "/{selected.host}";
    static final String INTERFACE_ADDRESS = HOST_ADDRESS + "/interface=*";
    static final String JVM_ADDRESS = HOST_ADDRESS + "/jvm=*";
    static final String PATH_ADDRESS = HOST_ADDRESS + "/path=*";
    static final String SOCKET_BINDING_GROUP_ADDRESS = HOST_ADDRESS + "/socket-binding-group=*";
    static final String SYSTEM_PROPERTY_ADDRESS = HOST_ADDRESS + "/system-property=*";
    static final String MANAGEMENT_INTERFACE_ADDRESS = HOST_ADDRESS + "/core-service=management/management-interface=*";

    private static final String HTTP_INTERFACE_ADDRESS = "{domain.controller}/core-service=management/management-interface=http-interface";
    static final AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    private static final String NATIVE_INTERFACE_ADDRESS = "{selected.host}/core-service=management/management-interface=native-interface";
    static final AddressTemplate NATIVE_INTERFACE_TEMPLATE = AddressTemplate.of(NATIVE_INTERFACE_ADDRESS);
    private static final AddressTemplate ELYTRON_TEMPLATE = AddressTemplate.of("{domain.controller}/subsystem=elytron");

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private Provider<Progress> progress;
    private Environment environment;
    private final Resources resources;

    @Inject
    public HostPresenter(EventBus eventBus,
            HostPresenter.MyView view,
            HostPresenter.MyProxy proxy,
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
        return new ResourceAddress().add(HOST, statementContext.selectedHost());
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeHostPath();
    }

    @Override
    protected void reload() {
        ResourceAddress hostAddress = new ResourceAddress().add(HOST, statementContext.selectedHost());
        Operation hostOp = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation interfacesOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, INTERFACE)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation jvmsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, JVM)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation pathsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, PATH)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation socketBindingGroupsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation systemPropertiesOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SYSTEM_PROPERTY)
                .param(INCLUDE_RUNTIME, true)
                .build();
        ResourceAddress coreServiceAddress = hostAddress.add(CORE_SERVICE, MANAGEMENT);
        Operation mgmtInterfacesOp = new Operation.Builder(coreServiceAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, MANAGEMENT_INTERFACE)
                .param(INCLUDE_RUNTIME, true)
                .build();

        dispatcher.execute(
                new Composite(hostOp, interfacesOp, jvmsOp, pathsOp, socketBindingGroupsOp, systemPropertiesOp,
                        mgmtInterfacesOp),
                (CompositeResult result) -> {
                    getView().updateHost(new Host(result.step(0).get(RESULT)));
                    getView().updateInterfaces(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateJvms(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updatePaths(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateSocketBindingGroups(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateSystemProperties(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateManagementInterfaces(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                });
    }

    @Override
    public void reloadView() {
        ResourceAddress coreServiceAddress = resourceAddress().add(CORE_SERVICE, MANAGEMENT);
        Operation operation = new Operation.Builder(coreServiceAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, MANAGEMENT_INTERFACE)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation,
                result -> getView().updateManagementInterfaces(asNamedNodes(result.asPropertyList())));
    }

    void saveHost(Form<Host> form, Map<String, Object> changedValues) {
        boolean hostNameChanged = changedValues.containsKey(NAME);
        crud.save(Names.HOST, form.getModel().getName(), AddressTemplate.of(HOST_ADDRESS), changedValues, () -> {
            reload();
            if (hostNameChanged) {
                DialogFactory.showConfirmation(resources.constants().hostNameChanged(),
                        resources.messages().hostNameChanged(),
                        () -> window.location.reload());
            }
        });
    }

    void save(String type, AddressTemplate template, Map<String, Object> changedValues) {
        crud.saveSingleton(type, template, changedValues, this::reloadView);
    }

    void reset(String type, AddressTemplate template, Form<ModelNode> form, Metadata metadata) {
        crud.resetSingleton(type, template, form, metadata, this::reloadView);
    }

    void launchEnableSSLWizard() {
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

        Task<FlowContext> loadDc = flowContext -> {
            ResourceAddress dcAddress = AddressTemplate.of("/host=" + environment.getDomainController())
                    .resolve(statementContext);
            Operation readDcOp = new Operation.Builder(dcAddress, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();

            return dispatcher.execute(readDcOp)
                    .doOnSuccess(value -> flowContext.push(new Host(value)))
                    .toCompletable();
        };
        tasks.add(loadDc);

        series(new FlowContext(progress.get()), tasks).subscribe(
                new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        Map<String, List<String>> existingResources = new HashMap<>();
                        flowContext.keys().forEach(key -> existingResources.put(key, flowContext.get(key)));
                        Host host = flowContext.emptyStack() ? null : flowContext.pop();

                        EnableSSLWizard wzd = new EnableSSLWizard.Builder(existingResources, resources, getEventBus(),
                                statementContext, dispatcher, progress, HostPresenter.this, environment)
                                .host(host)
                                .build();
                        wzd.show();
                    }
                });
    }

    private Task<FlowContext> loadResourceTask(String resourceName) {
        Task<FlowContext> task = context -> {
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
        return task;
    }

    void disableSSLWizard() {
        Constants constants = resources.constants();
        String serverName = environment.isStandalone() ? constants.standaloneServer() : constants.domainController();
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
                    // load the http-interface resource to get the port
                    Task<FlowContext> loadHttpInterface = flowContext -> {
                        Operation readHttpInterface = new Operation.Builder(httpAddress, READ_RESOURCE_OPERATION)
                                .build();
                        return dispatcher.execute(readHttpInterface)
                                .doOnSuccess(value -> {
                                    if (value.hasDefined(PORT)) {
                                        // only domain mode contains "port" attribute
                                        String port = value.get(PORT).asString();
                                        if (port.contains("$")) {
                                            // if it contains an expression value, resolve it at host level
                                            ResourceAddress address = AddressTemplate.of(
                                                    "/host=" + environment.getDomainController())
                                                    .resolve(statementContext);
                                            Operation readPort = new Operation.Builder(address, RESOLVE_EXPRESSION)
                                                    .param(EXPRESSION, port)
                                                    .build();
                                            dispatcher.execute(readPort,
                                                    portResult -> flowContext.set(PORT, portResult.asString()));
                                        } else {
                                            flowContext.set(PORT, port);
                                        }
                                    }
                                })
                                .toCompletable();

                    };
                    tasks.add(loadHttpInterface);

                    // in domain-mode read the /host=<dc> domain controller
                    // it is important for later use if user wants to reload dc if in admin-mode
                    Task<FlowContext> loadDc = flowContext -> {
                        ResourceAddress dcAddress = AddressTemplate.of("/host=" + environment.getDomainController())
                                .resolve(statementContext);
                        Operation readDcOp = new Operation.Builder(dcAddress, READ_RESOURCE_OPERATION)
                                .param(ATTRIBUTES_ONLY, true)
                                .build();

                        return dispatcher.execute(readDcOp)
                                .doOnSuccess(value -> flowContext.set(HOST, new Host(value)))
                                .toCompletable();
                    };
                    tasks.add(loadDc);

                    // as part of the disable ssl task, undefine the secure-port, it only exists in domain mode
                    Task<FlowContext> undefineSecurePortTask = flowContext -> {
                        Operation op = new Operation.Builder(httpAddress, UNDEFINE_ATTRIBUTE_OPERATION)
                                .param(NAME, SECURE_PORT)
                                .build();
                        return dispatcher.execute(op)
                                .toCompletable();
                    };
                    tasks.add(undefineSecurePortTask);
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
                                        Host host = flowContext.get(HOST);
                                        reloadServer(host, location);
                                    } else {
                                        reloadView();
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
        Messages messages = resources.messages();
        ModelNode dc = new ModelNode();
        dc.get(HOST).set(environment.getDomainController());
        ResourceAddress address = new ResourceAddress(dc);
        Operation operation = new Operation.Builder(address, RELOAD)
                .param(RESTART_SERVERS, false)
                .param(ADMIN_ONLY, host.isAdminMode())
                .build();
        String type = resources.constants().domainController();
        String name = host.getName();
        String title = messages.reload(name);

        dispatcher.execute(operation,
                result -> DialogFactory.buildBlocking(title, Dialog.Size.MEDIUM,
                        messages.reloadConsoleRedirect(urlConsole))
                        .show(),
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(messages.reloadErrorCause(type, name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(messages.reloadErrorCause(type, name, exception.getMessage()))));

    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.HOST_CONFIGURATION)
    @Requires(value = {HOST_ADDRESS, INTERFACE_ADDRESS, JVM_ADDRESS, PATH_ADDRESS, SOCKET_BINDING_GROUP_ADDRESS,
            SYSTEM_PROPERTY_ADDRESS, MANAGEMENT_INTERFACE_ADDRESS}, recursive = false)
    public interface MyProxy extends ProxyPlace<HostPresenter> {
    }

    public interface MyView extends MbuiView<HostPresenter> {
        void updateHost(Host host);
        void updateManagementInterfaces(List<NamedNode> endpoints);
        void updateInterfaces(List<NamedNode> interfaces);
        void updateJvms(List<NamedNode> interfaces);
        void updatePaths(List<NamedNode> paths);
        void updateSocketBindingGroups(List<NamedNode> groups);
        void updateSystemProperties(List<NamedNode> properties);
    }
    // @formatter:on
}
