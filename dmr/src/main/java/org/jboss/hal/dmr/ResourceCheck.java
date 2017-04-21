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
package org.jboss.hal.dmr;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/**
 * Function which checks whether a given resource exists. Pushes {@code 200} onto the context stack if it exists,
 * {@code 404} otherwise.
 *
 * @author Harald Pehl
 */
public class ResourceCheck implements Function<FunctionContext> {

    private final Dispatcher dispatcher;
    private final ResourceAddress address;

    public ResourceCheck(final Dispatcher dispatcher, final ResourceAddress address) {
        this.dispatcher = dispatcher;
        this.address = address;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION, address).build();
        dispatcher.executeInFunction(control, operation,
                result -> {
                    control.getContext().push(200);
                    control.proceed();
                },
                (op, failure) -> {
                    control.getContext().push(404);
                    control.proceed();
                });
    }
}
