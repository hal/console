/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.meta.subsystem;

import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import java.util.HashMap;
import java.util.Map;

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
        add(new SubsystemMetadata("datasources", Names.DATASOURCE, null, null, Ids.DATA_SOURCE_TYPE_COLUMN, true));
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
        add(new SubsystemMetadata("mail", "Mail", null, null, Ids.MAIL_SESSION_COLUMN, true));
        add(new SubsystemMetadata("messaging-activemq", "Messaging", "ActiveMQ", null, Ids.MESSAGING_SERVER_COLUMN, true));
        add(new SubsystemMetadata("remoting", "Remoting", null, NameTokens.REMOTING, null, false));
        add(new SubsystemMetadata("request-controller", "Request Controller", null, NameTokens.REQUEST_CONTROLLER, null, false));
        add(new SubsystemMetadata("resource-adapters", "Resource Adapters", null, null, Ids.RESOURCE_ADAPTER_COLUMN, true));
        add(new SubsystemMetadata("security", "Security", null, null, Ids.SECURITY_DOMAIN_COLUMN, true));
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
