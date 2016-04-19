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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.resources.Names;

import static com.google.common.base.Strings.emptyToNull;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class JdbcDriver extends NamedNode {

    public enum Provider {
        UNKNOWN(Names.NOT_AVAILABLE), DEPLOYMENT(Names.DEPLOYMENT), MODULE(Names.MODULE);

        private final String label;

        Provider(final String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }


    public JdbcDriver() {
        this("");
    }

    public JdbcDriver(final String name) {
        super(name, new ModelNode());
    }

    public JdbcDriver(final Property property) {
        super(property);
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
        String module = get(DRIVER_MODULE_NAME).asString();
        if (hasDefined(MODULE_SLOT)) {
            module += " slot " + get(MODULE_SLOT).asString(); //NON-NLS
        }
        return module;
    }

    public List<String> getDriverClasses() {
        List<String> values = new ArrayList<>();
        if (hasDefined(DRIVER_CLASS_NAME)) {
            values.add(emptyToNull(get(DRIVER_CLASS_NAME).asString()));
        }
        if (hasDefined(DRIVER_DATASOURCE_CLASS_NAME)) {
            values.add(emptyToNull(get(DRIVER_DATASOURCE_CLASS_NAME).asString()));
        }
        if (hasDefined(DRIVER_XA_DATASOURCE_CLASS_NAME)) {
            values.add(emptyToNull(get(DRIVER_XA_DATASOURCE_CLASS_NAME).asString()));
        }
        return values;
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

    void reset(ModelNode modelNode) {
        String name = getName();
        set(modelNode);
        setName(name);
    }
}
