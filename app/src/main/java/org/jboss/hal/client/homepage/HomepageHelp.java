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
import org.jboss.hal.resources.Resources;

import javax.annotation.PostConstruct;

import static org.jboss.hal.resources.Urls.*;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage-help")
abstract class HomepageHelp implements IsElement {

    // @formatter:off
    static HomepageHelp create(final Environment env, final Resources resources) {
        return new Templated_HomepageHelp(env, resources);
    }

    abstract Environment env();
    abstract Resources resources();
    // @formatter:on


    @DataElement Element generalResources;
    @DataElement Element getHelp;

    @PostConstruct
    void init() {
        if (env().getInstanceInfo() == InstanceInfo.WILDFLY) {
            generalResources.appendChild(helpLink(WILDFLY_HOMEPAGE,
                    resources().constants().homepageHelpWildFlyHomeText()));
            generalResources.appendChild(helpLink(WFLY10_DOCUMENTATION,
                    resources().constants().homepageHelpWilfdflyDocumentationText()));
            generalResources.appendChild(helpLink(WFLY10_ADMIN_GUIDE,
                    resources().constants().homepageHelpAdminGuideText()));
            generalResources.appendChild(helpLink(WILDSCRIBE_GITHUB_IO,
                    resources().constants().homepageHelpModelReferenceText()));
            generalResources.appendChild(helpLink(WILDFLY_ISSUES,
                    resources().constants().homepageHelpWildflyIssuesText()));
            generalResources.appendChild(helpLink(WILDFLY_NEWS, resources().constants().homepageHelpLatestNews()));

            getHelp.appendChild(helpLink(JBOSS_ORG_DEVELOPER_MATERIALS,
                    resources().constants().homepageHelpTutorialsText()));
            getHelp.appendChild(helpLink(JBOSS_COMMUNITY_DISCUSSIONS,
                    resources().constants().homepageHelpUserForumsText()));
            getHelp.appendChild(helpLink(WILDFLY_IRC, resources().constants().homepageHelpIrcText()));
            getHelp.appendChild(helpLink(WILDFLY_DEV_MAILING_LIST,
                    resources().constants().homepageHelpDevelopersMailingListText()));
        } else {
            generalResources.appendChild(helpLink(resources().constants().homepageHelpEapDocumentationLink(),
                    resources().constants().homepageHelpEapDocumentationText()));
            generalResources.appendChild(helpLink(resources().constants().homepageHelpLearnMoreEapLink(),
                    resources().constants().homepageHelpLearnMoreEapText()));
            generalResources.appendChild(helpLink(resources().constants().homepageHelpTroubleTicketLink(),
                    resources().constants().homepageHelpTroubleTicketText()));
            generalResources.appendChild(helpLink(resources().constants().homepageHelpTrainingLink(),
                    resources().constants().homepageHelpTrainingText()));

            getHelp.appendChild(helpLink(resources().constants().homepageHelpTutorialsLink(),
                    resources().constants().homepageHelpTutorialsText()));
            getHelp.appendChild(helpLink(resources().constants().homepageHelpEapCommunityLink(),
                    resources().constants().homepageHelpEapCommunityText()));
            getHelp.appendChild(helpLink(resources().constants().homepageHelpEapConfigurationsLink(),
                    resources().constants().homepageHelpEapConfigurationsText()));
            getHelp.appendChild(helpLink(resources().constants().homepageHelpKnowledgebaseLink(),
                    resources().constants().homepageHelpKnowledgebaseText()));
            getHelp.appendChild(helpLink(resources().constants().homepageHelpConsultingLink(),
                    resources().constants().homepageHelpConsultingText()));
        }
    }

    private Element helpLink(final String href, final String text) {
        return new Elements.Builder().li().a().attr("href", href).textContent(text).end().end().build();
    }
}
