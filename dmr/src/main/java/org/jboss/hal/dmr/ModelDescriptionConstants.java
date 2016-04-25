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
public interface ModelDescriptionConstants {

    // KEEP THESE IN ALPHABETICAL ORDER!
    String ACCESS_CONTROL = "access-control";
    String ACCESS_TYPE = "access-type";
    String ADD = "add";
    String ADD_OPERATION = "add-operation";
    String ADDRESS = "address";
    String ALLOW_RESOURCE_SERVICE_RESTART = "allow-resource-service-restart";
    String ALLOWED = "allowed";
    String ANY = "any";
    String ATTRIBUTES = "attributes";
    String ATTRIBUTES_ONLY = "attributes-only";

    String BATCH_JBERET = "batch-jberet";
    String BYTES = "bytes";

    String CAPABILITIES = "capabilities";
    String CAPABILITY_REFERENCE = "capability-reference";
    String CHILD_TYPE = "child-type";
    String CHILDREN = "children";
    String COMBINED_DESCRIPTIONS = "combined-descriptions";
    String COMPOSITE = "composite";
    String CONNECTION_URL = "connection-url";
    String CONFIGURATION = "configuration";
    String CRITERIA = "criteria";

    String DATA_SOURCE = "data-source";
    String DATASOURCES = "datasources";
    String DEFAULT = "default";
    String DEFAULT_BINDINGS = "default-bindings";
    String DEPLOYMENT = "deployment";
    String DEPLOYMENT_NAME = "deployment-name";
    String DEPLOYMENT_SCANNER = "deployment-scanner";
    String DESCRIBE = "describe";
    String DESCRIPTION = "description";
    String DISABLED = "disabled";
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
    String ENABLED = "enabled";
    String EXCEPTIONS = "exceptions";
    String EXECUTE = "execute";
    String EXPRESSIONS_ALLOWED = "expressions-allowed";
    String EXTENSION = "extension";

    String FAILED = "failed";
    String FAILURE_DESCRIPTION = "failure-description";

    String GLOBAL_MODULES = "global-modules";
    String GRACEFUL_SHUTDOWN_TIMEOUT = "graceful-shutdown-timeout";
    String GROUP = "group";

    String HASH = "hash";
    String HEAD_COMMENT_ALLOWED = "head-comment-allowed";
    String HOST = "host";
    String HOST_FAILURE_DESCRIPTION = "host-failure-description";
    String HOST_FAILURE_DESCRIPTIONS = "host-failure-descriptions";

    String IGNORED = "ignored";
    String IIOP_OPENJDK = "iiop-openjdk";
    String INCLUDE = "include";
    String INCLUDES = "includes";
    String INCLUDE_ALIASES = "include-aliases";
    String INCLUDE_DEFAULTS = "include-defaults";
    String INCLUDE_RUNTIME = "include-runtime";
    String INCLUDE_SINGLETONS = "include-singletons";
    String INFINISPAN = "infinispan";
    String INHERITED = "inherited";
    String INPUT_STREAM_INDEX = "input-stream-index";
    String INTERFACE = "interface";
    String IO = "io";

    String JCA = "jca";
    String JDBC_DRIVER = "jdbc-driver";
    String JMX = "jmx";
    String JNDI_NAME = "jndi-name";
    String JPA = "jpa";

    String LIST_ADD = "list-add";
    String LIST_REMOVE = "list-remove";
    String LOCAL = "local";
    String LOCALE = "locale";
    String LOGGING = "logging";

    String MAIL = "mail";
    String MAIL_SESSION = "mail-session";
    String MASK = "mask";
    String MAX = "max";
    String MAX_LENGTH = "max-length";
    String MAX_OCCURS = "max-occurs";
    String MAX_THREADS = "max-threads";
    String MESSAGING_ACTIVEMQ = "messaging-activemq";
    String MESSAGING_SERVER = "messaging-server";
    String METRIC = "metric";
    String MIN = "min";
    String MIN_LENGTH = "min-length";
    String MIN_OCCURS = "min-occurs";
    String MIN_VALUE = "min-value";
    String MODEL_DESCRIPTION = "model-description";
    String MODULE_SLOT = "module-slot";

    String NAME = "name";
    String NAMESPACE = "namespace";
    String NAMESPACES = "namespaces";
    String NILLABLE = "nillable";
    String NOT = "not";
    String NOT_SET = "not-set";

    String OP = "operation";
    String OPERATIONS = "operations";
    String OPERATION_HEADERS = "operation-headers";
    String OUTCOME = "outcome";

    String PASSWORD = "password";
    String PORT = "port";
    String PROFILE = "profile";
    String PROXIES = "proxies";

    String QUERY = "query";

    String READ = "read";
    String READ_ATTRIBUTE_OPERATION = "read-attribute";
    String READ_CHILDREN_NAMES_OPERATION = "read-children-names";
    String READ_CHILDREN_TYPES_OPERATION = "read-children-types";
    String READ_CHILDREN_RESOURCES_OPERATION = "read-children-resources";
    String READ_OPERATION_DESCRIPTION_OPERATION = "read-operation-description";
    String READ_OPERATION_NAMES_OPERATION = "read-operation-names";
    String READ_ONLY = "read-only";
    String READ_RESOURCE_DESCRIPTION_OPERATION = "read-resource-description";
    String READ_RESOURCE_METRICS = "read-resource-metrics";
    String READ_RESOURCE_OPERATION = "read-resource";
    String READ_WRITE = "read-write";
    String RELATIVE_TO = "relative-to";
    String REMOTING = "remoting";
    String REMOVE = "remove";
    String REQUEST_PROPERTIES = "request-properties";
    String RECURSIVE = "recursive";
    String RECURSIVE_DEPTH = "recursive-depth";
    String REPLY_PROPERTIES = "reply-properties";
    String REQUEST_CONTROLLER = "request-controller";
    String REQUIRED = "required";
    String REQUIRES = "requires";
    String RESPONSE = "response";
    String RESOURCE_ADAPTER = "resource-adapter";
    String RESOURCE_ADAPTERS = "resource-adapters";
    String RESULT = "result";
    String ROLES = "HEADER_ROLES";
    String RUNTIME = "runtime";
    String RUNTIME_NAME = "runtime-name";

    String SECURITY = "security";
    String SECURITY_DOMAIN = "security-domain";
    String SELECT = "select";
    String SERVER = "server";
    String SERVERS = "servers";
    String SERVER_CONFIG = "server-config";
    String SERVER_GROUP = "server-group";
    String SERVER_GROUPS = "server-groups";
    String SERVER_OPERATIONS = "server-operations";
    String SERVICE = "service";
    String SOCKET_BINDING = "socket-binding";
    String SOCKET_BINDING_GROUP = "socket-binding-group";
    String SHUTDOWN = "shutdown";
    String STANDALONE = "standalone";
    String STATISTICS_ENABLED = "statistics-enabled";
    String START = "start";
    String STEPS = "steps";
    String STORAGE = "storage";
    String SUBDEPLOYMENT = "subdeployment";
    String SUBSYSTEM = "subsystem";
    String SUCCESS = "success";

    String TRANSACTIONS = "transactions";
    String TRIM_DESCRIPTIONS = "trim-descriptions";
    String TYPE = "type";

    String UNIT = "unit";
    String URL = "url";

    String VALUE = "value";
    String VALUE_TYPE = "value-type";

    String WEBSERVICES = "webservices";
    String WHERE = "where";
    String WHOAMI = "whoami";
    String WRITE = "write";
    String WRITE_ATTRIBUTE_OPERATION = "write-attribute";

    String UNDEFINE_ATTRIBUTE_OPERATION = "undefine-attribute";
    String UNDEFINED = "undefined";
    String UNDERTOW = "undertow";

    String XA_DATA_SOURCE = "xa-data-source";
}

