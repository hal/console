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
package org.jboss.hal.client.homepage;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.eapHomeRow;
import static org.jboss.hal.resources.CSS.eapHomeTitle;

public class HomepageView extends HalViewImpl implements HomepagePresenter.MyView {

    private HomepagePresenter presenter;

    @Inject
    public HomepageView(Environment environment, AccessControl ac, Resources resources, Places places) {

        boolean standalone = environment.isStandalone();
        boolean ssoEnabled = environment.isSingleSignOn();
        boolean community = environment.getHalBuild() == Build.COMMUNITY;
        boolean su = ac.isSuperUserOrAdministrator();
        boolean patchable = environment.isPatchingEnabled();
        String name = environment.getInstanceInfo().productName();

        Iterable<HomepageSection> sections;
        HTMLElement header;
        HTMLElement deployments;
        HTMLElement configuration;
        HTMLElement runtime;
        HTMLElement accessControl = div().element(); // to get rid of warning "might not be initialized"
        HTMLElement patching = div().element();
        HTMLElement help;

        if (community) {
            header = div().css(eapHomeTitle)
                    .add(h(1).textContent(resources.theme().getFullName())).element();
        } else {
            header = div().css(eapHomeTitle)
                    .add(p()
                            .add(span().textContent(resources.constants().homepageNewToEap() + " "))
                            .add(a().css(clickable)
                                    .on(click, event -> presenter.launchGuidedTour())
                                    .textContent(resources.constants().homepageTakeATour())))
                    .add(h(1).textContent(resources.theme().getFullName())).element();
        }

        if (standalone) {
            sections = Collections.singleton(new HomepageSection(places, resources,
                    Ids.HOMEPAGE_DEPLOYMENTS_SECTION, NameTokens.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSection(),
                    resources.constants().homepageDeploymentsStandaloneStepIntro(),
                    Arrays.asList(resources.constants().homepageDeploymentsStandaloneStep1(),
                            resources.constants().homepageDeploymentsStepEnable()),
                    true));
            deployments = new HomepageModule(places,
                    Ids.HOMEPAGE_DEPLOYMENTS_MODULE, NameTokens.DEPLOYMENTS, Names.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSubHeader(),
                    resources.images().deployments(),
                    sections).element();

            sections = Collections.singleton(new HomepageSection(places, resources,
                    Ids.HOMEPAGE_CONFIGURATION_SECTION, NameTokens.CONFIGURATION,
                    resources.constants().homepageConfigurationSection(),
                    resources.constants().homepageConfigurationStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageConfigurationStandaloneStep1(),
                            resources.constants().homepageConfigurationStep2(),
                            resources.constants().homepageConfigurationStep3()),
                    true));
            configuration = new HomepageModule(places,
                    Ids.HOMEPAGE_CONFIGURATION_MODULE, NameTokens.CONFIGURATION, Names.CONFIGURATION,
                    resources.constants().homepageConfigurationStandaloneSubHeader(),
                    resources.images().configuration(),
                    sections).element();

            sections = Collections.singleton(new HomepageSection(places, resources,
                    Ids.HOMEPAGE_RUNTIME_SECTION, NameTokens.RUNTIME,
                    resources.constants().homepageRuntimeStandaloneSection(),
                    resources.constants().homepageRuntimeStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageRuntimeStandaloneStep1(),
                            resources.constants().homepageRuntimeStandaloneStep2()),
                    true));
            runtime = new HomepageModule(places,
                    Ids.HOMEPAGE_RUNTIME_MODULE, NameTokens.RUNTIME, Names.RUNTIME,
                    resources.constants().homepageRuntimeStandaloneSubHeader(),
                    resources.images().runtime(),
                    sections).element();

        } else {
            sections = Collections.singleton(new HomepageSection(places, resources,
                    Ids.HOMEPAGE_DEPLOYMENTS_SECTION, NameTokens.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSection(),
                    resources.constants().homepageDeploymentsDomainStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageDeploymentsDomainStep1(),
                            resources.constants().homepageDeploymentsDomainStep2(),
                            resources.constants().homepageDeploymentsStepEnable()),
                    true));
            deployments = new HomepageModule(places,
                    Ids.HOMEPAGE_DEPLOYMENTS_MODULE, NameTokens.DEPLOYMENTS, Names.DEPLOYMENTS,
                    resources.constants().homepageDeploymentsSubHeader(),
                    resources.images().deployments(),
                    sections).element();

            sections = Collections.singleton(new HomepageSection(places, resources,
                    Ids.HOMEPAGE_CONFIGURATION_SECTION, NameTokens.CONFIGURATION,
                    resources.constants().homepageConfigurationSection(),
                    resources.constants().homepageConfigurationStepIntro(),
                    Arrays.asList(
                            resources.constants().homepageConfigurationDomainStep1(),
                            resources.constants().homepageConfigurationStep2(),
                            resources.constants().homepageConfigurationStep3()),
                    true));
            configuration = new HomepageModule(places,
                    Ids.HOMEPAGE_CONFIGURATION_MODULE, NameTokens.CONFIGURATION, Names.CONFIGURATION,
                    resources.constants().homepageConfigurationDomainSubHeader(),
                    resources.images().configuration(),
                    sections).element();

            sections = Arrays.asList(
                    new HomepageSection(places, resources,
                            Ids.HOMEPAGE_RUNTIME_SERVER_GROUP_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainServerGroupSection(),
                            resources.constants().homepageRuntimeDomainServerGroupStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainServerGroupStep1(),
                                    resources.constants().homepageRuntimeDomainServerGroupStep2()),
                            true),
                    new HomepageSection(places, resources,
                            Ids.HOMEPAGE_RUNTIME_SERVER_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainCreateServerSection(),
                            resources.constants().homepageRuntimeDomainCreateServerStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainCreateServerStep1(),
                                    resources.constants().homepageRuntimeDomainCreateServerStep2()),
                            true),
                    new HomepageSection(places, resources,
                            Ids.HOMEPAGE_RUNTIME_MONITOR_SECTION, NameTokens.RUNTIME,
                            resources.constants().homepageRuntimeDomainMonitorServerSection(),
                            resources.constants().homepageRuntimeStepIntro(),
                            Arrays.asList(
                                    resources.constants().homepageRuntimeDomainMonitorServerStep1(),
                                    resources.constants().homepageRuntimeDomainMonitorServerStep2()),
                            true));
            runtime = new HomepageModule(places,
                    Ids.HOMEPAGE_RUNTIME_MODULE, NameTokens.RUNTIME, Names.RUNTIME,
                    resources.constants().homepageRuntimeDomainSubHeader(),
                    resources.images().runtime(),
                    sections).element();
        }

        if (su) {
            if (ssoEnabled) {
                accessControl = new HomepageModule(places,
                        Ids.HOMEPAGE_ACCESS_CONTROL_MODULE, NameTokens.ACCESS_CONTROL_SSO, Names.ACCESS_CONTROL,
                        resources.constants().homepageAccessControlSsoSubHeader(),
                        resources.images().accessControl(), Collections.emptyList()).element();
            } else {
                sections = Collections.singleton(new HomepageSection(places, resources,
                        Ids.HOMEPAGE_ACCESS_CONTROL_SECTION, NameTokens.ACCESS_CONTROL,
                        resources.constants().homepageAccessControlSection(),
                        resources.constants().homepageAccessControlStepIntro(),
                        Arrays.asList(
                                resources.constants().homepageAccessControlStep1(),
                                resources.constants().homepageAccessControlStep2()),
                        true));
                accessControl = new HomepageModule(places,
                        Ids.HOMEPAGE_ACCESS_CONTROL_MODULE, NameTokens.ACCESS_CONTROL, Names.ACCESS_CONTROL,
                        resources.constants().homepageAccessControlSubHeader(),
                        resources.images().accessControl(),
                        sections).element();
            }

            if (standalone) {
                sections = Collections.singleton(new HomepageSection(places, resources,
                        Ids.HOMEPAGE_PATCHING_SECTION, NameTokens.PATCHING,
                        resources.constants().homepagePatchingSection(),
                        resources.messages().homepagePatchingStandaloneStepIntro(name),
                        Arrays.asList(
                                resources.constants().homepagePatchingStep1(),
                                resources.constants().homepagePatchingStepApply()),
                        true));
            } else {
                sections = Collections.singleton(new HomepageSection(places, resources,
                        Ids.HOMEPAGE_PATCHING_SECTION, NameTokens.PATCHING,
                        resources.constants().homepagePatchingSection(),
                        resources.messages().homepagePatchingDomainStepIntro(name),
                        Arrays.asList(
                                resources.constants().homepagePatchingStep1(),
                                resources.constants().homepagePatchingDomainStep2(),
                                resources.constants().homepagePatchingStepApply()),
                        true));
            }
            if (patchable) {
                patching = new HomepageModule(places,
                        Ids.HOMEPAGE_PATCHING_MODULE, NameTokens.PATCHING, Names.PATCHING,
                        resources.messages().homepagePatchingSubHeader(name),
                        resources.images().patching(),
                        sections).element();
            }
        }

        help = new HomepageHelp(environment, resources).element();

        HTMLElement root = div()
                .add(div().css(eapHomeRow)
                        .add(header)
                        .add(deployments)
                        .add(configuration))
                .element();
        if (su) {
            root.appendChild(div().css(eapHomeRow)
                    .add(runtime)
                    .add(accessControl).element());
            if (patchable) {
                root.appendChild(div().css(eapHomeRow)
                        .add(patching).element());
            }
            root.appendChild(div().css(eapHomeRow)
                    .add(help).element());
        } else {
            root.appendChild(div().css(eapHomeRow)
                    .add(runtime)
                    .add(help).element());
        }
        initElement(root);
    }

    @Override
    public void setPresenter(HomepagePresenter presenter) {
        this.presenter = presenter;
    }
}
