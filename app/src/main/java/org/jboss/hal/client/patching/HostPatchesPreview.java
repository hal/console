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
package org.jboss.hal.client.patching;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.runtime.RuntimePreview;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_SERVICE_PATCHING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MASTER;

/**
 * @author Claudio Miranda
 */
class HostPatchesPreview extends RuntimePreview<NamedNode> {

    private final PreviewAttributes<NamedNode> attributes;
    private final HostActions hostActions;
    private final Resources resources;

    @SuppressWarnings("HardCodedStringLiteral")
    HostPatchesPreview(final HostActions hostActions, final NamedNode host,
            final Resources resources) {
        super(host.getName(), host.get(MASTER).asBoolean() ? Names.DOMAIN_CONTROLLER : Names.HOST_CONTROLLER, resources);
        this.hostActions = hostActions;
        this.resources = resources;

        previewBuilder()
                .add(alertContainer = div()
                        .add(alertIcon = span().asElement())
                        .add(alertText = span().asElement())
                        .asElement());

        attributes = new PreviewAttributes<>(host)
                .append(model -> {
                    String latest = model.get(CORE_SERVICE_PATCHING).get("cumulative-patch-id").asString();
                    // when there is no patch installed, the above attribute returns as "base"
                    if ("base".equals(latest)) {
                        latest = "No patch installed for this host.";
                    }
                    return new PreviewAttributes.PreviewAttribute("Latest applied patch", latest);
                });
        previewBuilder().addAll(attributes);
        update(host);
    }

    @Override
    public void update(final NamedNode item) {
        Host host = new Host(new Property(item.getName(), item.asModelNode()));
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

        attributes.asElements().forEach(element -> Elements.setVisible(element, !host.isStarting()));
    }
}
