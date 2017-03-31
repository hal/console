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

package org.jboss.hal.dmr;

/**
 * String constants frequently used in model descriptions and DMR operations.
 *
 * @author Brian Stansberry
 * @author Harald Pehl
 */
@SuppressWarnings({"DuplicateStringLiteralInspection", "SpellCheckingInspection"})
public interface ModelDescriptionConstants {

    // KEEP THESE IN ALPHABETICAL ORDER!
    String ACCESS_CONSTRAINTS = "access-constraints";
    String ACCESS_CONTROL = "access-control";
    String ACCESS_LOG = "access-log";
    String ACCESS_TYPE = "access-type";
    String ACCEPTOR = "acceptor";
    String ACL_MODULE = "acl-module";
    String ADD = "add";
    String ADDRESS = "address";
    String ADDRESS_SETTING = "address-setting";
    String ADMIN_OBJECTS = "admin-objects";
    String AJP_LISTENER = "ajp-listener";
    String ALLOWED = "allowed";
    String ALLOW_RESOURCE_SERVICE_RESTART = "allow-resource-service-restart";
    String ALTERNATIVES = "alternatives";
    String ANY = "any";
    String ARCHIVE = "archive";
    String ASYNC_OPERATIONS = "async-operations";
    String ATTRIBUTES = "attributes";
    String ATTRIBUTE_GROUP = "attribute-group";
    String ATTRIBUTES_ONLY = "attributes-only";
    String AUTO_START = "auto-start";

    String BACKUP_FOR = "backup-for";
    String BACKUP = "backup";
    String BACKUPS = "backups";
    String BASE_ROLE = "base-role";
    String BATCH_JBERET = "batch-jberet";
    String BATCH_SIZE = "batch-size";
    String BEHIND = "behind";
    String BINARY = "binary";
    String BINARY_JDBC = "binary-jdbc";
    String BLOCKING = "blocking";
    String BOOT_TIME = "boot-time";
    String BRIDGE = "bridge";
    String BROADCAST_GROUP = "broadcast-group";
    String BROWSE_CONTENT = "browse-content";
    String BUFFER_CACHE = "buffer-cache";
    String BUFFER_POOL = "buffer-pool";

    String CACHE_CONTAINER = "cache-container";
    String CACHE_TYPE = "cache-type";
    String CAPABILITY_REFERENCE = "capability-reference";
    String CHANNEL = "channel";
    String CHILD_TYPE = "child-type";
    String CHILDREN = "children";
    String CLASS_NAME = "class-name";
    String CLIENT_CONFIG = "client-config";
    String CLIENT_MAPPINGS = "client-mappings";
    String CLONE = "clone";
    String CLUSTER_CONNECTION = "cluster-connection";
    String COMBINED_DESCRIPTIONS = "combined-descriptions";
    String COMPONENT = "component";
    String COMPOSITE = "composite";
    String CONFIG_PROPERTIES = "config-properties";
    String CONFIGURATION = "configuration";
    String CONNECTION_DEFINITIONS = "connection-definitions";
    String CONNECTION_FACTORY = "connection-factory";
    String CONNECTION_URL = "connection-url";
    String CONNECTOR = "connector";
    String CONNECTORS = "connectors";
    String CONNECTOR_NAME = "connector-name";
    String CONNECTOR_SERVICE = "connector-service";
    String CONTENT = "content";
    String CONTEXT_ROOT = "context-root";
    String CONTEXT_SERVICE = "context-service";
    String COPY = "copy";
    String CORE_SERVICE = "core-service";
    String CRAWLER_SESSION_MANAGEMENT = "crawler-session-management";
    String CUSTOM = "custom";

    String DATA_COLUMN = "data-column";
    String DATA_SOURCE = "data-source";
    String DATASOURCES = "datasources";
    String DEFAULT = "default";
    String DEFAULT_BINDINGS = "default-bindings";
    String DEFAULT_CACHE = "default-cache";
    String DEFAULT_HOST = "default-host";
    String DEFAULT_INTERFACE = "default-interface";
    String DEPENDENT_ADDRESS = "dependent-address";
    String DEPLOY = "deploy";
    String DEPLOYMENT = "deployment";
    String DEPLOYMENT_NAME = "deployment-name";
    String DEPLOYMENT_SCANNER = "deployment-scanner";
    String DEPRECATED = "deprecated";
    String DESCRIPTION = "description";
    String DESTINATION_ADDRESS = "destination-address";
    String DESTINATION_PORT = "destination-port";
    String DISABLED = "disabled";
    String DISABLED_TIME = "disabled-time";
    String DISCOVERY_GROUP = "discovery-group";
    String DIVERT = "divert";
    String DRIVER_CLASS = "driver-class";
    String DRIVER_CLASS_NAME = "driver-class-name";
    String DRIVER_DATASOURCE_CLASS_NAME = "driver-datasource-class-name";
    String DRIVER_MODULE_NAME = "driver-module-name";
    String DRIVER_NAME = "driver-name";
    String DRIVER_MAJOR_VERSION = "driver-major-version";
    String DRIVER_MINOR_VERSION = "driver-minor-version";
    String DRIVER_VERSION = "driver-version";
    String DRIVER_XA_DATASOURCE_CLASS_NAME = "driver-xa-datasource-class-name";

    String EE = "ee";
    String EJB3 = "ejb3";
    String ELYTRON = "elytron";
    String ENDPOINT_CONFIG = "endpoint-config";
    String ENABLED = "enabled";
    String ENABLED_TIME = "enabled-time";
    String EVICTION = "eviction";
    String EXECUTE = "execute";
    String EXCEPTIONS = "exceptions";
    String EXCLUDE = "exclude";
    String EXPIRATION = "expiration";
    String EXPLODE = "explode";
    String EXPLODED = "exploded";
    String EXPRESSION = "expression";
    String EXPRESSIONS_ALLOWED = "expressions-allowed";

    String FAILED = "failed";
    String FAILED_OPERATION = "failed-operation";
    String FAILED_SERVICES = "failed-services";
    String FAILURE_DESCRIPTION = "failure-description";
    String FETCH_SIZE = "fetch-size";
    String FILE = "file";
    String FILTER_REF = "filter-ref";
    String FIXED_PORT = "fixed-port";
    String FLUSH_GRACEFULLY_CONNECTION_IN_POOL = "flush-gracefully-connection-in-pool";
    String FLUSH_IDLE_CONNECTION_IN_POOL = "flush-idle-connection-in-pool";
    String FLUSH_INVALID_CONNECTION_IN_POOL = "flush-invalid-connection-in-pool";
    String FLUSH_ALL_CONNECTION_IN_POOL = "flush-all-connection-in-pool";
    String FORK = "fork";
    String FROM = "from";
    String FULL_REPLACE_DEPLOYMENT = "full-replace-deployment";

    String GLOBAL_MODULES = "global-modules";
    String GROUP = "group";
    String GROUPING_HANDLER = "grouping-handler";

    String HA_POLICY = "ha-policy";
    String HANDLER = "handler";
    String HANDLERS = "handlers";
    String HASH = "hash";
    String HEADER_NAME = "header-name";
    String HOST = "host";
    String HOSTS = "hosts";
    String HOST_SCOPED_ROLE = "host-scoped-role";
    String HOST_STATE = "host-state";
    String HTTP = "http";
    String HTTP_ACCEPTOR = "http-acceptor";
    String HTTP_CONNECTOR = "http-connector";
    String HTTP_INVOKER = "http-invoker";
    String HTTP_LISTENER = "http-listener";
    String HTTPS = "https";
    String HTTPS_LISTENER = "https-listener";

    String ID_COLUMN = "id-column";
    String IIOP_OPENJDK = "iiop-openjdk";
    String IMAP = "imap";
    String IN_VM_ACCEPTOR = "in-vm-acceptor";
    String IN_VM_CONNECTOR = "in-vm-connector";
    String INCLUDE = "include";
    String INCLUDES = "includes";
    String INCLUDE_ALIASES = "include-aliases";
    String INCLUDE_ALL = "include-all";
    String INCLUDE_DEFAULTS = "include-defaults";
    String INCLUDE_RUNTIME = "include-runtime";
    String INCLUDE_SINGLETONS = "include-singletons";
    String INDEX = "index";
    String INET_ADDRESS = "inet-address";
    String INFINISPAN = "infinispan";
    String INTERFACE = "interface";
    String IO = "io";

    String JCA = "jca";
    String JDBC = "jdbc";
    String JDBC_DRIVER = "jdbc-driver";
    String JGROUPS = "jgroups";
    String JMS_BRIDGE = "jms-bridge";
    String JMS_QUEUE = "jms-queue";
    String JMS_TOPIC = "jms-topic";
    String JMX = "jmx";
    String JNDI_NAME = "jndi-name";
    String JPA = "jpa";
    String JSP = "jsp";
    String JVM = "jvm";

    String KILL = "kill";

    String LAUNCH_TYPE = "launch-type";
    String LEVEL = "level";
    String LINES = "lines";
    String LIST_ADD = "list-add";
    String LIST_REMOVE = "list-remove";
    String LISTENER = "listener";
    String LIVE_ONLY = "live-only";
    String LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING = "local-destination-outbound-socket-binding";
    String LOCATION = "location";
    String LOCKING = "locking";
    String LOGGING = "logging";
    String LOGGING_PROFILE = "logging-profile";
    String LOGIN_MODULE = "login-module";

    String MAIL = "mail";
    String MAIL_SESSION = "mail-session";
    String MANAGED = "managed";
    String MANAGED_EXECUTOR_SERVICE = "managed-executor-service";
    String MANAGED_SCHEDULED_EXECUTOR_SERVICE = "managed-scheduled-executor-service";
    String MANAGED_THREAD_FACTORY = "managed-thread-factory";
    String MANAGEMENT = "management";
    String MANAGEMENT_HTTP = "management-http";
    String MANAGEMENT_HTTPS = "management-https";
    String MANAGEMENT_MAJOR_VERSION = "management-major-version";
    String MANAGEMENT_MICRO_VERSION = "management-micro-version";
    String MANAGEMENT_MINOR_VERSION = "management-minor-version";
    String MAPPING_MODULE = "mapping-module";
    String MASTER = "master";
    String MAX = "max";
    String MAX_THREADS = "max-threads";
    String MESSAGING_ACTIVEMQ = "messaging-activemq";
    String METRIC = "metric";
    String MIME_MAPPING = "mime-mapping";
    String MIN = "min";
    String MISSING_TRANSITIVE_DEPENDENCY_PROBLEMS = "missing-transitive-dependency-problems";
    String MIXED_JDBC = "mixed-jdbc";
    String MODE = "mode";
    String MODULE = "module";
    String MODCLUSTER = "modcluster";
    String MODEL_DESCRIPTION = "model-description";
    String MULTICAST_ADDRESS = "multicast-address";
    String MULTICAST_PORT = "multicast-port";

    String NAME = "name";
    String NILLABLE = "nillable";
    String NONE = "none";
    String NOT = "not";

    String OP = "operation";
    String OPERATION = "operation";
    String OPERATIONS = "operations";
    String OPERATION_HEADERS = "operation-headers";
    String OUTBOUND_SOCKET_BINDING_REF = "outbound-socket-binding-ref";
    String OUTCOME = "outcome";

    String PARTITION_HANDLING = "partition-handling";
    String PASSWORD = "password";
    String PATH = "path";
    String PATTERN = "pattern";
    String PERSISTENCE = "persistence";
    String PERSISTENT_SESSIONS = "persistent-sessions";
    String POLICY = "policy";
    String POLICY_MODULE = "policy-module";
    String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
    String POP3 = "pop3";
    String PORT = "port";
    String PORT_OFFSET = "port-offset";
    String POSSIBLE_CAUSES = "possible-causes";
    String POST_HANDLER_CHAIN = "post-handler-chain";
    String PRE_HANDLER_CHAIN = "pre-handler-chain";
    String PREFIX = "prefix";
    String PRIORITY = "priority";
    String PROCESS_STATE = "process-state";
    String PRODUCT_NAME = "product-name";
    String PRODUCT_VERSION = "product-version";
    String PROFILE = "profile";
    String PROFILE_NAME = "profile-name";
    String PROPERTY = "property";
    String PROTOCOL = "protocol";
    String PROVIDER = "provider";
    String PROVIDER_MODULE = "provider-module";
    String PROXIES = "proxies";

    String QUERY = "query";
    String QUEUE = "queue";
    String QUEUE_LENGTH = "queue-length";
    String QUEUE_NAME = "queue-name";

    String READ = "read";
    String READ_ATTRIBUTE_OPERATION = "read-attribute";
    String READ_BOOT_ERRORS = "read-boot-errors";
    String READ_CHILDREN_NAMES_OPERATION = "read-children-names";
    String READ_CHILDREN_TYPES_OPERATION = "read-children-types";
    String READ_CHILDREN_RESOURCES_OPERATION = "read-children-resources";
    String READ_CONTENT = "read-content";
    String READ_LOG_FILE = "read-log-file";
    String READ_ONLY = "read-only";
    String READ_RESOURCE_DESCRIPTION_OPERATION = "read-resource-description";
    String READ_RESOURCE_OPERATION = "read-resource";
    String READ_WRITE = "read-write";
    String REALM = "realm";
    String REASON = "reason";
    String RELATIVE_TO = "relative-to";
    String REMOTE = "remote";
    String REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING = "remote-destination-outbound-socket-binding";
    String RELAY = "relay";
    String REMOTE_ACCEPTOR = "remote-acceptor";
    String REMOTE_COMMAND = "remote-command";
    String REMOTE_CONNECTOR = "remote-connector";
    String REMOTING = "remoting";
    String REMOVE = "remove";
    String REQUEST_PROPERTIES = "request-properties";
    String RECURSIVE = "recursive";
    String RECURSIVE_DEPTH = "recursive-depth";
    String RELEASE_CODENAME = "release-codename";
    String RELEASE_VERSION = "release-version";
    String RELOAD = "reload";
    String RELOAD_HOST = "reload-host";
    String RELOAD_REQUIRED = "reload-required";
    String RELOAD_SERVERS = "reload-servers";
    String REPLICATION_COLOCATED = "replication-colocated";
    String REPLICATION_MASTER = "replication-master";
    String REPLICATION_SLAVE = "replication-slave";
    String REPLY_PROPERTIES = "reply-properties";
    String REQUEST_CONTROLLER = "request-controller";
    String REQUIRED = "required";
    String REQUIRES = "requires";
    String RESPONSE = "response";
    String RESOLVE_EXPRESSION = "resolve-expression";
    String RESOLVE_EXPRESSION_ON_DOMAIN = "resolve-expression-on-domain";
    String RESOURCE_ADAPTERS = "resource-adapters";
    String RESOURCE_ADAPTER = "resource-adapter";
    String RESPONSE_HEADERS = "response-headers";
    String RESTART = "restart";
    String RESTART_REQUIRED = "restart-required";
    String RESTART_SERVERS = "restart-servers";
    String RESULT = "result";
    String RESUME = "resume";
    String RESUME_SERVERS = "resume-servers";
    String ROLE = "role";
    String ROLE_MAPPING = "role-mapping";
    String ROLES = "roles";
    String RUNNING_MODE = "running-mode";
    String RUNTIME = "runtime";
    String RUNTIME_NAME = "runtime-name";

    //String SECURITY = "security-elytron";
    String SECURITY = "security";
    String SECURITY_DOMAIN = "security-domain";
    String SECURITY_SETTING = "security-setting";
    String SELECT = "select";
    String SELECTOR = "selector";
    String SENSITIVE = "sensitive";
    String SERVER = "server";
    String SERVER_CONFIG = "server-config";
    String SERVER_GROUP = "server-group";
    String SERVER_GROUP_SCOPED_ROLE = "server-group-scoped-role";
    String SERVER_GROUPS = "server-groups";
    String SERVER_STATE = "server-state";
    String SERVER_TYPE = "server-type";
    String SERVLET_CONTAINER = "servlet-container";
    String SERVICE = "service";
    String SERVICES_MISSING_DEPENDENCIES = "services-missing-dependencies";
    String SERVICES_MISSING_TRANSITIVE_DEPENDENCIES = "services-missing-transitive-dependencies";
    String SESSION_COOKIE = "session-cookie";
    String SHARED_STORE_COLOCATED = "shared-store-colocated";
    String SHARED_STORE_MASTER = "shared-store-master";
    String SHARED_STORE_SLAVE = "shared-store-slave";
    String SINCE = "since";
    String SOCKET_BINDING = "socket-binding";
    String SOCKET_BINDING_REF = "socket-binding-ref";
    String SOCKET_BINDING_DEFAULT_INTERFACE = "socket-binding-default-interface";
    String SOCKET_BINDING_GROUP = "socket-binding-group";
    String SOCKET_BINDING_PORT_OFFSET = "socket-binding-port-offset";
    String SOURCE_NETWORK = "source-network";
    String SHUTDOWN = "shutdown";
    String SINGLE_SIGN_ON = "single-sign-on";
    String SLAVE = "slave";
    String SMTP = "smtp";
    String STACK = "stack";
    String STANDALONE = "standalone";
    String STANDARD_ROLE_NAMES = "standard-role-names";
    String START = "start";
    String START_SERVERS = "start-servers";
    String STATE_TRANSFER = "state-transfer";
    String STATIC_CONNECTORS = "static-connectors";
    String STATUS = "status";
    String STATISTICS_ENABLED = "statistics-enabled";
    String STEPS = "steps";
    String STORAGE = "storage";
    String STOP = "stop";
    String STOP_SERVERS = "stop-servers";
    String STORE = "store";
    String STREAM = "stream";
    String STRING = "string";
    String SUBDEPLOYMENT = "subdeployment";
    String SUBSYSTEM = "subsystem";
    String SUCCESS = "success";
    String SUGGEST_CAPABILITIES = "suggest-capabilities";
    String SUSPEND = "suspend";
    String SUSPEND_SERVERS = "suspend-servers";
    String SUSPEND_STATE = "suspend-state";
    String SYSTEM_PROPERTY = "system-property";

    String TABLE = "table";
    String TAIL = "tail";
    String TEST_CONNECTION_IN_POOL = "test-connection-in-pool";
    String THREAD_FACTORY = "thread-factory";
    String THREAD_POOL = "thread-pool";
    String THROUGH = "through";
    String TIMEOUT = "timeout";
    String TIMESTAMP_COLUMN = "timestamp-column";
    String TO = "to";
    String TO_PROFILE = "to-profile";
    String TRANSACTION_SUPPORT = "transaction-support";
    String TRANSACTION = "transaction";
    String TRANSACTIONS = "transactions";
    String TRANSPORT = "transport";
    String TRIM_DESCRIPTIONS = "trim-descriptions";
    String TRUST_MODULE = "trust-module";
    String TYPE = "type";

    String UNIT = "unit";
    String URL = "url";
    String USER_NAME = "user-name";
    String USERNAME = "username";

    String VALUE = "value";
    String VALUE_TYPE = "value-type";
    String VERBOSE = "verbose";

    String WEBSERVICES = "webservices";
    String WEBSOCKETS = "websockets";
    String WELCOME_FILE = "welcome-file";
    String WHERE = "where";
    String WHOAMI = "whoami";
    String WM_SECURITY_MAPPING_GROUPS = "wm-security-mapping-groups";
    String WM_SECURITY_MAPPING_USERS = "wm-security-mapping-users";
    String WORKER = "worker";
    String WORKMANAGER = "workmanager";
    String WRITE = "write";
    String WRITE_ATTRIBUTE_OPERATION = "write-attribute";

    String UNDEFINE_ATTRIBUTE_OPERATION = "undefine-attribute";
    String UNDEFINED = "undefined";
    String UNDEPLOY = "undeploy";
    String UNDERTOW = "undertow";

    String XA_DATA_SOURCE = "xa-data-source";
}

