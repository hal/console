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
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.messaging.AddressTemplates.MESSAGING_SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PAUSED;

class Destination extends DeploymentResource {

    enum Type {
        JMS_QUEUE("jms-queue", NameTokens.JMS_QUEUE, Names.JMS_QUEUE), JMS_TOPIC("jms-topic", NameTokens.JMS_TOPIC,
                Names.JMS_TOPIC), QUEUE("queue", NameTokens.QUEUE, Names.CORE_QUEUE);

        static Type[] SUBSYSTEM_RESOURCES = new Type[] { JMS_QUEUE, JMS_TOPIC, QUEUE };
        static Type[] DEPLOYMENT_RESOURCES = new Type[] { JMS_QUEUE, JMS_TOPIC };

        static Destination.Type fromResource(String resource) {
            if (JMS_QUEUE.resource.equals(resource)) {
                return JMS_QUEUE;
            } else if (JMS_TOPIC.resource.equals(resource)) {
                return JMS_TOPIC;
            } else if (QUEUE.resource.equals(resource)) {
                return QUEUE;
            }
            return null;
        }

        final String resource;
        final String token;
        final String type;

        Type(String resource, String token, String type) {
            this.resource = resource;
            this.token = token;
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

    boolean isPaused() {
        return hasDefined(PAUSED) && get(PAUSED).asBoolean();
    }

    AddressTemplate template() {
        AddressTemplate template = fromDeployment() ? MESSAGING_DEPLOYMENT_TEMPLATE : MESSAGING_SERVER_TEMPLATE;
        return template.append(type.resource + "=*");
    }
}
