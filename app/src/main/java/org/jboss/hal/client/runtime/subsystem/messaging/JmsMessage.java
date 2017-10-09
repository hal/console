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

import java.util.Date;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class JmsMessage extends NamedNode {

    JmsMessage(ModelNode node) {
        super(node.get(JMS_MESSAGE_ID).asString(), node);
    }

    String getMessageId() {
        return get(JMS_MESSAGE_ID).asString();
    }

    Date getTimestamp() {
        return failSafeDate(JMS_TIMESTAMP);
    }

    Date getExpiration() {
        return failSafeDate(JMS_EXPIRATION);
    }

    int getPriority() {
        if (hasDefined(JMS_PRIORITY)) {
            return get(JMS_PRIORITY).asInt();
        }
        return Integer.MIN_VALUE;
    }

    String getDeliveryMode() {
        if (hasDefined(JMS_DELIVERY_MODE)) {
            return get(JMS_DELIVERY_MODE).asString();
        }
        return null;
    }

    private Date failSafeDate(String attribute) {
        Date date = null;
        if (hasDefined(attribute)) {
            long value = get(attribute).asLong();
            if (value > 0) {
                date = new Date(value);
            }
        }
        return date;
    }
}
