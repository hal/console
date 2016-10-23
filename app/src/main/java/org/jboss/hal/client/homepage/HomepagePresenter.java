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
package org.jboss.hal.client.homepage;

import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.TopLevelPresenter;
import org.jboss.hal.meta.token.NameTokens;

import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class HomepagePresenter extends TopLevelPresenter<HomepagePresenter.MyView, HomepagePresenter.MyProxy> {

    // @formatter:off
    @NoGatekeeper
    @ProxyStandard
    @NameToken(NameTokens.HOMEPAGE)
    public interface MyProxy extends ProxyPlace<HomepagePresenter> {}

    public interface MyView extends HalView, HasPresenter<HomepagePresenter> {}
    // @formatter:on


    @Inject
    public HomepagePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    void launchGuidedTour() {
        Window.alert(NYI);
    }
}
