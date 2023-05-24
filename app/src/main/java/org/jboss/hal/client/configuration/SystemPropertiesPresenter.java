/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.jboss.hal.dmr.ModelDescriptionConstants.BOOT_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SYSTEM_PROPERTY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class SystemPropertiesPresenter
        extends ApplicationFinderPresenter<SystemPropertiesPresenter.MyView, SystemPropertiesPresenter.MyProxy> {

    static final String ROOT_ADDRESS = "/system-property=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final Environment environment;
    private final CrudOperations crud;

    @Inject
    public SystemPropertiesPresenter(EventBus eventBus,
            SystemPropertiesPresenter.MyView view,
            SystemPropertiesPresenter.MyProxy proxy,
            Finder finder,
            Environment environment,
            CrudOperations crud) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
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
        if (environment.isStandalone()) {
            crud.add(Ids.SYSTEM_PROPERTY_ADD, Names.SYSTEM_PROPERTY, ROOT_TEMPLATE, singletonList(VALUE),
                    (name, model) -> reload());
        } else {
            crud.add(Ids.SYSTEM_PROPERTY_ADD, Names.SYSTEM_PROPERTY, ROOT_TEMPLATE, asList(VALUE, BOOT_TIME),
                    (name, model) -> reload());
        }
    }

    void save(Form<NamedNode> form, Map<String, Object> changedValues) {
        crud.save(Names.SYSTEM_PROPERTY, form.getModel().getName(), ROOT_TEMPLATE, changedValues, this::reload);
    }

    void reset(Form<NamedNode> form, Metadata metadata) {
        crud.reset(Names.SYSTEM_PROPERTY, form.getModel().getName(), ROOT_TEMPLATE, form, metadata,
                new Form.FinishReset<NamedNode>(form) {
                    @Override
                    public void afterReset(Form<NamedNode> form) {
                        reload();
                    }
                });
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.SYSTEM_PROPERTIES)
    public interface MyProxy extends ProxyPlace<SystemPropertiesPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<SystemPropertiesPresenter> {
        void update(List<NamedNode> systemProperties);
    }
    // @formatter:on
}
