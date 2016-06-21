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
package org.jboss.hal.client.configuration;

import java.util.List;

import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.typeahead.NamesResultProcessor;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * @author Harald Pehl
 */
public class PathsTypeahead extends Typeahead {

    private static final Logger logger = LoggerFactory.getLogger(PathsTypeahead.class);
    private static Operation operation = defaultOperation();

    public static void updateOperation(final Environment environment, final Dispatcher dispatcher,
            final StatementContext statementContext) {
        if (environment.isStandalone() || statementContext.selectedProfile() == null) {
            operation = defaultOperation();
        } else {
            new Async<FunctionContext>(Progress.NOOP).single(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            //noinspection HardCodedStringLiteral
                            logger.error("Unable to update operation for paths type-ahead: " +
                                    "Error reading running servers: {}", context.getErrorMessage());
                            operation = defaultOperation();
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Server> servers = context.get(TopologyFunctions.RUNNING_SERVERS);
                            if (!servers.isEmpty() && servers.get(0).isStarted()) {
                                operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION,
                                        servers.get(0).getServerAddress()).param(CHILD_TYPE, "path").build();
                            } else {
                                operation = defaultOperation();
                            }
                        }
                    },
                    new TopologyFunctions.RunningServersOfProfile(environment, dispatcher,
                            statementContext.selectedProfile()));
        }
    }

    private static Operation defaultOperation() {
        return new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "path").build();
    }

    public PathsTypeahead() {
        super(data -> data.getString(NAME), data -> data.getString(NAME).split(WHITESPACE), new NamesResultProcessor(),
                data -> data.getString(NAME), null, () -> operation);
    }
}
