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

/** Subsystem registry. Lives in core so that extensions can use this class to register their subsystems. */
public class Subsystems {

    private final Map<String, SubsystemMetadata> configuration;
    private final Map<String, SubsystemMetadata> runtime;

    @Inject
    @SuppressWarnings("HardCodedStringLiteral")
    public Subsystems(Resources resources) {
        configuration = new HashMap<>();
        runtime = new HashMap<>();

        // ------------------------------------------------------ configuration

        addConfiguration(new SubsystemMetadata.Builder(BATCH_JBERET, Names.BATCH)
                .subtitle(Names.JBERET)
                .token(NameTokens.BATCH_CONFIGURATION)
                .preview(resources.previews().configurationBatch())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(BEAN_VALIDATION, "Bean Validation")
                .generic()
                .preview(resources.previews().configurationBeanValidation())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(CORE_MANAGEMENT, Names.CORE_MANAGEMENT)
                .token(NameTokens.CORE_MANAGEMENT)
                .preview(resources.previews().coreManagement())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(DATASOURCES, Names.DATASOURCES_DRIVERS)
                .nextColumn(Ids.DATA_SOURCE_DRIVER)
                .preview(resources.previews().configurationDatasourcesDrivers())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(DEPLOYMENT_SCANNER, "Deployment Scanners")
                .token(NameTokens.DEPLOYMENT_SCANNERS)
                .preview(resources.previews().configurationDeploymentScanner())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(EE, "EE")
                .token(NameTokens.EE)
                .preview(resources.previews().configurationEe())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(EJB3, Names.EJB3)
                .token(NameTokens.EJB3_CONFIGURATION)
                .preview(resources.previews().configurationEjb3())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(ELYTRON, Names.SECURITY)
                .subtitle(Names.ELYTRON)
                .nextColumn(Ids.ELYTRON)
                .preview(resources.previews().configurationSecurityElytron())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(IIOP_OPENJDK, "IIOP")
                .subtitle("OpenJDK")
                .token(NameTokens.IIOP)
                .preview(resources.previews().configurationIiop())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(INFINISPAN, "Infinispan")
                .nextColumn(Ids.CACHE_CONTAINER)
                .preview(resources.previews().configurationInfinispan())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(IO, Names.IO)
                .token(NameTokens.IO)
                .preview(resources.previews().configurationIo())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JAX_RS, "JAX-RS")
                .generic()
                .preview(resources.previews().configurationJaxRs())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JCA, "JCA")
                .token(NameTokens.JCA)
                .preview(resources.previews().configurationJca())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JDR, "JDR")
                .generic()
                .preview(resources.previews().configurationJdr())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JGROUPS, Names.JGROUPS)
                .token(NameTokens.JGROUPS)
                .preview(resources.previews().configurationJgroups())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JMX, "JMX")
                .token(NameTokens.JMX)
                .preview(resources.previews().configurationJmx())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JPA, Names.JPA)
                .token(NameTokens.JPA_CONFIGURATION)
                .preview(resources.previews().configurationJpa())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JSF, "JSF")
                .generic()
                .preview(resources.previews().configurationJsf())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(JSR77, "JSR77")
                .generic()
                .preview(resources.previews().configurationJsr77())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(LOGGING, Names.LOGGING)
                .nextColumn(Ids.LOGGING_CONFIG_AND_PROFILES)
                .preview(resources.previews().configurationLogging())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MAIL, "Mail")
                .nextColumn(Ids.MAIL_SESSION)
                .preview(resources.previews().configurationMail())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MESSAGING_ACTIVEMQ, Names.MESSAGING)
                .subtitle(Names.ACTIVE_MQ)
                .nextColumn(Ids.MESSAGING_CATEGORY)
                .preview(resources.previews().configurationMessaging())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MICROPROFILE_CONFIG_SMALLRYE, Names.MICROPROFILE_CONFIG)
                .subtitle(Names.SMALLRYE)
                .token(NameTokens.MICRO_PROFILE_CONFIG)
                .preview(resources.previews().configurationMicroProfileConfig())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MICROPROFILE_HEALTH_SMALLRYE, Names.MICROPROFILE_HEALTH)
                .subtitle(Names.SMALLRYE)
                .generic()
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MICROPROFILE_METRICS_SMALLRYE, Names.MICROPROFILE_METRICS)
                .subtitle(Names.SMALLRYE)
                .token(NameTokens.MICRO_PROFILE_METRICS)
                .preview(resources.previews().configurationMicroProfileMetrics())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(MODCLUSTER, Names.MODCLUSTER)
                .nextColumn(Ids.MODCLUSTER_PROXY)
                .preview(resources.previews().configurationModcluster())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(NAMING, "Naming")
                .subtitle(Names.JNDI)
                .generic()
                .preview(resources.previews().configurationNaming())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(POJO, "Pojo")
                .generic()
                .preview(resources.previews().configurationPojo())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(REMOTING, "Remoting")
                .token(NameTokens.REMOTING)
                .preview(resources.previews().configurationRemoting())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(REQUEST_CONTROLLER, "Request Controller")
                .token(NameTokens.REQUEST_CONTROLLER)
                .preview(resources.previews().configurationRequestController())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(RESOURCE_ADAPTERS, "Resource Adapters")
                .nextColumn(Ids.RESOURCE_ADAPTER)
                .preview(resources.previews().configurationResourceAdapters())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(SAR, "SAR")
                .generic()
                .preview(resources.previews().configurationSar())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(SECURITY, Names.SECURITY)
                .subtitle("Legacy")
                .nextColumn(Ids.SECURITY_DOMAIN)
                .token(NameTokens.SECURITY_CONFIGURATION)
                .preview(resources.previews().configurationSecurityDomains())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(SECURITY_MANAGER, "Security Manager")
                .token(NameTokens.SECURITY_MANAGER)
                .preview(resources.previews().configurationSecurityManager())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(SINGLETON, "Singleton")
                .generic()
                .preview(resources.previews().configurationSingleton())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(TRANSACTIONS, Names.TRANSACTION)
                .token(NameTokens.TRANSACTIONS)
                .preview(resources.previews().configurationTransactions())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(UNDERTOW, Names.WEB)
                .subtitle(Names.UNDERTOW)
                .nextColumn(Ids.UNDERTOW_SETTINGS)
                .preview(resources.previews().configurationUndertow())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(WEBSERVICES, "Web Services")
                .token(NameTokens.WEBSERVICES)
                .preview(resources.previews().configurationWebservices())
                .build());
        addConfiguration(new SubsystemMetadata.Builder(WELD, "Weld")
                .generic()
                .preview(resources.previews().configurationWeld())
                .build());

        // ------------------------------------------------------ runtime

        addRuntime(new SubsystemMetadata.Builder(BATCH_JBERET, Names.BATCH)
                .subtitle(Names.JBERET)
                .nextColumn(Ids.JOB)
                .build());
        addRuntime(new SubsystemMetadata.Builder(DATASOURCES, Names.DATASOURCES)
                .nextColumn(Ids.DATA_SOURCE_RUNTIME)
                .preview(resources.previews().runtimeDatasources())
                .build());
        addRuntime(new SubsystemMetadata.Builder(EJB3, Names.EJB3)
                .nextColumn(Ids.EJB3)
                .build());
        addRuntime(new SubsystemMetadata.Builder(SECURITY, Names.SECURITY)
                .subtitle(Names.ELYTRON)
                .nextColumn(Ids.ELYTRON_RUNTIME)
                .preview(resources.previews().runtimeSecurityElytron())
                .build());
        addRuntime(new SubsystemMetadata.Builder(IO, Names.IO)
                .nextColumn(Ids.WORKER)
                .preview(resources.previews().runtimeWorker())
                .build());
        addRuntime(new SubsystemMetadata.Builder(JAX_RS, Names.JAX_RS)
                .nextColumn(Ids.REST_RESOURCE)
                .preview(resources.previews().runtimeJaxRs())
                .build());
        addRuntime(new SubsystemMetadata.Builder(LOGGING, resources.constants().logFiles())
                .nextColumn(Ids.LOG_FILE)
                .preview(resources.previews().runtimeLogFiles())
                .build());
        addRuntime(new SubsystemMetadata.Builder(NAMING, Names.JNDI)
                .token(NameTokens.JNDI)
                .preview(resources.previews().runtimeJndi())
                .build());
        addRuntime(new SubsystemMetadata.Builder(JPA, Names.JPA)
                .nextColumn(Ids.JPA_RUNTIME)
                .preview(resources.previews().runtimeJpa())
                .build());
        addRuntime(new SubsystemMetadata.Builder(MESSAGING_ACTIVEMQ, Names.MESSAGING)
                .subtitle(Names.ACTIVE_MQ)
                .nextColumn(Ids.MESSAGING_SERVER_RUNTIME)
                .preview(resources.previews().runtimeMessagingServer())
                .build());
        addRuntime(new SubsystemMetadata.Builder(MICROPROFILE_HEALTH_SMALLRYE, Names.MICROPROFILE_HEALTH)
                .subtitle(Names.SMALLRYE)
                .token(NameTokens.MICRO_PROFILE_HEALTH)
                .build());
        addRuntime(new SubsystemMetadata.Builder(TRANSACTIONS, Names.TRANSACTION)
                .token(NameTokens.TRANSACTIONS_RUNTIME)
                .build());
        addRuntime(new SubsystemMetadata.Builder(UNDERTOW, Names.WEB)
                .subtitle(Names.UNDERTOW)
                .nextColumn(Ids.UNDERTOW_RUNTIME)
                .build());
        addRuntime(new SubsystemMetadata.Builder(WEBSERVICES, Names.WEBSERVICES)
                .nextColumn(Ids.ENDPOINT)
                .build());
    }

    private void addConfiguration(SubsystemMetadata subsystem) {
        configuration.put(subsystem.getName(), subsystem);
    }

    public boolean containsConfiguration(String name) {
        return configuration.containsKey(name);
    }

    public SubsystemMetadata getConfiguration(String name) {
        return configuration.get(name);
    }

    private void addRuntime(SubsystemMetadata subsystem) {
        runtime.put(subsystem.getName(), subsystem);
    }

    public boolean containsRuntime(String name) {
        return runtime.containsKey(name);
    }

    public SubsystemMetadata getRuntime(String name) {
        return runtime.get(name);
    }

}
