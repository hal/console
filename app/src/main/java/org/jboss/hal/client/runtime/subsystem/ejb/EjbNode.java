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
package org.jboss.hal.client.runtime.subsystem.ejb;

import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DELIVERY_ACTIVE;

class EjbNode extends DeploymentResource {

    enum Type {
        MDB("message-driven-bean", Names.MESSAGE_DRIVEN_BEAN), SINGLETON("singleton-bean", Names.SINGLETON_BEAN), STATEFUL(
                "stateful-session-bean",
                Names.STATEFUL_SESSION_BEAN), STATELESS("stateless-session-bean", Names.STATELESS_SESSION_BEAN);

        static Type fromResource(String resource) {
            if (MDB.resource.equals(resource)) {
                return MDB;
            } else if (SINGLETON.resource.equals(resource)) {
                return SINGLETON;
            } else if (STATEFUL.resource.equals(resource)) {
                return STATEFUL;
            } else if (STATELESS.resource.equals(resource)) {
                return STATELESS;
            }
            return null;
        }

        final String resource;
        final String type;

        Type(String resource, String type) {
            this.resource = resource;
            this.type = type;
        }
    }

    final Type type;

    EjbNode(ResourceAddress address, ModelNode modelNode) {
        super(address, modelNode);
        this.type = Type.fromResource(address.lastName());
    }

    boolean isDeliveryActive() {
        return hasDefined(DELIVERY_ACTIVE) && get(DELIVERY_ACTIVE).asBoolean();
    }

    boolean fromDeployment() {
        return getDeployment() != null;
    }

}
