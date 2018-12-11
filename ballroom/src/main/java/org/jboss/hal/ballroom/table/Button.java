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
import org.jboss.hal.meta.security.Constraints;

/** A table button */
public class Button<T> {

    final String name;
    final String title;
    final ButtonHandler<T> handler;
    final Scope scope;
    final Constraints constraints;

    public Button(String name, ButtonHandler<T> handler) {
        this(name, null, handler, null, Constraints.empty());
    }

    public Button(String name, ButtonHandler<T> handler, Scope scope) {
        this(name, null, handler, scope, Constraints.empty());
    }

    public Button(String name, ButtonHandler<T> handler, Constraint constraint) {
        this(name, null, handler, null, Constraints.single(constraint));
    }

    public Button(String name, ButtonHandler<T> handler, Constraints constraints) {
        this(name, null, handler, null, constraints);
    }

    public Button(String name, String title, ButtonHandler<T> handler, Constraint constraint) {
        this(name, title, handler, null, Constraints.single(constraint));
    }

    public Button(String name, String title, ButtonHandler<T> handler, Constraints constraints) {
        this(name, title, handler, null, constraints);
    }

    public Button(String name, String title, ButtonHandler<T> handler, Scope scope, Constraint constraint) {
        this(name, title, handler, scope, Constraints.single(constraint));
    }

    public Button(String name, String title, ButtonHandler<T> handler, Scope scope, Constraints constraints) {
        this.name = name;
        this.title = title;
        this.scope = scope;
        this.handler = handler;
        this.constraints = constraints;
    }
}
