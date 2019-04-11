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
package org.jboss.hal.client;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.header.PresenterType;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.mvp.SupportsExternalMode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;

/**
 * Presenter which uses the {@link org.jboss.hal.core.modelbrowser.ModelBrowser} to view and modify a resource
 * specified by {@link SupportsExpertMode#resourceAddress()}.
 */
public class ExpertModePresenter
        extends ApplicationPresenter<ExpertModePresenter.MyView, ExpertModePresenter.MyProxy>
        implements SupportsExternalMode {

    private final StatementContext statementContext;
    private ResourceAddress address;

    @Inject
    public ExpertModePresenter(EventBus eventBus, MyView view, MyProxy myProxy, StatementContext statementContext) {
        super(eventBus, view, myProxy);
        this.statementContext = statementContext;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        String parameter = request.getParameter(Places.ADDRESS_PARAM, null);
        if (parameter != null) {
            AddressTemplate template = AddressTemplate.of(parameter);
            address = template.resolve(statementContext);
        } else {
            address = ResourceAddress.root();
        }
    }

    @Override
    protected HeaderModeEvent headerMode() {
        return new HeaderModeEvent.Builder(PresenterType.APPLICATION)
                .supportsExternal(true)
                .backToNormalMode(true)
                .build();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setRoot(address);
    }


    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.EXPERT_MODE)
    public interface MyProxy extends ProxyPlace<ExpertModePresenter> {
    }

    public interface MyView extends HalView {
        void setRoot(ResourceAddress root);
    }
    // @formatter:on
}
