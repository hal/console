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

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.InstanceInfo;
import org.jboss.hal.resources.HalImages;
import org.jboss.hal.resources.I18n;

import javax.annotation.PostConstruct;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage-help")
abstract class HomepageHelp implements IsElement {

    // @formatter:off
    static HomepageHelp create(final Environment env, final I18n i18n, HalImages images) {
        return new Templated_HomepageHelp(env, i18n, images);
    }

    abstract Environment env();
    abstract I18n i18n();
    abstract HalImages images();
    // @formatter:on


    @DataElement Element generalResources;
    @DataElement Element getHelp;

    @PostConstruct
    void init() {
        if (env().getInstanceInfo() == InstanceInfo.WILDFLY) {
            generalResources.appendChild(helpLink("http://www.wildfly.org",
                    i18n().constants().homepage_help_wilfdfly_home_text()));
            generalResources.appendChild(helpLink("https://docs.jboss.org/author/display/WFLY10/Documentation",
                    i18n().constants().homepage_help_wilfdfly_documentation_text()));
            generalResources.appendChild(helpLink("https://docs.jboss.org/author/display/WFLY10/Admin+Guide",
                    i18n().constants().homepage_help_admin_guide_text()));
            generalResources.appendChild(helpLink("http://wildscribe.github.io/index.html",
                    i18n().constants().homepage_help_model_reference_text()));
            generalResources.appendChild(helpLink("https://issues.jboss.org/browse/WFLY",
                    i18n().constants().homepage_help_wildfly_issues_text()));
            generalResources.appendChild(helpLink("http://wildfly.org/news/", i18n().constants().homepage_help_latest_news()));

            getHelp.appendChild(helpLink("http://www.jboss.org/developer-materials/",
                    i18n().constants().homepage_help_tutorials_text()));
            getHelp.appendChild(helpLink("https://community.jboss.org/en/wildfly?view=discussions",
                    i18n().constants().homepage_help_user_forums_text()));
            getHelp.appendChild(helpLink("irc://freenode.org/#wildfly", i18n().constants().homepage_help_irc_text()));
            getHelp.appendChild(helpLink("https://lists.jboss.org/mailman/listinfo/wildfly-dev",
                    i18n().constants().homepage_help_developers_mailing_list_text()));
        } else {
            generalResources.appendChild(helpLink(i18n().constants().homepage_help_eap_documentation_link(),
                    i18n().constants().homepage_help_eap_documentation_text()));
            generalResources.appendChild(helpLink(i18n().constants().homepage_help_learn_more_eap_link(),
                    i18n().constants().homepage_help_learn_more_eap_text()));
            generalResources.appendChild(helpLink(i18n().constants().homepage_help_trouble_ticket_link(),
                    i18n().constants().homepage_help_trouble_ticket_text()));
            generalResources.appendChild(helpLink(i18n().constants().homepage_help_training_link(),
                    i18n().constants().homepage_help_training_text()));

            getHelp.appendChild(helpLink(i18n().constants().homepage_help_tutorials_link(),
                    i18n().constants().homepage_help_tutorials_text()));
            getHelp.appendChild(helpLink(i18n().constants().homepage_help_eap_community_link(),
                    i18n().constants().homepage_help_eap_community_text()));
            getHelp.appendChild(helpLink(i18n().constants().homepage_help_eap_configurations_link(),
                    i18n().constants().homepage_help_eap_configurations_text()));
            getHelp.appendChild(helpLink(i18n().constants().homepage_help_knowledgebase_link(),
                    i18n().constants().homepage_help_knowledgebase_text()));
            getHelp.appendChild(helpLink(i18n().constants().homepage_help_consulting_link(),
                    i18n().constants().homepage_help_consulting_text()));
        }
    }

    private Element helpLink(final String href, final String text) {
        return new Elements.Builder().li().a().attr("href", href).innerText(text).end().end().build();
    }
}
