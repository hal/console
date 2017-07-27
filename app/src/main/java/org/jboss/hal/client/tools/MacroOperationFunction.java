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
package org.jboss.hal.client.tools;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Operation;

class MacroOperationFunction implements Function<FunctionContext> {

    private final Dispatcher dispatcher;
    private final Operation operation;

    MacroOperationFunction(final Dispatcher dispatcher, final Operation operation) {
        this.dispatcher = dispatcher;
        this.operation = operation;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        dispatcher.executeInFunction(control, operation, result -> control.proceed());
    }
}
