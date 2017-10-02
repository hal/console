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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MASTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;

/** Reads the domain controller. Only executed in domain mode. Depends on {@link ReadEnvironment}. */
public class FindDomainController implements BootstrapTask {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(FindDomainController.class);

    private final Dispatcher dispatcher;
    private final Environment environment;

    @Inject
    public FindDomainController(Dispatcher dispatcher, Environment environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public Completable get() {
        if (environment.isStandalone()) {
            return Completable.complete();
        } else {
            Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, HOST)
                    .build();
            return dispatcher.execute(operation)
                    .doOnSuccess(result -> {
                        String firstHost = null;
                        String domainController = null;
                        List<Property> properties = result.asPropertyList();
                        if (properties.isEmpty()) {
                            throw new RuntimeException("No hosts found!");

                        } else {
                            for (Property property : properties) {
                                if (firstHost == null) {
                                    firstHost = property.getName();
                                }
                                if (property.getValue().get(MASTER).isDefined() && property.getValue()
                                        .get(MASTER)
                                        .asBoolean()) {
                                    domainController = property.getName();
                                    break;
                                }
                            }
                            if (domainController != null) {
                                environment.setDomainController(domainController);
                            } else {
                                // HAL-1309: If the user belongs to a host scoped role which is scoped to a slave,
                                // there might be no domain controller
                                logger.warn("No domain controller found! Use first host as replacement: '{}'",
                                        firstHost);
                                environment.setDomainController(firstHost);
                            }
                        }
                    })
                    .toCompletable();
        }
    }
}
