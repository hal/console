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

import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Claudio Miranda
 */
public class MailSession extends NamedNode {
    
    public MailSession(Property property) {
        super(property.getName(), property.getValue());

        //ModelNode m = property.getValue();
        if (hasDefined("server") && get("server").hasDefined("smtp")) 
            get("smtp-socket-binding").set(get("server").get("smtp").get("outbound-socket-binding-ref").asString());

        if (hasDefined("server") && get("server").hasDefined("imap")) 
            get("imap-socket-binding").set(get("server").get("imap").get("outbound-socket-binding-ref").asString());
        
        if (hasDefined("server") && get("server").hasDefined("pop")) 
            get("pop-socket-binding").set(get("server").get("pop").get("outbound-socket-binding-ref").asString());
    }
    
    public String getSmtp() {
        return safeGet("smtp-socket-binding");
    }
    
    public String getJndi() {
        return safeGet("jndi-name");
    }
    
    public String getFrom() {
        return safeGet("from");
    }
    
    public String getImap() {
        return safeGet("imap-socket-binding");
    }

    public String getPop() {
        return safeGet("pop-socket-binding");
    }

    private String safeGet(String name) {
        if (get(name).isDefined())
            return get(name).asString();
        else
            return null;
    }

}
