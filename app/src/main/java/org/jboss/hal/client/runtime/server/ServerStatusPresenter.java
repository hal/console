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
package org.jboss.hal.client.runtime.server;

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
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.meta.token.NameTokens.SERVER_STATUS;

/**
 * @author Harald Pehl
 */
public class ServerStatusPresenter
        extends ApplicationFinderPresenter<ServerStatusPresenter.MyView, ServerStatusPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(SERVER_STATUS)
    @Requires(SERVER_STATUS_ADDRESS)
    public interface MyProxy extends ProxyPlace<ServerStatusPresenter> {}

    public interface MyView extends HalView {
        void update(ModelNode modelNode);
    }
    // @formatter:on


    static final String SERVER_STATUS_ADDRESS = "/{selected.host}/{selected.server}/core-service=platform-mbean/type=runtime";
    static final AddressTemplate SERVER_STATUS_TEMPLATE = AddressTemplate.of(SERVER_STATUS_ADDRESS);

    static final String BOOT_CLASS_PATH = "boot-class-path";
    static final String CLASS_PATH = "class-path";
    static final String INPUT_ARGUMENTS = "input-arguments";
    static final String JVM_VERSION = "jvm-version";
    static final String LIBRARY_PATH = "library-path";
    static final String OS_NAME = "os";
    static final String OS_VERSION = "os-version";
    static final String PROCESSORS = "processors";
    static final String START_TIME = "start-time";
    static final String SYSTEM_PROPERTIES = "system-properties";
    static final String UPTIME = "uptime";

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public ServerStatusPresenter(final EventBus eventBus,
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
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.SERVER_MONITOR, Ids.asId(resources.constants().status()),
                        resources.constants().monitor(), resources.constants().status());
    }

    @Override
    protected void reload() {
        ResourceAddress address = SERVER_STATUS_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> getView().update(result));
    }
}
