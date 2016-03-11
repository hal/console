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

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Harald Pehl
 */
class RrdFunction implements Function<FunctionContext> {

    private static final Logger logger = LoggerFactory.getLogger(RrdFunction.class);

    private final ResourceDescriptions resourceDescriptions;
    private final SecurityFramework securityFramework;
    private final Dispatcher dispatcher;
    private final Composite composite;

    public RrdFunction(final ResourceDescriptions resourceDescriptions, final SecurityFramework securityFramework,
            final Dispatcher dispatcher, final Composite composite) {
        this.resourceDescriptions = resourceDescriptions;
        this.securityFramework = securityFramework;
        this.dispatcher = dispatcher;
        this.composite = composite;
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void execute(final Control<FunctionContext> control) {
        dispatcher.executeInFunction(control, composite,
                (CompositeResult compositeResult) -> {
                    try {
                        Set<RrdResult> results = new CompositeRrdParser(composite).parse(compositeResult);
                        for (RrdResult rr : results) {
                            if (rr.resourceDescription != null) {
                                logger.debug("Add resource description for {}", rr.address);
                                resourceDescriptions.add(rr.address, rr.resourceDescription);
                            }
                            if (rr.securityContext != null) {
                                logger.debug("Add security context for {}", rr.address);
                                securityFramework.add(rr.address, rr.securityContext);
                            }
                        }
                        control.proceed();
                    } catch (ParserException e) {
                        control.getContext().setError(e);
                        control.abort();
                    }
                });
    }
}
