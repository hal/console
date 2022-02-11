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
package org.jboss.hal.dmr;

import org.jboss.hal.dmr.dispatch.DispatchFailure;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;

import rx.Completable;
import rx.Single;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * Function which checks whether a given resource exists. Pushes {@code 200} onto the context stack if it exists, {@code 404}
 * otherwise.
 */
public class ResourceCheck implements Task<FlowContext> {

    private final Dispatcher dispatcher;
    private final ResourceAddress address;

    public ResourceCheck(Dispatcher dispatcher, ResourceAddress address) {
        this.dispatcher = dispatcher;
        this.address = address;
    }

    @Override
    public Completable call(FlowContext context) {
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
        return dispatcher.execute(operation)
                .doOnSuccess(result -> context.push(200))
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof DispatchFailure) {
                        context.push(404);
                        return Single.just(new ModelNode());
                    } else {
                        return Single.error(throwable);
                    }
                })
                .toCompletable();
    }
}
