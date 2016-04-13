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
package org.jboss.hal.meta.subsystem;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

/**
 * @author Harald Pehl
 */
public class Subsystems {

    private final Map<String, SubsystemMetadata> subsystems;

    @SuppressWarnings("HardCodedStringLiteral")
    public Subsystems() {
        subsystems = new HashMap<>();

        // built-in subsystems
        // TODO set builtin flag to true once the subsystem implementations are in place
        add(new SubsystemMetadata("batch-jberet", "Batch", "JBeret", NameTokens.BATCH, null, false));
        add(new SubsystemMetadata("datasources", Names.DATASOURCE, null, null, ModelDescriptionConstants.DATA_SOURCE, true));
        add(new SubsystemMetadata("deployment-scanner", "Deployment Scanners", null, NameTokens.DEPLOYMENT_SCANNERS, null, false));
        add(new SubsystemMetadata("ee", "EE", null, NameTokens.EE, null, false));
        add(new SubsystemMetadata("ejb3", "EJB3", null, NameTokens.EJB3, null, false));
        add(new SubsystemMetadata("iiop-openjdk", "IIOP", "OpenJDK", NameTokens.IIOP, null, false));
        add(new SubsystemMetadata("infinispan", "Infinispan", null, null, Ids.CACHE_CONTAINER_COLUMN, true));
        add(new SubsystemMetadata("io", "IO", null, NameTokens.IO, null, false));
        add(new SubsystemMetadata("jca", "JCA", null, NameTokens.JCA, null, false));
        add(new SubsystemMetadata("jmx", "JMX", null, NameTokens.JMX, null, false));
        add(new SubsystemMetadata("jpa", "JPA", null, NameTokens.JPA, null, false));
        add(new SubsystemMetadata("logging", "Logging", null, NameTokens.LOGGING, null, false));
        add(new SubsystemMetadata("mail", "Mail", null, null, ModelDescriptionConstants.MAIL_SESSION, true));
        add(new SubsystemMetadata("messaging-activemq", "Messaging", "ActiveMQ", null, ModelDescriptionConstants.MESSAGING_SERVER, true));
        add(new SubsystemMetadata("remoting", "Remoting", null, NameTokens.REMOTING, null, false));
        add(new SubsystemMetadata("request-controller", "Request Controller", null, NameTokens.REQUEST_CONTROLLER, null, false));
        add(new SubsystemMetadata("resource-adapters", "Resource Adapters", null, null, ModelDescriptionConstants.RESOURCE_ADAPTER, true));
        add(new SubsystemMetadata("security", "Security", null, null, ModelDescriptionConstants.SECURITY_DOMAIN, true));
        add(new SubsystemMetadata("transactions", "Transactions", null, NameTokens.TRANSACTIONS, null, false));
        add(new SubsystemMetadata("undertow", "Web", "Undertow", null, Ids.WEB_SETTINGS_COLUMN, true));
        add(new SubsystemMetadata("webservices", "Web Services", null, NameTokens.WEBSERVICES, null, false));
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
