/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.token;

import java.util.Set;

import org.jboss.hal.dmr.ModelDescriptionConstants;

/** HAL name tokens. */
@SuppressWarnings("DuplicateStringLiteralInspection")
public interface NameTokens {

    String CONFIGURATION_SUFFIX = "-configuration";
    String RUNTIME_SUFFIX = "-runtime";

    String ACCESS_CONTROL = "access-control";
    String ACCESS_CONTROL_SSO = "access-control-sso";
    String BATCH_CONFIGURATION = ModelDescriptionConstants.BATCH_JBERET + CONFIGURATION_SUFFIX;
    String BROWSE_CONTENT = "browse-content";
    String CACHE_CONTAINER = ModelDescriptionConstants.CACHE_CONTAINER;
    String CONFIGURATION = "configuration";
    String CONFIGURATION_CHANGES = "configuration-changes";
    String CORE_MANAGEMENT = "core-management";
    String DATA_SOURCE_CONFIGURATION = ModelDescriptionConstants.DATA_SOURCE + CONFIGURATION_SUFFIX;
    String DATA_SOURCE_RUNTIME = ModelDescriptionConstants.DATA_SOURCE + RUNTIME_SUFFIX;
    String DEPLOYMENT_SCANNERS = ModelDescriptionConstants.DEPLOYMENT_SCANNER;
    String DEPLOYMENT = "deployment";
    String DEPLOYMENTS = "deployments";
    String DISTRIBUTED_CACHE = ModelDescriptionConstants.DISTRIBUTED_CACHE;
    String DISTRIBUTABLE_WEB = ModelDescriptionConstants.DISTRIBUTABLE_WEB;
    String EE = ModelDescriptionConstants.EE;
    String ERROR = "error";
    String ELYTRON = ModelDescriptionConstants.ELYTRON;
    String ELYTRON_FACTORIES_TRANSFORMERS = "elytron-factories-transformers";
    String ELYTRON_MAPPERS_DECODERS = "elytron-mappers";
    String ELYTRON_SECURITY_REALMS = "elytron-security-realms";
    String ELYTRON_OTHER = "elytron-other";
    String ELYTRON_RUNTIME_SECURITY_REALMS = "elytron-runtime-security-realms";
    String ELYTRON_RUNTIME_STORES = "elytron-runtime-stores";
    String ELYTRON_RUNTIME_SSL = "elytron-runtime-ssl";
    String EJB3_CONFIGURATION = ModelDescriptionConstants.EJB3 + CONFIGURATION_SUFFIX;
    String EJB3_RUNTIME = ModelDescriptionConstants.EJB3 + RUNTIME_SUFFIX;
    String EXPERT_MODE = "expert-mode";
    String GENERIC_SUBSYSTEM = "generic-subsystem";
    String HOMEPAGE = "home";
    String HOST_CONFIGURATION = "host-configuration";
    String IIOP = ModelDescriptionConstants.IIOP_OPENJDK;
    String INTERFACE = "interface";
    String INVALIDATION_CACHE = ModelDescriptionConstants.INVALIDATION_CACHE;
    String IO = ModelDescriptionConstants.IO;
    String JCA = ModelDescriptionConstants.JCA;
    String JGROUPS = ModelDescriptionConstants.JGROUPS;
    String JMS_BRIDGE = ModelDescriptionConstants.JMS_BRIDGE;
    String JMS_QUEUE = "jms-queue";
    String JMS_TOPIC = "jms-topic";
    String JMX = ModelDescriptionConstants.JMX;
    String JNDI = "jndi";
    String JOB = "job" + RUNTIME_SUFFIX;
    String JPA_CONFIGURATION = ModelDescriptionConstants.JPA + CONFIGURATION_SUFFIX;
    String JPA_RUNTIME = ModelDescriptionConstants.JPA + RUNTIME_SUFFIX;
    String LOCAL_CACHE = ModelDescriptionConstants.LOCAL_CACHE;
    String LOG_FILE = "log-file";
    String LOGGING_CONFIGURATION = "logging-configuration";
    String LOGGING_PROFILE = "logging-profile";
    String MACRO_EDITOR = "macro-editor";
    String MAIL_SESSION = ModelDescriptionConstants.MAIL_SESSION;
    String MANAGEMENT = "management";
    String MANAGEMENT_INTERFACE = "management-interface";
    String MANAGEMENT_OPERATIONS = "management-operations";
    String MESSAGING = "messaging";
    String MESSAGING_REMOTE_ACTIVEMQ = "msg-remote-activemq";
    String MESSAGING_SERVER = "messaging-server";
    String MESSAGING_SERVER_CONNECTION = "messaging-server-connection";
    String MESSAGING_SERVER_CLUSTERING = "messaging-server-clustering";
    String MESSAGING_SERVER_DESTINATION = "messaging-server-destination";
    String MESSAGING_SERVER_RUNTIME = "messaging-server-runtime";
    String MESSAGING_SERVER_HA_POLICY = "messaging-server-ha-policy";
    String MICRO_PROFILE_CONFIG = "micro-profile-config";
    String MICRO_PROFILE_HEALTH = "micro-profile-health";
    String MICRO_PROFILE_METRICS = "micro-profile-metrics";
    String MODCLUSTER = ModelDescriptionConstants.MODCLUSTER;
    String MODEL_BROWSER = "model-browser";
    String PATCHING = "patching";
    String PATH = "path";
    String QUEUE = "queue";
    String REMOTING = ModelDescriptionConstants.REMOTING;
    String REMOTE_CACHE_CONTAINER = ModelDescriptionConstants.REMOTE_CACHE_CONTAINER;
    String REPLICATED_CACHE = ModelDescriptionConstants.REPLICATED_CACHE;
    String REQUEST_CONTROLLER = ModelDescriptionConstants.REQUEST_CONTROLLER;
    String RESOURCE_ADAPTER = ModelDescriptionConstants.RESOURCE_ADAPTER;
    String RUNTIME = "runtime";
    String SCATTERED_CACHE = ModelDescriptionConstants.SCATTERED_CACHE;
    String SECURITY_CONFIGURATION = "security";
    String SECURITY_MANAGER = "security-manager";
    String SECURITY_DOMAIN = ModelDescriptionConstants.SECURITY_DOMAIN;
    String SERVER_BOOT_ERRORS = "server-boot-errors";
    String SERVER_CONFIGURATION = "server-configuration";
    String SERVER_GROUP_CONFIGURATION = "server-group-configuration";
    String SERVER_GROUP_DEPLOYMENT = "server-group-deployment";
    String SERVER_RUNTIME = "server-runtime";
    String STANDALONE_SERVER = "standalone-server";
    String SOCKET_BINDING_GROUP = ModelDescriptionConstants.SOCKET_BINDING;
    String SYSTEM_PROPERTIES = "system-properties";
    String TRANSACTIONS = ModelDescriptionConstants.TRANSACTIONS;
    String TRANSACTIONS_RUNTIME = "transactions-runtime";
    String UNAUTHORIZED = "unauthorized";
    String UNDERTOW = "undertow";
    String UNDERTOW_APPLICATION_SECURITY_DOMAIN = "undertow-application-security-domain";
    String UNDERTOW_BUFFER_CACHE = "undertow-buffer-cache";
    String UNDERTOW_BYTE_BUFFER_POOL = "undertow-byte-buffer-pool";
    String UNDERTOW_FILTER = "undertow-filter";
    String UNDERTOW_HANDLER = "undertow-handler";
    String UNDERTOW_RUNTIME_DEPLOYMENT_VIEW = "undertow-runtime-deployment";
    String UNDERTOW_SERVER = "undertow-server";
    String UNDERTOW_SERVLET_CONTAINER = "undertow-servlet-container";
    String WEBSERVICES = ModelDescriptionConstants.WEBSERVICES;
    String WEBSERVICES_RUNTIME = "webservices-runtime";

    Set<String> getTokens();
}
