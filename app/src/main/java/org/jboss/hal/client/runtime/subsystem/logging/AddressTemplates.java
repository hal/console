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
package org.jboss.hal.client.runtime.subsystem.logging;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Harald Pehl
 */
public interface AddressTemplates {

    String LOGGING_SUBSYSTEM_ADDRESS = "/{selected.host}/{selected.server}/subsystem=logging";
    AddressTemplate LOGGING_SUBSYSTEM_TEMPLATE = AddressTemplate.of(LOGGING_SUBSYSTEM_ADDRESS);

    String LOG_FILE_ADDRESS = "/{selected.host}/{selected.server}/subsystem=logging/log-file=*";
    AddressTemplate LOG_FILE_TEMPLATE = AddressTemplate.of(LOG_FILE_ADDRESS);
}
