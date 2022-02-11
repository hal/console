/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.management;

import javax.inject.Inject;

import org.jboss.hal.core.accesscontrol.SensitiveGatekeeper;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.FinderView;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ManagementPresenter extends FinderPresenter<ManagementPresenter.MyView, ManagementPresenter.MyProxy> {

    @Inject
    public ManagementPresenter(EventBus eventBus,
            ManagementPresenter.MyView view,
            ManagementPresenter.MyProxy proxy,
            Finder finder,
            Resources resources) {
        super(eventBus, view, proxy, finder, resources);
    }

    @Override
    protected String initialColumn() {
        return Ids.MANAGEMENT;
    }

    @Override
    protected PreviewContent initialPreview() {
        return new PreviewContent(Names.MANAGEMENT, resources.previews().managementOverview());
    }

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.MANAGEMENT)
    @UseGatekeeper(SensitiveGatekeeper.class)
    public interface MyProxy extends ProxyPlace<ManagementPresenter> {
    }

    public interface MyView extends FinderView {
    }
    // @formatter:on
}
