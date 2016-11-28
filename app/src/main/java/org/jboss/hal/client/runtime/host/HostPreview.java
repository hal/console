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
package org.jboss.hal.client.runtime.host;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.clickable;

/**
 * @author Harald Pehl
 */
class HostPreview extends RuntimePreview<Host> {

    private final Element reloadLink;
    private final Element restartLink;
    private final PreviewAttributes<Host> attributes;
    private final HostActions hostActions;
    private final Resources resources;

    @SuppressWarnings("HardCodedStringLiteral")
    HostPreview(final HostActions hostActions, final Host host,
            final Resources resources) {
        super(host.getName(), host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER, resources);
        this.hostActions = hostActions;
        this.resources = resources;

        // @formatter:off
        previewBuilder()
            .div().rememberAs(ALERT_CONTAINER)
                .span().rememberAs(ALERT_ICON).end()
                .span().rememberAs(ALERT_TEXT).end()
                .span().textContent(" ").end()
                .a().rememberAs(RELOAD_LINK).css(clickable, alertLink)
                    .on(click, event -> hostActions.reload(host))
                    .textContent(resources.constants().reload())
                .end()
                .a().rememberAs(RESTART_LINK).css(clickable, alertLink)
                    .on(click, event -> hostActions.restart(host))
                    .textContent(resources.constants().restart())
                .end()
            .end();
        // @formatter:on

        alertContainer = previewBuilder().referenceFor(ALERT_CONTAINER);
        alertIcon = previewBuilder().referenceFor(ALERT_ICON);
        alertText = previewBuilder().referenceFor(ALERT_TEXT);
        reloadLink = previewBuilder().referenceFor(RELOAD_LINK);
        restartLink = previewBuilder().referenceFor(RESTART_LINK);

        attributes = new PreviewAttributes<>(host,
                asList(RELEASE_CODENAME, RELEASE_VERSION, PRODUCT_NAME, PRODUCT_VERSION,
                        HOST_STATE, RUNNING_MODE))
                .append(model -> new PreviewAttribute(
                        "Management Version",
                        String.join(".", model.get(MANAGEMENT_MAJOR_VERSION).asString(),
                                model.get(MANAGEMENT_MINOR_VERSION).asString(),
                                model.get(MANAGEMENT_MICRO_VERSION).asString())
                ))
                .end();
        previewBuilder().addAll(attributes);

        update(host);
    }

    @Override
    public void update(final Host host) {
        if (hostActions.isPending(host)) {
            pending(resources.messages().hostPending(host.getName()));
        } else if (host.isAdminMode()) {
            adminOnly(resources.messages().hostAdminMode(host.getName()));
        } else if (host.isStarting()) {
            starting(resources.messages().hostStarting(host.getName()));
        } else if (host.needsReload()) {
            needsReload(resources.messages().hostNeedsReload(host.getName()));
        } else if (host.needsRestart()) {
            needsRestart(resources.messages().hostNeedsRestart(host.getName()));
        } else if (host.isRunning()) {
            running(resources.messages().hostRunning(host.getName()));
        } else {
            unknown(resources.messages().hostUndefined(host.getName()));
        }

        Elements.setVisible(reloadLink, host.needsReload());
        Elements.setVisible(restartLink, host.needsRestart());
        attributes.asElements().forEach(element -> Elements.setVisible(element, !host.isStarting()));
    }
}
