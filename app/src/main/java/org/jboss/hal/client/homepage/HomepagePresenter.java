/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.homepage;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Slots;
import org.jboss.hal.core.TopLevelCategory;

import javax.inject.Inject;

import static org.jboss.hal.meta.token.NameTokens.HOMEPAGE;
import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class HomepagePresenter extends Presenter<HomepagePresenter.MyView, HomepagePresenter.MyProxy>
        implements TopLevelCategory {

    // @formatter:off
    @NoGatekeeper
    @ProxyStandard
    @NameToken(HOMEPAGE)
    public interface MyProxy extends ProxyPlace<HomepagePresenter> {}

    public interface MyView extends View, HasPresenter<HomepagePresenter> {}
    // @formatter:on


    @Inject
    public HomepagePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
        super(eventBus, view, proxy, Slots.MAIN);
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    public void launchGuidedTour() {
        Window.alert(NYI);
    }
}
