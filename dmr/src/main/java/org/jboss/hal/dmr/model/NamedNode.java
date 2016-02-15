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
package org.jboss.hal.dmr.model;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;

/**
 * @author Harald Pehl
 */
public class NamedNode extends ModelNode {

    protected final String name;
    protected final ModelNode node;

    public NamedNode(final ModelNode node) {
        this(UNDEFINED + "_" + NamedNode.class.getSimpleName() + "_" + System.currentTimeMillis(), node);
    }

    public NamedNode(final Property property) {
        this(property.getName(), property.getValue());
    }

    public NamedNode(final String name, final ModelNode node) {
        this.name = name;
        this.node = node;
        set(node);
        get(NAME).set(name);
    }

    public String getName() {
        return get(NAME).asString();
    }
}
