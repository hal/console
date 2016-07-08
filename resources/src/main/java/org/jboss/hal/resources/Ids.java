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
 * Ids used in HTML elements and across multiple classes. Please add IDs also for stuff that's already in
 * {@code ModelDescriptionConstants} (SoC).
 * <p>
 * The IDs defined here are reused by QA. So it's important that IDs are not spread over the complete code base but
 * gathered in this interface.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public interface Ids {

    // ------------------------------------------------------ common suffixes used in IDs

    String ADD_SUFFIX = "add";
    String ENTRY_SUFFIX = "entry";
    String FORM_SUFFIX = "form";
    String REFRESH_SUFFIX = "refresh";
    String TAB_SUFFIX = "tab";
    String TABLE_SUFFIX = "table";
    String WIZARD_SUFFIX = "wizard";


    // ------------------------------------------------------ ids (a-z)

    String CONFIGURATION = "configuration";
    String CONFIGURATION_SUBSYSTEM = "c-subsystem";
    String CONTENT = "content";
    String CONTENT_ADD = IdBuilder.build(Ids.CONTENT, ADD_SUFFIX);

    String DATA_SOURCE = "data-source";
    String DATA_SOURCE_ADD = IdBuilder.build(DATA_SOURCE, ADD_SUFFIX);
    String DATA_SOURCE_DRIVER = "data-source-driver"; // can't be replaced with IdBuilder.build(...)
    String DATA_SOURCE_REFRESH = IdBuilder.build(DATA_SOURCE, REFRESH_SUFFIX);
    String DATA_SOURCE_WIZARD = IdBuilder.build(DATA_SOURCE, WIZARD_SUFFIX);
    String DEPLOYMENT = "deployment";
    String DEPLOYMENT_ADD = IdBuilder.build(Ids.DEPLOYMENT, ADD_SUFFIX);
    String DEPLOYMENT_BROWSE_BY = "deployment-browse-by"; // can't be replaced with IdBuilder.build(...)
    String DOMAIN_BROWSE_BY = "domain-browse-by";
    String DRAG_AND_DROP_DEPLOYMENT = "drag-and-drop-deployment";

    String EE = "ee";
    String EE_ATTRIBUTES_ENTRY = IdBuilder.build(EE, "attributes", ENTRY_SUFFIX);
    String EE_ATTRIBUTES_FORM = IdBuilder.build(EE, "attributes", FORM_SUFFIX);
    String EE_CONTEXT_SERVICE = IdBuilder.build(EE, "service", "context-service");
    String EE_DEFAULT_BINDINGS_ENTRY = IdBuilder.build(EE, "default-bindings", ENTRY_SUFFIX);
    String EE_DEFAULT_BINDINGS_FORM = IdBuilder.build(EE, "default-bindings", FORM_SUFFIX);
    String EE_GLOBAL_MODULES_ENTRY = IdBuilder.build(EE, "global-modules", ENTRY_SUFFIX);
    String EE_GLOBAL_MODULES_FORM = IdBuilder.build(EE, "global-modules", FORM_SUFFIX);
    String EE_GLOBAL_MODULES_TABLE = IdBuilder.build(EE, "global-modules", TABLE_SUFFIX);
    String EE_MANAGED_EXECUTOR = IdBuilder.build(EE, "service", "executor");
    String EE_MANAGED_EXECUTOR_SCHEDULED = IdBuilder.build(EE, "service", "scheduled-executor");
    String EE_MANAGED_THREAD_FACTORY = IdBuilder.build(EE, "service", "thread-factories");
    String EE_SERVICES_ENTRY = IdBuilder.build(Ids.EE, "services", ENTRY_SUFFIX);

    String ENDPOINT = "endpoint";
    String ENDPOINT_ADD = IdBuilder.build(ENDPOINT, "add");
    String ENDPOINT_PING = IdBuilder.build(ENDPOINT, "ping");
    String ENDPOINT_SELECT = IdBuilder.build(ENDPOINT, "select");

    String FINDER = "hal-finder";

    String HEADER = "header";
    String HEADER_CONNECTED_TO = IdBuilder.build(HEADER, "connected-to");
    String HEADER_MESSAGES = IdBuilder.build(HEADER, "messages");
    String HEADER_ROLES = IdBuilder.build(HEADER, "roles");
    String HEADER_USERNAME = IdBuilder.build(HEADER, "username");

    String HOMEPAGE = "homepage";
    String HOMEPAGE_ACCESS_CONTROL_SECTION = IdBuilder.build(HOMEPAGE, "access-control-section");
    String HOMEPAGE_CONFIGURATION_SECTION = IdBuilder.build(HOMEPAGE, "configuration-section");
    String HOMEPAGE_DEPLOYMENTS_SECTION = IdBuilder.build(HOMEPAGE, "deployments-section");
    String HOMEPAGE_PATCHING_SECTION = IdBuilder.build(HOMEPAGE, "patching-section");
    String HOMEPAGE_RUNTIME_MONITOR_SECTION = IdBuilder.build(HOMEPAGE, "runtime-monitor-section");
    String HOMEPAGE_RUNTIME_SECTION = IdBuilder.build(HOMEPAGE, "runtime-section");
    String HOMEPAGE_RUNTIME_SERVER_GROUP_SECTION = IdBuilder.build(HOMEPAGE, "runtime-server-group-section");
    String HOMEPAGE_RUNTIME_SERVER_SECTION = IdBuilder.build(HOMEPAGE, "runtime-server-section");
    String HOST = "host";
    String HOST_REFRESH = IdBuilder.build(HOST, REFRESH_SUFFIX);

    String INTERFACE = "interface";
    String INTERFACE_ADD = IdBuilder.build(INTERFACE, ADD_SUFFIX);
    String INTERFACE_REFRESH = IdBuilder.build(INTERFACE, REFRESH_SUFFIX);

    String JDBC_DRIVER = "jdbc-driver";
    String JDBC_DRIVER_ADD = IdBuilder.build(JDBC_DRIVER, ADD_SUFFIX);
    String JDBC_DRIVER_ADD_FORM = IdBuilder.build(JDBC_DRIVER, ADD_SUFFIX, FORM_SUFFIX);
    String JDBC_DRIVER_REFRESH = IdBuilder.build(JDBC_DRIVER, REFRESH_SUFFIX);

    String LOG_FILE = "log-file";
    String LOG_FILE_EDITOR = IdBuilder.build(LOG_FILE, "editor");
    String LOG_FILE_REFRESH = IdBuilder.build(LOG_FILE, REFRESH_SUFFIX);
    String LOGGING = "logging";
    String LOGGING_PROFILE = "logging-profile";
    String LOGGING_PROFILE_ADD = IdBuilder.build(LOGGING_PROFILE, ADD_SUFFIX);

    String MACRO = "macro";
    String MACRO_EDITOR = IdBuilder.build(MACRO, "editor");
    String MACRO_LIST = IdBuilder.build(MACRO, "list");
    String MACRO_OPTIONS = IdBuilder.build(MACRO, "options");
    String MAIL_SERVER = "mail-server";
    String MAIL_SERVER_ENTRY = IdBuilder.build(MAIL_SERVER, ENTRY_SUFFIX);
    String MAIL_SERVER_DIALOG = IdBuilder.build(MAIL_SERVER, ADD_SUFFIX, FORM_SUFFIX);
    String MAIL_SERVER_FORM = IdBuilder.build(MAIL_SERVER, FORM_SUFFIX);
    String MAIL_SERVER_TABLE = IdBuilder.build(MAIL_SERVER, TABLE_SUFFIX);;
    String MAIL_SESSION = "mail-session";
    String MAIL_SESSION_ADD = IdBuilder.build(MAIL_SESSION, ADD_SUFFIX);
    String MAIL_SESSION_ATTRIBUTES_ENTRY = IdBuilder.build(MAIL_SESSION, "attributes", ENTRY_SUFFIX);
    String MAIL_SESSION_ATTRIBUTES_FORM = IdBuilder.build(MAIL_SESSION, "attributes", FORM_SUFFIX);
    String MAIL_SESSION_DIALOG = IdBuilder.build(MAIL_SESSION, FORM_SUFFIX);
    String MAIL_SESSION_REFRESH = IdBuilder.build(MAIL_SESSION, REFRESH_SUFFIX);
    String MODEL_BROWSER = "model-browser";

    String PREVIEW_ID = IdBuilder.build(FINDER, "preview");
    String PROFILE = "profile";
    String PROFILE_ADD = IdBuilder.build(PROFILE, ADD_SUFFIX);
    String PROFILE_REFRESH = IdBuilder.build(PROFILE, REFRESH_SUFFIX);

    String ROOT_CONTAINER = "hal-root-container";
    String RUNTIME_SUBSYSTEM = "r-subsystem";

    String SERVER = "server";
    String SERVER_ADD = IdBuilder.build(SERVER, ADD_SUFFIX);
    String SERVER_GROUP = "server-group";
    String SERVER_GROUP_ADD = IdBuilder.build(SERVER_GROUP, ADD_SUFFIX);
    String SERVER_GROUP_REFRESH = IdBuilder.build(SERVER_GROUP, REFRESH_SUFFIX);
    String SERVER_MONITOR = "server-monitor";
    String SERVER_REFRESH = IdBuilder.build(SERVER, REFRESH_SUFFIX);
    String SOCKET_BINDING = "socket-binding";
    String SOCKET_BINDING_ADD = IdBuilder.build(SOCKET_BINDING, ADD_SUFFIX);
    String SOCKET_BINDING_REFRESH = IdBuilder.build(SOCKET_BINDING, REFRESH_SUFFIX);
    String STANDALONE_SERVER = "standalone-server";
    String STORAGE_PREFIX = "org.jboss.hal";

    String TLC_ACCESS_CONTROL = "tlc-access-control";
    String TLC_CONFIGURATION = "tlc-configuration";
    String TLC_DEPLOYMENTS = "tlc-deployments";
    String TLC_HOMEPAGE = "tlc-homepage";
    String TLC_PATCHING = "tlc-patching";
    String TLC_RUNTIME = "tlc-runtime";

    String VERSION_INFO = "version-info";
    String VERSION_INFO_FORM = IdBuilder.build(VERSION_INFO, FORM_SUFFIX);

    String WEB_SETTINGS = "settings";

    String XA_DATA_SOURCE = "xa-data-source";
    String XA_DATA_SOURCE_ADD = IdBuilder.build(XA_DATA_SOURCE, ADD_SUFFIX);

    static String dataSourceId(String name, boolean xa) {
        return IdBuilder.build(xa ? "xa" : "non-xa", DATA_SOURCE, name);
    }

    static String hostId(final String name) {
        return IdBuilder.build(HOST, name);
    }

    static String loggingProfileId(final String name) {
        return IdBuilder.build(LOGGING, name);
    }

    static String serverId(final String name) {
        return IdBuilder.build(SERVER, name);
    }

    static String hostServerId(final String host, final String server) {
        return IdBuilder.build(host, server);
    }

    static String serverGroupServerId(final String serverGroup, final String server) {
        return IdBuilder.build(serverGroup, server);
    }

    static String serverGroupId(final String name) {
        return IdBuilder.build(SERVER_GROUP, name);
    }
}
