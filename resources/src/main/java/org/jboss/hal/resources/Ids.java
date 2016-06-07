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
 * Ids used in HTML elements and accross multiple classes
 */
public interface Ids {

    String CACHE_CONTAINER_COLUMN = "cache-container";
    String CONTENT_COLUMN = "content";

    String DATA_SOURCE_DRIVER_COLUMN = "data-source-driver";
    String DEPLOYMENT_BROWSE_BY_COLUMN = "deployment-browse-by";
    String DOMAIN_BROWSE_BY_COLUMN = "domain-browse-by";
    String DRAG_AND_DROP_DEPLOYMENT = "drag-and-drop-deployment";

    String EE_ATTRIBUTES_ENTRY = "ee-attributes-entry";
    String EE_ATTRIBUTES_FORM = "ee-attributes-form";
    String EE_GLOBAL_MODULES_ENTRY = "ee-global-modules-entry";
    String EE_GLOBAL_MODULES_TABLE = "ee-global-modules-table";
    String EE_GLOBAL_MODULES_FORM = "ee-global-modules-form";
    String EE_DEFAULT_BINDINGS_ENTRY = "ee-default-bindings-entry";
    String EE_DEFAULT_BINDINGS_FORM = "ee-default-bindings-form";

    String ENDPOINT_ADD = "endpoint-add";
    String ENDPOINT_PING = "endpoint-ping";
    String ENDPOINT_SELECT = "endpoint-select";

    String FINDER = "hal-finder";

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

    String MACRO_EDITOR = "macro-editor";
    String MACRO_LIST = "macro-list";
    String MACRO_OPTIONS = "macro-options";
    String MAIL_SESSION_ATTRIBUTES_ENTRY = "mail-session-attributes-entry";
    String MAIL_SESSION_ATTRIBUTES_FORM = "mail-session-attributes-form";
    String MAIL_SESSION_SERVERS_ENTRY = "mail-session-servers-entry";
    String MAIL_SESSION_SERVERS_FORM = "mail-session-servers-form";
    String MAIL_SESSION_SERVERS_TABLE = "mail-session-servers-table";
    String MODEL_BROWSER = "model-browser";

    String ROOT_CONTAINER = "hal-root-container";

    String STANDALONE_SERVER_COLUMN = "standalone-server";
    String STORAGE_PREFIX = "org.jboss.hal";

    String TLC_ACCESS_CONTROL = "tlc-access-control";
    String TLC_CONFIGURATION = "tlc-configuration";
    String TLC_DEPLOYMENTS = "tlc-deployments";
    String TLC_HOMEPAGE = "tlc-homepage";
    String TLC_PATCHING = "tlc-patching";
    String TLC_RUNTIME = "tlc-runtime";

    String VERSION_INFO = "version-info";
    String VERSION_INFO_FORM = IdBuilder.build(VERSION_INFO, "form");

    String WEB_SETTINGS_COLUMN = "settings";
}
