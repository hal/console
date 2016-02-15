/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.deployment;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * An uploaded deployment blob.
 *
 * @author Harald Pehl
 */
public class Content extends NamedNode {

    private final List<Assignment> assignments;

    public Content(final ModelNode node) {
        super(node);
        this.assignments = new ArrayList<>();
    }

    public String getRuntimeName() {
        ModelNode runtimeName = get("runtime-name");
        return runtimeName.isDefined() ? runtimeName.asString() : null;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    @Override
    public String toString() {
        return "Content{" + getName() + ", assigned to " + assignments + "}";
    }
}
