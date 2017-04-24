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
package org.jboss.hal.client.configuration;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BOOT_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SYSTEM_PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Harald Pehl
 */
public class SystemPropertiesPresenter
        extends MbuiPresenter<SystemPropertiesPresenter.MyView, SystemPropertiesPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.SYSTEM_PROPERTIES)
    public interface MyProxy extends ProxyPlace<SystemPropertiesPresenter> {}

    public interface MyView extends MbuiView<SystemPropertiesPresenter> {
        void update(List<NamedNode> systemProperties);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/system-property=*";
    private static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final CrudOperations crud;

    @Inject
    public SystemPropertiesPresenter(final EventBus eventBus,
            final SystemPropertiesPresenter.MyView view,
            final SystemPropertiesPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.SYSTEM_PROPERTIES),
                Names.CONFIGURATION, Names.SYSTEM_PROPERTIES);
    }

    @Override
    protected void reload() {
        crud.readChildren(ResourceAddress.root(), SYSTEM_PROPERTY,
                children -> getView().update(asNamedNodes(children)));
    }

    void add() {
        crud.add(Ids.SYSTEM_PROPERTY_ADD, Names.SYSTEM_PROPERTY, ROOT_TEMPLATE, asList(VALUE, BOOT_TIME),
                (name, model) -> reload());
    }
}
