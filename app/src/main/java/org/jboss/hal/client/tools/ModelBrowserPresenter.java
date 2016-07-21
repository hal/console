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
import org.jboss.hal.core.mvp.FullscreenPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

/**
 * Presenter for the model browser to view and modify the management model.
 *
 * @author Harald Pehl
 */
public class ModelBrowserPresenter
        extends FullscreenPresenter<ModelBrowserPresenter.MyView, ModelBrowserPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MODEL_BROWSER)
    public interface MyProxy extends ProxyPlace<ModelBrowserPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<ModelBrowserPresenter> {}
    // @formatter:on

    public static final String EXTERNAL_PARAM = "external";
    private boolean external;

    @Inject
    public ModelBrowserPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Resources resources) {
        super(eventBus, view, proxy, resources.constants().modelBrowser());
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        external = Boolean.parseBoolean(request.getParameter(EXTERNAL_PARAM, "false")); //NON-NLS
    }

    boolean isExternal() {
        return external;
    }
}
