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
package org.jboss.hal.meta.description;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AbstractMetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.ParserException;
import org.jboss.hal.meta.processing.RrdResult;
import org.jboss.hal.meta.processing.SingleRrdParser;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ResourceDescriptions extends AbstractMetadataRegistry<ResourceDescription> {

    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";

    private final Dispatcher dispatcher;
    // TODO Replace map with local storage (constrained by language and management model version)
    private final Map<ResourceAddress, ResourceDescription> registry;

    @Inject
    public ResourceDescriptions(final StatementContext statementContext, final Dispatcher dispatcher) {
        super(statementContext, RESOURCE_DESCRIPTION_TYPE);
        this.dispatcher = dispatcher;
        this.registry = new HashMap<>();
    }

    @Override
    protected ResourceDescription lookupAddress(final ResourceAddress address) {
        return registry.get(address);
    }

    @Override
    public void add(final ResourceAddress address, final ResourceDescription metadata) {
        registry.put(address, metadata);
    }

    @Override
    protected void addDeferred(final ResourceAddress address, final AsyncCallback<ResourceDescription> callback) {
        Operation operation = new Operation.Builder(READ_RESOURCE_DESCRIPTION_OPERATION, address)
                .param(OPERATIONS, true)
                .param(RECURSIVE, true)
                .build();
        dispatcher.execute(operation,
                result -> {
                    try {
                        Set<RrdResult> results = new SingleRrdParser().parse(address, result);
                        for (RrdResult rr : results) {
                            if (rr.resourceDescription != null) {
                                add(rr.address, rr.resourceDescription);
                            }
                        }
                    } catch (ParserException e) {
                        callback.onFailure(e);
                    }
                },
                (failedOp, failure) -> {
                    callback.onFailure(new RuntimeException(
                            UNABLE_TO_BIND_SINGLE + RESOURCE_DESCRIPTION_TYPE + " for " + address));
                },
                (exceptionalOp, exception) -> { callback.onFailure(exception); });
    }
}
