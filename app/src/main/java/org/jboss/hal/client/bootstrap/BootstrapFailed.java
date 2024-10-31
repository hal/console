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
package org.jboss.hal.client.bootstrap;

import org.jboss.elemento.Elements;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.setVisible;
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
import static org.jboss.hal.resources.UIConstants.TARGET;

public class BootstrapFailed {

    private static final String ADD_ALLOWED_ORIGIN = "/core-service=management/" +
            "management-interface=http-interface" +
            ":list-add(name=allowed-origins,value=" + Endpoints.getBaseUrl();

    private static void appendToBody(HTMLDivElement errorView) {
        document.documentElement.classList.add(bootstrapError);
        Elements.removeChildrenFrom(document.body);
        document.body.appendChild(errorView);
    }

    private static HTMLDivElement errorView(final String heading, final String error, final HTMLElement content) {
        return div()
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
                                        .add(h(1, heading))
                                        .add(div().css(alert, alertDanger, marginTopLarge)
                                                .add(span().css(pfIcon(errorCircleO)))
                                                .add(span()
                                                        .textContent(error)
                                                        .element()))
                                        .add(div()
                                                .add(p().textContent("The management console could not be loaded."))
                                                .add(content)))))
                .element();
    }

    public static void generalBootstrapError(final String error, Endpoints endpoints) {
        HTMLElement allowedOriginServer;
        HTMLElement allowedOriginConfig;

        HTMLElement content = ul()
                .add(li()
                        .add(p()
                                .add("Make sure the server is ")
                                .add(strong().textContent("up and running"))
                                .add(". Please check the log files for possible errors during startup.")))
                .add(li()
                        .add(p()
                                .add("Verify you have ")
                                .add(strong().textContent("added users"))
                                .add(" which are able to access the admin console."))
                        .add(p()
                                .add("To add a new user execute the ")
                                .add(code().textContent("add-user.sh"))
                                .add(" script within the bin folder of your installation and enter the requested information.")))
                .add(allowedOriginServer = li()
                        .add(p()
                                .add("Check that the management endpoint at ")
                                .add(a(endpoints.dmr())
                                        .attr(TARGET, "_blank")
                                        .textContent(endpoints.dmr()))
                                .add(" is available."))
                        .element())
                .add(allowedOriginConfig = li()
                        .add(p()
                                .add("Make sure you've added " + Endpoints.getBaseUrl()
                                        + " as ")
                                .add(strong().textContent("allowed origin"))
                                .add(" and reload the server. Use one of the following CLI commands to add allowed origins:"))
                        .add(ul()
                                .add(li()
                                        .add(p().textContent("Standalone:"))
                                        .add(pre().textContent(
                                                ADD_ALLOWED_ORIGIN)))
                                .add(li()
                                        .add(p().textContent("Domain:"))
                                        .add(pre().textContent(
                                                "/host=primary" + ADD_ALLOWED_ORIGIN))))
                        .element())
                .element();

        HTMLDivElement root = errorView("Bootstrap Error", error, content);
        setVisible(allowedOriginServer, !endpoints.isSameOrigin());
        setVisible(allowedOriginConfig, !endpoints.isSameOrigin());
        appendToBody(root);
    }

    public static void rbacProviderFailed(final String error) {
        HTMLElement content = ul()
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
                                        .add("Try to use CLI or shut the installation down and edit the xml configuration."))))
                .element();

        appendToBody(errorView("Access Provider Error", error, content));
    }

    public static void operationTimedOut(final String error) {
        HTMLElement content = ul()
                .add(li()
                        .add(p()
                                .add("Check the status of your domain, one or more hosts are unresponsive.")))
                .element();

        appendToBody(errorView("Bootstrap Error", error, content));
    }
}
