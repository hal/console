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

@SuppressWarnings("DuplicateStringLiteralInspection")
public interface NameTokens {

    String ACCESS_CONTROL = "/access-control-finder";
    String BATCH = "/configuration/{profile}/batch-jberet";
    String CONFIGURATION = "/configuration-finder";
    String DATA_SOURCE = "/configuration/{profile}/data-source/{name}";
    String DEPLOYMENT_SCANNERS = "/configuration/{profile}/deployment-scanners";
    String DEPLOYMENTS = "/deployments-finder";
    String EE = "/configuration/{profile}/ee";
    String EJB3 = "/configuration/{profile}/ejb3";
    String GENERIC_SUBSYSTEM = "/configuration/generic";
    String HOMEPAGE = "/home";
    String IIOP = "/configuration/{profile}/iiop";
    String INTERFACE = "/interface/{name}";
    String IO = "/configuration/{profile}/io";
    String JCA = "/configuration/{profile}/jca";
    String JMX = "/configuration/{profile}/jmx";
    String JPA = "/configuration/{profile}/jpa";
    String LOGGING = "/configuration/{profile}/logging";
    String MACRO_EDITOR = "/macro-editor";
    String MODEL_BROWSER = "/model-browser";
    String PATCHING = "/patching-finder";
    String PATH = "/path";
    String REMOTING = "/configuration/{profile}/remoting";
    String REQUEST_CONTROLLER = "/configuration/{profile}/request-controller";
    String RUNTIME = "/runtime-finder";
    String SOCKET_BINDING = "/socket-binding/{name}";
    String SYSTEM_PROPERTIES = "/system-properties";
    String TRANSACTIONS = "/configuration/{profile}/transactions";
    String UNDER_THE_BRIDGE = "/utb";
    String WEBSERVICES = "/configuration/{profile}/webservices";
    String XA_DATA_SOURCE = "/configuration/{profile}/xa-data-source/{name}";

    Set<String> getTokens();

    boolean wasRevealed(String token);

    void markedRevealed(String token);
}
