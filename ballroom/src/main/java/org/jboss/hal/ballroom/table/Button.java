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
package org.jboss.hal.ballroom.table;

import org.jboss.hal.meta.security.Constraint;

/**
 * A table button
 *
 * @author Harald Pehl
 */
public class Button<T> {

    final String title;
    final ButtonHandler<T> handler;
    final Scope scope;
    final Constraint constraint;

    public Button(final String title, final ButtonHandler<T> handler) {
        this(title, handler, null, null);
    }

    public Button(final String title, final ButtonHandler<T> handler, final Scope scope) {
        this(title, handler, scope, null);
    }

    public Button(final String title, final ButtonHandler<T> handler, final Constraint constraint) {
        this(title, handler, null, constraint);
    }

    public Button(final String title, final ButtonHandler<T> handler, final Scope scope,
            final Constraint constraint) {
        this.title = title;
        this.scope = scope;
        this.handler = handler;
        this.constraint = constraint;
    }
}
