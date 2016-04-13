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

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.gwtplatform.mvp.shared.proxy.RouteTokenFormatter;
import org.jboss.hal.client.accesscontrol.AccessControlPresenter;
import org.jboss.hal.client.accesscontrol.AccessControlView;
import org.jboss.hal.client.configuration.ConfigurationPresenter;
import org.jboss.hal.client.configuration.ConfigurationView;
import org.jboss.hal.client.configuration.InterfacePresenter;
import org.jboss.hal.client.configuration.InterfaceView;
import org.jboss.hal.client.configuration.PathsPresenter;
import org.jboss.hal.client.configuration.PathsView;
import org.jboss.hal.client.configuration.subsystem.GenericSubsystemPresenter;
import org.jboss.hal.client.configuration.subsystem.GenericSubsystemView;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceView;
import org.jboss.hal.client.deployment.DeploymentPresenter;
import org.jboss.hal.client.deployment.DeploymentView;
import org.jboss.hal.client.homepage.HomepagePresenter;
import org.jboss.hal.client.homepage.HomepageView;
import org.jboss.hal.client.patching.PatchingPresenter;
import org.jboss.hal.client.patching.PatchingView;
import org.jboss.hal.client.runtime.RuntimePresenter;
import org.jboss.hal.client.runtime.RuntimeView;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.client.skeleton.Templated_FooterView_Provider;
import org.jboss.hal.client.skeleton.Templated_HeaderView_Provider;
import org.jboss.hal.client.tools.MacroEditorPresenter;
import org.jboss.hal.client.tools.MacroEditorView;
import org.jboss.hal.client.tools.ModelBrowserPresenter;
import org.jboss.hal.client.tools.ModelBrowserView;
import org.jboss.hal.client.utb.UnderTheBridgePresenter;
import org.jboss.hal.client.utb.UnderTheBridgeView;
import org.jboss.hal.core.gin.AbstractTemplatedPresenterModule;
import org.jboss.hal.core.mvp.HalPlaceManager;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.GinModule;

@GinModule
public class AppModule extends AbstractTemplatedPresenterModule {

    @Override
    protected void configure() {

        // ------------------------------------------------------ GWTP

        DefaultModule defaultModule = new DefaultModule.Builder()
                .placeManager(HalPlaceManager.class)
                .tokenFormatter(RouteTokenFormatter.class)
                .build();
        install(defaultModule);

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.HOMEPAGE);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.HOMEPAGE);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.HOMEPAGE);


        // ------------------------------------------------------ misc

        bind(DataSourceTemplates.class).in(Singleton.class);


        // ------------------------------------------------------ skeleton & root presenter

        bindTemplatedSingletonPresenterWidget(HeaderPresenter.class,
                HeaderPresenter.MyView.class,
                Templated_HeaderView_Provider.class);

        bindTemplatedSingletonPresenterWidget(FooterPresenter.class,
                FooterPresenter.MyView.class,
                Templated_FooterView_Provider.class);

        bindPresenter(RootPresenter.class,
                RootPresenter.MyView.class,
                RootView.class,
                RootPresenter.MyProxy.class);


        // ------------------------------------------------------ remaining presenter (A-Z)

        bindPresenter(AccessControlPresenter.class,
                AccessControlPresenter.MyView.class,
                AccessControlView.class,
                AccessControlPresenter.MyProxy.class);

        bindPresenter(ConfigurationPresenter.class,
                ConfigurationPresenter.MyView.class,
                ConfigurationView.class,
                ConfigurationPresenter.MyProxy.class);

        bindPresenter(DataSourcePresenter.class,
                DataSourcePresenter.MyView.class,
                DataSourceView.class,
                DataSourcePresenter.MyProxy.class);

        bindPresenter(DeploymentPresenter.class,
                DeploymentPresenter.MyView.class,
                DeploymentView.class,
                DeploymentPresenter.MyProxy.class);

        bindPresenter(GenericSubsystemPresenter.class,
                GenericSubsystemPresenter.MyView.class,
                GenericSubsystemView.class,
                GenericSubsystemPresenter.MyProxy.class);

        bindPresenter(HomepagePresenter.class,
                HomepagePresenter.MyView.class,
                HomepageView.class,
                HomepagePresenter.MyProxy.class);

        bindPresenter(InterfacePresenter.class,
                InterfacePresenter.MyView.class,
                InterfaceView.class,
                InterfacePresenter.MyProxy.class);

        bindPresenter(MacroEditorPresenter.class,
                MacroEditorPresenter.MyView.class,
                MacroEditorView.class,
                MacroEditorPresenter.MyProxy.class);

        bindPresenter(ModelBrowserPresenter.class,
                ModelBrowserPresenter.MyView.class,
                ModelBrowserView.class,
                ModelBrowserPresenter.MyProxy.class);

        bindPresenter(PathsPresenter.class,
                PathsPresenter.MyView.class,
                PathsView.class,
                PathsPresenter.MyProxy.class);

        bindPresenter(PatchingPresenter.class,
                PatchingPresenter.MyView.class,
                PatchingView.class,
                PatchingPresenter.MyProxy.class);

        bindPresenter(RuntimePresenter.class,
                RuntimePresenter.MyView.class,
                RuntimeView.class,
                RuntimePresenter.MyProxy.class);

        bindPresenter(UnderTheBridgePresenter.class,
                UnderTheBridgePresenter.MyView.class,
                UnderTheBridgeView.class,
                UnderTheBridgePresenter.MyProxy.class);
    }
}
