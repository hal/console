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
import org.jboss.hal.resources.HalImages;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;

/**
 * @author Harald Pehl
 */
public class HomepageView extends ViewImpl implements HomepagePresenter.MyView {

    private HomepagePresenter presenter;

    @Inject
    public HomepageView(Environment env,
            User user,
            I18n i18n,
            HalImages images,
            TokenFormatter tokenFormatter) {

        boolean standalone = env.isStandalone();
        boolean community = env.getInstanceInfo() == WILDFLY;
        boolean su = user.isSuperuser() || user.isAdministrator();
        String name = community ? "WildFly" : "JBoss EAP";

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
                    .div().css("eap-home-title")
                    .h(1).innerText("WildFly").end()
                    .end().build();
        } else {
            // @formatter:off
            header = new Elements.Builder()
                .div().css("eap-home-title")
                    .p()
                        .span().innerText(i18n.constants().homepage_new_to_eap() + " ").end()
                        .a()
                            .css("clickable")
                            .on(click, event -> presenter.launchGuidedTour())
                            .innerText(i18n.constants().homepage_take_a_tour())
                        .end()
                    .end()
                    .h(1).innerText("Red Hat Jboss Enterprise Application Platform").end()
                .end().build();
            // @formatter:on
        }

        if (standalone) {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.Deployments,
                    i18n.constants().homepage_deployments_section(),
                    i18n.constants().homepage_deployments_standalone_step_intro(),
                    Arrays.asList(i18n.constants().homepage_deployments_standalone_step_1(),
                            i18n.constants().homepage_deployments_step_enable())));
            deployments = HomepageModule.create(tokenFormatter,
                    NameTokens.Deployments,
                    "Deployments",
                    i18n.constants().homepage_deployments_sub_header(),
                    images.deployments(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.Configuration,
                    i18n.constants().homepage_configuration_section(),
                    i18n.constants().homepage_configuration_step_intro(),
                    Arrays.asList(
                            i18n.constants().homepage_configuration_standalone_step1(),
                            i18n.constants().homepage_configuration_step2(),
                            i18n.constants().homepage_configuration_step3())));
            configuration = HomepageModule.create(tokenFormatter,
                    NameTokens.Configuration,
                    "Configuration",
                    i18n.constants().homepage_configuration_standalone_sub_header(),
                    images.configuration(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.Runtime,
                    i18n.constants().homepage_runtime_standalone_section(),
                    i18n.constants().homepage_runtime_step_intro(),
                    Arrays.asList(
                            i18n.constants().homepage_runtime_standalone_step1(),
                            i18n.constants().homepage_runtime_standalone_step2())));
            runtime = HomepageModule.create(tokenFormatter,
                    NameTokens.Runtime,
                    "Runtime",
                    i18n.constants().homepage_runtime_standalone_sub_header(),
                    images.runtime(),
                    sections).asElement();

        } else {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.Deployments,
                    i18n.constants().homepage_deployments_section(),
                    i18n.constants().homepage_deployments_domain_step_intro(),
                    Arrays.asList(
                            i18n.constants().homepage_deployments_domain_step_1(),
                            i18n.constants().homepage_deployments_domain_step_2(),
                            i18n.constants().homepage_deployments_step_enable())));
            deployments = HomepageModule.create(tokenFormatter,
                    NameTokens.Deployments,
                    "Deployments",
                    i18n.constants().homepage_deployments_sub_header(),
                    images.deployments(),
                    sections).asElement();

            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.Configuration,
                    i18n.constants().homepage_configuration_section(),
                    i18n.constants().homepage_configuration_step_intro(),
                    Arrays.asList(
                            i18n.constants().homepage_configuration_domain_step1(),
                            i18n.constants().homepage_configuration_step2(),
                            i18n.constants().homepage_configuration_step3())));
            configuration = HomepageModule.create(tokenFormatter,
                    NameTokens.Configuration,
                    "Configuration",
                    i18n.constants().homepage_configuration_domain_sub_header(),
                    images.configuration(),
                    sections).asElement();

            sections = Arrays.asList(
                    HomepageSection.create(tokenFormatter, i18n,
                            NameTokens.Runtime,
                            i18n.constants().homepage_runtime_domain_server_group_section(),
                            i18n.constants().homepage_runtime_domain_server_group_step_intro(),
                            Arrays.asList(
                                    i18n.constants().homepage_runtime_domain_server_group_step1(),
                                    i18n.constants().homepage_runtime_domain_server_group_step2())),
                    HomepageSection.create(tokenFormatter, i18n,
                            NameTokens.Runtime,
                            i18n.constants().homepage_runtime_domain_create_server_section(),
                            i18n.constants().homepage_runtime_domain_create_server_step_intro(),
                            Arrays.asList(
                                    i18n.constants().homepage_runtime_domain_create_server_step1(),
                                    i18n.constants().homepage_runtime_domain_create_server_step2())),
                    HomepageSection.create(tokenFormatter, i18n,
                            NameTokens.Runtime,
                            i18n.constants().homepage_runtime_domain_monitor_server_section(),
                            i18n.constants().homepage_runtime_step_intro(),
                            Arrays.asList(
                                    i18n.constants().homepage_runtime_domain_monitor_server_step1(),
                                    i18n.constants().homepage_runtime_domain_monitor_server_step2())));
            runtime = HomepageModule.create(tokenFormatter,
                    NameTokens.Runtime,
                    "Runtime",
                    i18n.constants().homepage_runtime_domain_sub_header(),
                    images.runtime(),
                    sections).asElement();
        }

        if (su) {
            sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                    NameTokens.AccessControl,
                    i18n.constants().homepage_access_control_section(),
                    i18n.constants().homepage_access_control_step_intro(),
                    Arrays.asList(
                            i18n.constants().homepage_access_control_step1(),
                            i18n.constants().homepage_access_control_step2())));
            accessControl = HomepageModule.create(tokenFormatter,
                    NameTokens.AccessControl,
                    "Access Control",
                    i18n.constants().homepage_access_control_sub_header(),
                    images.accessControl(),
                    sections).asElement();

            if (standalone) {
                sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                        NameTokens.Patching,
                        i18n.constants().homepage_patching_section(),
                        i18n.messages().homepage_patching_standalone_step_intro(name),
                        Arrays.asList(
                                i18n.constants().homepage_patching_step1(),
                                i18n.constants().homepage_patching_step_apply())));
            } else {
                sections = Collections.singleton(HomepageSection.create(tokenFormatter, i18n,
                        NameTokens.Patching,
                        i18n.constants().homepage_patching_section(),
                        i18n.messages().homepage_patching_domain_step_intro(name),
                        Arrays.asList(
                                i18n.constants().homepage_patching_step1(),
                                i18n.constants().homepage_patching_domain_step2(),
                                i18n.constants().homepage_patching_step_apply())));
            }
            patching = HomepageModule.create(tokenFormatter,
                    NameTokens.Patching,
                    "Patching",
                    i18n.messages().homepage_patching_sub_header(name),
                    images.patching(),
                    sections).asElement();
        }

        help = HomepageHelp.create(env, i18n, images).asElement();
        Elements.Builder rootBuilder = new Elements.Builder().div()
                .div().css("eap-home-row")
                .add(header)
                .add(deployments)
                .add(configuration)
                .end();
        if (su) {
            rootBuilder.div().css("eap-home-row")
                    .add(runtime)
                    .add(accessControl)
                    .end()
                    .div().css("eap-home-row")
                    .add(patching)
                    .add(help)
                    .end();
        } else {
            rootBuilder.div().css("eap-home-row")
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
