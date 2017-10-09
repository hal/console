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
package org.jboss.hal.client.runtime.subsystem.ejb;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.core.deployment.DeploymentResources.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.DeploymentResources.SUBDEPLOYMENT_ADDRESS;

interface AddressTemplates {

    String EJB3_SUBSYSTEM_ADDRESS = "{selected.host}/{selected.server}/subsystem=ejb3";

    String EJB3_ADDRESS = "/subsystem=ejb3";
    String EJB3_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + EJB3_ADDRESS;
    String EJB3_SUBDEPLOYMENT_ADDRESS = SUBDEPLOYMENT_ADDRESS + EJB3_ADDRESS;

    AddressTemplate EJB3_SUBSYSTEM_TEMPLATE = AddressTemplate.of(EJB3_SUBSYSTEM_ADDRESS);

    AddressTemplate EJB3_DEPLOYMENT_TEMPLATE = AddressTemplate.of(EJB3_DEPLOYMENT_ADDRESS);
    AddressTemplate EJB3_SUBDEPLOYMENT_TEMPLATE = AddressTemplate.of(EJB3_SUBDEPLOYMENT_ADDRESS);

    static AddressTemplate ejbDeploymentTemplate(EjbNode.Type type) {
        return EJB3_DEPLOYMENT_TEMPLATE.append(type.resource + "=*");
    }
}
