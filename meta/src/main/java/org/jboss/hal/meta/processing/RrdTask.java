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
import java.util.stream.Collectors;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.DispatchFailure;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import rx.Completable;
import rx.Single;
import rx.functions.Action1;

import static java.util.stream.Collectors.toList;

/** Creates, executes and parses the {@code read-resource-description} operations to read the missing metadata. */
class RrdTask implements Task<LookupContext> {

    private static final Logger logger = LoggerFactory.getLogger(RrdTask.class);

    private final Dispatcher dispatcher;
    private final int batchSize;
    private final CreateRrdOperations rrdOps;

    RrdTask(Environment environment, Dispatcher dispatcher, StatementContext statementContext, Settings settings,
            int batchSize, int depth) {
        this.dispatcher = dispatcher;
        this.batchSize = batchSize;
        this.rrdOps = new CreateRrdOperations(environment, statementContext, settings.get(Settings.Key.LOCALE).value(),
                depth);
    }

    @Override
    public Completable call(LookupContext context) {
        boolean recursive = context.recursive;
        List<Completable> completables = new ArrayList<>();

        // create and partition non-optional operations
        List<Operation> operations = rrdOps.create(context, recursive, false);
        List<List<Operation>> piles = Lists.partition(operations, batchSize);
        List<Composite> composites = piles.stream().map(Composite::new).collect(toList());
        for (Composite composite : composites) {
            completables.add(dispatcher.execute(composite)
                    .doOnSuccess(parseRrdAction(context, composite))
                    .toCompletable());
        }

        // create optional operations w/o partitioning!
        List<Operation> optionalOperations = rrdOps.create(context, recursive, true);
        // Do not refactor to
        // List<Composite> optionalComposites = optionalOperations.stream().map(Composite::new).collect(toList());
        // the GWT compiler will crash with an ArrayIndexOutOfBoundsException!
        List<Composite> optionalComposites = new ArrayList<>();
        optionalOperations.forEach(operation -> optionalComposites.add(new Composite(operation)));
        for (Composite composite : optionalComposites) {
            completables.add(dispatcher.execute(composite)
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof DispatchFailure) {
                            logger.debug("Ignore errors on optional resource operation {}", composite.asCli());
                            return Single.just(new CompositeResult(new ModelNode()));
                        } else {
                            return Single.error(throwable);
                        }
                    })
                    .doOnSuccess(parseRrdAction(context, composite))
                    .toCompletable());
        }

        if (!completables.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("About to execute {} ({}+{}) composite operations (regular+optional)",
                        composites.size() + optionalComposites.size(), composites.size(), optionalComposites.size());
                String compositeOps = composites.stream().map(Composite::asCli).collect(Collectors.joining(", "));
                logger.debug("Composite operations: {}", compositeOps);
                if (!optionalComposites.isEmpty()) {
                    String optionalOps = optionalComposites.stream()
                            .map(Composite::asCli)
                            .collect(Collectors.joining(", "));
                    logger.debug("Optional operations: {}", optionalOps);
                }
            }
            return Completable.concat(completables);
        } else {
            logger.debug("No DMR operations necessary");
            return Completable.complete();
        }
    }

    private Action1<CompositeResult> parseRrdAction(LookupContext context, Composite composite) {
        return (CompositeResult compositeResult) -> {
            RrdResult rrdResult = new CompositeRrdParser(composite).parse(compositeResult);
            context.toResourceDescriptionRegistry.putAll(rrdResult.resourceDescriptions);
            context.toResourceDescriptionDatabase.putAll(rrdResult.resourceDescriptions);
            context.toSecurityContextRegistry.putAll(rrdResult.securityContexts);
            context.toSecurityContextDatabase.putAll(rrdResult.securityContexts);
        };
    }
}
