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

import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

/**
 * IDs used in HTML elements and across multiple classes. Please add IDs to this interface even if there's already an
 * equivalent or similar constant in {@code ModelDescriptionConstants} (SoC).
 * <p>
 * The IDs defined here are reused by QA. So please make sure that IDs are not spread over the complete code base but
 * gathered in this interface. This is not always possible - for instance if the ID contains dynamic parts like a
 * resource name or selected server. But IDs which only contain static strings should be part of this interface.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public interface Ids {

    // ------------------------------------------------------ common suffixes used in IDs below

    String ADD_SUFFIX = "add";
    String ENTRY_SUFFIX = "entry";
    String FORM_SUFFIX = "form";
    String REFRESH_SUFFIX = "refresh";
    String TAB_SUFFIX = "tab";
    String TABLE_SUFFIX = "table";
    String WIZARD_SUFFIX = "wizard";


    // ------------------------------------------------------ ids (a-z)
    // Try to compose IDs by making use of the build() method, except the ID is used in an annotation.

    String CONFIGURATION = "configuration";
    String CONTENT = "content";
    String CONTENT_ADD = build(Ids.CONTENT, ADD_SUFFIX);

    String DATA_SOURCE_CONFIGURATION = "data-source-configuration";
    String DATA_SOURCE_ADD = build(DATA_SOURCE_CONFIGURATION, ADD_SUFFIX);
    String DATA_SOURCE_DRIVER = "data-source-driver";
    String DATA_SOURCE_REFRESH = build(DATA_SOURCE_CONFIGURATION, REFRESH_SUFFIX);
    String DATA_SOURCE_RUNTIME = "data-source-runtime";
    String DATA_SOURCE_WIZARD = build(DATA_SOURCE_CONFIGURATION, WIZARD_SUFFIX);
    String DEPLOYMENT = "deployment";
    String DEPLOYMENT_ADD = build(Ids.DEPLOYMENT, ADD_SUFFIX);
    String DEPLOYMENT_BROWSE_BY = "deployment-browse-by";
    String DOMAIN_BROWSE_BY = "domain-browse-by";
    String DRAG_AND_DROP_DEPLOYMENT = "drag-and-drop-deployment";

    String EE = "ee";
    String EE_ATTRIBUTES_ENTRY = build(EE, "attributes", ENTRY_SUFFIX);
    String EE_ATTRIBUTES_FORM = build(EE, "attributes", FORM_SUFFIX);
    String EE_CONTEXT_SERVICE = build(EE, "service", "context-service");
    String EE_DEFAULT_BINDINGS_ENTRY = build(EE, "default-bindings", ENTRY_SUFFIX);
    String EE_DEFAULT_BINDINGS_FORM = build(EE, "default-bindings", FORM_SUFFIX);
    String EE_GLOBAL_MODULES_ENTRY = build(EE, "global-modules", ENTRY_SUFFIX);
    String EE_GLOBAL_MODULES_FORM = build(EE, "global-modules", FORM_SUFFIX);
    String EE_GLOBAL_MODULES_TABLE = build(EE, "global-modules", TABLE_SUFFIX);
    String EE_MANAGED_EXECUTOR = build(EE, "service", "executor");
    String EE_MANAGED_EXECUTOR_SCHEDULED = build(EE, "service", "scheduled-executor");
    String EE_MANAGED_THREAD_FACTORY = build(EE, "service", "thread-factories");
    String EE_SERVICES_ENTRY = build(Ids.EE, "services", ENTRY_SUFFIX);

    String ENDPOINT = "endpoint";
    String ENDPOINT_ADD = build(ENDPOINT, "add");
    String ENDPOINT_PING = build(ENDPOINT, "ping");
    String ENDPOINT_SELECT = build(ENDPOINT, "select");

    String FINDER = "hal-finder";

    String HEADER = "header";
    String HEADER_CONNECTED_TO = build(HEADER, "connected-to");
    String HEADER_MESSAGES = build(HEADER, "messages");
    String HEADER_ROLES = build(HEADER, "roles");
    String HEADER_USERNAME = build(HEADER, "username");

    String HOMEPAGE = "homepage";
    String HOMEPAGE_ACCESS_CONTROL_SECTION = build(HOMEPAGE, "access-control-section");
    String HOMEPAGE_CONFIGURATION_SECTION = build(HOMEPAGE, "configuration-section");
    String HOMEPAGE_DEPLOYMENTS_SECTION = build(HOMEPAGE, "deployments-section");
    String HOMEPAGE_PATCHING_SECTION = build(HOMEPAGE, "patching-section");
    String HOMEPAGE_RUNTIME_MONITOR_SECTION = build(HOMEPAGE, "runtime-monitor-section");
    String HOMEPAGE_RUNTIME_SECTION = build(HOMEPAGE, "runtime-section");
    String HOMEPAGE_RUNTIME_SERVER_GROUP_SECTION = build(HOMEPAGE, "runtime-server-group-section");
    String HOMEPAGE_RUNTIME_SERVER_SECTION = build(HOMEPAGE, "runtime-server-section");
    String HOST = "host";
    String HOST_REFRESH = build(HOST, REFRESH_SUFFIX);

    String INTERFACE = "interface";
    String INTERFACE_ADD = build(INTERFACE, ADD_SUFFIX);
    String INTERFACE_REFRESH = build(INTERFACE, REFRESH_SUFFIX);

    String JDBC_DRIVER = "jdbc-driver";
    String JDBC_DRIVER_ADD = build(JDBC_DRIVER, ADD_SUFFIX);
    String JDBC_DRIVER_ADD_FORM = build(JDBC_DRIVER, ADD_SUFFIX, FORM_SUFFIX);
    String JDBC_DRIVER_REFRESH = build(JDBC_DRIVER, REFRESH_SUFFIX);

    String LOG_FILE = "log-file";
    String LOG_FILE_EDITOR = build(LOG_FILE, "editor");
    String LOG_FILE_REFRESH = build(LOG_FILE, REFRESH_SUFFIX);
    String LOGGING = "logging";
    String LOGGING_CONFIGURATION = "logging-configuration";
    String LOGGING_PROFILE = "logging-profile";
    String LOGGING_PROFILE_ADD = build(LOGGING_PROFILE, ADD_SUFFIX);

    String MACRO = "macro";
    String MACRO_EDITOR = build(MACRO, "editor");
    String MACRO_LIST = build(MACRO, "list");
    String MACRO_OPTIONS = build(MACRO, "options");
    String MAIL_SERVER = "mail-server";
    String MAIL_SERVER_ENTRY = build(MAIL_SERVER, ENTRY_SUFFIX);
    String MAIL_SERVER_DIALOG = build(MAIL_SERVER, ADD_SUFFIX, FORM_SUFFIX);
    String MAIL_SERVER_FORM = build(MAIL_SERVER, FORM_SUFFIX);
    String MAIL_SERVER_TABLE = build(MAIL_SERVER, TABLE_SUFFIX);
    ;
    String MAIL_SESSION = "mail-session";
    String MAIL_SESSION_ADD = build(MAIL_SESSION, ADD_SUFFIX);
    String MAIL_SESSION_ATTRIBUTES_ENTRY = build(MAIL_SESSION, "attributes", ENTRY_SUFFIX);
    String MAIL_SESSION_ATTRIBUTES_FORM = build(MAIL_SESSION, "attributes", FORM_SUFFIX);
    String MAIL_SESSION_DIALOG = build(MAIL_SESSION, FORM_SUFFIX);
    String MAIL_SESSION_REFRESH = build(MAIL_SESSION, REFRESH_SUFFIX);
    String MODEL_BROWSER = "model-browser";

    String PREVIEW_ID = build(FINDER, "preview");
    String PROFILE = "profile";
    String PROFILE_ADD = build(PROFILE, ADD_SUFFIX);
    String PROFILE_REFRESH = build(PROFILE, REFRESH_SUFFIX);

    String ROOT_CONTAINER = "hal-root-container";

    String SERVER = "server";
    String SERVER_ADD = build(SERVER, ADD_SUFFIX);
    String SERVER_GROUP = "server-group";
    String SERVER_GROUP_ADD = build(SERVER_GROUP, ADD_SUFFIX);
    String SERVER_GROUP_REFRESH = build(SERVER_GROUP, REFRESH_SUFFIX);
    String SERVER_MONITOR = "server-monitor";
    String SERVER_REFRESH = build(SERVER, REFRESH_SUFFIX);
    String SOCKET_BINDING = "socket-binding";
    String SOCKET_BINDING_ADD = build(SOCKET_BINDING, ADD_SUFFIX);
    String SOCKET_BINDING_REFRESH = build(SOCKET_BINDING, REFRESH_SUFFIX);
    String STANDALONE_SERVER = "standalone-server";
    String STORAGE_PREFIX = "org.jboss.hal";
    String SUBSYSTEM = "subsystem";

    String TLC_ACCESS_CONTROL = "tlc-access-control";
    String TLC_CONFIGURATION = "tlc-configuration";
    String TLC_DEPLOYMENTS = "tlc-deployments";
    String TLC_HOMEPAGE = "tlc-homepage";
    String TLC_PATCHING = "tlc-patching";
    String TLC_RUNTIME = "tlc-runtime";

    String VERSION_INFO = "version-info";
    String VERSION_INFO_FORM = build(VERSION_INFO, FORM_SUFFIX);

    String WEB_SETTINGS = "settings";

    String XA_DATA_SOURCE = "xa-data-source";
    String XA_DATA_SOURCE_ADD = build(XA_DATA_SOURCE, ADD_SUFFIX);


    // ------------------------------------------------------ methods

    /**
     * Turns a label which can contain whitespace and upper/lower case characters into an all lowercase id separated
     * with "-".
     */
    static String asId(@NonNls String text) {
        Iterable<String> parts = Splitter
                .on(CharMatcher.whitespace().or(CharMatcher.is('-')))
                .omitEmptyStrings()
                .trimResults()
                .split(text);
        return stream(parts.spliterator(), false)
                .map(String::toLowerCase)
                .map(CharMatcher.javaLetterOrDigit()::retainFrom)
                .collect(joining("-"));
    }

    static String build(@NonNls String id, @NonNls String... additionalIds) {
        return build(id, '-', additionalIds);
    }

    static String build(@NonNls String id, char separator, @NonNls String... additionalIds) {
        if (Strings.emptyToNull(id) == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        List<String> ids = Lists.newArrayList(id);
        if (additionalIds != null) {
            for (String additionalId : additionalIds) {
                ids.add(Strings.emptyToNull(additionalId));
            }
        }
        return Joiner.on(separator).skipNulls().join(ids);
    }

    static String dataSourceConfiguration(String name, boolean xa) {
        return build(xa ? "xa" : "non-xa", DATA_SOURCE_CONFIGURATION, name);
    }

    static String dataSourceRuntime(String name, boolean xa) {
        return build(xa ? "xa" : "non-xa", DATA_SOURCE_RUNTIME, name);
    }

    static String host(final String name) {
        return build(HOST, name);
    }

    static String loggingProfile(final String name) {
        return build(LOGGING, name);
    }

    static String server(final String name) {
        return build(SERVER, name);
    }

    static String hostServer(final String host, final String server) {
        return build(host, server);
    }

    static String serverGroupServer(final String serverGroup, final String server) {
        return build(serverGroup, server);
    }

    static String serverGroup(final String name) {
        return build(SERVER_GROUP, name);
    }

    static String uniqueId() {
        return Document.get().createUniqueId();
    }
}
