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
            generalResources.appendChild(helpLink("http://www.wildfly.org",
                    resources().constants().homepageHelpWilfdflyHomeText()));
            generalResources.appendChild(helpLink("https://docs.jboss.org/author/display/WFLY10/Documentation",
                    resources().constants().homepageHelpWilfdflyDocumentationText()));
            generalResources.appendChild(helpLink("https://docs.jboss.org/author/display/WFLY10/Admin+Guide",
                    resources().constants().homepageHelpAdminGuideText()));
            generalResources.appendChild(helpLink("http://wildscribe.github.io/index.html",
                    resources().constants().homepageHelpModelReferenceText()));
            generalResources.appendChild(helpLink("https://issues.jboss.org/browse/WFLY",
                    resources().constants().homepageHelpWildflyIssuesText()));
            generalResources.appendChild(helpLink("http://wildfly.org/news/", resources().constants().homepageHelpLatestNews()));

            getHelp.appendChild(helpLink("http://www.jboss.org/developer-materials/",
                    resources().constants().homepageHelpTutorialsText()));
            getHelp.appendChild(helpLink("https://community.jboss.org/en/wildfly?view=discussions",
                    resources().constants().homepageHelpUserForumsText()));
            getHelp.appendChild(helpLink("irc://freenode.org/#wildfly", resources().constants().homepageHelpIrcText()));
            getHelp.appendChild(helpLink("https://lists.jboss.org/mailman/listinfo/wildfly-dev",
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
        return new Elements.Builder().li().a().attr("href", href).innerText(text).end().end().build();
    }
}
