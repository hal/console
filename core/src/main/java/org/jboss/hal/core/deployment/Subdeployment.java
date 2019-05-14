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
package org.jboss.hal.core.deployment;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

public class Subdeployment extends ModelNode {

    private final Deployment parent;
    private final String name;
    private final List<Subsystem> subsystems;

    Subdeployment(final Deployment parent, final String name, final ModelNode node) {
        this.parent = parent;
        this.name = name;
        this.subsystems = new ArrayList<>();
        set(node);

        if (node.hasDefined(SUBSYSTEM)) {
            Deployment.parseSubsystems(node, subsystems);
        }
    }

    @Override
    public String toString() {
        return "Subdeployment{" + name + "}";
    }

    public String getName() {
        return name;
    }

    public Deployment getParent() {
        return parent;
    }

    public List<Subsystem> getSubsystems() {
        return subsystems;
    }

    public boolean hasSubsystem(String name) {
        return subsystems.stream().anyMatch(subsystem -> name.equals(subsystem.getName()));
    }
}
