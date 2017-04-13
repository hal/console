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
package org.jboss.hal.client.accesscontrol;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.accesscontrol.SensitiveGatekeeper;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.FinderView;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.accesscontrol.AddressTemplates.HOST_SCOPED_ROLE_ADDRESS;
import static org.jboss.hal.client.accesscontrol.AddressTemplates.ROLE_MAPPING_ADDRESS;
import static org.jboss.hal.client.accesscontrol.AddressTemplates.SERVER_GROUP_SCOPED_ROLE_ADDRESS;

/**
 * @author Harald Pehl
 */
public class AccessControlPresenter extends
        FinderPresenter<AccessControlPresenter.MyView, AccessControlPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.ACCESS_CONTROL)
    @UseGatekeeper(SensitiveGatekeeper.class)
    @Requires({ROLE_MAPPING_ADDRESS, HOST_SCOPED_ROLE_ADDRESS, SERVER_GROUP_SCOPED_ROLE_ADDRESS})
    public interface MyProxy extends ProxyPlace<AccessControlPresenter> {}

    public interface MyView extends FinderView {}
    // @formatter:on

    private final Environment environment;
    private final AccessControl accessControl;

    @Inject
    public AccessControlPresenter(final EventBus eventBus,
            final MyView view, final MyProxy myProxy, final Finder finder,
            final Environment environment, final AccessControl accessControl,
            final Resources resources) {
        super(eventBus, view, myProxy, finder, resources);
        this.environment = environment;
        this.accessControl = accessControl;
    }

    @Override
    protected void onReset() {
        accessControl.reload(() -> super.onReset());
    }

    @Override
    protected String initialColumn() {
        return Ids.ACCESS_CONTROL_BROWSE_BY;
    }

    @Override
    protected PreviewContent initialPreview() {
        return new AccessControlPreview(accessControl, environment, resources);
    }
}
