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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.NamedNode;

class ProxyPreview extends PreviewContent<NamedNode> {

    ProxyPreview(final NamedNode proxyNode) {
        super(proxyNode.getName());

        PreviewAttributes<NamedNode> attributes = new PreviewAttributes<>(proxyNode);
        attributes.append("advertise");
        attributes.append("advertise-socket");
        attributes.append("balancer");
        attributes.append("connector");
        attributes.append("node-timeout");
        attributes.append("proxies");
        attributes.append("proxy-list");
        attributes.append("sticky-session");
        attributes.append("worker-timeout");
        previewBuilder().addAll(attributes);
    }
}
