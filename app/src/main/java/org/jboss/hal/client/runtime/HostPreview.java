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
package org.jboss.hal.client.runtime;

import com.google.common.base.Joiner;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class HostPreview extends PreviewContent<Host> {

    private static final String ALERT_CONTAINER = "alert-container-element";
    private static final String ALERT_ICON = "alert-icon-element";
    private static final String ALERT_TEXT = "alert-text-element";
    private static final String RELOAD_LINK = "reload-link";
    private static final String RESTART_LINK = "restart-link";

    private final Resources resources;
    private final Element alertContainer;
    private final Element alertIcon;
    private final Element alertText;
    private final Element reloadLink;
    private final Element restartLink;
    private final PreviewAttributes<Host> attributes;

    @SuppressWarnings("HardCodedStringLiteral")
    HostPreview(final HostColumn hostColumn, final HostActions hostActions, final Host host,
            final Resources resources) {
        super(host.getName(), host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER);
        this.resources = resources;

        // @formatter:off
        previewBuilder()
            .div().rememberAs(ALERT_CONTAINER)
                .span().rememberAs(ALERT_ICON).end()
                .span().rememberAs(ALERT_TEXT).end()
                .span().textContent(" ").end()
                .a().rememberAs(RELOAD_LINK).css(clickable, alertLink)
                    .on(click, event -> hostActions.reload(host,
                        () -> hostColumn.beforeReload(host),
                        () -> hostColumn.refreshItem(Host.id(host), host),
                        () -> hostColumn.afterReloadRestart(host),
                        () -> hostColumn.onTimeout(host)))
                    .textContent(resources.constants().reload())
                .end()
                .a().rememberAs(RESTART_LINK).css(clickable, alertLink)
                    .on(click, event -> hostActions.restart(host,
                        () -> hostColumn.beforeRestart(host),
                        () -> hostColumn.refreshItem(Host.id(host), host),
                        () -> hostColumn.afterReloadRestart(host),
                        () -> hostColumn.onTimeout(host)))
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
                asList("release-codename", "release-version", "product-name", "product-version",
                        HOST_STATE, RUNNING_MODE))
                .append(model -> {
                    return new String[]{
                            "Management Version",
                            Joiner.on('.').join(
                                    model.get("management-major-version"),
                                    model.get("management-minor-version"),
                                    model.get("management-micro-version"))
                    };
                })
                .end();
        previewBuilder().addAll(attributes);

        update(host);
    }

    @Override
    public void update(final Host host) {
        if (host.isAdminMode()) {
            alertContainer.setClassName(alert + " " + alertInfo);
            alertIcon.setClassName(Icons.DISABLED);
            alertText.setInnerHTML(resources.messages().adminOnly(host.getName()).asString());

        } else if (host.isStarting()) {
            alertContainer.setClassName(alert + " " + alertInfo);
            alertIcon.setClassName(Icons.DISABLED);
            alertText.setInnerHTML(resources.messages().restartHostPending().asString());

        } else if (host.isSuspending() || host.needsReload() || host.needsRestart()) {
            alertContainer.setClassName(alert + " " + alertWarning);
            alertIcon.setClassName(Icons.WARNING);
            if (host.isSuspending()) {
                alertText.setInnerHTML(resources.messages().suspending(HOST, host.getName()).asString());

            } else if (host.needsReload()) {
                alertText.setInnerHTML(resources.messages().needsReload(HOST, host.getName()).asString());

            } else if (host.needsRestart()) {
                alertText.setInnerHTML(resources.messages().needsRestart(HOST, host.getName()).asString());
            }

        } else if (host.isRunning()) {
            alertContainer.setClassName(alert + " " + alertSuccess);
            alertIcon.setClassName(Icons.OK);
            alertText.setInnerHTML(resources.messages().running(HOST, host.getName()).asString());

        } else if (host.isTimeout()) {
            alertContainer.setClassName(alert + " " + alertDanger);
            alertIcon.setClassName(Icons.ERROR);
            alertText.setInnerHTML(resources.messages().timeout(HOST, host.getName()).asString());

        } else {
            alertContainer.setClassName(alert + " " + alertDanger);
            alertIcon.setClassName(Icons.ERROR);
            alertText.setInnerHTML(resources.messages().unknownState(HOST, host.getName()).asString());
        }

        Elements.setVisible(reloadLink, host.needsReload());
        Elements.setVisible(restartLink, host.needsRestart());
        attributes.asElements()
                .forEach(element -> Elements.setVisible(element, !host.isStarting() && !host.isTimeout()));
    }
}
