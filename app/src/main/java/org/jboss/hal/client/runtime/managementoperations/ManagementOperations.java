/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.managementoperations;

import java.util.Iterator;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class ManagementOperations extends NamedNode {

    enum ExecutionStatus {

        // description copied from https://docs.jboss.org/author/display/WFLY/Canceling+Management+Operations
        EXECUTING("executing", "The caller thread is actively executing"),
        AWAITING_OTHER_OPERATION("awaiting-other-operation",
                "The caller thread is blocking waiting for another operation to release the exclusive execution lock"),
        AWAITING_STABILITY("awaiting-stability",
                "The caller thread has made changes to the service container and is waiting for the service container to stabilize"),
        COMPLETING("completing", "The operation is committed and is completing execution"),
        ROLLING_BACK("rolling-back", "The operation is rolling back");

        String name;
        String description;

        ExecutionStatus(String name, String description) {
            this.name = name;
            this.description = description;
        }

        static ExecutionStatus find(String name) {
            for (ExecutionStatus status : values()) {
                if (status.name.equals(name)) {
                    return status;
                }
            }
            return null;
        }
    }


    private String address;

    ManagementOperations(NamedNode node) {
        super(node.getName(), node.asModelNode());
        StringBuilder builder = new StringBuilder();
        builder.append("/");
        for (Iterator<Property> iterator = get(ADDRESS).asPropertyList().iterator(); iterator.hasNext(); ) {
            Property segment = iterator.next();
            builder.append(segment.getName()).append("=").append(segment.getValue().asString());
            if (iterator.hasNext()) {
                builder.append("/");
            }
        }
        this.address = builder.toString();
    }

    public String getAccessMechanism() {
        return hasDefined(ACCESS_MECHANISM) ? get(ACCESS_MECHANISM).asString() : "";
    }

    public String getAddress() {
        return address;
    }

    public boolean isNonProgressing() {
        return hasDefined(HAL_NON_PROGRESSING) && get(HAL_NON_PROGRESSING).asBoolean();
    }

    public void setAsNonProgressing() {
        get(HAL_NON_PROGRESSING).set(true);
    }

    public String getActiveAddressHost() {
        return hasDefined(HAL_ACTIVE_ADDRESS_HOST) ? get(HAL_ACTIVE_ADDRESS_HOST).asString() : null;
    }

    public String getActiveAddressServer() {
        return hasDefined(HAL_ACTIVE_ADDRESS_SERVER) ? get(HAL_ACTIVE_ADDRESS_SERVER).asString() : null;
    }

    public String getCallerThread() {
        return get(CALLER_THREAD).asString();
    }

    public boolean isCancelled() {
        return get(CANCELLED).asBoolean();
    }

    public boolean isDomainRollout() {
        return get(DOMAIN_ROLLOUT).asBoolean();
    }

    public String getDomainUuid() {
        return hasDefined(DOMAIN_UUID) ? get(DOMAIN_UUID).asString() : null;
    }

    public Long getExclusiveRunningTime() {
        return get(EXCLUSIVE_RUNNING_TIME).asLong();
    }

    public String getExecutionStatus() {
        return get(EXECUTION_STATUS).asString();
    }

    public String getExecutionStatusDescription() {
        return ExecutionStatus.find(getExecutionStatus()).description;
    }

    public String getOperation() {
        return get(OPERATION).asString();
    }

    public Long getRunningTime() {
        return get(RUNNING_TIME).asLong();
    }

}
