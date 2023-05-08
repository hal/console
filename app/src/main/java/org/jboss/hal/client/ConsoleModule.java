/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client;

import org.jboss.hal.client.accesscontrol.AccessControl;
import org.jboss.hal.client.accesscontrol.AccessControlPresenter;
import org.jboss.hal.client.accesscontrol.AccessControlSsoPresenter;
import org.jboss.hal.client.accesscontrol.AccessControlSsoView;
import org.jboss.hal.client.accesscontrol.AccessControlTokens;
import org.jboss.hal.client.accesscontrol.AccessControlView;
import org.jboss.hal.client.configuration.ConfigurationPresenter;
import org.jboss.hal.client.configuration.ConfigurationView;
import org.jboss.hal.client.configuration.InterfacePresenter;
import org.jboss.hal.client.configuration.Mbui_InterfaceView;
import org.jboss.hal.client.configuration.Mbui_PathsView;
import org.jboss.hal.client.configuration.Mbui_SocketBindingGroupView;
import org.jboss.hal.client.configuration.PathsPresenter;
import org.jboss.hal.client.configuration.SocketBindingGroupPresenter;
import org.jboss.hal.client.configuration.SystemPropertiesPresenter;
import org.jboss.hal.client.configuration.SystemPropertiesView;
import org.jboss.hal.client.configuration.UpdatePathAutoComplete;
import org.jboss.hal.client.configuration.subsystem.batch.BatchPresenter;
import org.jboss.hal.client.configuration.subsystem.batch.Mbui_BatchView;
import org.jboss.hal.client.configuration.subsystem.coremanagement.CoreManagementPresenter;
import org.jboss.hal.client.configuration.subsystem.coremanagement.Mbui_CoreManagementView;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.client.configuration.subsystem.deploymentscanner.DeploymentScannerPresenter;
import org.jboss.hal.client.configuration.subsystem.deploymentscanner.Mbui_DeploymentScannerView;
import org.jboss.hal.client.configuration.subsystem.distributableweb.DistributableWebPresenter;
import org.jboss.hal.client.configuration.subsystem.distributableweb.Mbui_DistributableWebView;
import org.jboss.hal.client.configuration.subsystem.ee.EEPresenter;
import org.jboss.hal.client.configuration.subsystem.ee.EEView;
import org.jboss.hal.client.configuration.subsystem.elytron.ElytronSubsystemPresenter;
import org.jboss.hal.client.configuration.subsystem.elytron.FactoriesPresenter;
import org.jboss.hal.client.configuration.subsystem.elytron.FactoriesView;
import org.jboss.hal.client.configuration.subsystem.elytron.MapperDecoderPresenter;
import org.jboss.hal.client.configuration.subsystem.elytron.Mbui_ElytronSubsystemView;
import org.jboss.hal.client.configuration.subsystem.elytron.Mbui_MapperDecoderView;
import org.jboss.hal.client.configuration.subsystem.elytron.OtherSettingsPresenter;
import org.jboss.hal.client.configuration.subsystem.elytron.OtherSettingsView;
import org.jboss.hal.client.configuration.subsystem.iiop.IiopPresenter;
import org.jboss.hal.client.configuration.subsystem.iiop.Mbui_IiopView;
import org.jboss.hal.client.configuration.subsystem.infinispan.CacheContainerPresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.CacheContainerView;
import org.jboss.hal.client.configuration.subsystem.infinispan.DistributedCachePresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.DistributedCacheView;
import org.jboss.hal.client.configuration.subsystem.infinispan.InvalidationCachePresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.InvalidationCacheView;
import org.jboss.hal.client.configuration.subsystem.infinispan.LocalCachePresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.LocalCacheView;
import org.jboss.hal.client.configuration.subsystem.infinispan.RemoteCacheContainerPresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.RemoteCacheContainerView;
import org.jboss.hal.client.configuration.subsystem.infinispan.ReplicatedCachePresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.ReplicatedCacheView;
import org.jboss.hal.client.configuration.subsystem.infinispan.ScatteredCachePresenter;
import org.jboss.hal.client.configuration.subsystem.infinispan.ScatteredCacheView;
import org.jboss.hal.client.configuration.subsystem.io.IOPresenter;
import org.jboss.hal.client.configuration.subsystem.io.Mbui_IOView;
import org.jboss.hal.client.configuration.subsystem.jca.JcaPresenter;
import org.jboss.hal.client.configuration.subsystem.jca.JcaView;
import org.jboss.hal.client.configuration.subsystem.jgroups.JGroupsPresenter;
import org.jboss.hal.client.configuration.subsystem.jgroups.JGroupsView;
import org.jboss.hal.client.configuration.subsystem.jmx.JmxPresenter;
import org.jboss.hal.client.configuration.subsystem.jmx.JmxView;
import org.jboss.hal.client.configuration.subsystem.logging.LoggingPresenter;
import org.jboss.hal.client.configuration.subsystem.logging.LoggingProfilePresenter;
import org.jboss.hal.client.configuration.subsystem.logging.Mbui_LoggingProfileView;
import org.jboss.hal.client.configuration.subsystem.logging.Mbui_LoggingView;
import org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter;
import org.jboss.hal.client.configuration.subsystem.mail.MailSessionView;
import org.jboss.hal.client.configuration.subsystem.messaging.ClusteringPresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.ConnectionPresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.DestinationPresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.HaPolicyPresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.HaPolicyView;
import org.jboss.hal.client.configuration.subsystem.messaging.JmsBridgePresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.JmsBridgeView;
import org.jboss.hal.client.configuration.subsystem.messaging.Mbui_ClusteringView;
import org.jboss.hal.client.configuration.subsystem.messaging.Mbui_ConnectionView;
import org.jboss.hal.client.configuration.subsystem.messaging.Mbui_DestinationView;
import org.jboss.hal.client.configuration.subsystem.messaging.Mbui_MessagingSubsystemView;
import org.jboss.hal.client.configuration.subsystem.messaging.Mbui_RemoteActiveMQView;
import org.jboss.hal.client.configuration.subsystem.messaging.MessagingSubsystemPresenter;
import org.jboss.hal.client.configuration.subsystem.messaging.RemoteActiveMQPresenter;
import org.jboss.hal.client.configuration.subsystem.microprofile.Mbui_MicroProfileConfigView;
import org.jboss.hal.client.configuration.subsystem.microprofile.MicroProfileConfigPresenter;
import org.jboss.hal.client.configuration.subsystem.microprofile.MicroProfileMetricsPresenter;
import org.jboss.hal.client.configuration.subsystem.microprofile.MicroProfileMetricsView;
import org.jboss.hal.client.configuration.subsystem.modcluster.Mbui_ModclusterView;
import org.jboss.hal.client.configuration.subsystem.modcluster.ModclusterPresenter;
import org.jboss.hal.client.configuration.subsystem.remoting.Mbui_RemotingView;
import org.jboss.hal.client.configuration.subsystem.remoting.RemotingPresenter;
import org.jboss.hal.client.configuration.subsystem.requestcontroller.Mbui_RequestControllerView;
import org.jboss.hal.client.configuration.subsystem.requestcontroller.RequestControllerPresenter;
import org.jboss.hal.client.configuration.subsystem.resourceadapter.Mbui_ResourceAdapterView;
import org.jboss.hal.client.configuration.subsystem.resourceadapter.ResourceAdapterPresenter;
import org.jboss.hal.client.configuration.subsystem.security.Mbui_SecurityDomainView;
import org.jboss.hal.client.configuration.subsystem.security.Mbui_SecurityView;
import org.jboss.hal.client.configuration.subsystem.security.SecurityDomainPresenter;
import org.jboss.hal.client.configuration.subsystem.security.SecurityPresenter;
import org.jboss.hal.client.configuration.subsystem.securitymanager.SecurityManagerPresenter;
import org.jboss.hal.client.configuration.subsystem.securitymanager.SecurityManagerView;
import org.jboss.hal.client.configuration.subsystem.undertow.ApplicationSecurityDomainPresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.ApplicationSecurityDomainView;
import org.jboss.hal.client.configuration.subsystem.undertow.BufferCachePresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.ByteBufferPoolPresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.FilterPresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.HandlerPresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.Mbui_BufferCacheView;
import org.jboss.hal.client.configuration.subsystem.undertow.Mbui_ByteBufferPoolView;
import org.jboss.hal.client.configuration.subsystem.undertow.Mbui_FilterView;
import org.jboss.hal.client.configuration.subsystem.undertow.Mbui_HandlerView;
import org.jboss.hal.client.configuration.subsystem.undertow.Mbui_UndertowSubsystemView;
import org.jboss.hal.client.configuration.subsystem.undertow.ServletContainerPresenter;
import org.jboss.hal.client.configuration.subsystem.undertow.ServletContainerView;
import org.jboss.hal.client.configuration.subsystem.undertow.UndertowSubsystemPresenter;
import org.jboss.hal.client.configuration.subsystem.webservice.WebservicePresenter;
import org.jboss.hal.client.configuration.subsystem.webservice.WebserviceView;
import org.jboss.hal.client.deployment.BrowseContentPresenter;
import org.jboss.hal.client.deployment.BrowseContentView;
import org.jboss.hal.client.deployment.ServerGroupDeploymentPresenter;
import org.jboss.hal.client.deployment.ServerGroupDeploymentView;
import org.jboss.hal.client.deployment.StandaloneDeploymentPresenter;
import org.jboss.hal.client.deployment.StandaloneDeploymentView;
import org.jboss.hal.client.homepage.HomepagePresenter;
import org.jboss.hal.client.homepage.HomepageView;
import org.jboss.hal.client.installer.UpdateManagerFinderPresenter;
import org.jboss.hal.client.installer.UpdateManagerFinderView;
import org.jboss.hal.client.rhcp.RhcpPresenter;
import org.jboss.hal.client.rhcp.RhcpView;
import org.jboss.hal.client.rhcp.UnderTheBridgePresenter;
import org.jboss.hal.client.rhcp.UnderTheBridgeView;
import org.jboss.hal.client.runtime.ProcessStateHandler;
import org.jboss.hal.client.runtime.RuntimePresenter;
import org.jboss.hal.client.runtime.RuntimeView;
import org.jboss.hal.client.runtime.configurationchanges.ConfigurationChangesPresenter;
import org.jboss.hal.client.runtime.configurationchanges.ConfigurationChangesView;
import org.jboss.hal.client.runtime.group.Mbui_ServerGroupView;
import org.jboss.hal.client.runtime.group.ServerGroupPresenter;
import org.jboss.hal.client.runtime.host.HostPresenter;
import org.jboss.hal.client.runtime.host.Mbui_HostView;
import org.jboss.hal.client.runtime.managementoperations.ManagementOperationsPresenter;
import org.jboss.hal.client.runtime.managementoperations.ManagementOperationsView;
import org.jboss.hal.client.runtime.server.ServerBootErrorsPresenter;
import org.jboss.hal.client.runtime.server.ServerBootErrorsView;
import org.jboss.hal.client.runtime.server.ServerRuntimePresenter;
import org.jboss.hal.client.runtime.server.ServerRuntimeView;
import org.jboss.hal.client.runtime.server.StandaloneServerPresenter;
import org.jboss.hal.client.runtime.server.StandaloneServerView;
import org.jboss.hal.client.runtime.subsystem.batch.JobPresenter;
import org.jboss.hal.client.runtime.subsystem.batch.JobView;
import org.jboss.hal.client.runtime.subsystem.elytron.SSLPresenter;
import org.jboss.hal.client.runtime.subsystem.elytron.SSLView;
import org.jboss.hal.client.runtime.subsystem.elytron.StoresPresenter;
import org.jboss.hal.client.runtime.subsystem.elytron.StoresView;
import org.jboss.hal.client.runtime.subsystem.jndi.JndiPresenter;
import org.jboss.hal.client.runtime.subsystem.jndi.JndiView;
import org.jboss.hal.client.runtime.subsystem.logging.LogFilePresenter;
import org.jboss.hal.client.runtime.subsystem.logging.LogFileView;
import org.jboss.hal.client.runtime.subsystem.messaging.JmsQueuePresenter;
import org.jboss.hal.client.runtime.subsystem.messaging.JmsQueueView;
import org.jboss.hal.client.runtime.subsystem.microprofile.health.MicroProfileHealthPresenter;
import org.jboss.hal.client.runtime.subsystem.microprofile.health.MicroProfileHealthView;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.FooterView;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.client.skeleton.HeaderView;
import org.jboss.hal.client.tools.MacroEditorPresenter;
import org.jboss.hal.client.tools.MacroEditorView;
import org.jboss.hal.client.tools.ModelBrowserPresenter;
import org.jboss.hal.client.tools.ModelBrowserView;
import org.jboss.hal.core.ExceptionHandler;
import org.jboss.hal.core.mvp.HalPlaceManager;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.GinModule;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

@GinModule
public class ConsoleModule extends AbstractPresenterModule {

    @Override
    protected void configure() {

        // ------------------------------------------------------ GWTP

        DefaultModule defaultModule = new DefaultModule.Builder()
                .placeManager(HalPlaceManager.class)
                .build();
        install(defaultModule);

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.HOMEPAGE);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.ERROR);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.UNAUTHORIZED);

        // ------------------------------------------------------ misc

        bind(AccessControl.class).in(Singleton.class);
        bind(AccessControlTokens.class).in(Singleton.class);
        bind(DataSourceTemplates.class).in(Singleton.class);
        bind(ExceptionHandler.class).in(Singleton.class);
        bind(ProcessStateHandler.class).asEagerSingleton(); // to register the event handler
        bind(UpdatePathAutoComplete.class).asEagerSingleton(); // to register the event handler

        // ------------------------------------------------------ skeleton & root presenter

        bindPresenterWidget(HeaderPresenter.class,
                HeaderPresenter.MyView.class,
                HeaderView.class);

        bindSingletonPresenterWidget(FooterPresenter.class,
                FooterPresenter.MyView.class,
                FooterView.class);

        bindPresenter(RootPresenter.class,
                RootPresenter.MyView.class,
                RootView.class,
                RootPresenter.MyProxy.class);

        // ------------------------------------------------------ remaining presenters (A-Z)

        bindPresenter(AccessControlPresenter.class,
                AccessControlPresenter.MyView.class,
                AccessControlView.class,
                AccessControlPresenter.MyProxy.class);

        bindPresenter(AccessControlSsoPresenter.class,
                AccessControlSsoPresenter.MyView.class,
                AccessControlSsoView.class,
                AccessControlSsoPresenter.MyProxy.class);

        bindPresenter(ApplicationSecurityDomainPresenter.class,
                ApplicationSecurityDomainPresenter.MyView.class,
                ApplicationSecurityDomainView.class,
                ApplicationSecurityDomainPresenter.MyProxy.class);

        bindPresenter(BatchPresenter.class,
                BatchPresenter.MyView.class,
                Mbui_BatchView.class,
                BatchPresenter.MyProxy.class);

        bindPresenter(BrowseContentPresenter.class,
                BrowseContentPresenter.MyView.class,
                BrowseContentView.class,
                BrowseContentPresenter.MyProxy.class);

        bindPresenter(BufferCachePresenter.class,
                BufferCachePresenter.MyView.class,
                Mbui_BufferCacheView.class,
                BufferCachePresenter.MyProxy.class);

        bindPresenter(ByteBufferPoolPresenter.class,
                ByteBufferPoolPresenter.MyView.class,
                Mbui_ByteBufferPoolView.class,
                ByteBufferPoolPresenter.MyProxy.class);

        bindPresenter(CacheContainerPresenter.class,
                CacheContainerPresenter.MyView.class,
                CacheContainerView.class,
                CacheContainerPresenter.MyProxy.class);

        bindPresenter(ClusteringPresenter.class,
                ClusteringPresenter.MyView.class,
                Mbui_ClusteringView.class,
                ClusteringPresenter.MyProxy.class);

        bindPresenter(ConfigurationChangesPresenter.class,
                ConfigurationChangesPresenter.MyView.class,
                ConfigurationChangesView.class,
                ConfigurationChangesPresenter.MyProxy.class);

        bindPresenter(ConfigurationPresenter.class,
                ConfigurationPresenter.MyView.class,
                ConfigurationView.class,
                ConfigurationPresenter.MyProxy.class);

        bindPresenter(ConnectionPresenter.class,
                ConnectionPresenter.MyView.class,
                Mbui_ConnectionView.class,
                ConnectionPresenter.MyProxy.class);

        bindPresenter(CoreManagementPresenter.class,
                CoreManagementPresenter.MyView.class,
                Mbui_CoreManagementView.class,
                CoreManagementPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourceView.class,
                org.jboss.hal.client.configuration.subsystem.datasource.DataSourcePresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourceView.class,
                org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.deployment.DeploymentPresenter.class,
                org.jboss.hal.client.deployment.DeploymentPresenter.MyView.class,
                org.jboss.hal.client.deployment.DeploymentView.class,
                org.jboss.hal.client.deployment.DeploymentPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.undertow.DeploymentPresenter.class,
                org.jboss.hal.client.runtime.subsystem.undertow.DeploymentPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.undertow.DeploymentView.class,
                org.jboss.hal.client.runtime.subsystem.undertow.DeploymentPresenter.MyProxy.class);

        bindPresenter(DeploymentScannerPresenter.class,
                DeploymentScannerPresenter.MyView.class,
                Mbui_DeploymentScannerView.class,
                DeploymentScannerPresenter.MyProxy.class);

        bindPresenter(DestinationPresenter.class,
                DestinationPresenter.MyView.class,
                Mbui_DestinationView.class,
                DestinationPresenter.MyProxy.class);

        bindPresenter(DistributableWebPresenter.class,
                DistributableWebPresenter.MyView.class,
                Mbui_DistributableWebView.class,
                DistributableWebPresenter.MyProxy.class);

        bindPresenter(DistributedCachePresenter.class,
                DistributedCachePresenter.MyView.class,
                DistributedCacheView.class,
                DistributedCachePresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.ejb.EjbPresenter.class,
                org.jboss.hal.client.configuration.subsystem.ejb.EjbPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.ejb.Mbui_EjbView.class,
                org.jboss.hal.client.configuration.subsystem.ejb.EjbPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.ejb.EjbPresenter.class,
                org.jboss.hal.client.runtime.subsystem.ejb.EjbPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.ejb.EjbView.class,
                org.jboss.hal.client.runtime.subsystem.ejb.EjbPresenter.MyProxy.class);

        bindPresenter(ElytronSubsystemPresenter.class,
                ElytronSubsystemPresenter.MyView.class,
                Mbui_ElytronSubsystemView.class,
                ElytronSubsystemPresenter.MyProxy.class);

        bindPresenter(EEPresenter.class,
                EEPresenter.MyView.class,
                EEView.class,
                EEPresenter.MyProxy.class);

        bindPresenter(ErrorPresenter.class,
                ErrorPresenter.MyView.class,
                ErrorView.class,
                ErrorPresenter.MyProxy.class);

        bindPresenter(ExpertModePresenter.class,
                ExpertModePresenter.MyView.class,
                ExpertModeView.class,
                ExpertModePresenter.MyProxy.class);

        bindPresenter(FactoriesPresenter.class,
                FactoriesPresenter.MyView.class,
                FactoriesView.class,
                FactoriesPresenter.MyProxy.class);

        bindPresenter(FilterPresenter.class,
                FilterPresenter.MyView.class,
                Mbui_FilterView.class,
                FilterPresenter.MyProxy.class);

        bindPresenter(GenericSubsystemPresenter.class,
                GenericSubsystemPresenter.MyView.class,
                GenericSubsystemView.class,
                GenericSubsystemPresenter.MyProxy.class);

        bindPresenter(HaPolicyPresenter.class,
                HaPolicyPresenter.MyView.class,
                HaPolicyView.class,
                HaPolicyPresenter.MyProxy.class);

        bindPresenter(HandlerPresenter.class,
                HandlerPresenter.MyView.class,
                Mbui_HandlerView.class,
                HandlerPresenter.MyProxy.class);

        bindPresenter(HomepagePresenter.class,
                HomepagePresenter.MyView.class,
                HomepageView.class,
                HomepagePresenter.MyProxy.class);

        bindPresenter(HostPresenter.class,
                HostPresenter.MyView.class,
                Mbui_HostView.class,
                HostPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.undertow.HostPresenter.class,
                org.jboss.hal.client.configuration.subsystem.undertow.HostPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.undertow.HostView.class,
                org.jboss.hal.client.configuration.subsystem.undertow.HostPresenter.MyProxy.class);

        bindPresenter(IiopPresenter.class,
                IiopPresenter.MyView.class,
                Mbui_IiopView.class,
                IiopPresenter.MyProxy.class);

        bindPresenter(IOPresenter.class,
                IOPresenter.MyView.class,
                Mbui_IOView.class,
                IOPresenter.MyProxy.class);

        bindPresenter(InterfacePresenter.class,
                InterfacePresenter.MyView.class,
                Mbui_InterfaceView.class,
                InterfacePresenter.MyProxy.class);

        bindPresenter(InvalidationCachePresenter.class,
                InvalidationCachePresenter.MyView.class,
                InvalidationCacheView.class,
                InvalidationCachePresenter.MyProxy.class);

        bindPresenter(JGroupsPresenter.class,
                JGroupsPresenter.MyView.class,
                JGroupsView.class,
                JGroupsPresenter.MyProxy.class);

        bindPresenter(JcaPresenter.class,
                JcaPresenter.MyView.class,
                JcaView.class,
                JcaPresenter.MyProxy.class);

        bindPresenter(JmsBridgePresenter.class,
                JmsBridgePresenter.MyView.class,
                JmsBridgeView.class,
                JmsBridgePresenter.MyProxy.class);

        bindPresenter(JmsQueuePresenter.class,
                JmsQueuePresenter.MyView.class,
                JmsQueueView.class,
                JmsQueuePresenter.MyProxy.class);

        bindPresenter(JmxPresenter.class,
                JmxPresenter.MyView.class,
                JmxView.class,
                JmxPresenter.MyProxy.class);

        bindPresenter(JndiPresenter.class,
                JndiPresenter.MyView.class,
                JndiView.class,
                JndiPresenter.MyProxy.class);

        bindPresenter(JobPresenter.class,
                JobPresenter.MyView.class,
                JobView.class,
                JobPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.jpa.JpaPresenter.class,
                org.jboss.hal.client.runtime.subsystem.jpa.JpaPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.jpa.JpaView.class,
                org.jboss.hal.client.runtime.subsystem.jpa.JpaPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.jpa.JpaPresenter.class,
                org.jboss.hal.client.configuration.subsystem.jpa.JpaPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.jpa.Mbui_JpaView.class,
                org.jboss.hal.client.configuration.subsystem.jpa.JpaPresenter.MyProxy.class);

        bindPresenter(LocalCachePresenter.class,
                LocalCachePresenter.MyView.class,
                LocalCacheView.class,
                LocalCachePresenter.MyProxy.class);

        bindPresenter(LogFilePresenter.class,
                LogFilePresenter.MyView.class,
                LogFileView.class,
                LogFilePresenter.MyProxy.class);

        bindPresenter(LoggingPresenter.class,
                LoggingPresenter.MyView.class,
                Mbui_LoggingView.class,
                LoggingPresenter.MyProxy.class);

        bindPresenter(LoggingProfilePresenter.class,
                LoggingProfilePresenter.MyView.class,
                Mbui_LoggingProfileView.class,
                LoggingProfilePresenter.MyProxy.class);

        bindPresenter(MacroEditorPresenter.class,
                MacroEditorPresenter.MyView.class,
                MacroEditorView.class,
                MacroEditorPresenter.MyProxy.class);

        bindPresenter(ManagementOperationsPresenter.class,
                ManagementOperationsPresenter.MyView.class,
                ManagementOperationsView.class,
                ManagementOperationsPresenter.MyProxy.class);

        bindPresenter(MapperDecoderPresenter.class,
                MapperDecoderPresenter.MyView.class,
                Mbui_MapperDecoderView.class,
                MapperDecoderPresenter.MyProxy.class);

        bindPresenter(MicroProfileConfigPresenter.class,
                MicroProfileConfigPresenter.MyView.class,
                Mbui_MicroProfileConfigView.class,
                MicroProfileConfigPresenter.MyProxy.class);

        bindPresenter(MicroProfileHealthPresenter.class,
                MicroProfileHealthPresenter.MyView.class,
                MicroProfileHealthView.class,
                MicroProfileHealthPresenter.MyProxy.class);

        bindPresenter(MicroProfileMetricsPresenter.class,
                MicroProfileMetricsPresenter.MyView.class,
                MicroProfileMetricsView.class,
                MicroProfileMetricsPresenter.MyProxy.class);

        bindPresenter(ModclusterPresenter.class,
                ModclusterPresenter.MyView.class,
                Mbui_ModclusterView.class,
                ModclusterPresenter.MyProxy.class);

        bindPresenter(ModelBrowserPresenter.class,
                ModelBrowserPresenter.MyView.class,
                ModelBrowserView.class,
                ModelBrowserPresenter.MyProxy.class);

        bindPresenter(MailSessionPresenter.class,
                MailSessionPresenter.MyView.class,
                MailSessionView.class,
                MailSessionPresenter.MyProxy.class);

        bindPresenter(RemoteActiveMQPresenter.class,
                RemoteActiveMQPresenter.MyView.class,
                Mbui_RemoteActiveMQView.class,
                RemoteActiveMQPresenter.MyProxy.class);

        bindPresenter(MessagingSubsystemPresenter.class,
                MessagingSubsystemPresenter.MyView.class,
                Mbui_MessagingSubsystemView.class,
                MessagingSubsystemPresenter.MyProxy.class);

        bindPresenter(OtherSettingsPresenter.class,
                OtherSettingsPresenter.MyView.class,
                OtherSettingsView.class,
                OtherSettingsPresenter.MyProxy.class);

        bindPresenter(PathsPresenter.class,
                PathsPresenter.MyView.class,
                Mbui_PathsView.class,
                PathsPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.elytron.RealmsPresenter.class,
                org.jboss.hal.client.configuration.subsystem.elytron.RealmsPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.elytron.RealmsView.class,
                org.jboss.hal.client.configuration.subsystem.elytron.RealmsPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.elytron.RealmsPresenter.class,
                org.jboss.hal.client.runtime.subsystem.elytron.RealmsPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.elytron.RealmsView.class,
                org.jboss.hal.client.runtime.subsystem.elytron.RealmsPresenter.MyProxy.class);

        bindPresenter(RemoteCacheContainerPresenter.class,
                RemoteCacheContainerPresenter.MyView.class,
                RemoteCacheContainerView.class,
                RemoteCacheContainerPresenter.MyProxy.class);

        bindPresenter(RemotingPresenter.class,
                RemotingPresenter.MyView.class,
                Mbui_RemotingView.class,
                RemotingPresenter.MyProxy.class);

        bindPresenter(ReplicatedCachePresenter.class,
                ReplicatedCachePresenter.MyView.class,
                ReplicatedCacheView.class,
                ReplicatedCachePresenter.MyProxy.class);

        bindPresenter(RequestControllerPresenter.class,
                RequestControllerPresenter.MyView.class,
                Mbui_RequestControllerView.class,
                RequestControllerPresenter.MyProxy.class);

        bindPresenter(ResourceAdapterPresenter.class,
                ResourceAdapterPresenter.MyView.class,
                Mbui_ResourceAdapterView.class,
                ResourceAdapterPresenter.MyProxy.class);

        bindPresenter(RhcpPresenter.class,
                RhcpPresenter.MyView.class,
                RhcpView.class,
                RhcpPresenter.MyProxy.class);

        bindPresenter(RuntimePresenter.class,
                RuntimePresenter.MyView.class,
                RuntimeView.class,
                RuntimePresenter.MyProxy.class);

        bindPresenter(ScatteredCachePresenter.class,
                ScatteredCachePresenter.MyView.class,
                ScatteredCacheView.class,
                ScatteredCachePresenter.MyProxy.class);

        bindPresenter(SecurityPresenter.class,
                SecurityPresenter.MyView.class,
                Mbui_SecurityView.class,
                SecurityPresenter.MyProxy.class);

        bindPresenter(SecurityDomainPresenter.class,
                SecurityDomainPresenter.MyView.class,
                Mbui_SecurityDomainView.class,
                SecurityDomainPresenter.MyProxy.class);

        bindPresenter(SecurityManagerPresenter.class,
                SecurityManagerPresenter.MyView.class,
                SecurityManagerView.class,
                SecurityManagerPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.messaging.ServerPresenter.class,
                org.jboss.hal.client.configuration.subsystem.messaging.ServerPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.messaging.ServerView.class,
                org.jboss.hal.client.configuration.subsystem.messaging.ServerPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.undertow.ServerPresenter.class,
                org.jboss.hal.client.configuration.subsystem.undertow.ServerPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.undertow.ServerView.class,
                org.jboss.hal.client.configuration.subsystem.undertow.ServerPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.server.ServerPresenter.class,
                org.jboss.hal.client.runtime.server.ServerPresenter.MyView.class,
                org.jboss.hal.client.runtime.server.Mbui_ServerView.class,
                org.jboss.hal.client.runtime.server.ServerPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.messaging.ServerPresenter.class,
                org.jboss.hal.client.runtime.subsystem.messaging.ServerPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.messaging.ServerView.class,
                org.jboss.hal.client.runtime.subsystem.messaging.ServerPresenter.MyProxy.class);

        bindPresenter(ServerBootErrorsPresenter.class,
                ServerBootErrorsPresenter.MyView.class,
                ServerBootErrorsView.class,
                ServerBootErrorsPresenter.MyProxy.class);

        bindPresenter(ServerGroupPresenter.class,
                ServerGroupPresenter.MyView.class,
                Mbui_ServerGroupView.class,
                ServerGroupPresenter.MyProxy.class);

        bindPresenter(ServerGroupDeploymentPresenter.class,
                ServerGroupDeploymentPresenter.MyView.class,
                ServerGroupDeploymentView.class,
                ServerGroupDeploymentPresenter.MyProxy.class);

        bindPresenter(ServerRuntimePresenter.class,
                ServerRuntimePresenter.MyView.class,
                ServerRuntimeView.class,
                ServerRuntimePresenter.MyProxy.class);

        bindPresenter(ServletContainerPresenter.class,
                ServletContainerPresenter.MyView.class,
                ServletContainerView.class,
                ServletContainerPresenter.MyProxy.class);

        bindPresenter(SocketBindingGroupPresenter.class,
                SocketBindingGroupPresenter.MyView.class,
                Mbui_SocketBindingGroupView.class,
                SocketBindingGroupPresenter.MyProxy.class);

        bindPresenter(StandaloneDeploymentPresenter.class,
                StandaloneDeploymentPresenter.MyView.class,
                StandaloneDeploymentView.class,
                StandaloneDeploymentPresenter.MyProxy.class);

        bindPresenter(StandaloneServerPresenter.class,
                StandaloneServerPresenter.MyView.class,
                StandaloneServerView.class,
                StandaloneServerPresenter.MyProxy.class);

        bindPresenter(StoresPresenter.class,
                StoresPresenter.MyView.class,
                StoresView.class,
                StoresPresenter.MyProxy.class);

        bindPresenter(SSLPresenter.class,
                SSLPresenter.MyView.class,
                SSLView.class,
                SSLPresenter.MyProxy.class);

        bindPresenter(SystemPropertiesPresenter.class,
                SystemPropertiesPresenter.MyView.class,
                SystemPropertiesView.class,
                SystemPropertiesPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.configuration.subsystem.transaction.TransactionPresenter.class,
                org.jboss.hal.client.configuration.subsystem.transaction.TransactionPresenter.MyView.class,
                org.jboss.hal.client.configuration.subsystem.transaction.Mbui_TransactionView.class,
                org.jboss.hal.client.configuration.subsystem.transaction.TransactionPresenter.MyProxy.class);

        bindPresenter(org.jboss.hal.client.runtime.subsystem.transaction.TransactionsPresenter.class,
                org.jboss.hal.client.runtime.subsystem.transaction.TransactionsPresenter.MyView.class,
                org.jboss.hal.client.runtime.subsystem.transaction.TransactionsView.class,
                org.jboss.hal.client.runtime.subsystem.transaction.TransactionsPresenter.MyProxy.class);

        bindPresenter(UpdateManagerFinderPresenter.class,
                UpdateManagerFinderPresenter.MyView.class,
                UpdateManagerFinderView.class,
                UpdateManagerFinderPresenter.MyProxy.class);

        bindPresenter(UnderTheBridgePresenter.class,
                UnderTheBridgePresenter.MyView.class,
                UnderTheBridgeView.class,
                UnderTheBridgePresenter.MyProxy.class);

        bindPresenter(UnauthorizedPresenter.class,
                UnauthorizedPresenter.MyView.class,
                UnauthorizedView.class,
                UnauthorizedPresenter.MyProxy.class);

        bindPresenter(UndertowSubsystemPresenter.class,
                UndertowSubsystemPresenter.MyView.class,
                Mbui_UndertowSubsystemView.class,
                UndertowSubsystemPresenter.MyProxy.class);

        bindPresenter(WebservicePresenter.class,
                WebservicePresenter.MyView.class,
                WebserviceView.class,
                WebservicePresenter.MyProxy.class);
    }
}
