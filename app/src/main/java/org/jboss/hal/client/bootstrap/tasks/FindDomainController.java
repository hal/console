/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.bootstrap.tasks;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Completable;
import rx.Single;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.bootstrap.tasks.ReadHostNames.HOST_NAMES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class FindDomainController implements BootstrapTask {

    private static final Logger logger = LoggerFactory.getLogger(FindDomainController.class);

    private final Dispatcher dispatcher;
    private final Environment environment;

    @Inject
    public FindDomainController(Dispatcher dispatcher, Environment environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public Completable call(FlowContext context) {
        if (!environment.isStandalone()) {
            List<String> hosts = context.get(HOST_NAMES);
            if (hosts != null) {
                List<Completable> completables = hosts.stream()
                        .map(host -> {
                            ResourceAddress address = new ResourceAddress().add(HOST, host);
                            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build();
                            return dispatcher.execute(operation)
                                    .doOnSuccess(result -> {
                                        boolean master = result.get(MASTER).asBoolean();
                                        if (master) {
                                            String name = result.get(NAME).asString();
                                            environment.setDomainController(name);
                                            logger.info("Found domain controller: {}", name);
                                        }
                                    })
                                    .onErrorResumeNext(error -> {
                                        logger.warn("Unable to read host: {}", error.getMessage());
                                        return Single.just(new ModelNode());
                                    })
                                    .toCompletable();
                        })
                        .collect(toList());
                return Completable.concat(completables);
            } else {
                return Completable.complete();
            }
        } else {
            return Completable.complete();
        }
    }
}
