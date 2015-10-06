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
package org.jboss.hal.meta.description;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.hal.config.Environment;
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

    /**
     * Always use wildcards when resolving the supported keys. The descriptions do not differ between profiles,
     * server groups, hosts or servers.
     */
    static class WildcardStatementContext implements StatementContext {

        private final Environment environment;
        private final Map<String, String[]> keys;

        @Inject
        public WildcardStatementContext(Environment environment) {
            this.environment = environment;

            keys = new HashMap<>();
            keys.put(SELECTED_PROFILE, new String[]{"profile", "*"});
            keys.put(SELECTED_GROUP, new String[]{"server-group", "*"});
            keys.put(SELECTED_HOST, new String[]{"host", "*"});
            keys.put(SELECTED_SERVER, new String[]{"server", "*"});
        }

        public String resolve(final String key) {
            return null;
        }

        @Override
        public String[] resolveTuple(final String key) {
            if (!environment.isStandalone() && keys.containsKey(key)) {
                return keys.get(key);
            }
            return null;
        }
    }


    static final String RESOURCE_DESCRIPTION_TYPE = "resource description";

    private final Dispatcher dispatcher;
    // TODO Replace map with local storage (constrained by language and management model version)
    private final Map<ResourceAddress, ResourceDescription> registry;

    @Inject
    public ResourceDescriptions(final Environment environment, final Dispatcher dispatcher) {
        super(new WildcardStatementContext(environment), RESOURCE_DESCRIPTION_TYPE);
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
                            "Unable to add a single " + RESOURCE_DESCRIPTION_TYPE + " for " + address));
                },
                (exceptionalOp, exception) -> { callback.onFailure(exception); });
    }
}
