/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.resources;

/** Common names and technical terms which are not meant to be translated. */
public interface Names {

    // KEEP THESE IN ALPHABETICAL ORDER!
    String ACCESS_CONTROL = "Access Control";
    String ACCESS_LOG = "Access Log";
    String ACTIVE_MQ = "ActiveMQ";
    String ADDRESS_SETTING = "Address Setting";
    String ACL_MODULE = "ACL Module";
    String ADMIN_OBJECT = "Admin Object";
    String AFFINITY = "Affinity";
    String AJP_LISTENER = "AJP Listener";
    String ARCHIVE = "Archive";
    String APPLICATION_SECURITY_DOMAIN = "Application Security Domain";
    String ASYNC = "Async";
    String ASYNC_ACTION_HANDLER = "Async ActionHandler";
    String ATTRIBUTE_MAPPING = "Attribute Mapping";
    String AUDIT_LOG = "Audit Log";
    String AUDIT_MODULE = "Audit Module";
    String AUTHENTICATION_MODULE = "Authentication Module";
    String AUTHORIZATION_MODULE = "Authorization Module";

    String BACKUP = "Backup";
    String BACKUPS = "Backups";
    String BALANCER = "Balancer";
    String BATCH = "Batch";
    String BATCH_STATUS = "Batch Status";
    String BINDINGS_DIRECTORY = "Bindings Directory";
    String BLOCKING = "Blocking";
    String BOOT_ERRORS = "Boot Errors";
    String BOOTSTRAP = "Bootstrap";
    String BRIDGE = "Bridge";
    String BROWSER_DEFAULT_TITLE = "%n | Management Console";
    String BROWSER_FALLBACK_TITLE = "HAL Management Console";
    String BUFFER_CACHES = "Buffer Caches";
    String BYTE_BUFFER_POOL = "Byte Buffer Pool";

    String CACHE = "Cache";
    String CACHE_CONTAINER = "Cache Container";
    String CACHING_REALM = "Caching Realm";
    String CATEGORY = "Category";
    String CERTIFICATE = "Certificate";
    String CERTIFICATES = "Certificates";
    String CHANNEL = "Channel";
    String CHANNELS = "Channels";
    String CLIENT_CONFIGURATION = "Client Configuration";
    String CLIENT_MAPPING = "Client Mapping";
    String CLIENT_MAPPINGS = "Client Mappings";
    String CLUSTER_CONNECTION = "Cluster Connection";
    String CLUSTERING = "Clustering";
    String COLLECTION = "Collection";
    String CONFIG_SOURCE = "Config Source";
    String CONFIGURATION = "Configuration";
    String CONFIGURATION_CHANGES = "Configuration Changes";
    String CONNECTION = "Connection";
    String CONNECTION_FACTORY = "Connection Factory";
    String CONNECTION_POOL = "Connection Pool";
    String CONNECTION_DEFINITION = "Connection Definition";
    String CONNECTIONS = "Connections";
    String CONNECTOR_SERVICE = "Connector Service";
    String CONNECTORS = "Connectors";
    String CONSUMERS = "Consumers";
    String CONSOLE_ACCESS_LOG = "Console Access Log";
    String CONSOLE_ACTION_HANDLER = "Console ActionHandler";
    String CONTENT_REPOSITORY = "Content Repository";
    String CONTEXT = "Context";
    String CONTEXT_ROOT = "Context Root";
    String CONTEXT_ROOTS = "Context Roots";
    String COOKIES = "Cookies";
    String CORE_MANAGEMENT = "Core Management";
    String CORE_QUEUE = "Core Queue";
    String CRAWLER = "Crawler";
    String CREDENTIAL_REFERENCE = "Credential Reference";
    String CREDENTIAL_STORE = "Credential Store";
    String CUSTOM = "Custom";
    String CUSTOM_ACTION_HANDLER = "Custom ActionHandler";
    String CUSTOM_FORMATTER = "Custom Formatter";
    String CUSTOM_LOAD_METRIC = "Custom Load Metric";
    String CUSTOM_MODIFIABLE_REALM = "Custom Modifiable Realm";
    String CUSTOM_POLICY = "Custom Policy";

    String DATASOURCE = "Datasource";
    String DATASOURCES = "Datasources";
    String DATASOURCES_DRIVERS = "Datasources & Drivers";
    String DEFAULT_INTERFACE = "Default Interface";
    String DEPLOYMENT = "Deployment";
    String DEPLOYMENT_PERMISSIONS = "Deployment Permissions";
    String DEPLOYMENTS = "Deployments";
    String DESTINATION = "Destination";
    String DESTINATIONS = "Destinations";
    String DISTRIBUTED_CACHE = "Distributed Cache";
    String DIVERT = "Divert";
    String DOMAIN_CONTROLLER = "Domain Controller";

    String EE = "EE";
    String EJB3 = "EJB";
    String ELYTRON = "Elytron";
    String ENDPOINT = "Endpoint";
    String ENDPOINT_CONFIGURATION = "Endpoint Configuration";
    String ENTITY = "Entity";
    String ENTITY_CACHE = "Entity Cache";
    String EXECUTION_ID = "Execution ID";
    String EXECUTIONS = "Executions";
    String EXPIRATION = "Expiration";
    String EXPRESSION = "Expression";
    String EXTERNAL_JMS_QUEUE = "External JMS Queue";
    String EXTERNAL_JMS_TOPIC = "External JMS Topic";

    String FACTORIES_TRANSFORMERS = "Factories / Transformers";
    String FILE = "File";
    String FILE_HANDLER = "File Handler";
    String FILESYSTEM_REALM = "Filesystem Realm";
    String FILTERING_KEY_STORE = "Filtering Key Store";
    String FILTERS = "Filters";
    String FORK = "Fork";

    String GAV = "GAV";
    String GENERIC_ACCEPTOR = "Generic Acceptor";
    String GENERIC_CONNECTOR = "Generic Connector";
    String GLOBAL_DIRECTORY = "Global Directory";
    String GLOBAL_MODULES = "Global Modules";
    String GROUPING_HANDLER = "Grouping Handler";

    String HA_POLICY = "HA Policy";
    String HAL = "HAL";
    String HANDLER = "Handler";
    String HANDLERS = "Handlers";
    String HEAP = "Heap";
    String HOMEPAGE = "Homepage";
    String HOST = "Host";
    String HOST_CONTROLLER = "Host Controller";
    String HOSTS = "Hosts";
    String HOT_ROD = "Hot Rod";
    String HTTP = "HTTP";
    String HTTP_ACCEPTOR = "HTTP Acceptor";
    String HTTP_AUTHENTICATION_FACTORY = "HTTP Authentication Factory";
    String HTTP_CONNECTOR = "HTTP Connector";
    String HTTP_CONNECTOR_SECURITY = "HTTP Connector Security";
    String HTTP_CONNECTOR_SECURITY_POLICY = "HTTP Connector Security Policy";
    String HTTP_INVOKER = "HTTP Invoker";
    String HTTP_LISTENER = "HTTP Listener";
    String HTTPS = "HTTPS";
    String HTTPS_LISTENER = "HTTPS Listener";

    String IDENTITY_ATTRIBUTE_MAPPING = "Identity Attribute Mapping";
    String IDENTITY_MAPPING = "Identity Mapping";
    String IN_VM_ACCEPTOR = "In VM Acceptor";
    String IN_VM_CONNECTOR = "In VM Connector";
    String INBOUND = "Inbound";
    String INCLUDES_ATTRIBUTE = "Includes";
    String INFINISPAN = "Infinispan";
    String INSTANCE_ID = "Instance ID";
    String INTERFACE = "Interface";
    String INTERFACES = "Interfaces";
    String INVALIDATION_CACHE = "Invalidation Cache";
    String IO = "IO";

    String JACC_POLICY = "JACC Policy";
    String JAX_RS = "JAX-RS";
    String JBERET = "JBeret";
    String JDBC = "JDBC";
    String JDBC_DRIVER = "JDBC Driver";
    String JDBC_DRIVERS = "JDBC Drivers";
    String JDBC_REALM = "JDBC Realm";
    String JGROUPS = "JGroups";
    String JGROUPS_BROADCAST_GROUP = "JGroups Broadcast Group";
    String JGROUPS_DISCOVERY_GROUP = "JGroups Discovery Group";
    String JMS_BRIDGE = "JMS Bridge";
    String JNDI = "JNDI";
    String JOB = "Job";
    String JOURNAL_DIRECTORY = "Journal Directory";
    String JPA = "JPA";
    String JMS_QUEUE = "JMS Queue";
    String JMS_TOPIC = "JMS Topic";
    String JSON_FORMATTER = "JSON Formatter";
    String JSP = "JSP";

    String KEYCLOAK = "Keycloak";
    String KEY_MANAGER = "Key Manager";
    String KEY_OVERRIDES = "Key Overrides";
    String KEY_STORE = "Key Store";

    String LARGE_MESSAGES_DIRECTORY = "Large Messages Directory";
    String LDAP_KEY_STORE = "LDAP Key Store";
    String LDAP_REALM = "LDAP Realm";
    String LISTENER = "Listener";
    String LIVE_ONLY = "Live Only";
    String LOAD_METRIC = "Load Metric";
    String LOAD_PROVIDER_DYNAMIC = "Load Provider Dynamic";
    String LOAD_PROVIDER_SIMPLE = "Load Provider Simple";
    String LOCAL = "local";
    String LOCAL_CACHE = "Local Cache";
    String LOCAL_OUTBOUND_CONNECTION = "Local Outbound Connection";
    String LOCATION = "Location";
    String LOCATIONS = "Locations";
    String LOCKING = "Locking";
    String LOGGING = "Logging";
    String LOGGING_PROFILE = "Logging Profile";
    String LOGGING_PROFILES = "Logging Profiles";
    String LONG_RUNNING = "long running";

    String MAIL_SESSION = "Mail Session";
    String MANAGEMENT = "Management";
    String MANAGEMENT_INTERFACE = "Management Interface";
    String MANAGEMENT_CONSOLE = "Management Console";
    String MANAGEMENT_MODEL = "Management Model";
    String MANAGEMENT_OPERATIONS = "Management Operations";
    String MANIFEST = "Manifest";
    String MAPPED_ROLE_MAPPER = "Mapped Role Mapper";
    String MAPPERS_DECODERS = "Mappers / Decoders";
    String MAPPING_MODULE = "Mapping Module";
    String PRIMARY = "Primary";
    String MAXIMUM_PERMISSIONS = "Maximum Permissions";
    String MB = "MB";
    String MECHANISM_CONFIGURATION = "Mechanism Configuration";
    String MECHANISM_CONFIGURATIONS = "Mechanism Configurations";
    String MECHANISM_REALM_CONFIGURATION = "Mechanism Realm Configuration";
    String MECHANISM_REALM_CONFIGURATIONS = "Mechanism Realm Configurations";
    String MEMORY = "Memory";
    String MESSAGE_DRIVEN_BEAN = "Message Driven Bean";
    String MESSAGING = "Messaging";
    String MESSAGING_REMOTE_ACTIVEMQ = "Remote ActiveMQ";
    String MICROPROFILE_CONFIG = "MicroProfile Config";
    String MICROPROFILE_HEALTH = "MicroProfile Health";
    String MICROPROFILE_METRICS = "MicroProfile Metrics";
    String MIME_MAPPING = "Mime Mapping";
    String MINIMUM_PERMISSIONS = "Minimum Permissions";
    String MISSING_DEPENDENCIES = "Missing Dependencies";
    String MISSING_TRANSITIVE_DEPENDENCIES = "Missing Transitive Dependencies";
    String MODCLUSTER = "Modcluster";
    String MODULE = "Module";

    String NEW_ITEM_TEMPLATE = "New Item Template";
    String NODE = "Node";
    String NON_BLOCKING = "Non Blocking";
    String NON_HEAP = "Non-heap";
    String NONE = "none";
    String NOT_AVAILABLE = "n/a";
    String NYI = "not yet implemented";

    String OFF_HEAP = "Off Heap";
    String OUTBOUND_CONNECTION = "Outbound Connection";
    String OTHER_SETTINGS = "Other Settings";
    String OUTBOUND_LOCAL = "Outbound Local";
    String OUTBOUND_REMOTE = "Outbound Remote";
    String OTP_CREDENTIAL_MAPPER = "OTP Credential Mapper";

    String PAGING_DIRECTORY = "Paging Directory";
    String PARTICIPANT = "Participant";
    String PARTICIPANTS = "Participants";
    String PARTITION_HANDLING = "Partition Handling";
    String PATCH = "Patch";
    String PATCHES = "Patches";
    String PATHS = "Paths";
    String PATTERN_FORMATTER = "Pattern Formatter";
    String PERIODIC_HANDLER = "Periodic Handler";
    String PERIODIC_SIZE_HANDLER = "Periodic Size Handler";
    String PERMISSIONS = "Permissions";
    String PERMISSION_MAPPINGS = "Permission Mappings";
    String POLICY = "Policy";
    String POOL = "Pool";
    String POOLED_CONNECTION_FACTORY = "Pooled Connection Factory";
    String PORTS = "Ports";
    String POST_HANDLER_CHAIN = "Post Handler Chain";
    String PRE_HANDLER_CHAIN = "Pre Handler Chain";
    String PRIMARY_OWNER = "primary owner";
    String PRINCIPAL_QUERY = "Principal Query";
    String PRODUCERS = "Producers";
    String PROFILE = "Profile";
    String PROFILES = "Profiles";
    String PROPERTIES_REALM = "Properties Realm";
    String PROTOCOL = "Protocol";
    String PROXY = "Proxy";

    String QUERY = "Query";
    String QUERY_CACHE = "Query Cache";

    String RANKED = "ranked";
    String REALM = "Realm";
    String RELAY = "Relay";
    String REMOTE_ACCEPTOR = "Remote Acceptor";
    String REMOTE_CACHE_CONTAINER = "Remote Cache Container";
    String REMOTE_CLUSTER = "Remote Cluster";
    String REMOTE_CONNECTOR = "Remote Connector";
    String REMOTE_CONNECTOR_SECURITY = "Remote Connector Security";
    String REMOTE_CONNECTOR_SECURITY_POLICY = "Remote Connector Security Policy";
    String REMOTE_OUTBOUND_CONNECTION = "Remote Outbound Connection";
    String REMOTE_SITE = "Remote Site";
    String REPLICATED_CACHE = "Replicated Cache";
    String REPLICATION_COLOCATED = "Replication Colocated";
    String REPLICATION_PRIMARY = "Replication Primary";
    String REPLICATION_SECONDARY = "Replication Secondary";
    String REQUESTS = "Requests";
    String RESOLVERS = "Resolvers";
    String RESOURCE = "Resource";
    String RESOURCE_ADAPTER = "Resource Adapter";
    String RESOURCE_ADAPTERS = "Resource Adapters";
    String RESOURCE_PATHS = "Resource Paths";
    String RESPONSE_HEADER = "Response Header";
    String REST_RESOURCE = "REST Resource";
    String ROOT_LOGGER = "Root Logger";
    String ROUTING = "Routing";
    String RUNTIME = "Runtime";

    String SASL_AUTHENTICATION_FACTORY = "SASL Authentication Factory";
    String SCATTERED_CACHE = "Scattered Cache";
    String SECRET_KEY_CREDENTIAL_STORE = "Secret Key Credential Store";
    String SECOND_LEVEL_CACHE = "Second Level Cache";
    String SECURE_MANAGEMENT = "Secure Management";
    String SECURITY = "Security";
    String SECURITY_DOMAIN = "Security Domain";
    String SECURITY_REALMS = "Security Realms";
    String SECURITY_SETTING = "Security Setting";
    String SERVER = "Server";
    String SERVER_GROUP = "Server Group";
    String SERVER_GROUPS = "Server Groups";
    String SERVLET = "Servlet";
    String SERVLET_CONTAINER = "Servlet Container";
    String SESSION_ID = "Session ID";
    String SESSIONS = "Sessions";
    String SHARED_STORE_COLOCATED = "Shared Store Colocated";
    String SHARED_STORE_PRIMARY = "Shared Store Primary";
    String SHARED_STORE_SECONDARY = "Shared Store Secondary";
    String SIMPLE_PERMISSION_MAPPER = "Simple Permission Mapper";
    String SINGLE_SIGN_ON = "Single Sign On";
    String SINGLETON_BEAN = "Singleton Bean";
    String SIZE_HANDLER = "Size Handler";
    String SHORT_RUNNING = "short running";
    String SECONDARY = "Secondary";
    String SMALLRYE = "Smallrye";
    String SOCKET_ACTION_HANDLER = "Socket ActionHandler";
    String SOCKET_BINDING = "Socket Binding";
    String SOCKET_BINDING_GROUP = "Socket Binding Group";
    String SOCKET_BINDING_GROUPS = "Socket Binding Groups";
    String SOCKET_BINDINGS = "Socket Bindings";
    String SOCKET_BROADCAST_GROUP = "Socket Broadcast Group";
    String SOCKET_DISCOVERY_GROUP = "Socket Discovery Group";
    String SSL = "SSL";
    String STANDALONE_SERVER = "Standalone Server";
    String STATE_TRANSFER = "State Transfer";
    String STORE_RESOURCE = "Store";
    String STORES = "Stores";
    String STRING_TABLE = "String Table";
    String STACK = "Stack";
    String STATUS = "Status";
    String STATEFUL_SESSION_BEAN = "Stateful Session Bean";
    String STATELESS_SESSION_BEAN = "Stateless Session Bean";
    String SUB_RESOURCE_LOCATORS = "Sub Resource Locators";
    String SUBDEPLOYMENTS = "Subdeployments";
    String SUBSYSTEM = "Subsystem";
    String SUBSYSTEMS = "Subsystems";
    String SYSLOG_ACTION_HANDLER = "Syslog ActionHandler";
    String SYSTEM_PROPERTIES = "System Properties";
    String SYSTEM_PROPERTY = "System Property";

    String TARGET = "Target";
    String TASKS = "Tasks";
    String THREAD_POOL = "Thread Pool";
    String THREAD_POOLS = "Thread Pools";
    String THREADS = "Threads";
    String TIMER = "Timer";
    String TOPOLOGY = "Topology";
    String TRANSPORT = "Transport";
    String TRANSACTION = "Transaction";
    String TRANSACTIONS = "Transactions";
    String TRUST_MANAGER = "Trust Manager";
    String TRUST_MODULE = "Trust Module";

    String UNDEFINED = "undefined";
    String UNKNOWN = "unknown";
    String UNMANAGED_DEPLOYMENT = "Unmanaged Deployment";
    String UNDERTOW = "Undertow";
    String UPDATE_MANAGER = "Update Manager";
    String UPDATES = "Updates";
    String URL = "URL";
    String USER_PASSWORD_MAPPER = "User Password Mapper";

    String VALUE = "Value";

    String WEB = "Web";
    String WEBSERVICES = "Webservices";
    String WEBSERVICES_CONFIGURATION = "Webservices Configuration";
    String WEBSOCKET = "Web Socket";
    String WEBSOCKETS = "Web Sockets";
    String WELCOME_FILE = "Welcome File";
    String WORKER = "Worker";
    String WRITE_BEHAVIOUR = "Write Behaviour";
    String WRITE_BEHIND = "Write Behind";
    String WRITE_THROUGH = "Write Through";

    String X509_CREDENTIAL_MAPPER = "X509 Credential Mapper";
    String XA_DATASOURCE = "XA Datasource";
    String XML_FORMATTER = "XML Formatter";
}
