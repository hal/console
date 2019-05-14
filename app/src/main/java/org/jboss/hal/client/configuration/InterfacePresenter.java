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
package org.jboss.hal.client.configuration;

import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

@SuppressWarnings("SpellCheckingInspection")
public class InterfacePresenter
        extends MbuiPresenter<InterfacePresenter.MyView, InterfacePresenter.MyProxy>
        implements SupportsExpertMode {

    static final String ROOT_ADDRESS = "/interface=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private String interfce;

    @Inject
    public InterfacePresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        interfce = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ROOT_TEMPLATE.resolve(statementContext, interfce);
    }

    @Override
    public FinderPath finderPath() {
        return new FinderPath()
                .append(Ids.CONFIGURATION, Ids.asId(Names.INTERFACES), Names.CONFIGURATION, Names.INTERFACES)
                .append(Ids.INTERFACE, interfce, Names.INTERFACE, interfce);
    }

    @Override
    protected void reload() {
        crud.read(ROOT_TEMPLATE.resolve(statementContext, interfce), result -> getView().update(result));
    }

    @SuppressWarnings("UnusedParameters")
    void saveInterface(final Form<ModelNode> form, final Map<String, Object> changedValues) {
        crud.save(Names.INTERFACE, interfce, ROOT_TEMPLATE, changedValues, this::reload);
    }

    void resetInterface(final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(ROOT_TEMPLATE);
        crud.reset(Names.INTERFACE, interfce, ROOT_TEMPLATE, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(ROOT_ADDRESS)
    @NameToken(NameTokens.INTERFACE)
    public interface MyProxy extends ProxyPlace<InterfacePresenter> {
    }

    public interface MyView extends MbuiView<InterfacePresenter> {
        void update(ModelNode interfce);
    }
    // @formatter:on
}
