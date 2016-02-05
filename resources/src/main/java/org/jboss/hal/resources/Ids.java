/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.resources;

/**
 * Ids used in HTML elements
 */
public interface Ids {

    String CONFIGURATION_COLUMN = "configuration-column";
    String CONFIGURATION_FINDER = "configuration-finder";

    String DATA_SOURCE_ATTRIBUTES_FORM = "data-source-attributes-form";
    String DATA_SOURCE_ATTRIBUTES_TAB = "data-source-attributes-tab";
    String DATA_SOURCE_CONNECTION_FORM = "data-source-connection-form";
    String DATA_SOURCE_CONNECTION_TAB = "data-source-connection-tab";
    String DATA_SOURCE_TABLE = "data-source-table";

    String ENDPOINT_ADD = "endpoint-add";
    String ENDPOINT_PING = "endpoint-ping";
    String ENDPOINT_SELECT = "endpoint-select";

    String HEADER_CONNECTED_TO = "header-connected-to";
    String HEADER_MESSAGES = "header-messages";
    String HEADER_ROLES = "header-roles";
    String HEADER_USERNAME = "header-username";

    String HOMEPAGE_ACCESS_CONTROL_SECTION = "homepage-access-control-section";
    String HOMEPAGE_CONFIGURATION_SECTION = "homepage-configuration-section";
    String HOMEPAGE_DEPLOYMENTS_SECTION = "homepage-deployments-section";
    String HOMEPAGE_PATCHING_SECTION = "homepage-patching-section";
    String HOMEPAGE_RUNTIME_SECTION = "homepage-runtime-section";
    String HOMEPAGE_RUNTIME_MONITOR_SECTION = "homepage-runtime-monitor-section";
    String HOMEPAGE_RUNTIME_SERVER_SECTION = "homepage-runtime-server-section";
    String HOMEPAGE_RUNTIME_SERVER_GROUP_SECTION = "homepage-runtime-server-group-section";

    String INTERFACE_COLUMN = "interface-column";
    String ROOT_CONTAINER = "hal-root-container";
    String SOCKET_BINDING_COLUMN = "socket-binding-column";
    String SUBSYSTEM_COLUMN = "subsystem-column";

    String TLC_ACCESS_CONTROL = "tlc-access-control";
    String TLC_CONFIGURATION = "tlc-configuration";
    String TLC_DEPLOYMENTS = "tlc-deployments";
    String TLC_HOMEPAGE = "tlc-homepage";
    String TLC_PATCHING = "tlc-patching";
    String TLC_RUNTIME = "tlc-runtime";
}
