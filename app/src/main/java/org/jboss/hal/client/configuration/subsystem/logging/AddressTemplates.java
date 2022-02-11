/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.logging;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_EXPRESSION;

interface AddressTemplates {

    String LOGGING_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=logging";
    String ROOT_LOGGER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/root-logger=ROOT";
    String LOGGER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/logger=*";

    String ASYNC_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/async-handler=*";
    String CONSOLE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/console-handler=*";
    String CUSTOM_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/custom-handler=*";
    String FILE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/file-handler=*";
    String PERIODIC_ROTATING_FILE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/periodic-rotating-file-handler=*";
    String PERIODIC_SIZE_ROTATING_FILE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/periodic-size-rotating-file-handler=*";
    String SIZE_ROTATING_FILE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/size-rotating-file-handler=*";
    String SOCKET_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/socket-handler=*";
    String SYSLOG_HANDLER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/syslog-handler=*";

    String CUSTOM_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/custom-formatter=*";
    String PATTERN_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/pattern-formatter=*";
    String JSON_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/json-formatter=*";
    String XML_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/xml-formatter=*";

    String LOGGING_PROFILE_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/logging-profile=*";
    String SELECTED_LOGGING_PROFILE_ADDRESS = LOGGING_SUBSYSTEM_ADDRESS + "/logging-profile=" + SELECTION_EXPRESSION;

    AddressTemplate LOGGING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(LOGGING_SUBSYSTEM_ADDRESS);
    AddressTemplate ROOT_LOGGER_TEMPLATE = AddressTemplate.of(ROOT_LOGGER_ADDRESS);
    AddressTemplate LOGGER_TEMPLATE = AddressTemplate.of(LOGGER_ADDRESS);

    AddressTemplate ASYNC_HANDLER_TEMPLATE = AddressTemplate.of(ASYNC_HANDLER_ADDRESS);
    AddressTemplate CONSOLE_HANDLER_TEMPLATE = AddressTemplate.of(CONSOLE_HANDLER_ADDRESS);
    AddressTemplate CUSTOM_HANDLER_TEMPLATE = AddressTemplate.of(CUSTOM_HANDLER_ADDRESS);
    AddressTemplate FILE_HANDLER_TEMPLATE = AddressTemplate.of(FILE_HANDLER_ADDRESS);
    AddressTemplate PERIODIC_ROTATING_FILE_HANDLER_TEMPLATE = AddressTemplate
            .of(PERIODIC_ROTATING_FILE_HANDLER_ADDRESS);
    AddressTemplate PERIODIC_SIZE_ROTATING_FILE_HANDLER_TEMPLATE = AddressTemplate
            .of(PERIODIC_SIZE_ROTATING_FILE_HANDLER_ADDRESS);
    AddressTemplate SIZE_ROTATING_FILE_HANDLER_TEMPLATE = AddressTemplate.of(SIZE_ROTATING_FILE_HANDLER_ADDRESS);
    AddressTemplate SOCKET_HANDLER_TEMPLATE = AddressTemplate.of(SOCKET_HANDLER_ADDRESS);
    AddressTemplate SYSLOG_HANDLER_TEMPLATE = AddressTemplate.of(SYSLOG_HANDLER_ADDRESS);

    AddressTemplate CUSTOM_FORMATTER_TEMPLATE = AddressTemplate.of(CUSTOM_FORMATTER_ADDRESS);
    AddressTemplate PATTERN_FORMATTER_TEMPLATE = AddressTemplate.of(PATTERN_FORMATTER_ADDRESS);
    AddressTemplate JSON_FORMATTER_TEMPLATE = AddressTemplate.of(JSON_FORMATTER_ADDRESS);
    AddressTemplate XML_FORMATTER_TEMPLATE = AddressTemplate.of(XML_FORMATTER_ADDRESS);

    AddressTemplate LOGGING_PROFILE_TEMPLATE = AddressTemplate.of(LOGGING_PROFILE_ADDRESS);
    AddressTemplate SELECTED_LOGGING_PROFILE_TEMPLATE = AddressTemplate.of(SELECTED_LOGGING_PROFILE_ADDRESS);
}
