/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.skeleton;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/**
 * @author Harald Pehl
 */
class AboutDialog {

    private static class AboutBuilder extends Elements.CoreBuilder<AboutBuilder> {

        AboutBuilder() {
            super("hal.aboutBuilder");
        }

        @Override
        protected AboutBuilder that() {
            return this;
        }

        @SuppressWarnings("HardCodedStringLiteral")
        AboutBuilder line(@NonNls String title, String value) {
            if (value != null && !UNDEFINED.equals(value)) {
                start("dt").textContent(title).end();
                start("dd").textContent(value).end();
            }
            return this;
        }
    }

    private static final String SELECTOR_ID = "#" + Ids.ABOUT_MODAL;

    AboutDialog(final Environment environment, final Endpoints endpoints, final Resources resources) {
        if (Browser.getDocument().getElementById(Ids.ABOUT_MODAL) == null) {
            // @formatter:off
            AboutBuilder builder = new AboutBuilder()
                .div().id(Ids.ABOUT_MODAL).css(modal, fade, in)
                        .attr(ROLE, DIALOG)
                        .attr(TABINDEX, "-1")
                        .aria(LABELLED_BY, Ids.ABOUT_MODAL_TITLE)
                    .div().css(modalDialog)
                        .div().css(modalContent, aboutModalPf)
                            .div().css(modalHeader)
                                .button().css(close).aria(LABEL, resources.constants().close()).data(DISMISS, "modal") //NON-NLS
                                    .span().css(pfIcon("close")).aria(HIDDEN, String.valueOf(true)).end()
                                .end()
                            .end()
                            .div().css(modalBody)
                                .h(1).textContent(resources.theme().getFullName()).end()
                                .div().css(productVersionsPf)
                                    .start("dl").css(dlHorizontal)
                                        .line(resources.constants().productName(), environment.getInstanceInfo().productName())
                                        .line(resources.constants().productVersion(), environment.getInstanceInfo().productVersion())
                                        .line(resources.constants().releaseName(), environment.getInstanceInfo().releaseName())
                                        .line(resources.constants().releaseVersion(), environment.getInstanceInfo().releaseVersion())
                                        .line(resources.constants().managementVersion(), environment.getManagementVersion().toString())
                                        .line(resources.constants().consoleVersion(), environment.getHalVersion().toString())
                                        .line(resources.constants().operationMode(), environment.getOperationMode().name())
                                        .line(resources.constants().serverName(), environment.getInstanceInfo().serverName());
                                        if (!endpoints.isSameOrigin()) {
                                            builder.line(resources.constants().connectedTo(), endpoints.dmr());
                                        }
                                    builder.end()
                                .end()
                            .end()
                            .div().css(modalFooter)
                                .add("img").attr("src", resources.theme().logos().about().getSafeUri().asString())
                            .end()
                        .end()
                    .end()
                .end();
            // @formatter:on
            Element about = builder.build();
            Browser.getDocument().getBody().appendChild(about);
        }
    }

    void show() {
        $(SELECTOR_ID).modal(ModalOptions.create(true));
        $(SELECTOR_ID).modal("show");
    }
}
