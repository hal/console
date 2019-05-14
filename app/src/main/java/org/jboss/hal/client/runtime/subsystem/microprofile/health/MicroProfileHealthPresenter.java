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
package org.jboss.hal.client.runtime.subsystem.microprofile.health;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.microprofile.health.AddressTemplates.MICROPROFILE_HEALTH_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.microprofile.health.AddressTemplates.MICROPROFILE_HEALTH_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHECK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MICROPROFILE_HEALTH_SMALLRYE;

public class MicroProfileHealthPresenter
        extends ApplicationFinderPresenter<MicroProfileHealthPresenter.MyView, MicroProfileHealthPresenter.MyProxy> {

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public MicroProfileHealthPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
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
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, MICROPROFILE_HEALTH_SMALLRYE,
                        resources.constants().monitor(), Names.MICROPROFILE_HEALTH);
    }

    @Override
    protected void reload() {
        ResourceAddress address = MICROPROFILE_HEALTH_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, CHECK)
                .build();
        dispatcher.execute(operation, result -> getView().update(result));
    }

    StatementContext getStatementContext() {
        return statementContext;
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(MICROPROFILE_HEALTH_ADDRESS)
    @NameToken(NameTokens.MICRO_PROFILE_HEALTH)
    public interface MyProxy extends ProxyPlace<MicroProfileHealthPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<MicroProfileHealthPresenter> {
        void update(ModelNode model);
    }
    // @formatter:on
}
