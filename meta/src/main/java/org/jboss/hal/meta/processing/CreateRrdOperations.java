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
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptionStatementContext;
import org.jboss.hal.meta.security.SecurityContextStatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.processing.LookupResult.ALL_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;

class CreateRrdOperations {

    private final SecurityContextStatementContext securityContextStatementContext;
    private final ResourceDescriptionStatementContext resourceDescriptionStatementContext;
    private final int depth;

    CreateRrdOperations(Environment environment, StatementContext statementContext, int depth) {
        this.depth = depth;
        securityContextStatementContext = new SecurityContextStatementContext(statementContext, environment);
        resourceDescriptionStatementContext = new ResourceDescriptionStatementContext(statementContext, environment);
    }

    public List<Operation> create(LookupContext context, boolean recursive, boolean optional) {
        LookupResult lookupResult = context.lookupResult;
        List<Operation> operations = new ArrayList<>();
        lookupResult.templates().stream()
                .filter(template -> optional == template.isOptional())
                .forEach(template -> {
                    int missingMetadata = lookupResult.missingMetadata(template);
                    if (missingMetadata != ALL_PRESENT) {

                        ResourceAddress address = null;
                        Operation.Builder builder = null;

                        if (missingMetadata == NOTHING_PRESENT) {
                            address = template.resolve(securityContextStatementContext);
                            builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                                    .param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS)
                                    .param(OPERATIONS, true);

                        } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                            address = template.resolve(securityContextStatementContext);
                            builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                                    .param(ACCESS_CONTROL, TRIM_DESCRIPTIONS)
                                    .param(OPERATIONS, true);

                        } else if (missingMetadata == SECURITY_CONTEXT_PRESENT) {
                            address = template.resolve(resourceDescriptionStatementContext);
                            builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                                    .param(OPERATIONS, true);
                        }

                        if (builder != null && address != null) {
                            if (recursive) {
                                builder.param(RECURSIVE_DEPTH, depth);
                            }
                            operations.add(builder.build());
                        }
                    }
                });
        return operations;
    }
}
