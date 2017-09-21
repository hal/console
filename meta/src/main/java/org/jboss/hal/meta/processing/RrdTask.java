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

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.DispatchFailure;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Single;
import rx.functions.Action1;

class RrdTask implements Task<FlowContext> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(RrdTask.class);

    private final SecurityContextRegistry securityContextRegistry;
    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final Dispatcher dispatcher;
    private final Composite composite;
    private final boolean optional;

    RrdTask(SecurityContextRegistry securityContextRegistry,
            ResourceDescriptionRegistry resourceDescriptionRegistry,
            Dispatcher dispatcher,
            Composite composite,
            boolean optional) {
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.dispatcher = dispatcher;
        this.composite = composite;
        this.optional = optional;
    }

    @Override
    public Completable call(FlowContext context) {
        Action1<CompositeResult> action = (CompositeResult compositeResult) -> {
            RrdResult rrdResult = new CompositeRrdParser(composite).parse(compositeResult);
            rrdResult.securityContexts.forEach((address, securityContext) -> {
                logger.debug("Add security context for {}", address);
                securityContextRegistry.add(address, securityContext);
            });
            rrdResult.resourceDescriptions.forEach((address, resourceDescription) -> {
                logger.debug("Add resource description for {}", address);
                resourceDescriptionRegistry.add(address, resourceDescription);
            });
        };

        if (optional) {
            return dispatcher.execute(composite)
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof DispatchFailure) {
                            logger.debug("Ignore errors on optional resource operation {}", composite.asCli());
                            return Single.just(new CompositeResult(new ModelNode()));
                        } else {
                            return Single.error(throwable);
                        }
                    })
                    .doOnSuccess(action)
                    .toCompletable();
        } else {
            return dispatcher.execute(composite)
                    .doOnSuccess(action)
                    .toCompletable();
        }
    }
}
