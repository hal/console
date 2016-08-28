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

import java.util.Set;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.capabilitiy.Capability;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Pehl
 */
class RrdFunction implements Function<FunctionContext> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(RrdFunction.class);

    private final MetadataRegistry metadataRegistry;
    private final SecurityFramework securityFramework;
    private final ResourceDescriptions resourceDescriptions;
    private final Capabilities capabilities;
    private final Dispatcher dispatcher;
    private final Composite composite;
    private final boolean optional;

    RrdFunction(final MetadataRegistry metadataRegistry, final SecurityFramework securityFramework,
            final ResourceDescriptions resourceDescriptions, final Capabilities capabilities,
            final Dispatcher dispatcher, final Composite composite, final boolean optional) {
        this.metadataRegistry = metadataRegistry;
        this.securityFramework = securityFramework;
        this.resourceDescriptions = resourceDescriptions;
        this.capabilities = capabilities;
        this.dispatcher = dispatcher;
        this.composite = composite;
        this.optional = optional;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        dispatcher.executeInFunction(control, composite,
                (CompositeResult compositeResult) -> {
                    try {
                        Set<RrdResult> results = new CompositeRrdParser(composite).parse(compositeResult);
                        for (RrdResult rr : results) {
                            if (rr.securityContext != null) {
                                logger.debug("Add security context for {}", rr.address);
                                securityFramework.add(rr.address, rr.securityContext);
                            }
                            if (rr.resourceDescription != null) {
                                logger.debug("Add resource description for {}", rr.address);
                                resourceDescriptions.add(rr.address, rr.resourceDescription);
                            }
                            if (rr.resourceDescription != null && rr.securityContext != null) {
                                logger.debug("Add metadata for {}", rr.address);
                                metadataRegistry.add(rr.address,
                                        new Metadata(rr.securityContext, rr.resourceDescription, capabilities));
                            }
                            if (!rr.capabilities.isEmpty()) {
                                logger.debug("Add capabilities {} for {}", rr.capabilities, rr.address);
                                for (Capability capability : rr.capabilities) {
                                    capabilities.register(capability);
                                }
                            }
                        }
                        control.proceed();
                    } catch (ParserException e) {
                        control.getContext().setError(e);
                        control.abort();
                    }
                },
                (operation, failure) -> {
                    if (optional) {
                        logger.debug("Ignore errors on optional resource operation {}", operation.asCli());
                        control.proceed(); // ignore errors on optional resources!
                    } else {
                        control.getContext().setErrorMessage(failure);
                        control.abort();
                    }
                });
    }
}
