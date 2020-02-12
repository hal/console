/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.bootstrap;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.resources.UIConstants;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.nav;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.TARGET;

public class BootstrapFailed implements IsElement<HTMLDivElement> {

    private static final String ADD_ALLOWED_ORIGIN = "/core-service=management/" +
            "management-interface=http-interface" +
            ":list-add(name=allowed-origins,value=" + Endpoints.getBaseUrl();

    private final HTMLDivElement root;

    public BootstrapFailed(String error, Endpoints endpoints) {
        HTMLElement errorHolder;
        HTMLElement allowedOriginServer;
        HTMLElement allowedOriginConfig;

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
                                        .add(h(1, "Bootstrap Eror"))
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
                                                                        .add("Make sure you've added " + Endpoints.getBaseUrl() + " as ")
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
                                                                                        "/host=master" + ADD_ALLOWED_ORIGIN))))
                                                                .element()))))))
                .element();

        errorHolder.textContent = error;
        setVisible(allowedOriginServer, !endpoints.isSameOrigin());
        setVisible(allowedOriginConfig, !endpoints.isSameOrigin());
        document.documentElement.classList.add(bootstrapError);
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }
}
