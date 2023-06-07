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
package org.jboss.hal.client.bootstrap.endpoint;

import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.alertDanger;
import static org.jboss.hal.resources.CSS.bootstrapError;
import static org.jboss.hal.resources.CSS.column;
import static org.jboss.hal.resources.CSS.columnLg;
import static org.jboss.hal.resources.CSS.columnMd;
import static org.jboss.hal.resources.CSS.columnSm;
import static org.jboss.hal.resources.CSS.containerFluid;
import static org.jboss.hal.resources.CSS.errorCircleO;
import static org.jboss.hal.resources.CSS.logo;
import static org.jboss.hal.resources.CSS.logoText;
import static org.jboss.hal.resources.CSS.logoTextFirst;
import static org.jboss.hal.resources.CSS.logoTextLast;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.navbar;
import static org.jboss.hal.resources.CSS.navbarBrand;
import static org.jboss.hal.resources.CSS.navbarDefault;
import static org.jboss.hal.resources.CSS.navbarFixedTop;
import static org.jboss.hal.resources.CSS.navbarHeader;
import static org.jboss.hal.resources.CSS.navbarPf;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.row;

public class RbacProviderFailed implements IsElement<HTMLDivElement> {

    public static void appendToBody(String error) {
        failSafeRemoveFromParent(document.getElementById(Ids.HAL_MODAL));
        failSafeRemoveFromParent(document.getElementById(Ids.HAL_WIZARD));
        failSafeRemoveFromParent(document.getElementById(Ids.HEADER_CONTAINER));
        failSafeRemoveFromParent(document.getElementById(Ids.ROOT_CONTAINER));
        failSafeRemoveFromParent(document.getElementById(Ids.FOOTER_CONTAINER));
        document.body.appendChild(new RbacProviderFailed(error).element());
    }

    private final HTMLDivElement root;

    private RbacProviderFailed(final String error) {
        HTMLElement errorHolder;

        root = div()
                .add(nav().css(navbar, navbarDefault, navbarFixedTop, navbarPf)
                        .attr(UIConstants.ROLE, "navigation")
                        .add(div().css(navbarHeader)
                                .add(a().css(navbarBrand, logo)
                                        .add(span().css(logoText, logoTextFirst)
                                                .textContent("Management "))
                                        .add(span().css(logoText, logoTextLast)
                                                .textContent("Console")))))
                .add(div().css(containerFluid)
                        .add(div().css(row)
                                .add(div().css(column(12, columnLg, columnMd, columnSm))
                                        .add(h(1, "Access Provider Error"))
                                        .add(div().css(alert, alertDanger, marginTopLarge)
                                                .add(span().css(pfIcon(errorCircleO)))
                                                .add(errorHolder = span()
                                                        .textContent("You don't have permission to access this page.")
                                                        .element()))
                                        .add(div()
                                                .add(p().textContent("The management console could not be loaded."))
                                                .add(ul()
                                                        .add(li()
                                                                .add(p()
                                                                        .add("If you changed your access control provider to ")
                                                                        .add(strong().textContent("RBAC"))
                                                                        .add(", make sure that your configuration has current user mapped to one of the ")
                                                                        .add(strong().textContent("RBAC roles"))
                                                                        .add(", preferably with at least one in the Administrator or SuperUser role."))
                                                                .add(li()
                                                                        .add(p()
                                                                                .add("If you have started with one of the standard xml configurations shipped with WildFly,")
                                                                                .add(" the \"$local\" user should be mapped to the \"SuperUser\" role and the \"local\" authentication scheme should be enabled. ")
                                                                                .add("This should allow a user running the CLI on the same system as the WildFly process to have full administrative permissions. ")
                                                                                .add("Remote CLI users and web-based admin console users will have no permissions.")))

                                                                .add(li()
                                                                        .add(p()
                                                                                .add("You should map at least one user besides \"$local\". ")
                                                                                .add("Try to use CLI or shut the installation down and edit the xml configuration.")))))))))
                .element();

        errorHolder.textContent = error;
        document.documentElement.classList.add(bootstrapError);
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }
}
