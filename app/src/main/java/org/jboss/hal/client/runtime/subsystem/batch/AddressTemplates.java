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
package org.jboss.hal.client.runtime.subsystem.batch;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.core.deployment.DeploymentResources.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.DeploymentResources.SUBDEPLOYMENT_ADDRESS;

interface AddressTemplates {

    String BATCH_SUBSYSTEM_ADDRESS = "{selected.host}/{selected.server}/subsystem=batch-jberet";

    String JOB_ADDRESS = "/subsystem=batch-jberet/job=*";
    String BATCH_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + "/subsystem=batch-jberet";
    String BATCH_DEPLOYMENT_JOB_ADDRESS = DEPLOYMENT_ADDRESS + JOB_ADDRESS;
    String BATCH_SUBDEPLOYMENT_JOB_ADDRESS = SUBDEPLOYMENT_ADDRESS + JOB_ADDRESS;
    String EXECUTION_ADDRESS = BATCH_DEPLOYMENT_JOB_ADDRESS + "/execution=*";

    AddressTemplate BATCH_SUBSYSTEM_TEMPLATE = AddressTemplate.of(BATCH_SUBSYSTEM_ADDRESS);

    AddressTemplate BATCH_DEPLOYMENT_TEMPLATE = AddressTemplate.of(BATCH_DEPLOYMENT_ADDRESS);
    AddressTemplate BATCH_DEPLOYMENT_JOB_TEMPLATE = AddressTemplate.of(BATCH_DEPLOYMENT_JOB_ADDRESS);
    AddressTemplate BATCH_SUBDEPLOYMENT_JOB_TEMPLATE = AddressTemplate.of(BATCH_SUBDEPLOYMENT_JOB_ADDRESS);
    AddressTemplate EXECUTION_TEMPLATE = AddressTemplate.of(EXECUTION_ADDRESS);
}
