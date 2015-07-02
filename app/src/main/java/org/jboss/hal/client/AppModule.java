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

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import org.jboss.hal.client.homepage.HomepagePresenter;
import org.jboss.hal.client.homepage.HomepageView;
import org.jboss.hal.client.widget.FooterPresenter;
import org.jboss.hal.client.widget.FooterView;
import org.jboss.hal.client.widget.HeaderPresenter;
import org.jboss.hal.client.widget.HeaderView;
import org.jboss.hal.spi.GinModule;

/**
 * @author Harald Pehl
 */
@GinModule
public class AppModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        DefaultModule defaultModule = new DefaultModule.Builder()
                .defaultPlace(NameTokens.Homepage)
                .errorPlace(NameTokens.Homepage)
                .unauthorizedPlace(NameTokens.Homepage)
                .build();
        install(defaultModule);

        bindSingletonPresenterWidget(HeaderPresenter.class,
                HeaderPresenter.MyView.class,
                HeaderView.class);

        bindSingletonPresenterWidget(FooterPresenter.class,
                FooterPresenter.MyView.class,
                FooterView.class);

        bindPresenter(HomepagePresenter.class,
                HomepagePresenter.MyView.class,
                HomepageView.class,
                HomepagePresenter.MyProxy.class);
    }
}
