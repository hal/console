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
package org.jboss.hal.client.configuration.subsystem.jmx;

import java.util.HashSet;
import java.util.List;
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
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.AUDIT_LOG_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.JMX_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.jmx.AddressTemplates.JMX_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLER;

/**
 * @author Harald Pehl
 */
public class JmxPresenter extends ApplicationFinderPresenter<JmxPresenter.MyView, JmxPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(JMX_ADDRESS)
    @NameToken(NameTokens.JMX)
    public interface MyProxy extends ProxyPlace<JmxPresenter> {}

    public interface MyView extends HalView, HasPresenter<JmxPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public JmxPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final Dispatcher dispatcher,
            @Footer final Provider<Progress> progress,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext, final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return JMX_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(JMX_TEMPLATE.lastValue());
    }

    @Override
    protected void onReset() {
        super.onReset();
        load();
    }

    void load() {
        crud.readRecursive(JMX_TEMPLATE, result -> getView().update(result));
    }

    void saveAuditLog(final Map<String, Object> changedValues, final boolean changedHandler,
            final List<String> handler) {
        if (!changedHandler) {
            crud.saveSingleton(Names.AUDIT_LOG, AUDIT_LOG_TEMPLATE, changedValues, this::load);
        } else {
            changedValues.remove(HANDLER);
            Function[] functions = {
                    new HandlerFunctions.SaveAuditLog(dispatcher, statementContext, changedValues),
                    new HandlerFunctions.ReadHandlers(dispatcher, statementContext),
                    new HandlerFunctions.MergeHandler(dispatcher, statementContext, new HashSet<>(handler))
            };
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new SuccessfulOutcome(getEventBus(), resources) {
                        @Override
                        public void onSuccess(final FunctionContext context) {
                            load();
                        }
                    }, functions);
        }
    }
}
