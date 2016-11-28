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
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.SupportsExternalMode;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

/**
 * Presenter which uses the {@link org.jboss.hal.core.modelbrowser.ModelBrowser} to view and modify the management
 * model.
 *
 * @author Harald Pehl
 */
public class ModelBrowserPresenter
        extends ApplicationPresenter<ModelBrowserPresenter.MyView, ModelBrowserPresenter.MyProxy>
        implements HasTitle, SupportsExternalMode {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MODEL_BROWSER)
    public interface MyProxy extends ProxyPlace<ModelBrowserPresenter> {}

    public interface MyView extends HalView {
        void setRoot(ResourceAddress root);
    }
    // @formatter:on

    private final Resources resources;

    @Inject
    public ModelBrowserPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Resources resources) {
        super(eventBus, view, proxy);
        this.resources = resources;
    }

    @Override
    public String getTitle() {
        return resources.constants().modelBrowser();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setRoot(ResourceAddress.root());
    }
}
