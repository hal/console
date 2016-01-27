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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STEPS;

/**
 * @author Harald Pehl
 */
public class Composite extends Operation {

    private int operations;

    public Composite(Operation first, Operation... rest) {
        super(COMPOSITE, ResourceAddress.ROOT, new ModelNode(), null);
        this.operations = 0;

        List<Operation> operations = new ArrayList<>();
        operations.add(first);
        if (rest != null) {
            Collections.addAll(operations, rest);
        }
        add(operations);
    }

    public Composite(List<Operation> operations) {
        super(COMPOSITE, ResourceAddress.ROOT, new ModelNode(), null);
        this.operations = 0;
        add(operations);
    }

    private void add(final List<Operation> operations) {
        for (Operation operation : operations) {
            get(STEPS).add(operation);
            this.operations++;
        }
    }

    @Override
    public String toString() {
        return "Composite(" + operations + ")";
    }
}
