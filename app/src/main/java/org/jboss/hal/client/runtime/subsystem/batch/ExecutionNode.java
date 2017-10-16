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

import java.util.Date;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeDate;
import static org.jboss.hal.dmr.ModelNodeHelper.getOrDefault;

class ExecutionNode extends NamedNode {

    private final int executionId;
    private final int instanceId;

    ExecutionNode(Property property) {
        super(property);
        executionId = Integer.parseInt(property.getName());
        instanceId = getOrDefault(property.getValue(), INSTANCE_ID, () -> property.getValue().get(INSTANCE_ID).asInt(),
                -1);
    }

    int getExecutionId() {
        return executionId;
    }

    int getInstanceId() {
        return instanceId;
    }

    BatchStatus getBatchStatus() {
        return asEnumValue(this, BATCH_STATUS, BatchStatus::valueOf, BatchStatus.UNKNOWN);
    }

    String getExitError() {
        if (hasDefined(EXIT_STATUS) && get(EXIT_STATUS).asString().startsWith("Error")) { //NON-NLS
            return get(EXIT_STATUS).asString();
        }
        return null;
    }

    Date getCreateTime() {
        return failSafeDate(this, CREATE_TIME);
    }

    Date getStartTime() {
        return failSafeDate(this, START_TIME);
    }

    Date getEndTime() {
        return failSafeDate(this, END_TIME);
    }

    long getDuration() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    Date getLastUpdatedTime() {
        return failSafeDate(this, LAST_UPDATED_TIME);
    }


    enum BatchStatus {
        STARTED, STOPPED, COMPLETED, FAILED, ABANDONED, UNKNOWN
    }
}
