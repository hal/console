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

    String ACCESS_CONTROL = "access-control";
    String BATCH = ModelDescriptionConstants.BATCH_JBERET;
    String CONFIGURATION = "configuration";
    String DATA_SOURCE = ModelDescriptionConstants.DATA_SOURCE;
    String DEPLOYMENT_SCANNERS = ModelDescriptionConstants.DEPLOYMENT_SCANNER;
    String DEPLOYMENTS = "deployments";
    String EE = ModelDescriptionConstants.EE;
    String EJB3 = ModelDescriptionConstants.EJB3;
    String GENERIC_SUBSYSTEM = "generic-subsystem";
    String HOMEPAGE = "home";
    String IIOP = ModelDescriptionConstants.IIOP_OPENJDK;
    String INTERFACE = "interface";
    String IO = ModelDescriptionConstants.IO;
    String JCA = ModelDescriptionConstants.JCA;
    String JMX = ModelDescriptionConstants.JMX;
    String JPA = ModelDescriptionConstants.JPA;
    String LOGGING = ModelDescriptionConstants.LOGGING;
    String MACRO_EDITOR = "macro-editor";
    String MAIL_SESSION = ModelDescriptionConstants.MAIL_SESSION;
    String MODEL_BROWSER = "model-browser";
    String PATCHING = "patching";
    String PATH = "path";
    String REMOTING = ModelDescriptionConstants.REMOTING;
    String REQUEST_CONTROLLER = ModelDescriptionConstants.REQUEST_CONTROLLER;
    String RUNTIME = "runtime";
    String SOCKET_BINDING = ModelDescriptionConstants.SOCKET_BINDING;
    String SYSTEM_PROPERTIES = "system-properties";
    String TRANSACTIONS = ModelDescriptionConstants.TRANSACTIONS;
    String UNDER_THE_BRIDGE = "utb";
    String WEBSERVICES = ModelDescriptionConstants.WEBSERVICES;

    Set<String> getTokens();
}
