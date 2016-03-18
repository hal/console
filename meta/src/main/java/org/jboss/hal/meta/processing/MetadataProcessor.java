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
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Metadata processor which processes the required resources attached to tokens and stores the retrieved metadata in
 * the related registries. In addition you can call {@link #lookup(AddressTemplate, Progress, MetadataCallback)} to get
 * and dynamically create the metadata attached to a specific address template.
 *
 * @author Harald Pehl
 */
public class MetadataProcessor {

    public interface MetadataCallback {

        void onMetadata(Metadata metadata);

        void onError(Throwable error);
    }


    /**
     * Number of r-r-d operations part of one composite operation.
     */
    private final static int BATCH_SIZE = 3;

    private static final Logger logger = LoggerFactory.getLogger(MetadataProcessor.class);

    private final Dispatcher dispatcher;
    private final RequiredResources requiredResources;
    private final ResourceDescriptions resourceDescriptions;
    private final SecurityFramework securityFramework;
    private final Capabilities capabilities;
    private final Lookup lookup;
    private final CreateRrdOperations rrdOps;

    @Inject
    public MetadataProcessor(final Dispatcher dispatcher,
            final StatementContext statementContext,
            final RequiredResources requiredResources,
            final SecurityFramework securityFramework,
            final ResourceDescriptions resourceDescriptions,
            final Capabilities capabilities) {
        this.dispatcher = dispatcher;
        this.requiredResources = requiredResources;
        this.securityFramework = securityFramework;
        this.resourceDescriptions = resourceDescriptions;
        this.capabilities = capabilities;
        this.lookup = new Lookup(resourceDescriptions, securityFramework);
        this.rrdOps = new CreateRrdOperations(statementContext);
    }

    public void process(final String id, final Progress progress, final AsyncCallback<Void> callback) {
        Set<String> resources = requiredResources.getResources(id);
        logger.debug("Process required resources {} for id {}", resources, id); //NON-NLS
        if (resources.isEmpty()) {
            logger.debug("No required resources found -> callback.onSuccess(null)"); //NON-NLS
            callback.onSuccess(null);

        } else {
            //noinspection Guava
            Set<AddressTemplate> templates = FluentIterable.from(resources).transform(AddressTemplate::of).toSet();
            processInternal(templates, requiredResources.isRecursive(id), progress, callback);
        }
    }

    public void lookup(final AddressTemplate template, Progress progress, final MetadataCallback callback) {
        logger.debug("Lookup metadata for {}", template); //NON-NLS
        processInternal(singleton(template), false, progress, new AsyncCallback<Void>() {
            @Override
            public void onFailure(final Throwable throwable) {
                callback.onError(throwable);
            }

            @Override
            public void onSuccess(final Void aVoid) {
                SecurityContext securityContext = securityFramework.lookup(template);
                ResourceDescription description = resourceDescriptions.lookup(template);
                callback.onMetadata(new Metadata(securityContext, description, capabilities));
            }
        });
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void processInternal(final Set<AddressTemplate> templates, final boolean recursive, final Progress progress,
            final AsyncCallback<Void> callback) {
        LookupResult lookupResult = lookup.check(templates, recursive);
        if (lookupResult.allPresent()) {
            logger.debug("All metadata have been already processed -> callback.onSuccess(null)");
            callback.onSuccess(null);
        } else {
            logger.debug("{}", lookupResult);
            List<Operation> operations = rrdOps.create(lookupResult);
            List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
            List<Composite> composites = Lists.transform(piles, Composite::new);

            logger.debug("About to execute {} composite operations", composites.size());
            List<RrdFunction> functions = Lists.transform(composites,
                    composite -> new RrdFunction(resourceDescriptions, securityFramework, dispatcher, composite));
            //noinspection Duplicates
            Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    logger.debug("Failed to process metadata: {}", context.getErrorMessage());
                    callback.onFailure(context.getError());
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    logger.debug("Successfully processed metadata");
                    callback.onSuccess(null);
                }
            };
            if (functions.size() == 1) {
                new Async<FunctionContext>(progress).single(new FunctionContext(), outcome, functions.get(0));
            } else {
                //noinspection SuspiciousToArrayCall
                new Async<FunctionContext>(progress).waterfall(new FunctionContext(), outcome,
                        (org.jboss.gwt.flow.Function[]) functions.toArray(new RrdFunction[functions.size()]));
            }
        }
    }
}
