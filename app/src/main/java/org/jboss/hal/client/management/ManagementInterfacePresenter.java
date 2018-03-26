/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.HTMLElement;
import elemental2.dom.XMLHttpRequest;
import org.jboss.hal.ballroom.dialog.BlockingDialog;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.client.management.sslwizard.EnableSSLWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
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
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.SingleEmitter;

import static elemental2.dom.DomGlobal.window;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.hal.client.management.sslwizard.AbstractConfiguration.ELYTRON_TEMPLATE;
import static org.jboss.hal.client.management.sslwizard.AbstractConfiguration.SOCKET_BINDING_GROUP_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.resources.Ids.FORM;

public class ManagementInterfacePresenter
        extends MbuiPresenter<ManagementInterfacePresenter.MyView, ManagementInterfacePresenter.MyProxy>
        implements SupportsExpertMode {

    static final String HTTP_INTERFACE_ADDRESS = "{domain.controller}/core-service=management/management-interface=http-interface";
    static final AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    @NonNls private static Logger logger = LoggerFactory.getLogger(ManagementInterfacePresenter.class);

    private final CrudOperations crud;
    private final StatementContext statementContext;
    private Resources resources;
    private Environment environment;
    private Dispatcher dispatcher;
    private Provider<Progress> progress;

    @Inject
    public ManagementInterfacePresenter(EventBus eventBus, ManagementInterfacePresenter.MyView view,
            ManagementInterfacePresenter.MyProxy proxy, Finder finder, CrudOperations crud, Resources resources,
            Environment environment, Dispatcher dispatcher, @Footer Provider<Progress> progress,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.resources = resources;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append(Ids.MANAGEMENT, Ids.asId(Names.MANAGEMENT_INTERFACE), Names.MANAGEMENT,
                        Names.MANAGEMENT_INTERFACE);
    }

    @Override
    protected void reload() {
        crud.read(resourceAddress(), result -> getView().update(result));
    }

    public void reloadView() {
        reload();
    }

    void save(Map<String, Object> changedValues) {
        crud.saveSingleton(Names.MANAGEMENT_INTERFACE, HTTP_INTERFACE_TEMPLATE, changedValues, this::reload);
    }

    void reset(Form<ModelNode> form, Metadata metadata) {
        crud.resetSingleton(Names.MANAGEMENT_INTERFACE, HTTP_INTERFACE_TEMPLATE, form, metadata, this::reload);
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

        if (!environment.isStandalone()) {
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
        }

        series(new FlowContext(progress.get()), tasks).subscribe(
                new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext flowContext) {
                        Map<String, List<String>> existingResources = new HashMap<>();
                        flowContext.keys().forEach(key -> existingResources.put(key, flowContext.get(key)));
                        Host host = flowContext.emptyStack() ? null : flowContext.pop();

                        new EnableSSLWizard(existingResources, resources, environment, statementContext, dispatcher,
                                host, ManagementInterfacePresenter.this, progress, getEventBus()).show();
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
        HTMLElement formElement = form.asElement();

        ModelNode model = new ModelNode();
        model.setEmptyObject();
        form.edit(model);

        DialogFactory.buildConfirmation(constants.disableSSL(),
                resources.messages().disableSSLManagementQuestion(serverName), formElement, Dialog.Size.MEDIUM, () -> {

                    List<Task<FlowContext>> tasks = new ArrayList<>();

                    // load the http-interface resource to get the port, there are differente attributes for
                    // standalone and domain mode.
                    Task<FlowContext> loadHttpInterface = flowContext -> {
                        Operation readHttpInterface = new Operation.Builder(resourceAddress(), READ_RESOURCE_OPERATION)
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
                                    if (value.hasDefined(PORT)) {
                                        // only domain mode contains "port" attribute
                                        String port = value.get(PORT).asString();
                                        if (port.contains("$")) {
                                            // if it contains an expression value, resolve it at host level
                                            ResourceAddress address = AddressTemplate.of("/host=" + environment.getDomainController())
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
                    if (environment.isStandalone()) {

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
                                        ResourceAddress address = SOCKET_BINDING_GROUP_TEMPLATE.resolve(statementContext, sbg, httpBinding);
                                        Operation readPort = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                                                .param(NAME, PORT)
                                                .param(RESOLVE_EXPRESSIONS, true)
                                                .build();
                                        dispatcher.execute(readPort, portResult -> flowContext.set(PORT, portResult.asString()));
                                    })
                                    .toCompletable();
                        };
                        tasks.add(readHttpPortTask);

                        // as part of the disable ssl task, undefine the secure-socket-binding
                        // the attribute only exists in standalone mode
                        Task<FlowContext> undefSsbTask = flowContext -> {
                            Operation op = new Operation.Builder(resourceAddress(), UNDEFINE_ATTRIBUTE_OPERATION)
                                    .param(NAME, SECURE_SOCKET_BINDING)
                                    .build();
                            return dispatcher.execute(op)
                                    .toCompletable();

                        };
                        tasks.add(undefSsbTask);

                    } else {
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
                            Operation op = new Operation.Builder(resourceAddress(), UNDEFINE_ATTRIBUTE_OPERATION)
                                    .param(NAME, SECURE_PORT)
                                    .build();
                            return dispatcher.execute(op)
                                    .toCompletable();
                        };
                        tasks.add(undefineSecurePortTask);
                    }
                    // as part of the disable ssl task, undefine the ssl-context
                    Task<FlowContext> undefineSslContextTask = flowContext -> {
                        Operation op = new Operation.Builder(resourceAddress(), UNDEFINE_ATTRIBUTE_OPERATION)
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
                                        String location = "http://" + window.location.getHostname() + ":" + port + urlSuffix;
                                        Host host = flowContext.get(HOST);
                                        reloadBackend(host, location, false);
                                    } else {
                                        reload();
                                        MessageEvent.fire(getEventBus(),
                                                Message.success(resources.messages().disableSSLManagementSuccess()));
                                    }
                                }

                                @Override
                                public void onError(FlowContext context, Throwable throwable) {
                                    MessageEvent.fire(getEventBus(),
                                            Message.error(resources.messages().disableSSLManagementError(throwable.getMessage())));
                                }
                            });
                })
                .show();
    }

    public void reloadBackend(Host host, String urlConsole, boolean reloadConsole) {
        Operation operation;
        String type;
        String name;
        String title;
        SafeHtml pendingDescription;
        if (environment.isStandalone()) {
            boolean adminMode = Server.STANDALONE.isAdminMode();
            boolean suspended = Server.STANDALONE.isSuspended();
            String startMode;
            if (adminMode || suspended) {
                startMode = adminMode ? "admin-only" : "suspend";
            } else {
                startMode = "normal";
            }

            operation = new Operation.Builder(ResourceAddress.root(), RELOAD)
                    .param(START_MODE, startMode)
                    .build();

            type = resources.constants().standaloneServer();
            name = Server.STANDALONE.getName();
            title = resources.messages().restart(name);
            pendingDescription = resources.messages().restartStandalonePending(name);
        } else {
            ModelNode dc = new ModelNode();
            dc.get(HOST).set(environment.getDomainController());
            ResourceAddress address = new ResourceAddress(dc);
            operation = new Operation.Builder(address, RELOAD)
                    .param(RESTART_SERVERS, false)
                    .param(ADMIN_ONLY, host.isAdminMode())
                    .build();

            type = resources.constants().domainController();
            name = host.getName();
            title = resources.messages().reload(name);
            pendingDescription = resources.messages().reloadDomainControllerPending(name);
        }

        dispatcher.execute(operation, result -> {
                    if (reloadConsole) {
                        BlockingDialog pendingDialog = DialogFactory.buildLongRunning(title, pendingDescription);
                        pendingDialog.show();
                        ping(urlConsole)
                                .doOnError(exception -> {
                                    pendingDialog.close();
                                    DialogFactory.buildBlocking(title, Dialog.Size.MEDIUM,
                                            resources.messages().reloadConsoleTimeout(type, urlConsole))
                                            .show();
                                    logger.error("Error trying to connect to address: " + urlConsole);
                                })
                                .subscribe(() -> {
                                    logger.info("redirect browser to url: " + urlConsole);
                                    redirect(urlConsole);
                                });
                    } else {
                        DialogFactory.buildBlocking(title, Dialog.Size.MEDIUM, resources.messages().reloadConsoleRedirect(urlConsole))
                                .show();
                    }
                },
                (operation1, failure) -> {
                    MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().reloadErrorCause(type, name, failure)));
                },
                (operation1, exception) -> {
                    MessageEvent.fire(getEventBus(),
                            Message.error(resources.messages().reloadErrorCause(type, name, exception.getMessage())));
                });

    }

    private Completable ping(String url) {

        XMLHttpRequest xhr = new XMLHttpRequest();

        Single<Integer> execution = Single.fromEmitter((SingleEmitter<Integer> em) -> {
            xhr.onload = event -> {
                em.onSuccess(xhr.status);
            };
            xhr.onerror = event -> {
                logger.info(" Error trying to connect to url {} - status: {}", url, xhr.status + ", " + xhr.responseText);
                return -1;
            };
            xhr.open(GET.name(), url, true);
            xhr.send();
        });
        Predicate<Integer> until = xhrResponseStatus -> xhrResponseStatus != null && xhrResponseStatus > 0;

        return Observable
                .interval(500, MILLISECONDS) // execute a operation each INTERVAL millis
                .flatMapSingle(n -> execution, false, 1)
                .takeUntil(until::test) // until succeeded
                .toCompletable()
                .timeout(10, SECONDS); // wait succeeded or stop after timeout seconds
    }

    private native void redirect(String url) /*-{
        window.location.href = url;
    }-*/;

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.MANAGEMENT_INTERFACE)
    @Requires(value = HTTP_INTERFACE_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<ManagementInterfacePresenter> {
    }

    public interface MyView extends MbuiView<ManagementInterfacePresenter> {
        void update(ModelNode model);
    }
    // @formatter:on

}
