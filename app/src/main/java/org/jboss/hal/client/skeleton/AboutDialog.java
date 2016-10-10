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
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.resources.Theme;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class AboutDialog {

    private static class AboutBuilder extends Elements.CoreBuilder<AboutBuilder> {

        private final LabelBuilder labelBuilder;

        AboutBuilder() {
            super("hal.aboutBuilder");
            labelBuilder = new LabelBuilder();
        }

        @Override
        protected AboutBuilder that() {
            return this;
        }

        @SuppressWarnings("HardCodedStringLiteral")
        AboutBuilder line(@NonNls String key, String value) {
            if (value != null && !UNDEFINED.equals(value)) {
                start("dt").textContent(labelBuilder.label(key)).end();
                start("dd").textContent(value).end();
            }
            return this;
        }
    }


    private final Theme theme;
    private final Element aboutContent;

    AboutDialog(final Environment environment, final Endpoints endpoints, final Theme theme) {
        this.theme = theme;

        // @formatter:off
        AboutBuilder builder = new AboutBuilder()
            .h(1).textContent(theme.getFullName()).end()
            .div().css(productVersionsPf)
                .start("dl").css(dlHorizontal)
                    .line(PRODUCT_NAME, environment.getInstanceInfo().productName())
                    .line(PRODUCT_VERSION, environment.getInstanceInfo().productVersion())
                    .line(RELEASE_CODENAME, environment.getInstanceInfo().releaseName())
                    .line(RELEASE_VERSION, environment.getInstanceInfo().releaseVersion())
                    .line("management-version", environment.getManagementVersion().toString())
                    .line("console-version", environment.getHalVersion().toString())
                    .line("operation-Mode", environment.getOperationMode().name())
                    .line("server-name", environment.getInstanceInfo().serverName());
                    if (!endpoints.isSameOrigin()) {
                        builder.line("connected-to", endpoints.dmr());
                    }
                builder.end()
            .end();
        // @formatter:on
        aboutContent = builder.build();
    }

    void show() {
        Dialog dialog = new Dialog.Builder("")
                .closeOnEsc(true)
                .closeIcon(true)
                .onClose(() -> {
                    Element content = contentElement();
                    if (content != null) {
                        content.getClassList().remove(aboutModalPf);
                    }
                    Element footer = footerElement();
                    Elements.removeChildrenFrom(footer);
                })
                .fadeIn(true)
                .add(aboutContent)
                .build();

        Element content = contentElement();
        if (content != null) {
            content.getClassList().add(aboutModalPf);
        }
        Element footer = footerElement();
        if (footer != null) {
            Element img = Browser.getDocument().createElement("img"); //NON-NLS
            img.setAttribute("src", theme.logos().about().getSafeUri().asString());
            img.setTitle(theme.getFullName());
            footer.appendChild(img);
            Elements.setVisible(footer, true);
        }
        dialog.show();
    }

    private Element contentElement() {
        return Browser.getDocument().querySelector(Dialog.SELECTOR_ID + " ." + modalContent);
    }

    private Element footerElement() {
        return Browser.getDocument().querySelector(Dialog.SELECTOR_ID + " ." + modalFooter);
    }
}
