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
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.form.SubResourceProperties.MergeProperties;
import org.jboss.hal.core.mbui.form.SubResourceProperties.ReadProperties;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.HTTP_CONNECTOR_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.REMOTING_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.remoting.AddressTemplates.REMOTING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTING;

/**
 * @author Harald Pehl
 */
public class RemotingPresenter
        extends MbuiPresenter<RemotingPresenter.MyView, RemotingPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.REMOTING)
    @Requires(REMOTING_SUBSYSTEM_ADDRESS)
    public interface MyProxy extends ProxyPlace<RemotingPresenter> {}

    public interface MyView extends MbuiView<RemotingPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Provider<Progress> progress;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public RemotingPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final Dispatcher dispatcher,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.progress = progress;
        this.resources = resources;
        this.operationFactory = new OperationFactory();
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
        crud.readRecursive(REMOTING_SUBSYSTEM_TEMPLATE, result -> getView().update(result));
    }

    void saveConnector(final String name, Map<String, Object> changedValues,
            boolean propertiesModified, Map<String, String> properties) {
        changedValues.remove(PROPERTY);
        if (!propertiesModified) {
            if (!changedValues.isEmpty()) {
                crud.save(Names.REMOTE_CONNECTOR, name, CONNECTOR_TEMPLATE, changedValues, this::reload);
            }
        } else {
            saveWithProperties(Names.REMOTE_CONNECTOR, name, CONNECTOR_TEMPLATE.replaceWildcards(name),
                    changedValues, properties);
        }
    }

    void saveHttpConnector(String name, Map<String, Object> changedValues,
            boolean propertiesModified, Map<String, String> properties) {
        changedValues.remove(PROPERTY);
        if (!propertiesModified) {
            if (!changedValues.isEmpty()) {
                crud.save(Names.HTTP_CONNECTOR, name, HTTP_CONNECTOR_TEMPLATE, changedValues, this::reload);
            }
        } else {
            saveWithProperties(Names.HTTP_CONNECTOR, name, HTTP_CONNECTOR_TEMPLATE.replaceWildcards(name),
                    changedValues, properties);
        }
    }

    private void saveWithProperties(String type, String name, AddressTemplate template,
            Map<String, Object> changedValues, Map<String, String> properties) {
        Function[] functions = new Function[]{
                control -> {
                    if (changedValues.isEmpty()) {
                        control.proceed();
                    } else {
                        Composite operation = operationFactory
                                .fromChangeSet(template.resolve(statementContext), changedValues);
                        dispatcher.executeInFunction(control, operation, (CompositeResult result) -> control.proceed());
                    }
                },
                new ReadProperties(dispatcher, statementContext, template, PROPERTY),
                new MergeProperties(dispatcher, statementContext, template, PROPERTY, properties)
        };
        new Async<FunctionContext>(progress.get())
                .waterfall(new FunctionContext(), new SuccessfulOutcome(getEventBus(), resources) {
                    @Override
                    public void onSuccess(final FunctionContext context) {
                        MessageEvent.fire(getEventBus(),
                                Message.success(resources.messages().modifyResourceSuccess(type, name)));
                        reload();
                    }
                }, functions);
    }
}
