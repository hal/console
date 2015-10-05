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
package org.jboss.hal.meta.processing;

import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.processing.LookupResult.*;

/**
 * @author Harald Pehl
 */
class CreateRrdOperations {

    private final StatementContext statementContext;

    public CreateRrdOperations(final StatementContext statementContext) {
        this.statementContext = statementContext;
    }

    List<Operation> create(LookupResult lookupResult) {
        List<Operation> operations = new ArrayList<>();
        for (AddressTemplate template : lookupResult.templates()) {
            int missingMetadata = lookupResult.missingMetadata(template);
            if (missingMetadata != ALL_PRESENT) {
                ResourceAddress address = template.resolve(statementContext);
                Operation.Builder builder = new Operation.Builder(READ_RESOURCE_DESCRIPTION_OPERATION, address);
                switch (missingMetadata) {
                    case NOTHING_PRESENT:
                        // all missing
                        builder.param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS).param(OPERATIONS, true);
                        break;
                    case RESOURCE_DESCRIPTION_PRESENT:
                        // security context missing
                        builder.param(ACCESS_CONTROL, "trim-descriptions").param(OPERATIONS, true);
                        break;
                    case SECURITY_CONTEXT_PRESENT:
                        // resource description missing: use defaults for the r-r-d op
                        break;
                }
                if (lookupResult.recursive()) {
                    builder.param(RECURSIVE, true);
                }
                operations.add(builder.build());
            }
        }
        return operations;
    }
}
