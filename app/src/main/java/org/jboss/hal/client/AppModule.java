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
import org.jboss.gwt.circuit.Dispatcher;
import org.jboss.gwt.circuit.dag.DAGDispatcher;
import org.jboss.hal.client.accesscontrol.AccessControl;
import org.jboss.hal.client.accesscontrol.AccessControlPresenter;
import org.jboss.hal.client.accesscontrol.AccessControlTokens;
import org.jboss.hal.client.accesscontrol.AccessControlView;
import org.jboss.hal.client.configuration.ConfigurationPresenter;
import org.jboss.hal.client.configuration.ConfigurationView;
import org.jboss.hal.client.configuration.InterfacePresenter;
import org.jboss.hal.client.configuration.Mbui_InterfaceView_Provider;
import org.jboss.hal.client.configuration.Mbui_PathsView_Provider;
import org.jboss.hal.client.configuration.PathsPresenter;
import org.jboss.hal.client.configuration.UpdatePathAutoComplete;
import org.jboss.hal.client.configuration.subsystem.batch.BatchPresenter;
import org.jboss.hal.client.configuration.subsystem.batch.Mbui_BatchView_Provider;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.client.configuration.subsystem.deploymentscanner.DeploymentScannerPresenter;
import org.jboss.hal.client.configuration.subsystem.deploymentscanner.Mbui_DeploymentScannerView_Provider;
import org.jboss.hal.client.configuration.subsystem.ee.EEPresenter;
import org.jboss.hal.client.configuration.subsystem.ee.EEView;
import org.jboss.hal.client.configuration.subsystem.ejb.EjbPresenter;
import org.jboss.hal.client.configuration.subsystem.ejb.Mbui_EjbView_Provider;
import org.jboss.hal.client.configuration.subsystem.iiop.IiopPresenter;
import org.jboss.hal.client.configuration.subsystem.iiop.Mbui_IiopView_Provider;
import org.jboss.hal.client.configuration.subsystem.io.IOPresenter;
import org.jboss.hal.client.configuration.subsystem.io.Mbui_IOView_Provider;
import org.jboss.hal.client.configuration.subsystem.jca.JcaPresenter;
import org.jboss.hal.client.configuration.subsystem.jca.JcaView;
import org.jboss.hal.client.configuration.subsystem.jmx.JmxPresenter;
import org.jboss.hal.client.configuration.subsystem.jmx.JmxView;
import org.jboss.hal.client.configuration.subsystem.logging.LoggingPresenter;
import org.jboss.hal.client.configuration.subsystem.logging.LoggingProfilePresenter;
import org.jboss.hal.client.configuration.subsystem.logging.Mbui_LoggingProfileView_Provider;
import org.jboss.hal.client.configuration.subsystem.logging.Mbui_LoggingView_Provider;
import org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter;
import org.jboss.hal.client.configuration.subsystem.mail.MailSessionView;
import org.jboss.hal.client.configuration.subsystem.transactions.Mbui_TransactionView_Provider;
import org.jboss.hal.client.configuration.subsystem.transactions.TransactionPresenter;
import org.jboss.hal.client.deployment.BrowseContentPresenter;
import org.jboss.hal.client.deployment.BrowseContentView;
import org.jboss.hal.client.deployment.DeploymentPresenter;
import org.jboss.hal.client.deployment.DeploymentView;
import org.jboss.hal.client.deployment.ServerGroupDeploymentPresenter;
import org.jboss.hal.client.deployment.ServerGroupDeploymentView;
import org.jboss.hal.client.deployment.StandaloneDeploymentPresenter;
import org.jboss.hal.client.deployment.StandaloneDeploymentView;
import org.jboss.hal.client.homepage.HomepagePresenter;
import org.jboss.hal.client.homepage.HomepageView;
import org.jboss.hal.client.patching.PatchingPresenter;
import org.jboss.hal.client.patching.PatchingView;
import org.jboss.hal.client.rhcp.RhcpPresenter;
import org.jboss.hal.client.rhcp.RhcpView;
import org.jboss.hal.client.rhcp.UnderTheBridgePresenter;
import org.jboss.hal.client.rhcp.UnderTheBridgeView;
import org.jboss.hal.client.runtime.ProcessStateHandler;
import org.jboss.hal.client.runtime.RuntimePresenter;
import org.jboss.hal.client.runtime.RuntimeView;
import org.jboss.hal.client.runtime.group.Mbui_ServerGroupView_Provider;
import org.jboss.hal.client.runtime.group.ServerGroupPresenter;
import org.jboss.hal.client.runtime.host.HostPresenter;
import org.jboss.hal.client.runtime.host.Mbui_HostView_Provider;
import org.jboss.hal.client.runtime.server.Mbui_ServerView_Provider;
import org.jboss.hal.client.runtime.server.ServerPresenter;
import org.jboss.hal.client.runtime.server.ServerStatusPresenter;
import org.jboss.hal.client.runtime.server.ServerStatusView;
import org.jboss.hal.client.runtime.subsystem.jndi.JndiPresenter;
import org.jboss.hal.client.runtime.subsystem.jndi.JndiView;
import org.jboss.hal.client.runtime.subsystem.jpa.JpaPresenter;
import org.jboss.hal.client.runtime.subsystem.jpa.JpaView;
import org.jboss.hal.client.runtime.subsystem.logging.LogFilePresenter;
import org.jboss.hal.client.runtime.subsystem.logging.Templated_LogFileView_Provider;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.client.skeleton.Templated_FooterView_Provider;
import org.jboss.hal.client.skeleton.Templated_HeaderView_Provider;
import org.jboss.hal.client.tools.MacroEditorPresenter;
import org.jboss.hal.client.tools.MacroEditorView;
import org.jboss.hal.client.tools.ModelBrowserPresenter;
import org.jboss.hal.client.tools.ModelBrowserView;
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
                .build();
        install(defaultModule);

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.HOMEPAGE);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.HOMEPAGE);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.HOMEPAGE);


        // ------------------------------------------------------ misc

        bind(AccessControl.class).in(Singleton.class);
        bind(AccessControlTokens.class).in(Singleton.class);
        bind(DataSourceTemplates.class).in(Singleton.class);
        bind(Dispatcher.class).to(DAGDispatcher.class).in(Singleton.class);
        bind(ProcessStateHandler.class).asEagerSingleton(); // to register the event handler
        bind(UpdatePathAutoComplete.class).asEagerSingleton(); // to register the event handler


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

        bindTemplatedPresenter(BatchPresenter.class,
                BatchPresenter.MyView.class,
                Mbui_BatchView_Provider.class,
                BatchPresenter.MyProxy.class);

        bindPresenter(BrowseContentPresenter.class,
                BrowseContentPresenter.MyView.class,
                BrowseContentView.class,
                BrowseContentPresenter.MyProxy.class);

        bindPresenter(ConfigurationPresenter.class,
                ConfigurationPresenter.MyView.class,
                ConfigurationView.class,
                ConfigurationPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourceView.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourceView.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.MyProxy.class);

        bindPresenter(DeploymentPresenter.class,
                DeploymentPresenter.MyView.class,
                DeploymentView.class,
                DeploymentPresenter.MyProxy.class);

        bindTemplatedPresenter(DeploymentScannerPresenter.class,
                DeploymentScannerPresenter.MyView.class,
                Mbui_DeploymentScannerView_Provider.class,
                DeploymentScannerPresenter.MyProxy.class);

        bindTemplatedPresenter(EjbPresenter.class,
                EjbPresenter.MyView.class,
                Mbui_EjbView_Provider.class,
                EjbPresenter.MyProxy.class);

        bindPresenter(EEPresenter.class,
                EEPresenter.MyView.class,
                EEView.class,
                EEPresenter.MyProxy.class);

        bindPresenter(ExpertModePresenter.class,
                ExpertModePresenter.MyView.class,
                ExpertModeView.class,
                ExpertModePresenter.MyProxy.class);

        bindPresenter(GenericSubsystemPresenter.class,
                GenericSubsystemPresenter.MyView.class,
                GenericSubsystemView.class,
                GenericSubsystemPresenter.MyProxy.class);

        bindPresenter(HomepagePresenter.class,
                HomepagePresenter.MyView.class,
                HomepageView.class,
                HomepagePresenter.MyProxy.class);
        
        bindTemplatedPresenter(HostPresenter.class,
                HostPresenter.MyView.class,
                Mbui_HostView_Provider.class,
                HostPresenter.MyProxy.class);

        bindTemplatedPresenter(IiopPresenter.class,
                IiopPresenter.MyView.class,
                Mbui_IiopView_Provider.class,
                IiopPresenter.MyProxy.class);
        
        bindTemplatedPresenter(IOPresenter.class,
                IOPresenter.MyView.class,
                Mbui_IOView_Provider.class,
                IOPresenter.MyProxy.class);

        bindTemplatedPresenter(InterfacePresenter.class,
                InterfacePresenter.MyView.class,
                Mbui_InterfaceView_Provider.class,
                InterfacePresenter.MyProxy.class);

        bindPresenter(JcaPresenter.class,
                JcaPresenter.MyView.class,
                JcaView.class,
                JcaPresenter.MyProxy.class);

        bindPresenter(JmxPresenter.class,
                JmxPresenter.MyView.class,
                JmxView.class,
                JmxPresenter.MyProxy.class);

        bindPresenter(JndiPresenter.class,
                JndiPresenter.MyView.class,
                JndiView.class,
                JndiPresenter.MyProxy.class);

        bindPresenter(JpaPresenter.class,
                JpaPresenter.MyView.class,
                JpaView.class,
                JpaPresenter.MyProxy.class);

        bindTemplatedPresenter(LogFilePresenter.class,
                LogFilePresenter.MyView.class,
                Templated_LogFileView_Provider.class,
                LogFilePresenter.MyProxy.class);

        bindTemplatedPresenter(LoggingPresenter.class,
                LoggingPresenter.MyView.class,
                Mbui_LoggingView_Provider.class,
                LoggingPresenter.MyProxy.class);

        bindTemplatedPresenter(LoggingProfilePresenter.class,
                LoggingProfilePresenter.MyView.class,
                Mbui_LoggingProfileView_Provider.class,
                LoggingProfilePresenter.MyProxy.class);

        bindPresenter(MacroEditorPresenter.class,
                MacroEditorPresenter.MyView.class,
                MacroEditorView.class,
                MacroEditorPresenter.MyProxy.class);

        bindPresenter(ModelBrowserPresenter.class,
                ModelBrowserPresenter.MyView.class,
                ModelBrowserView.class,
                ModelBrowserPresenter.MyProxy.class);

        bindPresenter(MailSessionPresenter.class,
                MailSessionPresenter.MyView.class,
                MailSessionView.class,
                MailSessionPresenter.MyProxy.class);

        bindPresenter(PatchingPresenter.class,
                PatchingPresenter.MyView.class,
                PatchingView.class,
                PatchingPresenter.MyProxy.class);

        bindTemplatedPresenter(PathsPresenter.class,
                PathsPresenter.MyView.class,
                Mbui_PathsView_Provider.class,
                PathsPresenter.MyProxy.class);

        bindPresenter(RhcpPresenter.class,
                RhcpPresenter.MyView.class,
                RhcpView.class,
                RhcpPresenter.MyProxy.class);

        bindPresenter(RuntimePresenter.class,
                RuntimePresenter.MyView.class,
                RuntimeView.class,
                RuntimePresenter.MyProxy.class);

        bindTemplatedPresenter(ServerPresenter.class,
                ServerPresenter.MyView.class,
                Mbui_ServerView_Provider.class,
                ServerPresenter.MyProxy.class);

        bindTemplatedPresenter(ServerGroupPresenter.class,
                ServerGroupPresenter.MyView.class,
                Mbui_ServerGroupView_Provider.class,
                ServerGroupPresenter.MyProxy.class);

        bindPresenter(ServerGroupDeploymentPresenter.class,
                ServerGroupDeploymentPresenter.MyView.class,
                ServerGroupDeploymentView.class,
                ServerGroupDeploymentPresenter.MyProxy.class);

        bindPresenter(ServerStatusPresenter.class,
                ServerStatusPresenter.MyView.class,
                ServerStatusView.class,
                ServerStatusPresenter.MyProxy.class);

        bindPresenter(StandaloneDeploymentPresenter.class,
                StandaloneDeploymentPresenter.MyView.class,
                StandaloneDeploymentView.class,
                StandaloneDeploymentPresenter.MyProxy.class);

        bindTemplatedPresenter(TransactionPresenter.class,
                TransactionPresenter.MyView.class,
                Mbui_TransactionView_Provider.class,
                TransactionPresenter.MyProxy.class);

        bindPresenter(UnderTheBridgePresenter.class,
                UnderTheBridgePresenter.MyView.class,
                UnderTheBridgeView.class,
                UnderTheBridgePresenter.MyProxy.class);
    }
}
