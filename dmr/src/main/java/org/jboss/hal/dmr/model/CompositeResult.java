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

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Harald Pehl
 */
public class CompositeResult implements Iterable<ModelNode> {

    private final LinkedHashMap<String, ModelNode> steps;

    public CompositeResult(ModelNode steps) {
        this.steps = new LinkedHashMap<>();
        if (steps.isDefined()) {
            for (Property property : steps.asPropertyList()) {
                this.steps.put(property.getName(), property.getValue());
            }
        }
    }

    /**
     * @param index zero-based!
     * @return the related step result
     */
    public ModelNode step(int index) {
        return step("step-" + (index + 1)); //NON-NLS
    }

    /**
     * @param step Step as "step-n" (one-based!)
     * @return the related step result
     */
    public ModelNode step(String step) {
        if (steps.containsKey(step)) {
            return steps.get(step);
        }
        return new ModelNode();
    }

    @Override
    public Iterator<ModelNode> iterator() {
        return steps.values().iterator();
    }

    public int size() {return steps.size();}

    public boolean isEmpty() {return steps.isEmpty();}
}
