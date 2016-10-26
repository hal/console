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
package org.jboss.hal.client.configuration.subsystem.jca;

import java.util.Map;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.JCA_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.JCA_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.TRACER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;

/**
 * @author Harald Pehl
 */
public class JcaPresenter
        extends ApplicationFinderPresenter<JcaPresenter.MyView, JcaPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(JCA_ADDRESS)
    @NameToken(NameTokens.JCA)
    public interface MyProxy extends ProxyPlace<JcaPresenter> {}

    public interface MyView extends HalView, HasPresenter<JcaPresenter> {
        void reset();
        void update(ModelNode payload);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final OperationFactory operationFactory;

    @Inject
    public JcaPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);

        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.operationFactory = new OperationFactory();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().reset();
    }

    @Override
    public ResourceAddress resourceAddress() {
        return JCA_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(JCA_TEMPLATE.lastValue());
    }

    @Override
    protected void onReset() {
        super.onReset();
        load();
    }


    // ------------------------------------------------------ generic crud methods

    void load() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, JCA_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    void add(final String type, final String name, final AddressTemplate template, final ModelNode model) {
        Operation operation = new Operation.Builder(ADD, template.resolve(statementContext, name))
                .payload(model)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().addResourceSuccess(type, name)));
            load();
        });
    }

    void saveSingleton(final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        Composite operation = operationFactory.fromChangeSet(template.resolve(statementContext), changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
            load();
        });
    }

    void saveResource(final AddressTemplate template, final String name, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        Composite operation = operationFactory.fromChangeSet(template.resolve(statementContext), changedValues);
        dispatcher.execute(operation, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(), Message.success(successMessage));
            load();
        });
    }


    // ------------------------------------------------------ tracer specific methods

    void addTracer() {
        String type = new LabelBuilder().label(TRACER_TEMPLATE.lastKey());
        Operation operation = new Operation.Builder(ADD, TRACER_TEMPLATE.resolve(statementContext)).build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(getEventBus(), Message.success(resources.messages().addSingleResourceSuccess(type)));
            load();
        });
    }

    Operation lookupTracerOp() {
        return new Operation.Builder(READ_RESOURCE_OPERATION, TRACER_TEMPLATE.resolve(statementContext)).build();
    }
}
