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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.host.HostColumn.hostTemplate;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;

class HostPreview extends RuntimePreview<Host> {

    private final HTMLElement reloadLink;
    private final HTMLElement restartLink;
    private final PreviewAttributes<Host> attributes;
    private final HostActions hostActions;
    private final Resources resources;
    private final LabelBuilder labelBuilder;

    HostPreview(final HostActions hostActions, final Host host,
            final Resources resources) {
        super(host.getName(), host.isDomainController() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER, resources);
        this.hostActions = hostActions;
        this.resources = resources;
        this.labelBuilder = new LabelBuilder();

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().asElement())
                        .add(alertText = span().asElement())
                        .add(span().textContent(" "))
                        .add(reloadLink = a().css(clickable, alertLink)
                                .on(click, event -> hostActions.reload(host))
                                .data(UIConstants.CONSTRAINT, Constraint.executable(hostTemplate(host), RELOAD).data())
                                .textContent(resources.constants().reload())
                                .asElement())
                        .add(restartLink = a().css(clickable, alertLink)
                                .on(click, event -> hostActions.restart(host))
                                .data(UIConstants.CONSTRAINT, Constraint.executable(hostTemplate(host), RESTART).data())
                                .textContent(resources.constants().restart())
                                .asElement())
                        .asElement());

        attributes = new PreviewAttributes<>(host,
                asList(RELEASE_CODENAME, RELEASE_VERSION, PRODUCT_NAME, PRODUCT_VERSION,
                        HOST_STATE, RUNNING_MODE))
                .append(model -> new PreviewAttribute(
                        labelBuilder.label(MANAGEMENT_VERSION),
                        String.join(".", model.get(MANAGEMENT_MAJOR_VERSION).asString(),
                                model.get(MANAGEMENT_MINOR_VERSION).asString(),
                                model.get(MANAGEMENT_MICRO_VERSION).asString())
                ))
                .append(model -> new PreviewAttribute(labelBuilder.label(LAST_CONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getLastConnected())
                                : Names.NOT_AVAILABLE))
                .append(model -> new PreviewAttribute(labelBuilder.label(DISCONNECTED),
                        model.getLastConnected() != null
                                ? Format.mediumDateTime(model.getDisconnected())
                                : Names.NOT_AVAILABLE));
        previewBuilder().addAll(attributes);

        update(host);
    }

    @Override
    public void update(final Host host) {
        if (!host.isConnected()) {
            disconnected(resources.messages().hostDisconnected(host.getName()));
        } else if (hostActions.isPending(host)) {
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

        // Do not simply hide the links, but add the hidden CSS class.
        // Important when constraints for the links are processed later.
        Elements.toggle(reloadLink, hidden, !host.needsReload());
        Elements.toggle(restartLink, hidden, !host.needsRestart());

        if (host.isStarting()) {
            attributes.asElements().forEach(element -> Elements.setVisible(element, false));
        } else {
            attributes.setVisible(RELEASE_CODENAME, host.isConnected());
            attributes.setVisible(RELEASE_VERSION, host.isConnected());
            attributes.setVisible(PRODUCT_NAME, host.isConnected());
            attributes.setVisible(PRODUCT_VERSION, host.isConnected());
            attributes.setVisible(HOST_STATE, host.isConnected() && hostActions.isPending(host));
            attributes.setVisible(RUNNING_MODE, host.isConnected() && hostActions.isPending(host));
            attributes.setVisible(labelBuilder.label(MANAGEMENT_VERSION), host.isConnected());
            attributes.setVisible(labelBuilder.label(LAST_CONNECTED), !host.isConnected());
            attributes.setVisible(labelBuilder.label(DISCONNECTED), !host.isConnected());
        }
    }
}
