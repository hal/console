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
package org.jboss.hal.client.homepage;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Urls.*;

class HomepageHelp implements IsElement<HTMLDivElement> {

    private final HTMLDivElement root;

    public HomepageHelp(Environment environment, Resources resources) {
        HTMLElement generalResources;
        HTMLElement getHelp;

        root = div().css(eapHomeCol)
                .add(div().css(eapHomeModule)
                        .add(div().css(eapHomeModuleIcon)
                                .add(img(resources.images().help().getSafeUri().asString())))
                        .add(div().css(eapHomeModuleContainer)
                                .add(div().css(eapHomeModuleHeader)
                                        .add(h(2, resources.constants().homepageHelpNeedHelp()))
                                        .add(p()))
                                .add(div().css(eapHomeModuleCol)
                                        .add(p().textContent(resources.constants().homepageHelpGeneralResources()))
                                        .add(generalResources = ul().element()))
                                .add(div().css(eapHomeModuleCol)
                                        .add(p().textContent(resources.constants().homepageHelpGetHelp()))
                                        .add(getHelp = ul().element()))))
                .element();

        if (environment.getHalBuild() == Build.COMMUNITY) {
            generalResources.appendChild(helpLink(WILDFLY_HOMEPAGE,
                    resources.constants().homepageHelpWildFlyHomeText()));
            generalResources.appendChild(helpLink(WILDFLY_DOCUMENTATION,
                    resources.constants().homepageHelpWilfdflyDocumentationText()));
            generalResources.appendChild(helpLink(WILDSCRIBE_GITHUB_IO,
                    resources.constants().homepageHelpModelReferenceText()));
            generalResources.appendChild(helpLink(WILDFLY_ISSUES,
                    resources.constants().homepageHelpWildflyIssuesText()));
            generalResources.appendChild(helpLink(WILDFLY_NEWS, resources.constants().homepageHelpLatestNews()));

            getHelp.appendChild(helpLink(JBOSS_ORG_DEVELOPER_MATERIALS,
                    resources.constants().homepageHelpTutorialsText()));
            getHelp.appendChild(helpLink(JBOSS_COMMUNITY_DISCUSSIONS,
                    resources.constants().homepageHelpUserForumsText()));
            getHelp.appendChild(helpLink(WILDFLY_IRC, resources.constants().homepageHelpIrcText()));
            getHelp.appendChild(helpLink(WILDFLY_DEV_MAILING_LIST,
                    resources.constants().homepageHelpDevelopersMailingListText()));
        } else {
            generalResources.appendChild(helpLink(resources.constants().homepageHelpEapDocumentationLink(),
                    resources.constants().homepageHelpEapDocumentationText()));
            generalResources.appendChild(helpLink(resources.constants().homepageHelpLearnMoreEapLink(),
                    resources.constants().homepageHelpLearnMoreEapText()));
            generalResources.appendChild(helpLink(resources.constants().homepageHelpTroubleTicketLink(),
                    resources.constants().homepageHelpTroubleTicketText()));
            generalResources.appendChild(helpLink(resources.constants().homepageHelpTrainingLink(),
                    resources.constants().homepageHelpTrainingText()));

            getHelp.appendChild(helpLink(resources.constants().homepageHelpTutorialsLink(),
                    resources.constants().homepageHelpTutorialsText()));
            getHelp.appendChild(helpLink(resources.constants().homepageHelpEapCommunityLink(),
                    resources.constants().homepageHelpEapCommunityText()));
            getHelp.appendChild(helpLink(resources.constants().homepageHelpEapConfigurationsLink(),
                    resources.constants().homepageHelpEapConfigurationsText()));
            getHelp.appendChild(helpLink(resources.constants().homepageHelpKnowledgebaseLink(),
                    resources.constants().homepageHelpKnowledgebaseText()));
            getHelp.appendChild(helpLink(resources.constants().homepageHelpConsultingLink(),
                    resources.constants().homepageHelpConsultingText()));
        }
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }

    private HTMLElement helpLink(String href, String text) {
        return li().add(a(href).textContent(text)).element();
    }
}
