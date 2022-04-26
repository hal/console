/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.js.Browser;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.MissingMetadataException;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptionDatabase;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.meta.security.SecurityContextDatabase;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import elemental2.promise.Promise;

import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;

/**
 * Reads resource {@linkplain Metadata metadata} using read-resource-description operations and stores it into the
 * {@link MetadataRegistry}. If you're sure the metadata is present, use the {@link MetadataRegistry} instead.
 */
public class MetadataProcessor {

    /** Recursive depth for the r-r-d operations. Keep this small - some browsers choke on too big payload size */
    static final int RRD_DEPTH = 3;

    /** Number of r-r-d operations part of one composite operation. */
    private static final int BATCH_SIZE = 3;

    private static final Logger logger = LoggerFactory.getLogger(MetadataProcessor.class);

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final RequiredResources requiredResources;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final ResourceDescriptionDatabase resourceDescriptionDatabase;
    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextDatabase securityContextDatabase;
    private final SecurityContextRegistry securityContextRegistry;
    private final Settings settings;
    private final WorkerChannel workerChannel;

    @Inject
    public MetadataProcessor(Environment environment,
            Dispatcher dispatcher,
            StatementContext statementContext,
            RequiredResources requiredResources,
            MetadataRegistry metadataRegistry,
            SecurityContextDatabase securityContextDatabase,
            SecurityContextRegistry securityContextRegistry,
            ResourceDescriptionDatabase resourceDescriptionDatabase,
            ResourceDescriptionRegistry resourceDescriptionRegistry,
            Settings settings,
            WorkerChannel workerChannel) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.requiredResources = requiredResources;
        this.securityContextDatabase = securityContextDatabase;
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionDatabase = resourceDescriptionDatabase;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.settings = settings;
        this.workerChannel = workerChannel;
    }

    public void lookup(AddressTemplate template, Progress progress, MetadataCallback callback) {
        logger.debug("Lookup metadata for {}", template);
        processInternal(singleton(template), false, progress)
                .then(c -> {
                    callback.onMetadata(metadataRegistry.lookup(template));
                    return null;
                })
                .catch_(error -> {
                    callback.onError(new MissingMetadataException("MetadataContext", template));
                    return null;
                });
    }

    public Promise<Metadata> lookup(AddressTemplate template, Progress progress) {
        logger.debug("Lookup metadata for {}", template);
        return processInternal(singleton(template), false, progress)
                .then(__ -> Promise.resolve(metadataRegistry.lookup(template)));
    }

    public Promise<Void> process(String id, Progress progress) {
        Set<String> resources = requiredResources.getResources(id);
        boolean recursive = requiredResources.isRecursive(id);
        logger.debug("Process required resources {} for id '{}' (recursive={})", resources, id, recursive);
        if (resources.isEmpty()) {
            logger.debug("No required resources found -> done");
            return Promise.resolve((Void) null);

        } else {
            Set<AddressTemplate> templates = resources.stream().map(AddressTemplate::of).collect(toSet());
            return processInternal(templates, recursive, progress);
        }
    }

    private Promise<Void> processInternal(Set<AddressTemplate> templates, boolean recursive, Progress progress) {
        // we can skip the tasks if the metadata is already in the registries
        LookupRegistryTask lookupRegistries = new LookupRegistryTask(resourceDescriptionRegistry,
                securityContextRegistry);
        if (lookupRegistries.allPresent(templates, recursive)) {
            logger.debug("All metadata have been already processed -> done");
            return Promise.resolve((Void) null);

        } else {
            boolean ie = Browser.isIE();
            List<Task<LookupContext>> tasks = new ArrayList<>();
            tasks.add(lookupRegistries);
            if (!ie) {
                tasks.add(new LookupDatabaseTask(resourceDescriptionDatabase, securityContextDatabase));
            }
            tasks.add(new RrdTask(environment, dispatcher, statementContext, settings, BATCH_SIZE, RRD_DEPTH));
            tasks.add(new UpdateRegistryTask(resourceDescriptionRegistry, securityContextRegistry));
            if (!ie) {
                tasks.add(new UpdateDatabaseTask(workerChannel));
            }

            LookupContext context = new LookupContext(progress, templates, recursive);
            Stopwatch stopwatch = Stopwatch.createStarted();
            return Flow.sequential(context, tasks).then(
                    c -> {
                        stopwatch.stop();
                        logger.info("Successfully processed metadata in {} ms", stopwatch.elapsed(MILLISECONDS));
                        return Promise.resolve((Void) null);
                    });
        }
    }

    public interface MetadataCallback {

        void onMetadata(Metadata metadata);

        void onError(Throwable error);
    }
}
