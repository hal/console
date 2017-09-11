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
package org.jboss.hal.client.configuration.subsystem.remoting;

import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTING;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.flow.Flow.series;

public class RemotingPresenter
        extends MbuiPresenter<RemotingPresenter.MyView, RemotingPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.REMOTING)
    @Requires({REMOTING_SUBSYSTEM_ADDRESS,
            CONNECTOR_SECURITY_ADDRESS, CONNECTOR_SECURITY_POLICY_ADDRESS,
            HTTP_CONNECTOR_SECURITY_ADDRESS, HTTP_CONNECTOR_SECURITY_POLICY_ADDRESS})
    public interface MyProxy extends ProxyPlace<RemotingPresenter> {}

    public interface MyView extends MbuiView<RemotingPresenter> {
        void update(ModelNode payload);
        void updateConnector(@Nullable NamedNode connector);
        void updateHttpConnector(@Nullable NamedNode httpConnector);
        void updateLocalOutbound(@Nullable NamedNode localOutbound);
        void updateOutbound(@Nullable NamedNode outbound);
        void updateRemoteOutbound(@Nullable NamedNode remoteOutbound);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final PropertiesOperations propertiesOperations;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Provider<org.jboss.hal.flow.Progress> progress;
    private final Resources resources;

    private ModelNode payload;
    private String connector;
    private String httpConnector;
    SelectionAwareStatementContext selectedConnectorContext;
    SelectionAwareStatementContext selectedHttpConnectorContext;

    @Inject
    public RemotingPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            PropertiesOperations propertiesOperations,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            @Footer Provider<org.jboss.hal.flow.Progress> progress,
            Resources resources) {

        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.propertiesOperations = propertiesOperations;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.progress = progress;
        this.resources = resources;

        selectedConnectorContext = new SelectionAwareStatementContext(statementContext, () -> connector);
        selectedHttpConnectorContext = new SelectionAwareStatementContext(statementContext, () -> httpConnector);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return REMOTING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(REMOTING);
    }

    @Override
    protected void reload() {
        crud.readRecursive(REMOTING_SUBSYSTEM_TEMPLATE, result -> {
            payload = result;
            getView().update(result);
        });
    }


    // ------------------------------------------------------ remote connector

    void selectConnector(String connector) {
        this.connector = connector;
        NamedNode namedNode = connector == null ? null : new NamedNode(connector, failSafeGet(payload,
                CONNECTOR_TEMPLATE.lastName() + "/" + connector));
        getView().updateConnector(namedNode);
    }

    void saveConnector(Form<NamedNode> form, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_TEMPLATE);
        ResourceAddress address = SELECTED_CONNECTOR_TEMPLATE.resolve(selectedConnectorContext);
        propertiesOperations.saveWithProperties(Names.REMOTE_CONNECTOR, form.getModel().getName(), address,
                changedValues, metadata, PROPERTY, form.<Map<String, String>>getFormItem(PROPERTY).getValue(),
                this::reload);
    }

    void resetConnector(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_TEMPLATE);
        ResourceAddress address = SELECTED_CONNECTOR_TEMPLATE.resolve(selectedConnectorContext);
        crud.reset(Names.REMOTE_CONNECTOR, form.getModel().getName(), address, form, metadata,
                new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(final Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    void createConnectorSecurity() {
        Operation operation = new Operation.Builder(
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), ADD
        ).build();
        dispatcher.execute(operation, result -> reload());
    }

    void saveConnectorSecurity(Form<ModelNode> form, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_SECURITY_TEMPLATE);
        propertiesOperations.saveSingletonWithProperties(Names.REMOTE_CONNECTOR_SECURITY, address, changedValues,
                metadata, PROPERTY, form.<Map<String, String>>getFormItem(PROPERTY).getValue(), this::reload);
    }

    void resetConnectorSecurity(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_SECURITY_TEMPLATE);
        crud.resetSingleton(Names.REMOTE_CONNECTOR_SECURITY, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeConnectorSecurity(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        crud.removeSingleton(Names.REMOTE_CONNECTOR_SECURITY, address, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void createConnectorSecurityPolicy() {
        failSafeCreatePolicy(Names.REMOTE_CONNECTOR_SECURITY_POLICY, SELECTED_CONNECTOR_SECURITY_TEMPLATE,
                SELECTED_CONNECTOR_SECURITY_POLICY_TEMPLATE, selectedConnectorContext);
    }

    void saveConnectorSecurityPolicy(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_SECURITY_TEMPLATE);
        crud.saveSingleton(Names.REMOTE_CONNECTOR_SECURITY_POLICY,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), changedValues, metadata,
                this::reload);
    }

    void resetConnectorSecurityPolicy(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(CONNECTOR_SECURITY_TEMPLATE);
        crud.resetSingleton(Names.REMOTE_CONNECTOR_SECURITY_POLICY,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeConnectorSecurityPolicy(Form<ModelNode> form) {
        crud.removeSingleton(Names.REMOTE_CONNECTOR_SECURITY_POLICY,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext),
                new FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ http connector

    void selectHttpConnector(@Nullable String httpConnector) {
        this.httpConnector = httpConnector;
        NamedNode namedNode = httpConnector == null ? null : new NamedNode(httpConnector, failSafeGet(payload,
                HTTP_CONNECTOR_TEMPLATE.lastName() + "/" + httpConnector));
        getView().updateHttpConnector(namedNode);
    }

    void saveHttpConnector(Form<NamedNode> form, Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_TEMPLATE);
        ResourceAddress address = SELECTED_HTTP_CONNECTOR_TEMPLATE.resolve(selectedHttpConnectorContext);
        propertiesOperations.saveWithProperties(Names.HTTP_CONNECTOR, form.getModel().getName(),
                address, changedValues, metadata, PROPERTY, form.<Map<String, String>>getFormItem(PROPERTY).getValue(),
                this::reload);
    }

    void resetHttpConnector(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_TEMPLATE);
        ResourceAddress address = SELECTED_HTTP_CONNECTOR_TEMPLATE.resolve(selectedHttpConnectorContext);
        crud.reset(Names.HTTP_CONNECTOR, form.getModel().getName(), address, form, metadata,
                new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(final Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    void createHttpConnectorSecurity() {
        Operation operation = new Operation.Builder(
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedHttpConnectorContext), ADD
        ).build();
        dispatcher.execute(operation, result -> reload());
    }

    void saveHttpConnectorSecurity(Form<ModelNode> form, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        propertiesOperations.saveSingletonWithProperties(Names.HTTP_CONNECTOR_SECURITY, address, changedValues,
                metadata, PROPERTY, form.<Map<String, String>>getFormItem(PROPERTY).getValue(), this::reload);
    }

    void resetHttpConnectorSecurity(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        crud.resetSingleton(Names.HTTP_CONNECTOR_SECURITY, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeHttpConnectorSecurity(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext);
        crud.removeSingleton(Names.HTTP_CONNECTOR_SECURITY, address, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void createHttpConnectorSecurityPolicy() {
        failSafeCreatePolicy(Names.HTTP_CONNECTOR_SECURITY_POLICY, SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE,
                SELECTED_HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE, selectedHttpConnectorContext);
    }

    void saveHttpConnectorSecurityPolicy(final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        crud.saveSingleton(Names.HTTP_CONNECTOR_SECURITY_POLICY,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), changedValues, metadata,
                this::reload);
    }

    void resetHttpConnectorSecurityPolicy(final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(HTTP_CONNECTOR_SECURITY_TEMPLATE);
        crud.resetSingleton(Names.HTTP_CONNECTOR_SECURITY_POLICY,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void removeHttpConnectorSecurityPolicy(final Form<ModelNode> form) {
        crud.removeSingleton(Names.HTTP_CONNECTOR_SECURITY_POLICY,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext),
                new FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ local outbound connection

    void selectLocalOutbound(@Nullable String localOutbound) {
        NamedNode namedNode = localOutbound == null ? null : new NamedNode(localOutbound, failSafeGet(payload,
                LOCAL_OUTBOUND_TEMPLATE.lastName() + "/" + localOutbound));
        getView().updateLocalOutbound(namedNode);
    }

    void saveLocalOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveWithProperties(Names.LOCAL_OUTBOUND_CONNECTION, form.getModel().getName(),
                LOCAL_OUTBOUND_TEMPLATE, changedValues, PROPERTY,
                form.<Map<String, String>>getFormItem(PROPERTY).getValue(), this::reload);
    }

    void resetLocalOutbound(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(LOCAL_OUTBOUND_TEMPLATE);
        crud.reset(Names.LOCAL_OUTBOUND_CONNECTION, form.getModel().getName(), LOCAL_OUTBOUND_TEMPLATE, form, metadata,
                new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(final Form<NamedNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ outbound connection

    void selectOutbound(@Nullable String outbound) {
        NamedNode namedNode = outbound == null ? null : new NamedNode(outbound, failSafeGet(payload,
                OUTBOUND_TEMPLATE.lastName() + "/" + outbound));
        getView().updateOutbound(namedNode);
    }

    void saveOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveWithProperties(Names.OUTBOUND_CONNECTION, form.getModel().getName(), OUTBOUND_TEMPLATE,
                changedValues, PROPERTY, form.<Map<String, String>>getFormItem(PROPERTY).getValue(), this::reload);
    }

    void resetOutbound(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(OUTBOUND_TEMPLATE);
        crud.reset(Names.OUTBOUND_CONNECTION, form.getModel().getName(), OUTBOUND_TEMPLATE, form, metadata,
                new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(final Form<NamedNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ local outbound connection

    void selectRemoteOutbound(@Nullable String remoteOutbound) {
        NamedNode namedNode = remoteOutbound == null ? null : new NamedNode(remoteOutbound, failSafeGet(payload,
                REMOTE_OUTBOUND_TEMPLATE.lastName() + "/" + remoteOutbound));
        getView().updateRemoteOutbound(namedNode);
    }

    void saveRemoteOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveWithProperties(Names.REMOTE_OUTBOUND_CONNECTION, form.getModel().getName(),
                REMOTE_OUTBOUND_TEMPLATE, changedValues, PROPERTY,
                form.<Map<String, String>>getFormItem(PROPERTY).getValue(), this::reload);
    }

    void resetRemoteOutbound(Form<NamedNode> form) {
        Metadata metadata = metadataRegistry.lookup(REMOTE_OUTBOUND_TEMPLATE);
        crud.reset(Names.REMOTE_OUTBOUND_CONNECTION, form.getModel().getName(), REMOTE_OUTBOUND_TEMPLATE, form,
                metadata, new FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(final Form<NamedNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ helper methods

    /**
     * Creates the singleton {@code /subsystem=remoting/(http-)connector=foo/security=sasl/sasl-policy=policy}
     * in a fail-safe manner: A missing {@code security=sasl} parent resource is created on demand.
     */
    private void failSafeCreatePolicy(String type, AddressTemplate securityTemplate, AddressTemplate policyTemplate,
            StatementContext statementContext) {

        series(progress.get(), new FlowContext(),
                new ResourceCheck(dispatcher, securityTemplate.resolve(statementContext)),
                control -> {
                    int status = control.getContext().pop();
                    if (status == 200) {
                        control.proceed();
                    } else {
                        Operation operation = new Operation.Builder(securityTemplate.resolve(statementContext), ADD)
                                .build();
                        dispatcher.executeInFlow(control, operation, result -> control.proceed());
                    }
                },
                control -> {
                    Operation operation = new Operation.Builder(policyTemplate.resolve(statementContext), ADD).build();
                    dispatcher.executeInFlow(control, operation, result -> control.proceed());
                })
                .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext context) {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSingleResourceSuccess(type)));
                        reload();
                    }
                });
    }
}
