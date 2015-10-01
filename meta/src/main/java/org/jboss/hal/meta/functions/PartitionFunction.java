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
package org.jboss.hal.meta.functions;

import com.google.common.collect.Lists;
import org.jboss.gwt.flow.Control;
import org.jboss.hal.meta.dmr.Composite;
import org.jboss.hal.meta.dmr.Operation;

import java.util.List;

/**
 * @author Harald Pehl
 */
public class PartitionFunction implements MetadataFunction {

    /**
     * Number of r-r-d operations part of one composite operation.
     */
    private final static int BATCH_SIZE = 3;

    @Override
    public void execute(final Control<MetadataContext> control) {
        MetadataContext metadataContext = control.getContext();
        List<Operation> operations = metadataContext.pop();

        // create composite operations
        List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
        List<Composite> composites = Lists.transform(piles, Composite::new);

        // next one please
        metadataContext.push(composites);
        control.proceed();
    }
}
