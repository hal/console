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
package org.jboss.hal.meta.token;

import java.util.Set;

public interface NameTokens {

    String ACCESS_CONTROL = "/access-control";
    String BATCH = "/configuration/{profile}/batch";
    String CONFIGURATION = "/configuration";
    String DATA_SOURCE = "/configuration/{profile}/data-source/{name}";
    String DEPLOYMENT_SCANNERS = "/configuration/{profile}/deployment-scanners";
    String DEPLOYMENTS = "/deployments";
    String EE = "/configuration/{profile}/ee";
    String EJB3 = "/configuration/{profile}/ejb3";
    String HOMEPAGE = "/home";
    String IIOP = "/configuration/{profile}/iiop";
    String INTERFACE = "/interface/{name}";
    String IO = "/configuration/{profile}/io";
    String JCA = "/configuration/{profile}/jca";
    String JMX = "/configuration/{profile}/jmx";
    String JPA = "/configuration/{profile}/jpa";
    String LOGGING = "/configuration/{profile}/logging";
    String MODEL_BROWSER = "/model-browser";
    String PATCHING = "/patching";
    String PATH = "/path";
    String REMOTING = "/configuration/{profile}/remoting";
    String REQUEST_CONTROLLER = "/configuration/{profile}/request-controller";
    String RUNTIME = "/runtime";
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
