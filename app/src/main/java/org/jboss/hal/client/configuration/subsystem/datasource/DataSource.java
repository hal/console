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
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.IdBuilder;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_NAME;

/**
 * @author Harald Pehl
 */
public class DataSource extends NamedNode {

    static String id(String name, boolean xa) {
        return IdBuilder.build(name, xa ? "xa" : "non-xa");
    }

    private final boolean xa;

    public DataSource(final boolean xa) {
        this("", xa);
    }

    DataSource(final String name, final boolean xa) {
        super(name, new ModelNode());
        this.xa = xa;
    }

    public DataSource(final String name, final ModelNode modelNode, final boolean xa) {
        super(name, modelNode);
        this.xa = xa;
    }

    public DataSource(final Property property, final boolean xa) {
        super(property);
        this.xa = xa;
    }

    public boolean isXa() {
        return xa;
    }

    public void setDriver(final JdbcDriver driver) {
        get(DRIVER_NAME).set(driver.getName());
        get(DRIVER_CLASS).set(driver.get(DRIVER_CLASS_NAME));
    }
}
