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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.datasource.JdbcDriver;

class Context {

    private final boolean xa;
    private final Map<String, Object> changedValues;
    private boolean created;
    DataSourceTemplate template;
    DataSource dataSource;
    JdbcDriver driver;
    Map<String, String> xaProperties;

    Context(boolean xa) {
        this.xa = xa;
        this.changedValues = new HashMap<>();
        this.xaProperties = new HashMap<>();
    }

    void custom() {
        dataSource = new DataSource(xa);
        driver = new JdbcDriver();
        xaProperties.clear();
    }

    void useTemplate() {
        dataSource = template.getDataSource();
        driver = template.getDriver();
        xaProperties.putAll(template.getXaProperties());
    }

    void recordChange(String name, Object value) {
        changedValues.put(name, value);
    }

    boolean isXa() {
        return xa;
    }

    boolean isCreated() {
        return created;
    }

    void setCreated(final boolean created) {
        this.created = created;
    }

    boolean hasChanges() {
        return isCreated() && !changedValues.isEmpty();
    }

    Map<String, Object> changes() {
        return changedValues;
    }
}
