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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.Names;

/**
 * @author Claudio Miranda
 */
class MailSession extends NamedNode {

    static final String SMTP = "smtp";
    static final String IMAP = "imap";
    static final String POP3 = "pop3";
    static final String SERVER = "server";
    static final String OUTBOUND_SOCKET_BINDING_REF = "outbound-socket-binding-ref";

    public MailSession(final String name, final ModelNode node) {
        super(name, node);
    }

    MailSession(Property property) {
        super(property.getName(), property.getValue());
    }

    List<String> getServers() {
        List<String> servers = new ArrayList<>();
        if (hasServer(SMTP)) {
            servers.add(SMTP.toUpperCase());
        }
        if (hasServer(IMAP)) {
            servers.add(IMAP.toUpperCase());
        }
        if (hasServer(POP3)) {
            servers.add(POP3.toUpperCase());
        }
        return servers;
    }

    boolean hasServer(String name) {
        return hasDefined(SERVER) && get(SERVER).hasDefined(name);
    }

    String getServerSocketBinding(String name) {
        ModelNode node = ModelNodeHelper.failSafeGet(this, SERVER + "." + name + "." + OUTBOUND_SOCKET_BINDING_REF);
        return node.isDefined() ? node.asString() : Names.NOT_AVAILABLE;
    }
}
