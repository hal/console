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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Harald Pehl
 */
public class MetadataProcessor {

    /**
     * Number of r-r-d operations part of one composite operation.
     */
    final static int BATCH_SIZE = 3;

    private static final Logger logger = LoggerFactory.getLogger(MetadataProcessor.class);

    private final Dispatcher dispatcher;
    private final RequiredResources requiredResources;
    private final ResourceDescriptions resourceDescriptions;
    private final SecurityFramework securityFramework;
    private final Lookup lookup;
    private final CreateRrdOperations rrdOps;
    private final Progress progress;

    @Inject
    public MetadataProcessor(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final RequiredResources requiredResources,
            final ResourceDescriptions resourceDescriptions,
            final SecurityFramework securityFramework,
            final @Footer Progress progress) {
        this.dispatcher = dispatcher;
        this.requiredResources = requiredResources;
        this.resourceDescriptions = resourceDescriptions;
        this.securityFramework = securityFramework;
        this.lookup = new Lookup(resourceDescriptions, securityFramework);
        this.rrdOps = new CreateRrdOperations(statementContext);
        this.progress = progress;
    }

    public void process(String token, final AsyncCallback<Void> callback) {
        Set<String> resources = requiredResources.getResources(token);
        logger.debug("Process metadata on {}: {}", token, resources);
        if (resources.isEmpty()) {
            logger.debug("No required resources found -> callback.onSuccess(null)");
            callback.onSuccess(null);

        } else {
            Set<AddressTemplate> templates = FluentIterable.from(resources).transform(AddressTemplate::of).toSet();
            LookupResult lookupResult = lookup.check(token, templates, requiredResources.isRecursive(token));
            List<Operation> operations = rrdOps.create(lookupResult);
            if (operations.isEmpty()) {
                logger.debug("Everything present -> callback.onSuccess(null)");
                callback.onSuccess(null);

            } else {
                List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
                List<Composite> composites = Lists.transform(piles, Composite::new);
                logger.debug("About to execute {} composite operations", composites.size());
                List<RrdFunction> functions = Lists.transform(composites,
                        composite -> new RrdFunction(resourceDescriptions, securityFramework, dispatcher, composite));

                Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                    @Override
                    public void onFailure(final FunctionContext context) {
                        callback.onFailure(context.getError());
                    }

                    @Override
                    public void onSuccess(final FunctionContext context) {
                        callback.onSuccess(null);
                    }
                };
                new Async<FunctionContext>(progress).waterfall(new FunctionContext(), outcome,
                        functions.toArray(new RrdFunction[functions.size()]));
            }
        }
    }
}
