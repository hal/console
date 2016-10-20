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
package org.jboss.hal.client.tools;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.mvp.ExternalMode;
import org.jboss.hal.core.mvp.FullscreenPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.core.mvp.Places.ADDRESS_PARAM;

/**
 * Presenter for the model browser to view and modify the management model.
 *
 * @author Harald Pehl
 */
public class ModelBrowserPresenter
        extends FullscreenPresenter<ModelBrowserPresenter.MyView, ModelBrowserPresenter.MyProxy>
        implements ExternalMode {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MODEL_BROWSER)
    public interface MyProxy extends ProxyPlace<ModelBrowserPresenter> {}

    public interface MyView extends PatternFlyView {
        void setRoot(ResourceAddress root);
    }
    // @formatter:on

    private final StatementContext statementContext;
    private ResourceAddress address;
    private boolean external;

    @Inject
    public ModelBrowserPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, resources.constants().modelBrowser());
        this.statementContext = statementContext;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        String addressParameter = request.getParameter(ADDRESS_PARAM, null);
        if (addressParameter != null) {
            address = AddressTemplate.of(addressParameter).resolve(statementContext);
        } else {
            address = ResourceAddress.ROOT;
        }
        external = Boolean.parseBoolean(request.getParameter(Places.EXTERNAL_PARAM, String.valueOf(false)));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        if (external) {
            Skeleton.externalMode();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setRoot(address);
    }
}
