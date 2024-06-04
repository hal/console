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

import org.jboss.elemento.IsElement;
import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Version;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.img;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.resources.CSS.eapHomeCol;
import static org.jboss.hal.resources.CSS.eapHomeModule;
import static org.jboss.hal.resources.CSS.eapHomeModuleCol;
import static org.jboss.hal.resources.CSS.eapHomeModuleContainer;
import static org.jboss.hal.resources.CSS.eapHomeModuleHeader;
import static org.jboss.hal.resources.CSS.eapHomeModuleIcon;

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
            // Strings for community are not localized
            int major = Version.parseVersion(environment.getInstanceInfo().productVersion()).getMajor();
            generalResources.appendChild(helpLink("https://www.wildfly.org", "WildFly Home"));
            generalResources.appendChild(helpLink("https://docs.wildfly.org" + "/" + major, "WildFly Documentation"));
            generalResources.appendChild(helpLink("https://docs.wildfly.org" + "/" + major + "/wildscribe",
                    "Model Reference Documentation"));
            generalResources.appendChild(helpLink("https://wildfly.org/news/", "Latest News"));
            generalResources.appendChild(helpLink("https://issues.jboss.org/browse/WFLY", "Browse Issues"));

            getHelp.appendChild(helpLink("https://www.wildfly.org/get-started/", "Getting Started"));
            getHelp.appendChild(helpLink("https://www.wildfly.org/guides/", "Guides"));
            getHelp.appendChild(helpLink("https://groups.google.com/forum/#!forum/wildfly", "Join the Forum"));
            getHelp.appendChild(helpLink("https://wildfly.zulipchat.com/", "Join Zulip Chat"));
            getHelp.appendChild(helpLink("https://lists.jboss.org/archives/list/wildfly-dev@lists.jboss.org/",
                    "Developers Mailing List"));
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
