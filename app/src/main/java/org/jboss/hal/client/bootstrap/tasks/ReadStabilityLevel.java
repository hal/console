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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.Objects;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERMISSIBLE_STABILITY_LEVELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STABILITY;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

public class ReadStabilityLevel implements Task<FlowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ReadStabilityLevel.class);

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    @Inject
    public ReadStabilityLevel(Environment environment, StatementContext statementContext, Dispatcher dispatcher) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        StabilityLevel defaultStabilityLevel = environment.getHalBuild().defaultStability;
        if (environment.isStandalone()) {
            AddressTemplate template = AddressTemplate.of("core-service=server-environment");
            Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            return dispatcher.execute(operation)
                    .then(result -> {
                        environment.setStabilityLevel(readStabilityLevel(result, defaultStabilityLevel));
                        environment.setStabilityLevels(readStabilityLevels(result));
                        return context.resolve();
                    });
        } else {
            AddressTemplate template = AddressTemplate.of("{domain.controller}/core-service=host-environment");
            Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            return dispatcher.execute(operation)
                    .then(result -> {
                        if (result.isDefined()) {
                            environment.setStabilityLevel(readStabilityLevel(result, defaultStabilityLevel));
                            environment.setStabilityLevels(readStabilityLevels(result));
                        } else {
                            logger.warn("Unable to read stability level. Fall back to: {}", defaultStabilityLevel);
                            environment.setStabilityLevel(defaultStabilityLevel);
                            environment.setStabilityLevels(new StabilityLevel[0]);
                        }
                        return context.resolve();
                    })
                    .catch_(error -> {
                        logger.warn("Unable to read stability level. Fall back to: {}", defaultStabilityLevel);
                        environment.setStabilityLevel(defaultStabilityLevel);
                        environment.setStabilityLevels(new StabilityLevel[0]);
                        return context.resolve();
                    });
        }
    }

    private StabilityLevel readStabilityLevel(ModelNode modelNode, StabilityLevel defaultStabilityLevel) {
        return asEnumValue(modelNode, STABILITY, StabilityLevel::valueOf, defaultStabilityLevel);
    }

    private StabilityLevel[] readStabilityLevels(ModelNode modelNode) {
        return modelNode.get(PERMISSIBLE_STABILITY_LEVELS).asList().stream()
                .map(node -> asEnumValue(node, StabilityLevel::valueOf, null))
                .filter(Objects::nonNull)
                .toArray(StabilityLevel[]::new);
    }
}
