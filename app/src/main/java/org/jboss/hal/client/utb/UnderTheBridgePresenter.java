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
package org.jboss.hal.client.utb;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.TopLevelPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.token.NameTokens;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class UnderTheBridgePresenter
        extends TopLevelPresenter<UnderTheBridgePresenter.MyView, UnderTheBridgePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.UNDER_THE_BRIDGE)
    public interface MyProxy extends ProxyPlace<UnderTheBridgePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<UnderTheBridgePresenter> {
        void show(ModelNode model);
    }
    // @formatter:on

    private ModelNode model;

    @Inject
    public UnderTheBridgePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
        super(eventBus, view, proxy);
        model = new ModelNode();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().show(model);
    }

    public void saveModel(final ModelNode model) {
        this.model = model;
        getView().show(model);
    }
}
