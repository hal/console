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
import javax.inject.Inject;

import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * TODO set customImplementation flags to true once the subsystems are implemented
 *
 * @author Harald Pehl
 */
public class Subsystems {

    private final Map<String, SubsystemMetadata> subsystems;

    @SuppressWarnings("HardCodedStringLiteral")
    @Inject
    public Subsystems(Resources resources) {
        subsystems = new HashMap<>();

        // @formatter:off
        add(new SubsystemMetadata(BATCH_JBERET, "Batch", "JBeret", NameTokens.BATCH_CONFIGURATION, null, true, resources.previews().configurationBatch()));
        add(new SubsystemMetadata(DATASOURCES, Names.DATASOURCES_DRIVERS, null, null, Ids.DATA_SOURCE_DRIVER, true, resources.previews().configurationDatasourcesDrivers()));
        add(new SubsystemMetadata(DEPLOYMENT_SCANNER, "Deployment Scanners", null, NameTokens.DEPLOYMENT_SCANNERS, null, true, resources.previews().configurationDeploymentScanner()));
        add(new SubsystemMetadata(EE, "EE", null, NameTokens.EE, null, true, resources.previews().configurationEe()));
        add(new SubsystemMetadata(EJB3, "EJB3", null, NameTokens.EJB3, null, true, resources.previews().configurationEjb3()));
        add(new SubsystemMetadata(IIOP_OPENJDK, "IIOP", "OpenJDK", NameTokens.IIOP, null, true));
        add(new SubsystemMetadata(INFINISPAN, "Infinispan", null, null, null, false));
        add(new SubsystemMetadata(IO, "IO", null, NameTokens.IO, null, true, resources.previews().configurationIo()));
        add(new SubsystemMetadata(JCA, "JCA", null, NameTokens.JCA, null, true));
        add(new SubsystemMetadata(JMX, "JMX", null, NameTokens.JMX, null, true));
        add(new SubsystemMetadata(JPA, "JPA", null, NameTokens.JPA_CONFIGURATION, null, true));
        add(new SubsystemMetadata(LOGGING, Names.LOGGING, null, null, Ids.LOGGING, true, resources.previews().configurationLogging()));
        add(new SubsystemMetadata(MAIL, "Mail", null, null, Ids.MAIL_SESSION, true, resources.previews().configurationMail()));
        add(new SubsystemMetadata(MESSAGING_ACTIVEMQ, "Messaging", "ActiveMQ", null, null, true));
        add(new SubsystemMetadata(MODCLUSTER, "Modcluster", null, NameTokens.MODCLUSTER, null, true, resources.previews().configurationModcluster()));
        add(new SubsystemMetadata(REMOTING, "Remoting", null, NameTokens.REMOTING, null, true, resources.previews().configurationRemoting()));
        add(new SubsystemMetadata(REQUEST_CONTROLLER, "Request Controller", null, NameTokens.REQUEST_CONTROLLER, null, false));
        add(new SubsystemMetadata(RESOURCE_ADAPTERS, "Resource Adapters", null, null, Ids.RESOURCE_ADAPTER, true, resources.previews().configurationResourceAdapters()));
        add(new SubsystemMetadata(SECURITY, "Security", null, null, null, false));
        add(new SubsystemMetadata(TRANSACTIONS, "Transactions", null, NameTokens.TRANSACTIONS, null, true));
        add(new SubsystemMetadata(UNDERTOW, "Web", "Undertow", null, null, false));
        add(new SubsystemMetadata(WEBSERVICES, "Web Services", null, NameTokens.WEBSERVICES, null, false));
        // @formatter:on
    }

    private void add(SubsystemMetadata subsystem) {
        subsystems.put(subsystem.getName(), subsystem);
    }

    public boolean contains(String name) {
        return subsystems.containsKey(name);
    }

    public SubsystemMetadata get(String name) {
        return subsystems.get(name);
    }
}
