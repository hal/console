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
package org.jboss.hal.client.runtime.subsystem.batch;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    String BATCH_SUBSYSTEM_ADDRESS = "{selected.host}/{selected.server}/subsystem=batch-jberet";

    String DEPLOYMENT_ADDRESS = "{selected.host}/{selected.server}/deployment=*";
    String BATCH_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + "/subsystem=batch-jberet";
    String BATCH_DEPLOYMENT_JOB_ADDRESS = DEPLOYMENT_ADDRESS + "/subsystem=batch-jberet/job=*";
    String BATCH_SUBDEPLOYMENT_JOB_ADDRESS = DEPLOYMENT_ADDRESS + "/subdeployment=*/subsystem=batch-jberet/job=*";

    AddressTemplate BATCH_SUBSYSTEM_TEMPLATE = AddressTemplate.of(BATCH_SUBSYSTEM_ADDRESS);

    AddressTemplate BATCH_DEPLOYMENT_TEMPLATE = AddressTemplate.of(BATCH_DEPLOYMENT_ADDRESS);
    AddressTemplate BATCH_DEPLOYMENT_JOB_TEMPLATE = AddressTemplate.of(BATCH_DEPLOYMENT_JOB_ADDRESS);
    AddressTemplate BATCH_SUBDEPLOYMENT_JOB_TEMPLATE = AddressTemplate.of(BATCH_SUBDEPLOYMENT_JOB_ADDRESS);
}
