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

    private final Map<String, SubsystemMetadata> configurationSubsystems;
    private final Map<String, SubsystemMetadata> runtimeSubsystems;

    @SuppressWarnings("HardCodedStringLiteral")
    @Inject
    public Subsystems(Resources resources) {
        configurationSubsystems = new HashMap<>();
        runtimeSubsystems = new HashMap<>();

        // @formatter:off ------------------------------------------------------ configuration
        addConfiguration(new SubsystemMetadata(BATCH_JBERET, "Batch", "JBeret", NameTokens.BATCH_CONFIGURATION, null, false));
        addConfiguration(new SubsystemMetadata(DATASOURCES, Names.DATASOURCES_DRIVERS, null, null, Ids.DATA_SOURCE_DRIVER, true, resources.previews().configurationDatasourcesDrivers()));
        addConfiguration(new SubsystemMetadata(DEPLOYMENT_SCANNER, "Deployment Scanners", null, NameTokens.DEPLOYMENT_SCANNERS, null, true, resources.previews().configurationDeploymentScanner()));
        addConfiguration(new SubsystemMetadata(EE, "EE", null, NameTokens.EE, null, true, resources.previews().configurationEe()));
        addConfiguration(new SubsystemMetadata(EJB3, "EJB3", null, NameTokens.EJB3, null, false));
        addConfiguration(new SubsystemMetadata(IIOP_OPENJDK, "IIOP", "OpenJDK", NameTokens.IIOP, null, true));
        addConfiguration(new SubsystemMetadata(INFINISPAN, "Infinispan", null, null, null, true));
        addConfiguration(new SubsystemMetadata(IO, "IO", null, NameTokens.IO, null, true, resources.previews().configurationIo()));
        addConfiguration(new SubsystemMetadata(JCA, "JCA", null, NameTokens.JCA, null, false));
        addConfiguration(new SubsystemMetadata(JMX, "JMX", null, NameTokens.JMX, null, false));
        addConfiguration(new SubsystemMetadata(JPA, "JPA", null, NameTokens.JPA_CONFIGURATION, null, false));
        addConfiguration(new SubsystemMetadata(LOGGING, Names.LOGGING, null, null, Ids.LOGGING, true, resources.previews().configurationLogging()));
        addConfiguration(new SubsystemMetadata(MAIL, "Mail", null, null, Ids.MAIL_SESSION, true, resources.previews().configurationMail()));
        addConfiguration(new SubsystemMetadata(MESSAGING_ACTIVEMQ, "Messaging", "ActiveMQ", null, null, true));
        addConfiguration(new SubsystemMetadata(REMOTING, "Remoting", null, NameTokens.REMOTING, null, false));
        addConfiguration(new SubsystemMetadata(REQUEST_CONTROLLER, "Request Controller", null, NameTokens.REQUEST_CONTROLLER, null, false));
        addConfiguration(new SubsystemMetadata(RESOURCE_ADAPTERS, "Resource Adapters", null, null, null, true));
        addConfiguration(new SubsystemMetadata(SECURITY, "Security", null, null, null, true));
        addConfiguration(new SubsystemMetadata(TRANSACTIONS, "Transactions", null, NameTokens.TRANSACTIONS, null, true));
        addConfiguration(new SubsystemMetadata(UNDERTOW, "Web", "Undertow", null, Ids.WEB_SETTINGS, true));
        addConfiguration(new SubsystemMetadata(WEBSERVICES, "Web Services", null, NameTokens.WEBSERVICES, null, false));

        // ------------------------------------------------------ runtime
        addRuntime(new SubsystemMetadata(BATCH_JBERET, "Batch", "JBeret", NameTokens.BATCH_RUNTIME, null, false));
        // @formatter:on
    }

    private void addConfiguration(SubsystemMetadata subsystem) {
        configurationSubsystems.put(subsystem.getName(), subsystem);
    }

    private void addRuntime(SubsystemMetadata subsystem) {
        runtimeSubsystems.put(subsystem.getName(), subsystem);
    }

    public Map<String, SubsystemMetadata> getConfigurationSubsystems() {
        return configurationSubsystems;
    }

    public Map<String, SubsystemMetadata> getRuntimeSubsystems() {
        return runtimeSubsystems;
    }
}
