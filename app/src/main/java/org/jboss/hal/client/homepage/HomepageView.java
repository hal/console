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

import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class HomepageView extends ViewImpl implements HomepagePresenter.MyView {

    private HomepagePresenter presenter;

    @Inject
    public HomepageView(Environment env,
            User user,
            Resources resources,
            TokenFormatter tokenFormatter) {

        boolean standalone = env.isStandalone();
        boolean community = env.getInstanceInfo() == WILDFLY;
        boolean su = user.isSuperuser() || user.isAdministrator();
        String name = env.getInstanceInfo().productName();

        Document document = Browser.getDocument();
        Iterable<HomepageSection> sections;
        Element header;
        Element deployments;
        Element configuration;
        Element runtime;
        Element accessControl = document.createDivElement(); // to get rid of warning "might not be initialized"
        Element patching = document.createDivElement();
        Element help;

        if (community) {
            header = new Elements.Builder()
                    .div().css(eapHomeTitle)
                    .h(1).innerText(env.getInstanceInfo().description()).end()
                    .end().build();
        } else {
            // @formatter:off
            header = new Elements.Builder()
                .div().css(eapHomeTitle)
                    .p()
                        .span().innerText(resources.constants().homepageNewToEap() + " ").end()
                        .a()
                            .css(clickable)
                            .on(click, event -> presenter.launchGuidedTour())
                            .innerText(resources.constants().homepageTakeATour())
                        .end()
                    .end()
                    .h(1).innerText(env.getInstanceInfo().description()).end()
                .end().build();
            // @formatter:on
        }

        if (standalone) {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_DEPLOYMENTS_SECTION, NameTokens.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSection(),
                    resources.constants().homepageDeploymentsStandaloneStepIntro(),
                    Arrays.asList(resources.constants().homepageDeploymentsStandaloneStep1(),
                            resources.constants().homepageDeploymentsStepEnable()), true));
            deployments = HomepageModule.create(tokenFormatter,
                    NameTokens.DEPLOYMENTS,
                    Names.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSubHeader(),
                    resources.images().deployments(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_CONFIGURATION_SECTION, NameTokens.CONFIGURATION,
                    resources.constants().homepageConfigurationSection(),
                    resources.constants().homepageConfigurationStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageConfigurationStandaloneStep1(),
                            resources.constants().homepageConfigurationStep2(),
                            resources.constants().homepageConfigurationStep3()), true));
            configuration = HomepageModule.create(tokenFormatter,
                    NameTokens.CONFIGURATION,
                    Names.CONFIGURATION,
                    resources.constants().homepageConfigurationStandaloneSubHeader(),
                    resources.images().configuration(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_RUNTIME_SECTION, NameTokens.RUNTIME,
                    resources.constants().homepageRuntimeStandaloneSection(),
                    resources.constants().homepageRuntimeStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageRuntimeStandaloneStep1(),
                            resources.constants().homepageRuntimeStandaloneStep2()), true));
            runtime = HomepageModule.create(tokenFormatter,
                    NameTokens.RUNTIME,
                    Names.RUNTIME,
                    resources.constants().homepageRuntimeStandaloneSubHeader(),
                    resources.images().runtime(),
                    sections).asElement();

        } else {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_DEPLOYMENTS_SECTION, NameTokens.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSection(),
                    resources.constants().homepageDeploymentsDomainStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageDeploymentsDomainStep1(),
                            resources.constants().homepageDeploymentsDomainStep2(),
                            resources.constants().homepageDeploymentsStepEnable()), true));
            deployments = HomepageModule.create(tokenFormatter,
                    NameTokens.DEPLOYMENTS,
                    Names.DEPLOYMENTS, //NON-NLS
                    resources.constants().homepageDeploymentsSubHeader(),
                    resources.images().deployments(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_CONFIGURATION_SECTION, NameTokens.CONFIGURATION,
                    resources.constants().homepageConfigurationSection(),
                    resources.constants().homepageConfigurationStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageConfigurationDomainStep1(),
                            resources.constants().homepageConfigurationStep2(),
                            resources.constants().homepageConfigurationStep3()), true));
            configuration = HomepageModule.create(tokenFormatter,
                    NameTokens.CONFIGURATION,
                    Names.CONFIGURATION,
                    resources.constants().homepageConfigurationDomainSubHeader(),
                    resources.images().configuration(),
                    sections).asElement();

            sections = Arrays.asList(
                    HomepageSection.create(tokenFormatter, resources,
                            Ids.HOMEPAGE_RUNTIME_SERVER_GROUP_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainServerGroupSection(),
                            resources.constants().homepageRuntimeDomainServerGroupStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainServerGroupStep1(),
                                    resources.constants().homepageRuntimeDomainServerGroupStep2()), true),
                    HomepageSection.create(tokenFormatter, resources,
                            Ids.HOMEPAGE_RUNTIME_SERVER_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainCreateServerSection(),
                            resources.constants().homepageRuntimeDomainCreateServerStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainCreateServerStep1(),
                                    resources.constants().homepageRuntimeDomainCreateServerStep2()), true),
                    HomepageSection.create(tokenFormatter, resources,
                            Ids.HOMEPAGE_RUNTIME_MONITOR_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainMonitorServerSection(),
                            resources.constants().homepageRuntimeStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainMonitorServerStep1(),
                                    resources.constants().homepageRuntimeDomainMonitorServerStep2()), true));
            runtime = HomepageModule.create(tokenFormatter,
                    NameTokens.RUNTIME,
                    Names.RUNTIME,
                    resources.constants().homepageRuntimeDomainSubHeader(),
                    resources.images().runtime(),
                    sections).asElement();
        }

        if (su) {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                    Ids.HOMEPAGE_ACCESS_CONTROL_SECTION, NameTokens.ACCESS_CONTROL,
                    resources.constants().homepageAccessControlSection(),
                    resources.constants().homepageAccessControlStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageAccessControlStep1(),
                            resources.constants().homepageAccessControlStep2()), true));
            accessControl = HomepageModule.create(tokenFormatter,
                    NameTokens.ACCESS_CONTROL,
                    "Access Control", //NON-NLS
                    resources.constants().homepageAccessControlSubHeader(),
                    resources.images().accessControl(),
                    sections).asElement();

            if (standalone) {
                sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                        Ids.HOMEPAGE_PATCHING_SECTION, NameTokens.PATCHING,
                        resources.constants().homepagePatchingSection(),
                        resources.messages().homepagePatchingStandaloneStepIntro(name),
                        Arrays.asList(
                                resources.constants().homepagePatchingStep1(),
                                resources.constants().homepagePatchingStepApply()), true));
            } else {
                sections = Collections.singleton(HomepageSection.create(tokenFormatter, resources,
                        Ids.HOMEPAGE_PATCHING_SECTION, NameTokens.PATCHING,
                        resources.constants().homepagePatchingSection(),
                        resources.messages().homepagePatchingDomainStepIntro(name),
                        Arrays.asList(
                                resources.constants().homepagePatchingStep1(),
                                resources.constants().homepagePatchingDomainStep2(),
                                resources.constants().homepagePatchingStepApply()), true));
            }
            patching = HomepageModule.create(tokenFormatter,
                    NameTokens.PATCHING,
                    "Patching", //NON-NLS
                    resources.messages().homepagePatchingSubHeader(name),
                    resources.images().patching(),
                    sections).asElement();
        }

        help = HomepageHelp.create(env, resources).asElement();
        Elements.Builder rootBuilder = new Elements.Builder().div()
                .div().css(eapHomeRow)
                .add(header)
                .add(deployments)
                .add(configuration)
                .end();
        if (su) {
            rootBuilder.div().css(eapHomeRow)
                    .add(runtime)
                    .add(accessControl)
                    .end()
                    .div().css(eapHomeRow)
                    .add(patching)
                    .add(help)
                    .end();
        } else {
            rootBuilder.div().css(eapHomeRow)
                    .add(runtime)
                    .add(help)
                    .end();
        }
        Element root = rootBuilder.end().build();
        initWidget(Elements.asWidget(root));
    }

    @Override
    public void setPresenter(final HomepagePresenter presenter) {
        this.presenter = presenter;
    }
}
