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
package org.jboss.hal.core.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.NamedObject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class JdbcDriver extends ModelNode implements NamedObject {

    public enum Provider {
        UNKNOWN(Names.NOT_AVAILABLE), DEPLOYMENT(Names.DEPLOYMENT.toLowerCase()), MODULE(Names.MODULE.toLowerCase());

        private final String text;

        Provider(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }
    }


    public JdbcDriver() {
        this("");
    }

    public JdbcDriver(String name) {
        this(name, new ModelNode());
    }

    public JdbcDriver(Property property) {
        this(property.getName(), property.getValue());
    }

    public JdbcDriver(String name, ModelNode modelNode) {
        set(modelNode);
        get(DRIVER_NAME).set(name);
    }

    public String getName() {
        return get(DRIVER_NAME).asString();
    }

    public Provider getProvider() {
        if (hasDefined(DEPLOYMENT_NAME)) {
            return Provider.DEPLOYMENT;
        } else if (hasDefined(DRIVER_MODULE_NAME)) {
            return Provider.MODULE;
        } else {
            return Provider.UNKNOWN;
        }
    }

    public String getDeploymentName() {
        return get(DEPLOYMENT_NAME).asString();
    }

    public String getModule() {
        return get(DRIVER_MODULE_NAME).asString();
    }

    public String getDriverVersion() {
        String version = Names.NOT_AVAILABLE;
        if (hasDefined(DRIVER_MAJOR_VERSION)) {
            version = get(DRIVER_MAJOR_VERSION).asString();
            if (hasDefined(DRIVER_MINOR_VERSION)) {
                version += "." + get(DRIVER_MINOR_VERSION).asString();
            }
        }
        return version;
    }
}
