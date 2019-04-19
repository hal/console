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
package org.jboss.hal.client;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.header.HeaderModeEvent;
import org.jboss.hal.core.mvp.HalPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.meta.token.NameTokens;

import static org.jboss.hal.client.RootPresenter.CLEAR_FOOTER;
import static org.jboss.hal.client.RootPresenter.CLEAR_HEADER;

public class UnauthorizedPresenter
        extends HalPresenter<UnauthorizedPresenter.MyView, UnauthorizedPresenter.MyProxy> {

    @Inject
    public UnauthorizedPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void onBind() {
        super.onBind();
        clearSlot(CLEAR_HEADER);
        clearSlot(CLEAR_FOOTER);
    }

    @Override
    protected HeaderModeEvent headerMode() {
        return null;
    }

    // @formatter:off
    @NoGatekeeper
    @ProxyStandard
    @NameToken(NameTokens.UNAUTHORIZED)
    public interface MyProxy extends ProxyPlace<UnauthorizedPresenter> {
    }

    public interface MyView extends HalView {
    }
    // @formatter:on
}
