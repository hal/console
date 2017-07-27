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
package org.jboss.hal.client.configuration.subsystem.batch;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String BATCH_SUBSYSTEM_ADDRESS = "/{selected.profile}/subsystem=batch-jberet";
    String IN_MEMORY_JOB_REPO_ADDRESS = BATCH_SUBSYSTEM_ADDRESS + "/in-memory-job-repository=*";
    String JDBC_JOB_REPO_ADDRESS = BATCH_SUBSYSTEM_ADDRESS + "/jdbc-job-repository=*";
    String THREAD_FACTORY_ADDRESS = BATCH_SUBSYSTEM_ADDRESS + "/thread-factory=*";
    String THREAD_POOL_ADDRESS = BATCH_SUBSYSTEM_ADDRESS + "/thread-pool=*";

    AddressTemplate BATCH_SUBSYSTEM_TEMPLATE = AddressTemplate.of(BATCH_SUBSYSTEM_ADDRESS);
    AddressTemplate IN_MEMORY_JOB_REPO_TEMPLATE = AddressTemplate.of(IN_MEMORY_JOB_REPO_ADDRESS);
    AddressTemplate JDBC_JOB_REPO_TEMPLATE = AddressTemplate.of(JDBC_JOB_REPO_ADDRESS);
    AddressTemplate THREAD_FACTORY_TEMPLATE = AddressTemplate.of(THREAD_FACTORY_ADDRESS);
    AddressTemplate THREAD_POOL_TEMPLATE = AddressTemplate.of(THREAD_POOL_ADDRESS);
}
