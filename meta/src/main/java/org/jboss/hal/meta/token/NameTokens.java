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
package org.jboss.hal.meta.token;

import java.util.Set;

import org.jboss.hal.dmr.ModelDescriptionConstants;

@SuppressWarnings("DuplicateStringLiteralInspection")
public interface NameTokens {

    String CONFIGURATION_SUFFIX = "-configuration";
    String RUNTIME_SUFFIX = "-runtime";

    String ACCESS_CONTROL = "access-control";
    String BATCH_CONFIGURATION = ModelDescriptionConstants.BATCH_JBERET + CONFIGURATION_SUFFIX;
    String BROWSE_CONTENT = "browse-content";
    String CONFIGURATION = "configuration";
    String DATA_SOURCE_CONFIGURATION = ModelDescriptionConstants.DATA_SOURCE + CONFIGURATION_SUFFIX;
    String DATA_SOURCE_RUNTIME = ModelDescriptionConstants.DATA_SOURCE + RUNTIME_SUFFIX;
    String DEPLOYMENT_SCANNERS = ModelDescriptionConstants.DEPLOYMENT_SCANNER;
    String DEPLOYMENT = "deployment";
    String DEPLOYMENTS = "deployments";
    String EE = ModelDescriptionConstants.EE;
    String EJB3 = ModelDescriptionConstants.EJB3;
    String EXPERT_MODE = "expert-mode";
    String GENERIC_SUBSYSTEM = "generic-subsystem";
    String HOMEPAGE = "home";
    String HOST_CONFIGURATION = "host-configuration";
    String IIOP = ModelDescriptionConstants.IIOP_OPENJDK;
    String INTERFACE = "interface";
    String IO = ModelDescriptionConstants.IO;
    String JCA = ModelDescriptionConstants.JCA;
    String JMS_BRIDGE = ModelDescriptionConstants.JMS_BRIDGE;
    String JMX = ModelDescriptionConstants.JMX;
    String JNDI = "jndi";
    String JPA_CONFIGURATION = ModelDescriptionConstants.JPA + CONFIGURATION_SUFFIX;
    String JPA_RUNTIME = ModelDescriptionConstants.JPA + RUNTIME_SUFFIX;
    String LOG_FILE = "log-file";
    String LOGGING_CONFIGURATION = "logging-configuration";
    String LOGGING_PROFILE = "logging-profile";
    String MACRO_EDITOR = "macro-editor";
    String MAIL_SESSION = ModelDescriptionConstants.MAIL_SESSION;
    String MESSAGING = "messaging";
    String MESSAGING_SERVER = "messaging-server";
    String MESSAGING_SERVER_CONNECTION = "messaging-server-connection";
    String MESSAGING_SERVER_CLUSTERING = "messaging-server-clustering";
    String MESSAGING_SERVER_DESTINATION = "messaging-server-destination";
    String MESSAGING_SERVER_HA_POLICY = "messaging-server-ha-policy";
    String MODCLUSTER = ModelDescriptionConstants.MODCLUSTER;
    String MODEL_BROWSER = "model-browser";
    String PATCHING = "patching";
    String PATH = "path";
    String REMOTING = ModelDescriptionConstants.REMOTING;
    String REQUEST_CONTROLLER = ModelDescriptionConstants.REQUEST_CONTROLLER;
    String RESOURCE_ADAPTER = ModelDescriptionConstants.RESOURCE_ADAPTER;
    String RUNTIME = "runtime";
    String SECURITY_CONFIGURATION = "security";
    String SECURITY_DOMAIN = ModelDescriptionConstants.SECURITY_DOMAIN;
    String SERVER_CONFIGURATION = "server-configuration";
    String SERVER_GROUP_CONFIGURATION = "server-group-configuration";
    String SERVER_GROUP_DEPLOYMENT = "server-group-deployment";
    String SERVER_STATUS = "server-status";
    String SOCKET_BINDING = ModelDescriptionConstants.SOCKET_BINDING;
    String SYSTEM_PROPERTIES = "system-properties";
    String TRANSACTIONS = ModelDescriptionConstants.TRANSACTIONS;
    String UNDERTOW = "undertow";
    String UNDERTOW_BUFFER_CACHE = "undertow-buffer-cache";
    String UNDERTOW_FILTER = "undertow-filter";
    String UNDERTOW_HANDLER = "undertow-handler";
    String UNDERTOW_SERVER = "undertow-server";
    String UNDERTOW_SERVLET_CONTAINER = "undertow-servlet-container";
    String WEBSERVICES = ModelDescriptionConstants.WEBSERVICES;

    Set<String> getTokens();
}
