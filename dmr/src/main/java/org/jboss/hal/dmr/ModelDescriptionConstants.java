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
 * String constants frequently used in model descriptions.
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
    String BYTES = "bytes";
    String CHILD_TYPE = "child-type";
    String CHILDREN = "children";
    String COMBINED_DESCRIPTIONS = "combined-descriptions";
    String COMPOSITE = "composite";
    String CRITERIA = "criteria";
    String DATA_SOURCE = "data-source";
    String DEFAULT = "default";
    String DESCRIBE = "describe";
    String DESCRIPTION = "description";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    String DISABLED = "disabled";
    String ENABLED = "enabled";
    String EXCEPTIONS = "exceptions";
    String EXECUTE = "execute";
    String EXPRESSION_ALLOWED = "expression-allowed";
    String EXTENSION = "extension";
    String FAILED = "failed";
    String FAILURE_DESCRIPTION = "failure-description";
    String GRACEFUL_SHUTDOWN_TIMEOUT = "graceful-shutdown-timeout";
    String GROUP = "group";
    String HASH = "hash";
    String HEAD_COMMENT_ALLOWED = "head-comment-allowed";
    String HOST = "host";
    String HOST_FAILURE_DESCRIPTION = "host-failure-description";
    String HOST_FAILURE_DESCRIPTIONS = "host-failure-descriptions";
    String IGNORED = "ignored";
    String INCLUDE = "include";
    String INCLUDES = "includes";
    String INCLUDE_ALIASES = "include-aliases";
    String INCLUDE_RUNTIME = "include-runtime";
    String INHERITED = "inherited";
    String INPUT_STREAM_INDEX = "input-stream-index";
    String INTERFACE = "interface";
    String JNDI_NAME = "jndi-name";
    String LOCAL = "local";
    String LOCALE = "locale";
    String MASK = "mask";
    String MAX = "max";
    String MAX_LENGTH = "max-length";
    String MAX_OCCURS = "max-occurs";
    String MAX_THREADS = "max-threads";
    String MIN = "min";
    String MIN_LENGTH = "min-length";
    String MIN_OCCURS = "min-occurs";
    String MIN_VALUE = "min-value";
    String MODEL_DESCRIPTION = "model-description";
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
    String PORT = "port";
    String PROFILE = "profile";
    String PROXIES = "proxies";
    String READ = "read";
    String READ_ATTRIBUTE_OPERATION = "read-attribute";
    String READ_CHILDREN_NAMES_OPERATION = "read-children-names";
    String READ_CHILDREN_TYPES_OPERATION = "read-children-types";
    String READ_CHILDREN_RESOURCES_OPERATION = "read-children-resources";
    String READ_OPERATION_DESCRIPTION_OPERATION = "read-operation-description";
    String READ_OPERATION_NAMES_OPERATION = "read-operation-names";
    String READ_RESOURCE_DESCRIPTION_OPERATION = "read-resource-description";
    String READ_RESOURCE_METRICS = "read-resource-metrics";
    String READ_RESOURCE_OPERATION = "read-resource";
    String RELATIVE_TO = "relative-to";
    String REMOVE = "remove";
    String REQUEST_PROPERTIES = "request-properties";
    String RECURSIVE = "recursive";
    String RECURSIVE_DEPTH = "recursive-depth";
    String REQUIRED = "required";
    String REQUIRES = "requires";
    String RESPONSE = "response";
    String RESULT = "result";
    String ROLES = "HEADER_ROLES";
    String RUNTIME_NAME = "runtime-name";
    String SCHEME = "scheme";
    String SERVER = "server";
    String SERVERS = "servers";
    String SERVER_CONFIG = "server-config";
    String SERVER_GROUP = "server-group";
    String SERVER_GROUPS = "server-groups";
    String SERVER_OPERATIONS = "server-operations";
    String SOCKET_BINDING = "socket-binding";
    String SHUTDOWN = "shutdown";
    String STANDALONE = "standalone";
    String START = "start";
    String STEPS = "steps";
    String STORAGE = "storage";
    String SUBDEPLOYMENT = "subdeployment";
    String SUBSYSTEM = "subsystem";
    String SUCCESS = "success";
    String TRIM_DESCRIPTIONS = "trim-descriptions";
    String TYPE = "type";
    String UNIT = "unit";
    String URL = "url";
    String VALUE = "value";
    String VALUE_TYPE = "value-type";
    String WHOAMI = "whoami";
    String WRITE = "write";
    String WRITE_ATTRIBUTE_OPERATION = "write-attribute";
    String UNDEFINE_ATTRIBUTE_OPERATION = "undefine-attribute";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    String UNDEFINED = "undefined";
    String XA_DATA_SOURCE = "xa-data-source";
}

