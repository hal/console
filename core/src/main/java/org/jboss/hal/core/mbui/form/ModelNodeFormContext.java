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
package org.jboss.hal.core.mbui.form;

import javax.inject.Inject;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;

/**
 * Helper class to get access to some required dependencies for {@link ModelNodeForm}s.
 *
 * @author Harald Pehl
 */
public class ModelNodeFormContext {

    @Inject
    public static ModelNodeFormContext INSTANCE;

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public ModelNodeFormContext(final Dispatcher dispatcher, final StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public StatementContext statementContext() {
        return statementContext;
    }
}
