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
package org.jboss.hal.client.configuration;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import javax.inject.Inject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.resources.Ids.SUBSYSTEM_COLUMN;
import static org.jboss.hal.resources.Names.SUBSYSTEM;

/**
 * @author Harald Pehl
 */
public class SubsystemColumn extends FinderColumn<String> {

    @Inject
    public SubsystemColumn(final Finder finder,
            final Dispatcher dispatcher) {

        super(new Builder<>(finder, SUBSYSTEM_COLUMN, SUBSYSTEM, (String item) -> () -> new LabelBuilder().label(item))
                .showCount()
                .withFilter()
                .itemsProvider((context, callback) -> {
                    Operation subsystemOp = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                            .param(CHILD_TYPE, ModelDescriptionConstants.SUBSYSTEM).build();
                    dispatcher.execute(subsystemOp, result -> {
                        callback.onSuccess(Lists.transform(result.asList(), ModelNode::asString));
                    });
                }));
    }
}
