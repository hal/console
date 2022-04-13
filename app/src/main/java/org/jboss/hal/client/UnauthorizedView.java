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
package org.jboss.hal.client;

import org.jboss.hal.client.bootstrap.LoadingPanel;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLDivElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.hal.resources.CSS.*;

public class UnauthorizedView extends HalViewImpl implements UnauthorizedPresenter.MyView {

    public UnauthorizedView() {
        HTMLDivElement root = div()
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
                                        .add(h(1, "Forbidden"))
                                        .add(div().css(alert, alertDanger, marginTopLarge)
                                                .add(span().css(pfIcon(errorCircleO)))
                                                .add(span().textContent(
                                                        "You don't have permission to access this page."))))))
                .element();
        initElement(root);
        LoadingPanel.get().off();
        document.documentElement.classList.add(bootstrapError);
    }
}
