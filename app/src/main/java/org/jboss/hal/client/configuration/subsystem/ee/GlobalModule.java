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
package org.jboss.hal.client.configuration.subsystem.ee;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * A regular bean to hold global module data, for the add operation of a new global module.
 * 
 * @author Claudio Miranda
 */
public class GlobalModule extends NamedNode {

    public GlobalModule() {
        super("", new ModelNode());
    }

    public String getSlot() {
        return get("slot").asString();
    }

    public void setSlot(String slot) {
        get("slot").set(slot);
    }

    public boolean isAnnotations() {
        return get("annotations").asBoolean();
    }

    public void setAnnotations(boolean annotations) {
        get("annotations").set(annotations);
    }

    public boolean isMetaInf() {
        return get("meta-inf").asBoolean();
    }

    public void setMetaInf(boolean metaInf) {
        get("meta-inf").set(metaInf);
    }

    public boolean isServices() {
        return get("services").asBoolean();
    }

    public void setServices(boolean services) {
        get("services").set(services);
    }
}
