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
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;

class Destination extends DeploymentResource {

    enum Type {
        JMS_QUEUE("jms-queue", Names.JMS_QUEUE),
        JMS_TOPIC("jms-topic", Names.JMS_TOPIC),
        QUEUE("queue", Names.QUERY),
        UNDEFINED("undefined", Names.NOT_AVAILABLE);

        static Type[] SUBSYSTEM_RESOURCES = new Type[]{JMS_QUEUE, JMS_TOPIC, QUEUE};
        static Type[] DEPLOYMENT_RESOURCES = new Type[]{JMS_QUEUE, JMS_TOPIC};

        static Destination.Type fromResource(String resource) {
            if (JMS_QUEUE.resource.equals(resource)) {
                return JMS_QUEUE;
            } else if (JMS_TOPIC.resource.equals(resource)) {
                return JMS_TOPIC;
            } else if (QUEUE.resource.equals(resource)) {
                return QUEUE;
            }
            return Destination.Type.UNDEFINED;
        }

        final String resource;
        final String type;

        Type(@NonNls String resource, String type) {
            this.resource = resource;
            this.type = type;
        }
    }


    final Type type;

    Destination(ResourceAddress address, ModelNode modelNode) {
        super(address, modelNode);
        this.type = Destination.Type.fromResource(address.lastName());
    }

    boolean fromDeployment() {
        return getDeployment() != null;
    }
}
