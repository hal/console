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
package org.jboss.hal.client.skeleton;

import org.jboss.elemento.IsElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.config.Version;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.stability;
import static org.jboss.hal.resources.CSS.stabilityBanner;

public class StabilityBanner implements IsElement<HTMLElement> {

    public static void noStabilityLevelOffset() {
        HTMLElement r = (HTMLElement) document.querySelector(":root");
        r.style.setProperty("--stability-offset", "0px");
    }

    private final HTMLElement root;

    public StabilityBanner(Environment environment, Resources resources) {
        String link = "https://docs.wildfly.org/" + version(environment) + "/Admin_Guide.html#Feature_stability_levels";
        StabilityLevel stabilityLevel = environment.getStabilityLevel();
        String stabilityClass = stability + "-" + stabilityLevel.label;
        this.root = div().css(stabilityBanner, stabilityClass)
                .add(span().css(CSS.stabilityBannerText)
                        .add(i().css(stabilityLevel.icon))
                        .add(" ")
                        .add(span().innerHtml(resources.messages().stabilityLevelText(stabilityLevel.label)))
                        .add(" ")
                        .add(i().css(stabilityLevel.icon))
                        .add(" "))
                .add(span().css(CSS.stabilityBannerMoreInfo)
                        .add(a("#")
                                .textContent(resources.constants().stabilityLevelGotIt())
                                .on(click, e -> {
                                    e.preventDefault();
                                    noStabilityLevelOffset();
                                    failSafeRemoveFromParent(element());
                                }))
                        .add(" | ")
                        .add(a(link, "_blank")
                                .add(resources.constants().stabilityLevelMoreInfo())))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private String version(Environment environment) {
        Version productVersion = Version.parseVersion(environment.getInstanceInfo().productVersion());
        return productVersion.getMinor() == 0
                ? String.valueOf(productVersion.getMajor())
                : productVersion.getMajor() + "." + productVersion.getMinor();
    }
}
