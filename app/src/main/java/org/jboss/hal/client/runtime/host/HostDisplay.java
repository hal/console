/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.host;

import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

public abstract class HostDisplay implements ItemDisplay<Host> {

    private final Host item;
    private final HostActions hostActions;
    private final Resources resources;

    public HostDisplay(Host item, HostActions hostActions, Resources resources) {
        this.item = item;
        this.hostActions = hostActions;
        this.resources = resources;
    }

    @Override
    public String getId() {
        return Ids.host(item.getAddressName());
    }

    @Override
    public String getTitle() {
        return item.getName();
    }

    @Override
    public HTMLElement element() {
        return item.isDomainController() ? ItemDisplay
                .withSubtitle(item.getName(), Names.DOMAIN_CONTROLLER) : null;
    }

    @Override
    public String getFilterData() {
        return String.join(" ", item.getName(),
                item.isDomainController() ? "dc" : "hc", // NON-NLS
                item.isConnected() ? "on" : "off", // NON-NLS
                ModelNodeHelper.asAttributeValue(item.getHostState()));
    }

    @Override
    public String getTooltip() {
        if (!item.isConnected()) {
            return resources.constants().disconnectedUpper();
        } else if (hostActions.isPending(item)) {
            return resources.constants().pending();
        } else if (item.isAdminMode()) {
            return resources.constants().adminOnly();
        } else if (item.isBooting() || item.isStarting()) {
            return resources.constants().starting();
        } else if (item.needsReload()) {
            return resources.constants().needsReload();
        } else if (item.needsRestart()) {
            return resources.constants().needsRestart();
        } else if (item.isRunning()) {
            return resources.constants().running();
        } else {
            return resources.constants().unknownState();
        }
    }

    @Override
    public HTMLElement getIcon() {
        if (!item.isConnected()) {
            return Icons.disconnected();
        } else if (hostActions.isPending(item)) {
            return Icons.pending();
        } else if (item.isAdminMode()) {
            return Icons.disabled();
        } else if (item.isBooting() || item.isStarting()) {
            return Icons.pending();
        } else if (item.needsReload() || item.needsRestart()) {
            return Icons.warning();
        } else if (item.isRunning()) {
            return Icons.ok();
        } else {
            return Icons.unknown();
        }
    }
}
