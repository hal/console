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
package org.jboss.hal.core.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Used for data-sources in configuration, runtime and deployments */
public class DataSource extends NamedNode {

    private final boolean xa;
    private final ResourceAddress address;
    private String path;
    private String deployment;
    private String subdeployment;

    public DataSource(boolean xa) {
        this("", xa);
    }

    public DataSource(String name, boolean xa) {
        super(name, new ModelNode());
        this.xa = xa;
        this.address = null;
    }

    public DataSource(String name, ModelNode modelNode, boolean xa) {
        super(name, modelNode);
        this.xa = xa;
        this.address = null;
    }

    public DataSource(Property property, boolean xa) {
        super(property);
        this.xa = xa;
        this.address = null;
    }

    public DataSource(ResourceAddress address, ModelNode modelNode, boolean xa) {
        super(address.lastValue(), modelNode);
        this.xa = xa;
        this.address = address;

        address.asList().forEach(segment -> {
            if (segment.hasDefined(DEPLOYMENT)) {
                deployment = segment.get(DEPLOYMENT).asString();
            }
            if (segment.hasDefined(SUBDEPLOYMENT)) {
                subdeployment = segment.get(SUBDEPLOYMENT).asString();
            }
        });
        this.path = subdeployment != null ? deployment + "/" + subdeployment : deployment;
    }

    public boolean isXa() {
        return xa;
    }

    public boolean isEnabled() {
        return hasDefined(ENABLED) && get(ENABLED).asBoolean(false);
    }

    public boolean isStatisticsEnabled() {
        return hasDefined(STATISTICS) && get(STATISTICS, JDBC, STATISTICS_ENABLED).asBoolean();
    }

    public void setDriver(JdbcDriver driver) {
        get(DRIVER_NAME).set(driver.getName());
        if (isXa()) {
            get(XA_DATASOURCE_CLASS).set(driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME));
        } else {
            get(DRIVER_CLASS).set(driver.get(DRIVER_CLASS_NAME));
        }
    }

    public boolean fromDeployment() {
        return address != null;
    }

    public ResourceAddress getAddress() {
        return address;
    }

    public String getDeployment() {
        return deployment;
    }

    public String getSubdeployment() {
        return subdeployment;
    }

    /**
     * Returns {@code deployment}/{@code subdeployment} if {@code subdeployment != null}, {@code deployment} otherwise. Should
     * not be used to build DMR operations, but rather in the UI.
     */
    public String getPath() {
        return path;
    }
}
