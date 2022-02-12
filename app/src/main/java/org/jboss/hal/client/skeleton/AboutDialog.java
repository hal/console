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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.ImmutableMap;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

class AboutDialog {

    private static final String SELECTOR_ID = HASH + Ids.ABOUT_MODAL;

    AboutDialog(Environment environment, Endpoints endpoints, Resources resources) {
        if (document.getElementById(Ids.ABOUT_MODAL) == null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String> builder()
                    .put(resources.constants().name(), failSafe(environment.getName()))
                    .put(resources.constants().organization(), failSafe(environment.getOrganization()))
                    .put(resources.constants().productName(), failSafe(environment.getInstanceInfo().productName()))
                    .put(resources.constants().productVersion(),
                            failSafe(environment.getInstanceInfo().productVersion()))
                    .put(resources.constants().releaseName(), failSafe(environment.getInstanceInfo().releaseName()))
                    .put(resources.constants().releaseVersion(),
                            failSafe(environment.getInstanceInfo().releaseVersion()))
                    .put(resources.constants().managementVersion(),
                            failSafe(environment.getManagementVersion().toString()))
                    .put(resources.constants().consoleVersion(), failSafe(environment.getHalVersion().toString()))
                    .put(resources.constants().operationMode(), failSafe(environment.getOperationMode().name()));
            if (!endpoints.isSameOrigin()) {
                builder.put(resources.constants().connectedTo(), failSafe(endpoints.dmr()));
            }
            List<HTMLElement> elements = new ArrayList<>();
            builder.build().forEach((key, value) -> {
                elements.add(dt().textContent(key).element());
                elements.add(dd().textContent(value).element());
            });

            HTMLElement about = div().id(Ids.ABOUT_MODAL)
                    .css(modal, fade, in)
                    .attr(ROLE, DIALOG)
                    .attr(TABINDEX, "-1")
                    .aria(LABELLED_BY, Ids.ABOUT_MODAL_TITLE)
                    .add(div().css(modalDialog)
                            .add(div().css(modalContent, aboutModalPf)
                                    .add(div().css(modalHeader)
                                            .add(button().css(close)
                                                    .aria(LABEL, resources.constants().close())
                                                    .data(DISMISS, "modal") // NON-NLS
                                                    .add(span().css(pfIcon("close"))
                                                            .aria(HIDDEN, String.valueOf(true)))))
                                    .add(div().css(modalBody)
                                            .add(h(1).textContent(resources.theme().getFullName()))
                                            .add(div().css(productVersionsPf)
                                                    .add(dl().css(dlHorizontal)
                                                            .addAll(elements))))
                                    .add(div().css(modalFooter)
                                            .add(img(resources.theme().logos().about().getSafeUri().asString())))))
                    .element();
            document.body.appendChild(about);
        }
    }

    private String failSafe(String value) {
        return value != null && !UNDEFINED.equals(value) ? value : Names.NOT_AVAILABLE;
    }

    void show() {
        $(SELECTOR_ID).modal(ModalOptions.create(true));
        $(SELECTOR_ID).modal("show");
    }
}
