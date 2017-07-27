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
package org.jboss.hal.client.configuration.subsystem.mail;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.OUTBOUND_SOCKET_BINDING_REF;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

class MailSession extends NamedNode {

    MailSession(Property property) {
        super(property);
    }

    MailSession(final String name, final ModelNode node) {
        super(name, node);
    }

    List<String> getServers() {
        List<String> servers = new ArrayList<>();
        if (hasServer(ModelDescriptionConstants.SMTP)) {
            servers.add(ModelDescriptionConstants.SMTP.toUpperCase());
        }
        if (hasServer(ModelDescriptionConstants.IMAP)) {
            servers.add(ModelDescriptionConstants.IMAP.toUpperCase());
        }
        if (hasServer(ModelDescriptionConstants.POP3)) {
            servers.add(ModelDescriptionConstants.POP3.toUpperCase());
        }
        return servers;
    }

    boolean hasServer(String name) {
        return hasDefined(SERVER) && get(SERVER).hasDefined(name);
    }

    String getServerSocketBinding(String name) {
        ModelNode node = ModelNodeHelper.failSafeGet(this, SERVER + "/" + name + "/" + OUTBOUND_SOCKET_BINDING_REF);
        return node.isDefined() ? node.asString() : Names.NOT_AVAILABLE;
    }
}
