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
package org.jboss.hal.core.subsystem;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class Subsystems {

    private final Map<String, SubsystemMetadata> subsystems;

    @SuppressWarnings("HardCodedStringLiteral")
    public Subsystems() {
        subsystems = new HashMap<>();

        // TODO set builtin flag to true once the subsystem implementations are in place
        add(new SubsystemMetadata(BATCH_JBERET, "Batch", "JBeret", NameTokens.BATCH, null, false));
        add(new SubsystemMetadata(DATASOURCES, Names.DATASOURCES_DRIVERS, null, null,
                Ids.DATA_SOURCE_DRIVER_COLUMN, true));
        add(new SubsystemMetadata(DEPLOYMENT_SCANNER, "Deployment Scanners", null, NameTokens.DEPLOYMENT_SCANNERS,
                null, true));
        add(new SubsystemMetadata(EE, "EE", null, NameTokens.EE, null, true));
        add(new SubsystemMetadata(EJB3, "EJB3", null, NameTokens.EJB3, null, false));
        add(new SubsystemMetadata(IIOP_OPENJDK, "IIOP", "OpenJDK", NameTokens.IIOP, null, true));
        add(new SubsystemMetadata(INFINISPAN, "Infinispan", null, null, Ids.CACHE_CONTAINER_COLUMN, true));
        add(new SubsystemMetadata(IO, "IO", null, NameTokens.IO, null, true));
        add(new SubsystemMetadata(JCA, "JCA", null, NameTokens.JCA, null, false));
        add(new SubsystemMetadata(JMX, "JMX", null, NameTokens.JMX, null, false));
        add(new SubsystemMetadata(JPA, "JPA", null, NameTokens.JPA, null, false));
        add(new SubsystemMetadata(LOGGING, Names.LOGGING, null, null, ModelDescriptionConstants.LOGGING, true));
        add(new SubsystemMetadata(MAIL, "Mail", null, null, ModelDescriptionConstants.MAIL_SESSION, true));
        add(new SubsystemMetadata(MESSAGING_ACTIVEMQ, "Messaging", "ActiveMQ", null,
                ModelDescriptionConstants.MESSAGING_SERVER, true));
        add(new SubsystemMetadata(REMOTING, "Remoting", null, NameTokens.REMOTING, null, false));
        add(new SubsystemMetadata(REQUEST_CONTROLLER, "Request Controller", null, NameTokens.REQUEST_CONTROLLER, null,
                false));
        add(new SubsystemMetadata(RESOURCE_ADAPTERS, "Resource Adapters", null, null,
                RESOURCE_ADAPTER, true));
        add(new SubsystemMetadata(SECURITY, "Security", null, null, ModelDescriptionConstants.SECURITY_DOMAIN, true));
        add(new SubsystemMetadata(TRANSACTIONS, "Transactions", null, NameTokens.TRANSACTIONS, null, false));
        add(new SubsystemMetadata(UNDERTOW, "Web", "Undertow", null, Ids.WEB_SETTINGS_COLUMN, true));
        add(new SubsystemMetadata(WEBSERVICES, "Web Services", null, NameTokens.WEBSERVICES, null, false));
    }

    private void add(SubsystemMetadata subsystem) {
        subsystems.put(subsystem.getName(), subsystem);
    }

    public boolean isBuiltIn(String name) {
        return subsystems.containsKey(name);
    }

    public SubsystemMetadata getSubsystem(String name) {
        return subsystems.get(name);
    }
}
