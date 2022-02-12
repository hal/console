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
package org.jboss.hal.core.runtime.host;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Version;
import org.jboss.hal.core.finder.PreviewAttributes;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public final class HostPreviewAttributes {

    public static void refresh(Host host, PreviewAttributes<Host> attributes, HostActions hostActions) {
        attributes.refresh(host);
        LabelBuilder labelBuilder = new LabelBuilder();
        boolean alive = host.isAlive();
        attributes.setVisible(RELEASE_CODENAME, alive);
        attributes.setVisible(RELEASE_VERSION, alive);
        attributes.setVisible(PRODUCT_NAME, alive);
        attributes.setVisible(PRODUCT_VERSION, alive);
        attributes.setVisible(HOST_STATE, host.isConnected() && !hostActions.isPending(host));
        attributes.setVisible(RUNNING_MODE, alive && !hostActions.isPending(host));
        attributes.setVisible(labelBuilder.label(MANAGEMENT_VERSION),
                alive && !Version.EMPTY_VERSION.equals(host.getManagementVersion()));
        attributes.setVisible(labelBuilder.label(LAST_CONNECTED), !host.isConnected());
        attributes.setVisible(labelBuilder.label(DISCONNECTED), !host.isConnected());
    }

    private HostPreviewAttributes() {
    }
}
