/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.management;

import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ManagementInterfacePresenter
        extends MbuiPresenter<ManagementInterfacePresenter.MyView, ManagementInterfacePresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String HTTP_INTERFACE_ADDRESS = "{domain.controller}/core-service=management/management-interface=http-interface";
    static final AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(HTTP_INTERFACE_ADDRESS);
    private static Logger logger = LoggerFactory.getLogger(ManagementInterfacePresenter.class);

    private final CrudOperations crud;
    private final StatementContext statementContext;
    private Resources resources;
    private Environment environment;
    private Dispatcher dispatcher;
    private Provider<Progress> progress;

    // @Inject
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

    // @formatter:off
    // @ProxyCodeSplit
    // @NameToken(NameTokens.MANAGEMENT_INTERFACE)
    // @Requires(value = HTTP_INTERFACE_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<ManagementInterfacePresenter> {
    }

    public interface MyView extends MbuiView<ManagementInterfacePresenter> {
        void update(ModelNode model);
    }
    // @formatter:on

}
