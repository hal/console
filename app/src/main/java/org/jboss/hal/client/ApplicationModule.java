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
package org.jboss.hal.client;

import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.gwtplatform.mvp.shared.proxy.RouteTokenFormatter;
import org.jboss.hal.client.homepage.HomepagePresenter;
import org.jboss.hal.client.homepage.Templated_HomepageView_Provider;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.client.skeleton.Templated_FooterView_Provider;
import org.jboss.hal.client.skeleton.Templated_HeaderView_Provider;
import org.jboss.hal.core.HalPlaceManager;
import org.jboss.hal.core.gin.AbstractTemplatedPresenterModule;
import org.jboss.hal.spi.GinModule;

@GinModule
public class ApplicationModule extends AbstractTemplatedPresenterModule {

    @Override
    protected void configure() {
        DefaultModule defaultModule = new DefaultModule.Builder()
                .placeManager(HalPlaceManager.class)
                .tokenFormatter(RouteTokenFormatter.class)
                .build();
        install(defaultModule);

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.Homepage);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.Homepage);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.Homepage);

        bindTemplatedSingletonPresenterWidget(HeaderPresenter.class,
                HeaderPresenter.MyView.class,
                Templated_HeaderView_Provider.class);

        bindTemplatedSingletonPresenterWidget(FooterPresenter.class,
                FooterPresenter.MyView.class,
                Templated_FooterView_Provider.class);

        bindPresenter(ApplicationPresenter.class,
                ApplicationPresenter.MyView.class,
                ApplicationView.class,
                ApplicationPresenter.MyProxy.class);

        bindTemplatedPresenter(HomepagePresenter.class,
                HomepagePresenter.MyView.class,
                Templated_HomepageView_Provider.class,
                HomepagePresenter.MyProxy.class);
    }
}
