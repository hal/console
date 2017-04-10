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
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.RegistryStatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.processing.LookupResult.ALL_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;
import static org.jboss.hal.meta.processing.MetadataProcessor.RRD_DEPTH;

/**
 * @author Harald Pehl
 */
class CreateRrdOperations {

    private final StatementContext statementContext;

    CreateRrdOperations(final StatementContext statementContext, final Environment environment) {
        this.statementContext = new RegistryStatementContext(statementContext, environment);
    }

    public List<Operation> create(LookupResult lookupResult, boolean optional) {
        List<Operation> operations = new ArrayList<>();
        lookupResult.templates().stream()
                .filter(template -> optional == template.isOptional())
                .forEach(template -> {
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
                                builder.param(ACCESS_CONTROL, TRIM_DESCRIPTIONS).param(OPERATIONS, true);
                                break;
                            case SECURITY_CONTEXT_PRESENT:
                                // resource description missing
                                builder.param(OPERATIONS, true);
                                break;
                        }
                        if (lookupResult.recursive()) {
                            builder.param(RECURSIVE_DEPTH, RRD_DEPTH);
                        }
                        operations.add(builder.build());
                    }
                });
        return operations;
    }
}
