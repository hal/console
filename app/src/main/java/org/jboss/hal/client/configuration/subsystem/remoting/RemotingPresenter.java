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
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.ResourceCheck;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.AddressTemplate;
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

/**
 * @author Harald Pehl
 */
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
    private final StatementContext statementContext;
    private final Provider<Progress> progress;
    private final Resources resources;

    private ModelNode payload;
    private String connector;
    private String httpConnector;
    SelectionAwareStatementContext selectedConnectorContext;
    SelectionAwareStatementContext selectedHttpConnectorContext;

    @Inject
    public RemotingPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final PropertiesOperations propertiesOperations,
            final Dispatcher dispatcher,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.propertiesOperations = propertiesOperations;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
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
        return finderPathFactory.subsystemPath(REMOTING);
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
                CONNECTOR_TEMPLATE.lastKey() + "/" + connector));
        getView().updateConnector(namedNode);
    }

    void saveConnector(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations
                .saveWithProperties(Names.REMOTE_CONNECTOR, form.getModel().getName(), CONNECTOR_TEMPLATE, form,
                        changedValues, PROPERTY, this::reload);
    }

    void createConnectorSecurity() {
        Operation operation = new Operation.Builder(ADD,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext)).build();
        dispatcher.execute(operation, result -> reload());
    }

    void saveConnectorSecurity(Form<ModelNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveSingletonWithProperties(Names.REMOTE_CONNECTOR_SECURITY,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), form, changedValues, PROPERTY,
                this::reload);
    }

    void createConnectorSecurityPolicy() {
        failSafeCreatePolicy(Names.REMOTE_CONNECTOR_SECURITY_POLICY, SELECTED_CONNECTOR_SECURITY_TEMPLATE,
                SELECTED_CONNECTOR_SECURITY_POLICY_TEMPLATE, selectedConnectorContext);
    }

    void saveConnectorSecurityPolicy(Map<String, Object> changedValues) {
        crud.saveSingleton(Names.REMOTE_CONNECTOR_SECURITY_POLICY,
                SELECTED_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), changedValues, this::reload);
    }


    // ------------------------------------------------------ http connector

    void selectHttpConnector(@Nullable String httpConnector) {
        this.httpConnector = httpConnector;
        NamedNode namedNode = httpConnector == null ? null : new NamedNode(httpConnector, failSafeGet(payload,
                HTTP_CONNECTOR_TEMPLATE.lastKey() + "/" + httpConnector));
        getView().updateHttpConnector(namedNode);
    }

    void saveHttpConnector(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations
                .saveWithProperties(Names.HTTP_CONNECTOR, form.getModel().getName(), HTTP_CONNECTOR_TEMPLATE, form,
                        changedValues, PROPERTY, this::reload);
    }

    void createHttpConnectorSecurity() {
        Operation operation = new Operation.Builder(ADD,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedHttpConnectorContext)).build();
        dispatcher.execute(operation, result -> reload());
    }

    void saveHttpConnectorSecurity(Form<ModelNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveSingletonWithProperties(Names.HTTP_CONNECTOR_SECURITY,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), form, changedValues,
                PROPERTY, this::reload);
    }

    void createHttpConnectorSecurityPolicy() {
        failSafeCreatePolicy(Names.HTTP_CONNECTOR_SECURITY_POLICY, SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE,
                SELECTED_HTTP_CONNECTOR_SECURITY_POLICY_TEMPLATE, selectedHttpConnectorContext);
    }

    void saveHttpConnectorSecurityPolicy(final Map<String, Object> changedValues) {
        crud.saveSingleton(Names.HTTP_CONNECTOR_SECURITY_POLICY,
                SELECTED_HTTP_CONNECTOR_SECURITY_TEMPLATE.resolve(selectedConnectorContext), changedValues,
                this::reload);
    }


    // ------------------------------------------------------ local outbound connection

    void selectLocalOutbound(@Nullable String localOutbound) {
        NamedNode namedNode = localOutbound == null ? null : new NamedNode(localOutbound, failSafeGet(payload,
                LOCAL_OUTBOUND_TEMPLATE.lastKey() + "/" + localOutbound));
        getView().updateLocalOutbound(namedNode);
    }

    void saveLocalOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations
                .saveWithProperties(Names.LOCAL_OUTBOUND_CONNECTION, form.getModel().getName(), LOCAL_OUTBOUND_TEMPLATE,
                        form, changedValues, PROPERTY, this::reload);
    }


    // ------------------------------------------------------ outbound connection

    void selectOutbound(@Nullable String outbound) {
        NamedNode namedNode = outbound == null ? null : new NamedNode(outbound, failSafeGet(payload,
                OUTBOUND_TEMPLATE.lastKey() + "/" + outbound));
        getView().updateOutbound(namedNode);
    }

    void saveOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveWithProperties(Names.OUTBOUND_CONNECTION, form.getModel().getName(), OUTBOUND_TEMPLATE,
                form, changedValues, PROPERTY, this::reload);
    }


    // ------------------------------------------------------ local outbound connection

    void selectRemoteOutbound(@Nullable String remoteOutbound) {
        NamedNode namedNode = remoteOutbound == null ? null : new NamedNode(remoteOutbound, failSafeGet(payload,
                REMOTE_OUTBOUND_TEMPLATE.lastKey() + "/" + remoteOutbound));
        getView().updateRemoteOutbound(namedNode);
    }

    void saveRemoteOutbound(Form<NamedNode> form, Map<String, Object> changedValues) {
        propertiesOperations.saveWithProperties(Names.REMOTE_OUTBOUND_CONNECTION, form.getModel().getName(),
                REMOTE_OUTBOUND_TEMPLATE,
                form, changedValues, PROPERTY, this::reload);
    }


    // ------------------------------------------------------ helper methods

    /**
     * Creates the singleton {@code /subsystem=remoting/(http-)connector=foo/security=sasl/sasl-policy=policy}
     * in a fail-safe manner: A missing {@code security=sasl} parent resource is created on demand.
     */
    private void failSafeCreatePolicy(String type, AddressTemplate securityTemplate, AddressTemplate policyTemplate,
            StatementContext statementContext) {

        Function[] functions = new Function[]{
                new ResourceCheck(dispatcher, securityTemplate.resolve(statementContext)),
                (Function<FunctionContext>) control -> {
                    int status = control.getContext().pop();
                    if (status == 200) {
                        control.proceed();
                    } else {
                        Operation operation = new Operation.Builder(ADD, securityTemplate.resolve(statementContext))
                                .build();
                        dispatcher.execute(operation, result -> control.proceed());
                    }
                },
                (Function<FunctionContext>) control -> {
                    Operation operation = new Operation.Builder(ADD, policyTemplate.resolve(statementContext)).build();
                    dispatcher.execute(operation, result -> control.proceed());
                }
        };

        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), new SuccessfulOutcome(getEventBus(), resources) {
                    @Override
                    public void onSuccess(final FunctionContext context) {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().addSingleResourceSuccess(type)));
                        reload();
                    }
                }, functions);
    }
}
