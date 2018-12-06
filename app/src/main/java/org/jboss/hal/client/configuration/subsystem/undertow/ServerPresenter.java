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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.client.shared.sslwizard.EnableSSLPresenter;
import org.jboss.hal.client.shared.sslwizard.EnableSSLWizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
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
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.FilteringStatementContext.Filter;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.encodeValue;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.flow.Flow.series;
import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;

public class ServerPresenter
        extends ApplicationFinderPresenter<ServerPresenter.MyView, ServerPresenter.MyProxy>
        implements SupportsExpertMode, EnableSSLPresenter {

    private static final String EQUALS = "=";
    private static final String EQ_WILDCARD = "=*";

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private Provider<Progress> progress;
    private Environment environment;
    private final Resources resources;
    private String serverName;
    private String hostName;
    private String locationName;

    @Inject
    public ServerPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            @Footer Provider<Progress> progress,
            Environment environment,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new FilteringStatementContext(statementContext,
                new Filter() {
                    @Override
                    public String filter(String placeholder, AddressTemplate template) {
                        if (SELECTION_KEY.equals(placeholder)) {
                            return serverName;
                        } else if (HOST.equals(placeholder)) {
                            return hostName;
                        }
                        return null;
                    }

                    @Override
                    public String[] filterTuple(String placeholder, AddressTemplate template) {
                        return null;
                    }
                });
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
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        serverName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.SERVER),
                        resources.constants().settings(), Names.SERVER)
                .append(Ids.UNDERTOW_SERVER, Ids.undertowServer(serverName), Names.SERVER, serverName);
    }

    @Override
    protected void reload() {
        reload(result -> getView().update(result));
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(SELECTED_SERVER_TEMPLATE.resolve(statementContext), payload::accept);
    }

    void saveServer(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.save(Names.SERVER, serverName, SELECTED_SERVER_TEMPLATE.resolve(statementContext), changedValues,
                metadata, this::reload);
    }

    void resetServer(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.reset(Names.SERVER, serverName, SELECTED_SERVER_TEMPLATE.resolve(statementContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ host

    void addHost() {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.UNDERTOW_HOST_ADD,
                resources.messages().addResourceTitle(Names.HOST), metadata, singletonList(DEFAULT_WEB_MODULE),
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + EQUALS + name)
                            .resolve(statementContext);
                    crud.add(Names.HOST, name, address, model, (n, a) -> reload());
                });
        dialog.show();

    }

    void saveHost(String name, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + EQUALS + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.save(Names.HOST, name, address, changedValues, metadata, this::reload);
    }

    void resetHost(String name, Form<NamedNode> form) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + EQUALS + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.reset(Names.HOST, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeHost(String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + EQUALS + name).resolve(statementContext);
        crud.remove(Names.HOST, name, address, this::reload);
    }

    void selectHost(String hostName) {
        this.hostName = hostName;
    }

    String hostSegment() {
        return hostName != null ? Names.HOST + ": " + hostName : Names.NOT_AVAILABLE;
    }

    Operation hostSettingOperation(HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        return new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
    }

    void addHostSetting(HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.addSingleton(hostSetting.type, address, null, a -> reload());
    }

    void saveHostSetting(HostSetting hostSetting, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.saveSingleton(hostSetting.type, address, changedValues, metadata, this::reload);
    }

    void resetHostSetting(HostSetting hostSetting, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.resetSingleton(hostSetting.type, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeHostSetting(HostSetting hostSetting, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.removeSingleton(hostSetting.type, address, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }


    // ------------------------------------------------------ host filter-ref

    void showFilterRef(NamedNode host) {
        selectHost(host.getName());
        getView().updateFilterRef(asNamedNodes(failSafePropertyList(host, FILTER_REF)));
    }

    void addFilterRef() {
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_FILTER_REF_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(NAME)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, FILTER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.FILTER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + EQUALS + name)
                            .resolve(statementContext);
                    crud.add(Names.FILTER, name, address, model, (n, a) -> reloadFilterRef());
                });
        dialog.show();
    }

    void saveFilterRef(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + EQUALS + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        crud.save(Names.FILTER, name, address, changedValues, metadata, this::reloadFilterRef);
    }

    void resetFilterRef(Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + EQUALS + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        crud.reset(Names.FILTER, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reloadFilterRef();
            }
        });
    }

    void removeFilterRef(String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + EQUALS + name).resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadFilterRef);
    }

    private void reloadFilterRef() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateFilterRef(
                    asNamedNodes(failSafePropertyList(modelNode, String.join("/", HOST, hostName, FILTER_REF))));
        });
    }

    // ------------------------------------------------------ host location

    void showLocation(NamedNode host) {
        selectHost(host.getName());
        getView().updateLocation(asNamedNodes(failSafePropertyList(host, LOCATION)));
    }

    void addLocation() {
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_LOCATION_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(HANDLER)
                .registerSuggestHandler(
                        new ReadChildrenAutoComplete(dispatcher, statementContext, HANDLER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.LOCATION), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE
                            .append(LOCATION + EQUALS + encodeValue(name))
                            .resolve(statementContext);
                    crud.add(Names.LOCATION, name, address, model, (n, a) -> reloadLocation());
                });
        dialog.show();
    }

    void saveLocation(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(name))
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        crud.save(Names.LOCATION, name, address, changedValues, metadata, this::reloadLocation);
    }

    void resetLocation(Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(name))
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        crud.reset(Names.LOCATION, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reloadLocation();
            }
        });
    }

    void removeLocation(String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(name))
                .resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadLocation);
    }

    private void reloadLocation() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateLocation(asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", HOST, hostName, LOCATION))));
        });
    }

    private void selectLocation(String locationName) {
        this.locationName = locationName;
    }

    String locationSegment() {
        return locationName != null ? Names.LOCATION + ": " + locationName : Names.NOT_AVAILABLE;
    }


    // ------------------------------------------------------ host location filter-ref

    void showLocationFilterRef(NamedNode location) {
        selectLocation(location.getName());
        getView().updateLocationFilterRef(asNamedNodes(failSafePropertyList(location, FILTER_REF)));
    }

    void addLocationFilterRef() {
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(NAME)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, FILTER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.FILTER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE
                            .append(LOCATION + EQUALS + encodeValue(locationName))
                            .append(FILTER_REF + EQUALS + name)
                            .resolve(statementContext);
                    crud.add(Names.FILTER, name, address, model, (n, a) -> reloadLocationFilterRef());
                });
        dialog.show();
    }

    void saveLocationFilterRef(Form<NamedNode> form, Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(locationName))
                .append(FILTER_REF + EQUALS + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        crud.save(Names.FILTER, name, address, changedValues, metadata, this::reloadLocationFilterRef);

    }

    void resetLocationFilterRef(Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(locationName))
                .append(FILTER_REF + EQUALS + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        crud.reset(Names.FILTER, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reloadLocationFilterRef();
            }
        });

    }

    void removeLocationFilterRef(String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + EQUALS + encodeValue(locationName))
                .append(FILTER_REF + EQUALS + name)
                .resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadLocationFilterRef);
    }

    private void reloadLocationFilterRef() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateLocationFilterRef(asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", HOST, hostName, LOCATION, encodeValue(locationName), FILTER_REF))));
        });
    }

    // ------------------------------------------------------ listener

    void addListener(Listener listenerType) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + EQ_WILDCARD));
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(listenerType.baseId, Ids.ADD),
                resources.messages().addResourceTitle(listenerType.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + EQUALS + name)
                            .resolve(statementContext);
                    crud.add(listenerType.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveListener(Listener listenerType, String name, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + EQUALS + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + EQ_WILDCARD));
        crud.save(listenerType.type, name, address, changedValues, metadata, this::reload);
    }

    void resetListener(Listener listenerType, String name, Form<NamedNode> form) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + EQUALS + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + EQ_WILDCARD));
        crud.reset(listenerType.type, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeListener(Listener listenerType, String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + EQUALS + name)
                .resolve(statementContext);
        crud.remove(listenerType.type, name, address, this::reload);
    }

    // ------------------------------------------------------ enable / disable ssl context

    void enableSsl(String httpsName) {
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

                        EnableSSLWizard ww = new EnableSSLWizard.Builder(existingResources, resources, getEventBus(),
                                statementContext, dispatcher, progress, ServerPresenter.this, environment)
                                .undertowServer(serverName)
                                .httpsListenerName(httpsName)
                                .build();
                        ww.show();
                    }
                });
    }

    void disableSsl(String httpsListener) {
        AddressTemplate httpsTemplate = SERVER_TEMPLATE.append(HTTPS_LISTENER + EQ_WILDCARD);
        Metadata metadata = metadataRegistry.lookup(httpsTemplate);
        SafeHtml description = resources.messages().disableSSLUndertowQuestion(httpsListener);
        String label = new LabelBuilder().label(SECURITY_REALM);
        TextBoxItem securityRealmItem = new TextBoxItem(SECURITY_REALM, label);
        securityRealmItem.setRequired(true);
        SafeHtml securityRealmDescription = SafeHtmlUtils.fromTrustedString(
                metadata.getDescription().get(ATTRIBUTES).get(SECURITY_REALM).get(DESCRIPTION).asString());
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(SECURITY_REALM, Ids.FORM), Metadata.empty())
                .unboundFormItem(securityRealmItem, 0, securityRealmDescription)
                .build();

        HTMLElement content = div()
                    .add(p().innerHtml(description))
                .add(form.element())
                .get();

        Dialog dialog = new Dialog.Builder(resources.constants().disableSSL())
                .size(Dialog.Size.MEDIUM)
                .primary(resources.constants().yes(), () -> {
                    boolean valid = form.save();
                    // if the form contains validation error, don't close the dialog
                    if (valid) {
                        ResourceAddress httpsAddress = httpsTemplate.resolve(statementContext, serverName,
                                httpsListener);
                        Operation undefineSslCtx = new Operation.Builder(httpsAddress, UNDEFINE_ATTRIBUTE_OPERATION)
                                .param(NAME, SSL_CONTEXT)
                                .build();
                        Operation writeSecurityRealm = new Operation.Builder(httpsAddress, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, SECURITY_REALM)
                                .param(VALUE, securityRealmItem.getValue())
                                .build();
                        Composite composite = new Composite();
                        composite.add(undefineSslCtx);
                        composite.add(writeSecurityRealm);

                        dispatcher.execute(composite, (CompositeResult result) -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.success(resources.messages().disableSSLUndertowSuccess(httpsListener)));
                            reload();
                        }, (operation, failure) -> {
                            MessageEvent.fire(getEventBus(),
                                    Message.error(
                                            resources.messages().disableSSLUndertowError(httpsListener, failure)));
                        }, (operation, exception) -> {
                            SafeHtml message = resources.messages()
                                    .disableSSLUndertowError(httpsListener, exception.getMessage());
                            MessageEvent.fire(getEventBus(), Message.error(message));
                        });
                    }
                    return valid;
                })
                .secondary(resources.constants().no(), () -> true)
                .closeIcon(true)
                .closeOnEsc(true)
                .add(content)
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        ModelNode model = new ModelNode().setEmptyObject();
        form.edit(model);
    }

    @Override
    public void reloadView() {
        reload();
    }

    private Task<FlowContext> loadResourceTask(String resourceName) {
        Task<FlowContext> task = context -> {
            ResourceAddress address = ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, resourceName)
                    .build();
            return dispatcher.execute(operation)
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

    // ------------------------------------------------------ getter

    StatementContext getStatementContext() {
        return statementContext;
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(SERVER_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_SERVER)
    public interface MyProxy extends ProxyPlace<ServerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ServerPresenter> {
        void update(ModelNode payload);
        void updateFilterRef(List<NamedNode> filters);
        void updateLocation(List<NamedNode> locations);
        void updateLocationFilterRef(List<NamedNode> filters);
    }
    // @formatter:on
}
