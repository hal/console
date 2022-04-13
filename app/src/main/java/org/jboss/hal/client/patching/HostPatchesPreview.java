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
package org.jboss.hal.client.patching;

import org.jboss.elemento.Elements;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.patching.HostPatchesColumn.hostTemplate;
import static org.jboss.hal.client.patching.HostPatchesColumn.namedNodeToHost;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE_PATCHING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MASTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART;
import static org.jboss.hal.resources.CSS.alertLink;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;

class HostPatchesPreview extends RuntimePreview<Host> {

    private final HTMLElement restartLink;
    private final PreviewAttributes<Host> attributes;
    private final HostActions hostActions;
    private final Resources resources;

    @SuppressWarnings("HardCodedStringLiteral")
    HostPatchesPreview(HostActions hostActions, Host host, Resources resources) {
        super(host.getName(), host.get(MASTER).asBoolean() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER,
                resources);
        this.hostActions = hostActions;
        this.resources = resources;

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().element())
                        .add(alertText = span().element())
                        .add(span().textContent(" "))
                        .add(restartLink = a().css(clickable, alertLink)
                                .on(click, event -> hostActions.restart(namedNodeToHost(host)))
                                .data(UIConstants.CONSTRAINT, Constraint.executable(hostTemplate(host), RESTART).data())
                                .textContent(resources.constants().restart()).element())
                        .element());

        attributes = new PreviewAttributes<>(host)
                .append(h -> {
                    String latest = Names.NOT_AVAILABLE;
                    if (h.hasDefined(CORE_SERVICE_PATCHING)) {
                        latest = h.get(CORE_SERVICE_PATCHING).get("cumulative-patch-id").asString();
                        // if there is no patch installed, the above attribute returns as "base"
                        // so, lets display an informative message as there is no patch installed.
                        if ("base".equals(latest)) {
                            latest = resources.messages().noPatchesForHost();
                        }
                    }
                    return new PreviewAttributes.PreviewAttribute(resources.messages().patchLatestInstalledLabel(),
                            latest);
                });
        previewBuilder().addAll(attributes);
        update(host);
    }

    @Override
    public void update(Host host) {
        if (hostActions.isPending(host)) {
            pending(resources.messages().hostPending(host.getName()));
        } else if (host.isAdminMode()) {
            adminOnly(resources.messages().hostAdminMode(host.getName()));
        } else if (host.isStarting()) {
            starting(resources.messages().hostStarting(host.getName()));
        } else if (host.needsReload()) {
            needsReload(resources.messages().hostNeedsReload(host.getName()));
        } else if (host.needsRestart()) {
            needsRestart(resources.messages().patchHostNeedsRestart(host.getName()));
        } else if (host.isRunning()) {
            running(resources.messages().hostRunning(host.getName()));
        } else {
            unknown(resources.messages().hostUndefined(host.getName()));
        }

        // Do not simply hide the links, but add the hidden CSS class.
        // Important when constraints for the links are processed later.
        Elements.toggle(restartLink, hidden, !host.needsRestart());

        attributes.forEach(element -> Elements.setVisible(element, !host.isStarting()));
    }
}
