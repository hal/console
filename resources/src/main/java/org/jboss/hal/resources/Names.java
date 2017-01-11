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
package org.jboss.hal.resources;

/**
 * Common names and technical terms which are not meant to be translated.
 *
 * @author Harald Pehl
 */
public interface Names {

    String ACCESS_CONTROL = "Access Control";
    String ACCESS_LOG = "Access Log";
    String ADDRESS_SETTING = "Address Setting";
    String ACL_MODULE = "ACL Module";
    String ADMIN_OBJECT = "Admin Object";
    String AJP_LISTENER = "AJP Listener";
    String ARCHIVE = "Archive";
    String APPLICATION_SECURITY_DOMAIN = "Application Security Domain";
    String AUDIT_LOG = "Audit Log";
    String AUDIT_MODULE = "Audit Module";
    String AUTHENTICATION_MODULE = "Authentication Module";
    String AUTHORIZATION_MODULE = "Authorization Module";

    String BOOTSTRAP = "Bootstrap";
    String BRIDGE = "Bridge";
    String BROADCAST_GROUP = "Broadcast Group";
    String BUFFER_CACHES = "Buffer Caches";

    String CLIENT_CONFIGURATION = "Client Configuration";
    String CLUSTER_CONNECTION = "Cluster Connection";
    String CLUSTERING = "Clustering";
    String COLLECTION = "Collection";
    String CONFIGURATION = "Configuration";
    String CONNECTION = "Connection";
    String CONNECTION_FACTORY = "Connection Factory";
    String CONNECTION_DEFINITION = "Connection Definition";
    String CONNECTIONS = "Connections";
    String CONNECTOR_SERVICE = "Connector Service";
    String COOKIES = "Cookies";
    String CORE_QUEUE = "Core Queue";
    String CRAWLER = "Crawler";

    String DATASOURCE = "Datasource";
    String DATASOURCES = "Datasources";
    String DATASOURCES_DRIVERS = "Datasources & Drivers";
    String DEPLOYMENT = "Deployment";
    String DEPLOYMENTS = "Deployments";
    String DESTINATIONS = "Destinations";
    String DISCOVERY_GROUP = "Discovery Group";
    String DIVERT = "Divert";
    String DOMAIN_CONTROLLER = "Domain Controller";

    String EE = "EE";
    String ENDPOINT_CONFIGURATION = "Endpoint Configuration";
    String ENTITY = "Entity";
    String ENTITY_CACHE = "Entity Cache";

    String FILTER = "Filter";
    String FILTERS = "Filters";

    String GENERIC_ACCEPTOR = "Generic Acceptor";
    String GENERIC_CONNECTOR = "Generic Connector";
    String GLOBAL_MODULES = "Global Modules";
    String GROUPING_HANDLER = "Grouping Handler";

    String HA_POLICY = "HA Policy";
    String HAL = "HAL";
    String HANDLER = "Handler";
    String HANDLERS = "Handlers";
    String HANDLER_CHAIN = "Handler Chain";
    String HEAP = "Heap";
    String HOST = "Host";
    String HOST_CONTROLLER = "Host Controller";
    String HOSTS = "Hosts";
    String HTTP_ACCEPTOR = "HTTP Acceptor";
    String HTTP_CONNECTOR = "HTTP Connector";
    String HTTP_CONNECTOR_SECURITY = "HTTP Connector Security";
    String HTTP_CONNECTOR_SECURITY_POLICY = "HTTP Connector Security Policy";
    String HTTP_LISTENER = "HTTP Listener";
    String HTTPS_LISTENER = "HTTPS Listener";

    String IN_VM_ACCEPTOR = "In VM Acceptor";
    String IN_VM_CONNECTOR = "In VM Connector";
    String INTERFACE = "Interface";
    String INTERFACES = "Interfaces";

    String JDBC = "JDBC";
    String JDBC_DRIVER = "JDBC Driver";
    String JDBC_DRIVERS = "JDBC Drivers";
    String JMS_BRIDGE = "JMS Bridge";
    String JNDI = "JNDI";
    String JPA = "JPA";
    String JMS_QUEUE = "JMS Queue";
    String JMS_TOPIC = "JMS Topic";
    String JSP = "JSP";

    String LISTENER = "Listener";
    String LIVE_ONLY = "Live Only";
    String LOCAL_OUTBOUND_CONNECTION = "Local Outbound Connection";
    String LOCATION = "Location";
    String LOCATIONS = "Locations";
    String LOGGING = "Logging";
    String LOGGING_PROFILE = "Logging Profile";
    String LOGGING_PROFILES = "Logging Profiles";
    String LONG_RUNNING = "long running";

    String MAIL_SESSION = "Mail Session";
    String MANAGEMENT_CONSOLE = "Management Console";
    String MANAGEMENT_MODEL = "Management Model";
    String MAPPING_MODULE = "Mapping Module";
    String MASTER = "Master";
    String MIME_MAPPING = "Mime Mapping";
    String MB = "MB";
    String MODULE = "Module";
    String NAME = "Name";

    String NOT_AVAILABLE = "n/a";
    String NOT_SUPPORTED = "not supported";
    String NYI = "not yet implemented";

    String OUTBOUND_CONNECTION = "Outbound Connection";

    String PATCHING = "Patching";
    String PATHS = "Paths";
    String PATTERN = "Pattern";
    String POOLED_CONNECTION_FACTORY = "Pooled Connection Factory";
    String POST_HANDLER_CHAIN = "Post Handler Chain";
    String PRE_HANDLER_CHAIN = "Pre Handler Chain";
    String PROFILE = "Profile";
    String PROFILES = "Profiles";

    String QUERY = "Query";
    String QUERY_CACHE = "Query Cache";

    String REALM = "Realm";
    String REMOTE_ACCEPTOR = "Remote Acceptor";
    String REMOTE_CONNECTOR = "Remote Connector";
    String REMOTE_CONNECTOR_SECURITY = "Remote Connector Security";
    String REMOTE_CONNECTOR_SECURITY_POLICY = "Remote Connector Security Policy";
    String REMOTE_OUTBOUND_CONNECTION = "Remote Outbound Connection";
    String REPLICATION_COLOCATED = "Replication Colocated";
    String REPLICATION_MASTER = "Replication Master";
    String REPLICATION_SLAVE = "Replication Slave";
    String RESOURCE = "Resource";
    String RESOURCE_ADAPTER = "Resource Adapter";
    String RESPONSE_HEADER = "Response Header";
    String ROOT_LOGGER = "Root Logger";
    String RUNTIME = "Runtime";

    String SECOND_LEVEL_CACHE = "Second Level Cache";
    String SECURITY_DOMAIN = "Security Domain";
    String SECURITY_SETTING = "Security Setting";
    String SERVER = "Server";
    String SERVER_GROUP = "Server Group";
    String SERVER_GROUPS = "Server Groups";
    String SERVLET_CONTAINER = "Servlet Container";
    String SESSIONS = "Sessions";
    String SHARED_STORE_COLOCATED = "Shared Store Colocated";
    String SHARED_STORE_MASTER = "Shared Store Master";
    String SHARED_STORE_SLAVE = "Shared Store Slave";
    String SINGLE_SIGN_ON = "Single Sign On";
    String SHORT_RUNNING = "short running";
    String SLAVE = "Slave";
    String SOCKET_BINDING = "Socket Binding";
    String SOCKET_BINDINGS = "Socket Bindings";
    String STANDALONE_SERVER = "Standalone Server";
    String SUBDEPLOYMENTS = "Subdeployments";
    String SUBSYSTEM = "Subsystem";
    String SUBSYSTEMS = "Subsystems";
    String SYSTEM_PROPERTIES = "System Properties";

    String THREAD_POOL = "Thread Pool";
    String THREAD_POOLS = "Thread Pools";
    String THREADS = "Threads";
    String TOPOLOGY = "Topology";
    String TRUST_MODULE = "Trust Module";

    String UNKNOWN = "unknown";
    String UNMANAGED_DEPLOYMENT = "Unmanaged Deployment";

    String VALUE = "Value";

    String WEBSERVICES_CONFIGURATION = "Webservices Configuration";
    String WEBSOCKETS = "Web Sockets";
    String WELCOME_FILE = "Welcome File";

    String XA_DATASOURCE = "XA Datasource";
}
