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
package org.jboss.hal.client.runtime.subsystem.undertow;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.core.deployment.DeploymentResources.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.core.deployment.DeploymentResources.SUBDEPLOYMENT_ADDRESS;

public interface AddressTemplates {

    String WEB_ADDRESS = "/subsystem=undertow";
    String SERVER_ADDRESS = "/server=*";

    String WEB_SUBSYSTEM_ADDRESS = "/{selected.host}/{selected.server}" + WEB_ADDRESS;
    String WEB_SERVER_ADDRESS = WEB_SUBSYSTEM_ADDRESS + SERVER_ADDRESS;

    String MODCLUSTER_ADDRESS = WEB_SUBSYSTEM_ADDRESS + "/configuration=filter/mod-cluster=*";
    String MODCLUSTER_BALANCER_ADDRESS = WEB_SUBSYSTEM_ADDRESS + "/configuration=filter/mod-cluster=*/balancer=*";
    String MODCLUSTER_BALANCER_NODE_ADDRESS = WEB_SUBSYSTEM_ADDRESS + "/configuration=filter/mod-cluster=*/balancer=*/node=*";
    String MODCLUSTER_BALANCER_NODE_CONTEXT_ADDRESS = WEB_SUBSYSTEM_ADDRESS + "/configuration=filter/mod-cluster=*/balancer=*/node=*/context=*";

    String WEB_DEPLOYMENT_ADDRESS = DEPLOYMENT_ADDRESS + WEB_ADDRESS;
    String WEB_SUBDEPLOYMENT_ADDRESS = SUBDEPLOYMENT_ADDRESS + WEB_ADDRESS;

    AddressTemplate AJP_LISTENER_TEMPLATE = AddressTemplate.of(WEB_SERVER_ADDRESS + "/ajp-listener=*");
    AddressTemplate MODCLUSTER_TEMPLATE = AddressTemplate.of(MODCLUSTER_ADDRESS);
    AddressTemplate MODCLUSTER_BALANCER_TEMPLATE = AddressTemplate.of(MODCLUSTER_BALANCER_ADDRESS);
    AddressTemplate MODCLUSTER_BALANCER_NODE_TEMPLATE = AddressTemplate.of(MODCLUSTER_BALANCER_NODE_ADDRESS);
    AddressTemplate MODCLUSTER_BALANCER_NODE_CONTEXT_TEMPLATE = AddressTemplate.of(MODCLUSTER_BALANCER_NODE_CONTEXT_ADDRESS);

    AddressTemplate WEB_SUBSYSTEM_TEMPLATE = AddressTemplate.of(WEB_SUBSYSTEM_ADDRESS);
    AddressTemplate WEB_SERVER_TEMPLATE = AddressTemplate.of(WEB_SERVER_ADDRESS);

    AddressTemplate WEB_DEPLOYMENT_TEMPLATE = AddressTemplate.of(WEB_DEPLOYMENT_ADDRESS);
    AddressTemplate WEB_SUBDEPLOYMENT_TEMPLATE = AddressTemplate.of(WEB_SUBDEPLOYMENT_ADDRESS);
    AddressTemplate WEB_DEPLOYMENT_SERVLET_TEMPLATE = AddressTemplate.of(WEB_DEPLOYMENT_ADDRESS + "/servlet=*");
    AddressTemplate WEB_DEPLOYMENT_WEBSOCKETS_TEMPLATE = AddressTemplate.of(WEB_DEPLOYMENT_ADDRESS + "/websocket=*");
}
